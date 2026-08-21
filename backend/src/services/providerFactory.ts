import { TranslationProvider } from "../types";
import { LibreTranslateProvider } from "./providers/LibreTranslateProvider";
import { GoogleTranslateProvider } from "./providers/GoogleTranslateProvider";
import { MockProvider } from "./providers/MockProvider";

export function createTranslationProvider(): TranslationProvider {
  const providerName = (process.env.TRANSLATION_PROVIDER || "libretranslate").toLowerCase();

  if (providerName === "mock") {
    if (process.env.NODE_ENV === "production") {
      throw new Error(
        "TRANSLATION_PROVIDER=mock is not allowed when NODE_ENV=production. " +
          "Set TRANSLATION_PROVIDER to 'libretranslate' or 'google' with a real API key."
      );
    }
    return new MockProvider();
  }

  if (providerName === "google") {
    const apiKey = process.env.GOOGLE_TRANSLATE_API_KEY;
    if (!apiKey) {
      throw new Error("GOOGLE_TRANSLATE_API_KEY is required when TRANSLATION_PROVIDER=google");
    }
    return new GoogleTranslateProvider(apiKey);
  }

  // Default: libretranslate
  const baseUrl = process.env.LIBRETRANSLATE_BASE_URL || "https://libretranslate.com";
  const apiKey = process.env.LIBRETRANSLATE_API_KEY;
  return new LibreTranslateProvider({ baseUrl, apiKey });
}
