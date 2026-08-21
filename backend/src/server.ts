import dotenv from "dotenv";
dotenv.config();

import { createApp } from "./app";
import { createTranslationProvider } from "./services/providerFactory";

const PORT = Number(process.env.PORT) || 3000;

try {
  const provider = createTranslationProvider();
  const app = createApp(provider);

  app.listen(PORT, () => {
    // eslint-disable-next-line no-console
    console.log(`LinguaTranslate backend listening on port ${PORT}`);
    // eslint-disable-next-line no-console
    console.log(`Provider: ${process.env.TRANSLATION_PROVIDER || "libretranslate"}`);
  });
} catch (err) {
  // eslint-disable-next-line no-console
  console.error("Failed to start server:", (err as Error).message);
  process.exit(1);
}
