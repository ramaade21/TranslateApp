import { TranslationProvider } from "../types";
import { LibreTranslateProvider } from "./providers/LibreTranslateProvider";
import { GoogleTranslateProvider } from "./providers/GoogleTranslateProvider";
import { MyMemoryProvider } from "./providers/MyMemoryProvider";
import { MockProvider } from "./providers/MockProvider";

export function createTranslationProvider(): TranslationProvider {
  // .trim() matters here: a stray trailing space from copy-pasting an
  // env var value into a dashboard (easy to do on mobile) would
  // otherwise silently fail this equality check and fall through to
  // the default provider with no error - very hard to diagnose from
  // the outside, since everything *looks* configured correctly.
  const providerName = (process.env.TRANSLATION_PROVIDER || "mymemory").trim().toLowerCase();

  // eslint-disable-next-line no-console
  console.log(`[startup] Using translation provider: ${providerName}`);

  if (providerName === "mock") {
    if (process.env.NODE_ENV === "production") {
      throw new Error(
        "TRANSLATION_PROVIDER=mock is not allowed when NODE_ENV=production. " +
          "Set TRANSLATION_PROVIDER to 'mymemory', 'libretranslate', or 'google'."
      );
    }
    return new MockProvider();
  }

  if (providerName === "google") {
    const apiKey = process.env.GOOGLE_TRANSLATE_API_KEY?.trim();
    if (!apiKey) {
      throw new Error("GOOGLE_TRANSLATE_API_KEY is required when TRANSLATION_PROVIDER=google");
    }
    return new GoogleTranslateProvider(apiKey);
  }

  if (providerName === "libretranslate") {
    const baseUrl = (process.env.LIBRETRANSLATE_BASE_URL || "https://libretranslate.com").trim();
    const apiKey = process.env.LIBRETRANSLATE_API_KEY?.trim();
    // eslint-disable-next-line no-console
    console.log(`[startup] LibreTranslate base URL: ${baseUrl}`);
    return new LibreTranslateProvider({ baseUrl, apiKey });
  }

  // Default: mymemory - genuinely free, no API key required.
  return new MyMemoryProvider(process.env.MYMEMORY_EMAIL?.trim());
}
