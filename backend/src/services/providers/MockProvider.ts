import { LanguageCode, TranslationProvider } from "../../types";
import { detectLanguageHeuristic } from "../../utils/languageHeuristics";

/**
 * NOT FOR PRODUCTION USE.
 *
 * This provider exists only so the backend can be run and tested
 * (routes, validation, error handling, contracts) without a live
 * network dependency or paid API key. It performs no real
 * translation - it simply tags the text so automated tests and local
 * manual testing can verify request/response plumbing end to end.
 *
 * Selected only when TRANSLATION_PROVIDER=mock. server.ts refuses to
 * start with this provider when NODE_ENV=production.
 */
export class MockProvider implements TranslationProvider {
  async translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<{ translatedText: string; detectedLanguage?: LanguageCode }> {
    const detected = sourceLanguage === "auto" ? detectLanguageHeuristic(text) : undefined;
    return {
      translatedText: `[${targetLanguage}] ${text}`,
      detectedLanguage: detected,
    };
  }

  async detectLanguage(text: string): Promise<LanguageCode> {
    return detectLanguageHeuristic(text);
  }
}
