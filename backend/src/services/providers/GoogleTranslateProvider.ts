import fetch from "node-fetch";
import { LanguageCode, TranslationProvider } from "../../types";
import { ProviderError } from "../../utils/ApiException";
import { detectLanguageHeuristic } from "../../utils/languageHeuristics";

/**
 * Real translation provider backed by the Google Cloud Translation
 * v2 REST API. Requires GOOGLE_TRANSLATE_API_KEY to be set.
 */
export class GoogleTranslateProvider implements TranslationProvider {
  private static readonly ENDPOINT = "https://translation.googleapis.com/language/translate2";

  constructor(private readonly apiKey: string) {}

  async translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<{ translatedText: string; detectedLanguage?: LanguageCode }> {
    const params = new URLSearchParams({
      q: text,
      target: targetLanguage,
      format: "text",
      key: this.apiKey,
    });
    if (sourceLanguage !== "auto") {
      params.set("source", sourceLanguage);
    }

    try {
      const res = await fetch(`${GoogleTranslateProvider.ENDPOINT}?${params.toString()}`, {
        method: "POST",
      });

      if (!res.ok) {
        throw new Error(`Google Translate responded with status ${res.status}`);
      }

      const json = (await res.json()) as {
        data?: {
          translations?: Array<{ translatedText: string; detectedSourceLanguage?: string }>;
        };
      };

      const translation = json.data?.translations?.[0];
      if (!translation) throw new Error("Google Translate response missing translations");

      let detected: LanguageCode | undefined;
      if (sourceLanguage === "auto") {
        const lang = translation.detectedSourceLanguage;
        detected = lang === "en" || lang === "id" || lang === "ja"
          ? lang
          : detectLanguageHeuristic(text);
      }

      return { translatedText: translation.translatedText, detectedLanguage: detected };
    } catch (err) {
      throw new ProviderError(
        `Translation service is temporarily unavailable: ${(err as Error).message}`
      );
    }
  }

  async detectLanguage(text: string): Promise<LanguageCode> {
    // Google's detect endpoint could be used here; for simplicity and
    // to minimize API calls/cost, the heuristic detector is used
    // directly for standalone detect-language requests.
    return detectLanguageHeuristic(text);
  }
}
