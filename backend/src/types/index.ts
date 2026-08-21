// Supported language codes. "auto" is only valid as a source language.
export type LanguageCode = "auto" | "en" | "id" | "ja";

export const SUPPORTED_TARGET_LANGUAGES: LanguageCode[] = ["en", "id", "ja"];

export interface TranslateRequestBody {
  text: string;
  sourceLanguage: LanguageCode;
  targetLanguage: LanguageCode;
}

export interface TranslateResponseData {
  originalText: string;
  translatedText: string;
  sourceLanguage: LanguageCode;
  targetLanguage: LanguageCode;
  detectedLanguage?: LanguageCode;
}

export interface DetectLanguageRequestBody {
  text: string;
}

export interface ApiSuccess<T> {
  success: true;
  data: T;
}

export interface ApiError {
  success: false;
  error: {
    code: string;
    message: string;
  };
}

/**
 * Abstraction every translation provider must implement.
 * This lets the backend swap providers (LibreTranslate, Google, etc.)
 * without touching route handlers.
 */
export interface TranslationProvider {
  translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<{ translatedText: string; detectedLanguage?: LanguageCode }>;

  detectLanguage(text: string): Promise<LanguageCode>;
}
