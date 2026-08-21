import { Router } from "express";
import { TranslationService } from "../services/translationService";
import { ApiSuccess, TranslateRequestBody, TranslateResponseData } from "../types";
import { ValidationError } from "../utils/ApiException";

export function createTranslateRouter(service: TranslationService): Router {
  const router = Router();

  router.post("/translate", async (req, res, next) => {
    try {
      const body = req.body as Partial<TranslateRequestBody>;

      if (typeof body.text !== "string") {
        throw new ValidationError("'text' is required and must be a string.");
      }
      if (typeof body.sourceLanguage !== "string") {
        throw new ValidationError("'sourceLanguage' is required.");
      }
      if (typeof body.targetLanguage !== "string") {
        throw new ValidationError("'targetLanguage' is required.");
      }

      const data = await service.translate(
        body.text,
        body.sourceLanguage as TranslateRequestBody["sourceLanguage"],
        body.targetLanguage as TranslateRequestBody["targetLanguage"]
      );

      const response: ApiSuccess<TranslateResponseData> = { success: true, data };
      res.status(200).json(response);
    } catch (err) {
      next(err);
    }
  });

  return router;
}
