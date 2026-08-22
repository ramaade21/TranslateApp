import { NextFunction, Request, Response } from "express";
import crypto from "crypto";

/**
 * Requires every request to carry a shared secret header that only the
 * LinguaTranslate Android app knows (embedded in BuildConfig, not
 * committed to source control).
 *
 * IMPORTANT - honest limitation: a secret embedded in a distributed
 * APK can be extracted by a sufficiently motivated attacker who
 * decompiles the app. This check is a real, meaningful barrier
 * against casual scraping/abuse and against anyone who doesn't have
 * the app - it is NOT equivalent to per-user authentication. If you
 * need to stop a specific abusive user (not just anonymous bots),
 * add real user accounts/tokens on top of this.
 *
 * Uses a constant-time comparison to avoid leaking the key via
 * response-time side channels.
 */
export function requireAppApiKey(req: Request, res: Response, next: NextFunction): void {
  const expected = process.env.APP_API_KEY;

  // Fail closed: if the operator forgot to set the key in production,
  // refuse to serve requests rather than silently running unprotected.
  if (!expected) {
    if (process.env.NODE_ENV === "production") {
      // This responds directly rather than via next(err)/errorHandler,
      // so without an explicit log line here it was invisible in
      // platform logs (Railway/Render) - making a missing APP_API_KEY
      // env var very hard to diagnose from the outside. Always log it.
      // eslint-disable-next-line no-console
      console.error(
        "[SERVER_MISCONFIGURED] APP_API_KEY is not set. " +
          "Set it in the platform's environment variables dashboard."
      );
      res.status(500).json({
        success: false,
        error: { code: "SERVER_MISCONFIGURED", message: "Server is not configured correctly." },
      });
      return;
    }
    // In local dev without a key set, allow through so `npm run dev` works out of the box.
    next();
    return;
  }

  const provided = req.header("X-App-Key") ?? "";
  const expectedBuf = Buffer.from(expected);
  const providedBuf = Buffer.from(provided);

  const isValid =
    expectedBuf.length === providedBuf.length &&
    crypto.timingSafeEqual(expectedBuf, providedBuf);

  if (!isValid) {
    res.status(401).json({
      success: false,
      error: { code: "UNAUTHORIZED", message: "Missing or invalid app credentials." },
    });
    return;
  }

  next();
}
