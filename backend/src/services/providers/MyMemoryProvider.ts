import fetch from "node-fetch";
import { LanguageCode, TranslationProvider } from "../../types";
import { ProviderError } from "../../utils/ApiException";
import { detectLanguageHeuristic } from "../../utils/languageHeuristics";

/**
 * Real translation provider backed by the MyMemory Translation API
 * (https://mymemory.translated.net). Unlike libretranslate.com, this
 * is genuinely free for light/hobby use with no API key required -
 * good default for getting started without any paid signup.
 *
 * Rate limit without an email/key: ~5,000 words/day per IP. An email
 * address can optionally be sent to raise that limit (MYMEMORY_EMAIL
 * env var) - MyMemory does not require registration for this, it's
 * just used to contact you if you exceed limits.
 *
 * MyMemory has no standalone language-detection endpoint, so "auto"
 * source language is resolved via the same heuristic detector used
 * as a fallback elsewhere in this codebase.
 */
export class MyMemoryProvider implements TranslationProvider {
  private static readonly ENDPOINT = "https://api.mymemory.translated.net/get";

  constructor(private readonly contactEmail?: string) {}

  async translate(
    text: string,
    sourceLanguage: LanguageCode,
    targetLanguage: LanguageCode
  ): Promise<{ translatedText: string; detectedLanguage?: LanguageCode }> {
    let detected: LanguageCode | undefined;
    let source = sourceLanguage;

    if (source === "auto") {
      detected = detectLanguageHeuristic(text);
      source = detected;
    }

    // If the (possibly auto-detected) source turns out to equal the
    // target, there's nothing to translate - some providers error on
    // an identical langpair, so short-circuit instead of calling out.
    // This matters especially for short/ambiguous input where the
    // heuristic detector has no strong signal either way and defaults
    // to English.
    if (source === targetLanguage) {
      return { translatedText: text, detectedLanguage: detected };
    }

    const params = new URLSearchParams({
      q: text,
      langpair: `${source}|${targetLanguage}`,
    });
    if (this.contactEmail) {
      params.set("de", this.contactEmail);
    }

    try {
      const res = await fetch(`${MyMemoryProvider.ENDPOINT}?${params.toString()}`);

      if (!res.ok) {
        throw new Error(`MyMemory responded with status ${res.status}`);
      }

      const json = (await res.json()) as {
        responseData?: { translatedText?: string };
        responseStatus?: number | string;
        responseDetails?: string;
      };

      const translatedText = json.responseData?.translatedText;
      if (!translatedText) {
        throw new Error(json.responseDetails || "MyMemory response missing translatedText");
      }

      return { translatedText, detectedLanguage: detected };
    } catch (err) {
      throw new ProviderError(
        `Translation service is temporarily unavailable: ${(err as Error).message}`
      );
    }
  }

  async detectLanguage(text: string): Promise<LanguageCode> {
    // No dedicated detect endpoint on the free tier - heuristic only.
    return detectLanguageHeuristic(text);
  }
}
