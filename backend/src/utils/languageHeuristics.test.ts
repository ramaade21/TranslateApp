import { detectLanguageHeuristic } from "./languageHeuristics";

describe("detectLanguageHeuristic", () => {
  it("detects English from common words", () => {
    expect(detectLanguageHeuristic("Good morning, how are you?")).toBe("en");
  });

  it("detects Indonesian from common words", () => {
    expect(detectLanguageHeuristic("Selamat pagi, apa kabar?")).toBe("id");
  });

  it("detects Japanese from script", () => {
    expect(detectLanguageHeuristic("おはようございます")).toBe("ja");
  });

  it("detects informal Indonesian slang via morphology (-in suffix)", () => {
    expect(detectLanguageHeuristic("beresin")).toBe("id");
  });

  it("detects informal Indonesian slang via morphology (-kan suffix)", () => {
    expect(detectLanguageHeuristic("kerjakan tugas itu")).toBe("id");
  });

  it("defaults to English with no strong signal either way", () => {
    expect(detectLanguageHeuristic("xyz123")).toBe("en");
  });

  it("handles empty text without throwing", () => {
    expect(detectLanguageHeuristic("")).toBe("en");
  });
});
