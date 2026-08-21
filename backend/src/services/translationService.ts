import {
  LanguageCode,
  SUPPORTED_TARGET_LANGUAGES,
  TranslateResponseData,
  TranslationProvider,
} from "../types";
import { UnsupportedLanguageError, ValidationError } from "../utils/ApiException";

export class TranslationService {
  constructor(private readonly provider: TranslationProvider) {}

  async translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<TranslateResponseData> {
    const trimmed = text?.trim();
    if (!trimmed) {
      throw new ValidationError("Text must not be empty.");
    }
    if (trimmed.length > 5000) {
      throw new ValidationError("Text is too long (max 5000 characters).");
    }
    if (!SUPPORTED_TARGET_LANGUAGES.includes(targetLanguage)) {
      throw new UnsupportedLanguageError(`Unsupported target language: ${targetLanguage}`);
    }
    if (sourceLanguage !== "auto" && !SUPPORTED_TARGET_LANGUAGES.includes(sourceLanguage)) {
      throw new UnsupportedLanguageError(`Unsupported source language: ${sourceLanguage}`);
    }
    if (sourceLanguage === targetLanguage) {
      throw new ValidationError("Source and target languages must be different.");
    }

    const result = await this.provider.translate(trimmed, sourceLanguage, targetLanguage);

    return {
      originalText: trimmed,
      translatedText: result.translatedText,
      sourceLanguage,
      targetLanguage,
      detectedLanguage: result.detectedLanguage,
    };
  }

  async detectLanguage(text: string): Promise<LanguageCode> {
    const trimmed = text?.trim();
    if (!trimmed) {
      throw new ValidationError("Text must not be empty.");
    }
    return this.provider.detectLanguage(trimmed);
  }
}
