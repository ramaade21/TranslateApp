import fetch from "node-fetch";
import { LanguageCode, TranslationProvider } from "../../types";
import { ProviderError } from "../../utils/ApiException";
import { detectLanguageHeuristic } from "../../utils/languageHeuristics";

interface LibreTranslateConfig {
  baseUrl: string;
  apiKey?: string;
}

/**
 * Real translation provider backed by the LibreTranslate REST API
 * (self-hostable, also available as a hosted service). This is the
 * default production provider for LinguaTranslate.
 *
 * https://libretranslate.com/docs
 */
export class LibreTranslateProvider implements TranslationProvider {
  constructor(private readonly config: LibreTranslateConfig) {}

  async translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<{ translatedText: string; detectedLanguage?: LanguageCode }> {
    let detected: LanguageCode | undefined;
    let source = sourceLanguage;

    if (source === "auto") {
      detected = await this.detectLanguage(text);
      source = detected;
    }

    try {
      const res = await fetch(`${this.config.baseUrl}/translate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          q: text,
          source,
          target: targetLanguage,
          format: "text",
          api_key: this.config.apiKey || undefined,
        }),
      });

      if (!res.ok) {
        throw new Error(`LibreTranslate responded with status ${res.status}`);
      }

      const json = (await res.json()) as { translatedText?: string };
      if (!json.translatedText) {
        throw new Error("LibreTranslate response missing translatedText");
      }

      return { translatedText: json.translatedText, detectedLanguage: detected };
    } catch (err) {
      throw new ProviderError(
        `Translation service is temporarily unavailable: ${(err as Error).message}`
      );
    }
  }

  async detectLanguage(text: string): Promise<LanguageCode> {
    try {
      const res = await fetch(`${this.config.baseUrl}/detect`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ q: text, api_key: this.config.apiKey || undefined }),
      });

      if (!res.ok) throw new Error(`status ${res.status}`);

      const json = (await res.json()) as Array<{ language: string; confidence: number }>;
      const top = json?.[0]?.language;
      if (top === "en" || top === "id" || top === "ja") {
        return top;
      }
      // Provider returned a language this app doesn't support -
      // fall back to the heuristic detector instead of failing outright.
      return detectLanguageHeuristic(text);
    } catch {
      // Detection is best-effort: never fail the whole request over it.
      return detectLanguageHeuristic(text);
    }
  }
}
