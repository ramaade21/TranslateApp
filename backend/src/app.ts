import express, { Express } from "express";
import cors from "cors";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import { TranslationProvider } from "./types";
import { TranslationService } from "./services/translationService";
import { createTranslateRouter } from "./routes/translate";
import { createDetectLanguageRouter } from "./routes/detectLanguage";
import { healthRouter } from "./routes/health";
import { errorHandler, notFoundHandler } from "./middleware/errorHandler";
import { requireAppApiKey } from "./middleware/apiKeyAuth";
import { enforceHttps } from "./middleware/enforceHttps";

/**
 * Builds the Express app. Provider is injected so tests can supply a
 * fake/mock provider without touching environment variables.
 */
export function createApp(provider: TranslationProvider): Express {
  const app = express();
  const service = new TranslationService(provider);

  // Render/Railway/most PaaS terminate TLS at their edge and forward
  // over plain HTTP internally, setting X-Forwarded-Proto. Trusting
  // the proxy lets us correctly detect HTTPS and apply secure cookies/
  // HSTS/redirects, and lets express-rate-limit key on the real client IP
  // instead of the proxy's IP.
  app.set("trust proxy", 1);

  app.use(
    helmet({
      hsts: { maxAge: 31536000, includeSubDomains: true, preload: true },
      // This is a JSON API with no browser-rendered HTML, so a strict
      // default-src 'none' CSP is safe and removes an entire class of
      // injection concerns for any accidental HTML error pages.
      contentSecurityPolicy: {
        directives: { defaultSrc: ["'none'"] },
      },
    })
  );

  app.use(enforceHttps);

  const allowedOrigins = (process.env.ALLOWED_ORIGINS || "").split(",").map((s) => s.trim()).filter(Boolean);
  app.use(
    cors({
      // Native Android requests carry no Origin header, so CORS mainly
      // guards against browser-based abuse of this API. Default is
      // closed (no origins) unless explicitly configured - "*" is
      // intentionally not the default anymore.
      origin: allowedOrigins.length > 0 ? allowedOrigins : false,
    })
  );

  app.use(express.json({ limit: "256kb" }));

  // Explicitly forbid caching of every /api response, at every layer
  // (browser, CDN, PaaS edge proxy). This was a real, confirmed bug:
  // Railway's edge was caching /api/translate responses keyed on the
  // exact request body, so identical text kept getting the answer
  // from whichever provider was active the FIRST time that exact text
  // was translated - even after the provider was switched and
  // redeployed. Translation results can also depend on server-side
  // config that changes over time, so they must never be cached.
  app.use("/api", (_req, res, next) => {
    res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
    res.setHeader("Pragma", "no-cache");
    res.setHeader("Expires", "0");
    res.setHeader("Surrogate-Control", "no-store");
    next();
  });

  // Global baseline limiter (defense in depth)...
  //
  // `validate: { trustProxy: false, xForwardedForHeader: false }` disables
  // express-rate-limit's strict runtime check that the configured
  // `trust proxy` hop count exactly matches the X-Forwarded-For chain
  // length. On PaaS platforms like Railway/Render, the actual proxy
  // topology can vary in ways that don't perfectly match a fixed
  // `trust proxy` number, and without this, the library throws
  // synchronously on every request - which isn't an ApiException, so it
  // was surfacing as an opaque 500 on every single /api/translate call.
  // Rate limiting itself still works correctly; this only silences the
  // extra self-check.
  const globalLimiter = rateLimit({
    windowMs: Number(process.env.RATE_LIMIT_WINDOW_MS) || 60_000,
    max: Number(process.env.RATE_LIMIT_MAX_REQUESTS) || 60,
    standardHeaders: true,
    legacyHeaders: false,
    validate: { trustProxy: false, xForwardedForHeader: false },
    message: { success: false, error: { code: "RATE_LIMITED", message: "Too many requests." } },
  });
  app.use("/api", globalLimiter);

  // ...plus a stricter per-IP limiter specifically on the paid/costed
  // translation endpoints, so one client can't burn through provider
  // quota even if they're within the generous global limit.
  const translateLimiter = rateLimit({
    windowMs: 60_000,
    max: Number(process.env.TRANSLATE_RATE_LIMIT_PER_MINUTE) || 20,
    standardHeaders: true,
    legacyHeaders: false,
    validate: { trustProxy: false, xForwardedForHeader: false },
    message: { success: false, error: { code: "RATE_LIMITED", message: "Too many translation requests. Please slow down." } },
  });

  // Require the shared app key on every /api call - blocks anonymous
  // scripts/scrapers that don't have the app's embedded credential.
  app.use("/api", requireAppApiKey);

  // Only log requests in non-production, and never log full request bodies
  // (which may contain user speech/text), per the app's privacy rules.
  if (process.env.NODE_ENV !== "production") {
    app.use((req, _res, next) => {
      // eslint-disable-next-line no-console
      console.log(`${req.method} ${req.path}`);
      next();
    });
  }

  app.use("/api/translate", translateLimiter);
  app.use("/api/detect-language", translateLimiter);

  app.use("/api", createTranslateRouter(service));
  app.use("/api", createDetectLanguageRouter(service));
  app.use("/", healthRouter);

  app.use(notFoundHandler);
  app.use(errorHandler);

  return app;
}
