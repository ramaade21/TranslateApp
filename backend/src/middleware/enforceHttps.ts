import { NextFunction, Request, Response } from "express";

/**
 * Redirects plain-HTTP requests to HTTPS in production. Relies on
 * `app.set("trust proxy", 1)` being configured so `req.secure`
 * correctly reflects the original client protocol (via
 * X-Forwarded-Proto) even though the PaaS terminates TLS before the
 * request reaches this process.
 *
 * No-op outside production so local development over plain HTTP
 * still works.
 */
export function enforceHttps(req: Request, res: Response, next: NextFunction): void {
  if (process.env.NODE_ENV !== "production") {
    next();
    return;
  }

  if (req.secure) {
    next();
    return;
  }

  res.redirect(301, `https://${req.headers.host}${req.originalUrl}`);
}
