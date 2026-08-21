import { NextFunction, Request, Response } from "express";
import { ApiException } from "../utils/ApiException";
import { ApiError } from "../types";

export function errorHandler(err: Error, _req: Request, res: Response, _next: NextFunction): void {
  if (err instanceof ApiException) {
    const body: ApiError = {
      success: false,
      error: { code: err.code, message: err.message },
    };
    res.status(err.statusCode).json(body);
    return;
  }

  // Unknown/unexpected error - never leak stack traces or internals to the client.
  // eslint-disable-next-line no-console
  console.error("Unhandled error:", err);
  const body: ApiError = {
    success: false,
    error: { code: "INTERNAL_ERROR", message: "Something went wrong. Please try again." },
  };
  res.status(500).json(body);
}

export function notFoundHandler(req: Request, res: Response): void {
  const body: ApiError = {
    success: false,
    error: { code: "NOT_FOUND", message: `Route ${req.method} ${req.path} not found.` },
  };
  res.status(404).json(body);
}
