import { Router } from "express";
import { TranslationService } from "../services/translationService";
import { DetectLanguageRequestBody } from "../types";
import { ValidationError } from "../utils/ApiException";

export function createDetectLanguageRouter(service: TranslationService): Router {
  const router = Router();

  router.post("/detect-language", async (req, res, next) => {
    try {
      const body = req.body as Partial<DetectLanguageRequestBody>;
      if (typeof body.text !== "string") {
        throw new ValidationError("'text' is required and must be a string.");
      }

      const language = await service.detectLanguage(body.text);
      res.status(200).json({ success: true, language });
    } catch (err) {
      next(err);
    }
  });

  return router;
}
