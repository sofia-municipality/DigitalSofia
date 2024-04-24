import { useCallback } from "react";
import { useTranslation } from "react-i18next";

export const useCustomTranslate = () => {
  const { t } = useTranslation();
  return useCallback(
    (template, replacements) => {
      replacements = replacements || {};
      // Translate
      template = t(template);
      // Replace
      return template.replace(/{([^}]+)}/g, function (_, key) {
        return replacements[key] || "{" + key + "}";
      });
    },
    [t]
  );
};