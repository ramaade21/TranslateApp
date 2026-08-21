import { NextFunction, Request, Response } from "express";

/**
 * Redirects plain-HTTP requests to HTTPS in production. Relies on
 * `app.set("trust proxy", 1)` being configured so `req.secure`
 * correctly reflects the original client protocol (via
 * X-Forwarded-Proto) even though the PaaS terminates TLS before the
 * request reaches this process.
 *
 * The /health path is always exempted: platform healthcheck probes
 * (Render, Railway, etc.) hit the running container directly over
 * plain HTTP on its internal network, bypassing the public HTTPS
 * edge entirely - so req.secure is false for them even though
 * nothing insecure is actually happening. Redirecting those probes
 * makes the deployment's healthcheck fail outright, since healthcheck
 * clients don't follow redirects. Health status isn't sensitive, so
 * exempting it is safe.
 *
 * No-op outside production so local development over plain HTTP
 * still works.
 */
export function enforceHttps(req: Request, res: Response, next: NextFunction): void {
  if (process.env.NODE_ENV !== "production") {
    next();
    return;
  }

  if (req.path === "/health") {
    next();
    return;
  }

  if (req.secure) {
    next();
    return;
  }

  res.redirect(301, `https://${req.headers.host}${req.originalUrl}`);
}
