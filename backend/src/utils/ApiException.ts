export class ApiException extends Error {
  public readonly statusCode: number;
  public readonly code: string;

  constructor(statusCode: number, code: string, message: string) {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.name = "ApiException";
  }
}

export class ValidationError extends ApiException {
  constructor(message: string) {
    super(400, "VALIDATION_ERROR", message);
  }
}

export class ProviderError extends ApiException {
  constructor(message: string) {
    super(502, "PROVIDER_ERROR", message);
  }
}

export class UnsupportedLanguageError extends ApiException {
  constructor(message: string) {
    super(422, "UNSUPPORTED_LANGUAGE", message);
  }
}
