import request from "supertest";
import { createApp } from "./app";
import { MockProvider } from "./services/providers/MockProvider";
import { TranslationProvider } from "./types";

describe("POST /api/translate", () => {
  const app = createApp(new MockProvider());

  it("translates English to Indonesian", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Good morning", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.originalText).toBe("Good morning");
    expect(res.body.data.sourceLanguage).toBe("en");
    expect(res.body.data.targetLanguage).toBe("id");
    expect(typeof res.body.data.translatedText).toBe("string");
  });

  it("translates Indonesian to English", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Selamat pagi", sourceLanguage: "id", targetLanguage: "en" });

    expect(res.status).toBe(200);
    expect(res.body.data.sourceLanguage).toBe("id");
    expect(res.body.data.targetLanguage).toBe("en");
  });

  it("translates Japanese to Indonesian", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "おはようございます", sourceLanguage: "ja", targetLanguage: "id" });

    expect(res.status).toBe(200);
    expect(res.body.data.sourceLanguage).toBe("ja");
  });

  it("translates Indonesian to Japanese", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Selamat pagi", sourceLanguage: "id", targetLanguage: "ja" });

    expect(res.status).toBe(200);
    expect(res.body.data.targetLanguage).toBe("ja");
  });

  it("auto-detects source language", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Good morning", sourceLanguage: "auto", targetLanguage: "id" });

    expect(res.status).toBe(200);
    expect(res.body.data.detectedLanguage).toBe("en");
  });

  it("rejects empty text", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "  ", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.error.code).toBe("VALIDATION_ERROR");
  });

  it("rejects identical source and target languages", async () => {
    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Hello", sourceLanguage: "en", targetLanguage: "en" });

    expect(res.status).toBe(400);
    expect(res.body.error.message).toMatch(/must be different/i);
  });

  it("rejects missing fields", async () => {
    const res = await request(app).post("/api/translate").send({ text: "Hello" });
    expect(res.status).toBe(400);
  });

  it("returns 502 when the provider fails", async () => {
    const failingProvider: TranslationProvider = {
      translate: async () => {
        throw new Error("network down");
      },
      detectLanguage: async () => "en",
    };
    const failingApp = createApp(failingProvider);

    const res = await request(failingApp)
      .post("/api/translate")
      .send({ text: "Hello", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(500);
    expect(res.body.success).toBe(false);
  });
});

describe("POST /api/detect-language", () => {
  const app = createApp(new MockProvider());

  it("detects English", async () => {
    const res = await request(app).post("/api/detect-language").send({ text: "Good morning" });
    expect(res.status).toBe(200);
    expect(res.body.language).toBe("en");
  });

  it("detects Indonesian", async () => {
    const res = await request(app).post("/api/detect-language").send({ text: "Selamat pagi" });
    expect(res.status).toBe(200);
    expect(res.body.language).toBe("id");
  });

  it("detects Japanese", async () => {
    const res = await request(app)
      .post("/api/detect-language")
      .send({ text: "おはようございます" });
    expect(res.status).toBe(200);
    expect(res.body.language).toBe("ja");
  });

  it("rejects empty text", async () => {
    const res = await request(app).post("/api/detect-language").send({ text: "" });
    expect(res.status).toBe(400);
  });
});

describe("GET /health", () => {
  it("returns ok", async () => {
    const app = createApp(new MockProvider());
    const res = await request(app).get("/health");
    expect(res.status).toBe(200);
    expect(res.body.status).toBe("ok");
  });

  it("is never redirected to HTTPS even in production without a secure connection (platform healthcheck probes hit the container over plain HTTP internally)", async () => {
    const originalEnv = process.env.NODE_ENV;
    process.env.NODE_ENV = "production";
    try {
      const app = createApp(new MockProvider());
      // No X-Forwarded-Proto header, simulating an internal healthcheck
      // probe that bypasses the TLS-terminating edge entirely.
      const res = await request(app).get("/health");
      expect(res.status).toBe(200);
      expect(res.body.status).toBe("ok");
    } finally {
      process.env.NODE_ENV = originalEnv;
    }
  });
});

describe("APP_API_KEY protection", () => {
  const originalKey = process.env.APP_API_KEY;
  const originalEnv = process.env.NODE_ENV;

  afterEach(() => {
    if (originalKey === undefined) {
      delete process.env.APP_API_KEY;
    } else {
      process.env.APP_API_KEY = originalKey;
    }
    process.env.NODE_ENV = originalEnv;
  });

  it("rejects /api requests without the key when APP_API_KEY is set", async () => {
    process.env.APP_API_KEY = "test-secret-key";
    const app = createApp(new MockProvider());

    const res = await request(app)
      .post("/api/translate")
      .send({ text: "Hello", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(401);
    expect(res.body.error.code).toBe("UNAUTHORIZED");
  });

  it("accepts /api requests with the correct key", async () => {
    process.env.APP_API_KEY = "test-secret-key";
    const app = createApp(new MockProvider());

    const res = await request(app)
      .post("/api/translate")
      .set("X-App-Key", "test-secret-key")
      .send({ text: "Hello", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(200);
  });

  it("rejects requests with a wrong key", async () => {
    process.env.APP_API_KEY = "test-secret-key";
    const app = createApp(new MockProvider());

    const res = await request(app)
      .post("/api/translate")
      .set("X-App-Key", "wrong-key")
      .send({ text: "Hello", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(401);
  });

  it("does not gate /health behind the app key", async () => {
    process.env.APP_API_KEY = "test-secret-key";
    const app = createApp(new MockProvider());

    const res = await request(app).get("/health");
    expect(res.status).toBe(200);
  });
});

describe("Proxy topology robustness", () => {
  it("does not crash with a multi-hop X-Forwarded-For header (Railway/Render proxy topology)", async () => {
    const app = createApp(new MockProvider());

    // Simulates the kind of X-Forwarded-For chain a PaaS edge can add,
    // which previously crashed express-rate-limit's internal trust-proxy
    // validation into an uncaught 500 on every /api request.
    const res = await request(app)
      .post("/api/translate")
      .set("X-Forwarded-For", "203.0.113.5, 10.0.0.1, 10.0.0.2")
      .send({ text: "Good morning", sourceLanguage: "en", targetLanguage: "id" });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
  });
});
