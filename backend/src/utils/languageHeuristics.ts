import { LanguageCode } from "../types";

// Common Indonesian words, including everyday informal/slang terms -
// not just formal function words. Used only as a lightweight fallback
// signal when a provider's own detection is unavailable or fails.
const INDONESIAN_MARKERS = new Set([
  // formal function words
  "yang", "dan", "di", "ke", "dari", "ini", "itu", "tidak", "saya", "kamu",
  "anda", "kami", "mereka", "adalah", "akan", "sudah", "belum", "apa",
  "kabar", "selamat", "pagi", "siang", "malam", "terima", "kasih", "mau",
  "bisa", "dengan", "untuk", "pada", "juga", "tapi", "atau", "karena",
  "dia", "kita", "harus", "perlu", "tolong", "coba", "lagi", "sekarang",
  "besok", "kemarin", "rumah", "makan", "minum", "kerja", "jalan", "pergi",
  "datang", "pulang", "teman", "keluarga", "sayang", "cinta", "lihat",
  "dengar", "bicara", "jangan", "boleh", "kalau", "jika", "supaya",
  "sebab", "sehingga", "walaupun", "meskipun", "sambil", "ketika", "saat",
  // common informal/slang
  "gimana", "udah", "gak", "nggak", "enggak", "banget", "aja", "doang",
  "kayak", "emang", "bikin", "pengen", "gitu", "sih", "deh", "kok",
  "kenapa", "yaudah", "yah", "nih", "tuh", "dong", "loh", "lho", "kan",
  "ngomong", "beresin", "kerjain", "bantuin", "cariin", "belanjain",
  "makasih", "oke", "iya", "nanti", "tadi", "barusan", "duluan",
]);

const ENGLISH_MARKERS = new Set([
  "the", "and", "is", "are", "you", "i", "to", "of", "in", "it", "this",
  "that", "was", "for", "on", "with", "as", "have", "be", "not", "good",
  "morning", "how", "thanks", "thank", "please", "going", "where",
  "what", "when", "who", "why", "can", "will", "would", "should", "could",
  "do", "does", "did", "has", "had", "were", "am", "we", "they", "he",
  "she", "him", "her", "them", "my", "your", "his", "its", "our", "their",
]);

/**
 * Returns true if a word matches common Indonesian morphology - the
 * informal "-in" suffix (e.g. "beresin", "kerjain") or the "-kan"
 * suffix (e.g. "kerjakan", "bantukan") are strong Indonesian signals
 * that don't occur as a productive pattern in English, so they help
 * catch informal/slang words that aren't in the fixed marker list
 * above.
 */
function hasIndonesianMorphology(word: string): boolean {
  if (word.length < 5) return false;
  return word.endsWith("in") || word.endsWith("kan") || word.endsWith("nya");
}

/**
 * Returns true if the text contains any Japanese script characters
 * (Hiragana, Katakana, or CJK Kanji ranges).
 */
function containsJapaneseScript(text: string): boolean {
  return /[\u3040-\u309F\u30A0-\u30FF\u4E00-\u9FFF]/.test(text);
}

/**
 * Lightweight, dependency-free language detection used as a fallback
 * when the configured translation provider does not support a
 * dedicated detect-language call, or when that call fails.
 *
 * This is intentionally simple: it is not meant to replace a proper
 * language-ID model, only to keep the API functional for the MVP.
 */
export function detectLanguageHeuristic(text: string): LanguageCode {
  const trimmed = text.trim();
  if (!trimmed) return "en";

  if (containsJapaneseScript(trimmed)) {
    return "ja";
  }

  const words = trimmed
    .toLowerCase()
    .replace(/[^\p{L}\s]/gu, "")
    .split(/\s+/)
    .filter(Boolean);

  let idScore = 0;
  let enScore = 0;

  for (const word of words) {
    if (INDONESIAN_MARKERS.has(word)) idScore++;
    if (ENGLISH_MARKERS.has(word)) enScore++;
    if (hasIndonesianMorphology(word)) idScore++;
  }

  if (idScore === 0 && enScore === 0) {
    // No strong signal - default to English, the most common source
    // language for this app's audience, rather than guessing wildly.
    return "en";
  }

  return idScore > enScore ? "id" : "en";
}
