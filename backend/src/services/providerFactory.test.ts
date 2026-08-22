import { createTranslationProvider } from "./providerFactory";
import { MyMemoryProvider } from "./providers/MyMemoryProvider";
import { LibreTranslateProvider } from "./providers/LibreTranslateProvider";
import { MockProvider } from "./providers/MockProvider";

describe("createTranslationProvider", () => {
  const originalEnv = { ...process.env };

  afterEach(() => {
    process.env = { ...originalEnv };
  });

  it("defaults to MyMemoryProvider when TRANSLATION_PROVIDER is unset", () => {
    delete process.env.TRANSLATION_PROVIDER;
    expect(createTranslationProvider()).toBeInstanceOf(MyMemoryProvider);
  });

  it("selects LibreTranslateProvider for 'libretranslate'", () => {
    process.env.TRANSLATION_PROVIDER = "libretranslate";
    expect(createTranslationProvider()).toBeInstanceOf(LibreTranslateProvider);
  });

  it("trims trailing whitespace from TRANSLATION_PROVIDER (e.g. accidental copy-paste space)", () => {
    process.env.TRANSLATION_PROVIDER = "libretranslate ";
    expect(createTranslationProvider()).toBeInstanceOf(LibreTranslateProvider);
  });

  it("trims trailing whitespace with a trailing newline too", () => {
    process.env.TRANSLATION_PROVIDER = "libretranslate\n";
    expect(createTranslationProvider()).toBeInstanceOf(LibreTranslateProvider);
  });

  it("is case-insensitive", () => {
    process.env.TRANSLATION_PROVIDER = "LibreTranslate";
    expect(createTranslationProvider()).toBeInstanceOf(LibreTranslateProvider);
  });

  it("selects MockProvider outside production", () => {
    process.env.TRANSLATION_PROVIDER = "mock";
    process.env.NODE_ENV = "development";
    expect(createTranslationProvider()).toBeInstanceOf(MockProvider);
  });

  it("refuses MockProvider in production", () => {
    process.env.TRANSLATION_PROVIDER = "mock";
    process.env.NODE_ENV = "production";
    expect(() => createTranslationProvider()).toThrow(/not allowed when NODE_ENV=production/);
  });

  it("throws if google provider is selected without an API key", () => {
    process.env.TRANSLATION_PROVIDER = "google";
    delete process.env.GOOGLE_TRANSLATE_API_KEY;
    expect(() => createTranslationProvider()).toThrow(/GOOGLE_TRANSLATE_API_KEY is required/);
  });
});
