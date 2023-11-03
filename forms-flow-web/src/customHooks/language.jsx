import { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { LANGUAGE } from "../constants/constants";
import { useFetchLanguages } from "../apiManager/apiHooks/language";
import { getLanguage } from "../apiManager/services/languageServices";
import fallbackTranslations from "../resourceBundles";

const convertArrayToObject = (data) =>
  data.reduce((acc, item) => {
    Object.entries(item).forEach(([key, value]) => (acc[key] = value));
    return acc;
  }, {});

export const useSetInitialLanguageTranslations = () => {
  const [i18nReady, seti18nReady] = useState(false);
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const [data, isLoading] = useFetchLanguages(tenantKey);

  useEffect(() => {
    if (data && !isLoading) {
      const getTranslations = data.map(async (lang) => {
        const languageCode = lang.name.toLowerCase();
        try {
          const translationsData = await getLanguage(lang.name, tenantKey);
          if (!translationsData?.length) {
            return {
              [languageCode]: {
                translation: fallbackTranslations[languageCode],
              },
            };
          } else {
            const translations = convertArrayToObject(translationsData);
            return {
              [languageCode]: {
                translation: translations,
              },
            };
          }
        } catch (_) {
          return Promise.resolve({
            [languageCode]: {
              translation: fallbackTranslations[languageCode],
            },
          });
        }
      });

      Promise.all(getTranslations).then((res) => {
        const resources = convertArrayToObject(res);
        i18n
          .use(LanguageDetector)
          .use(initReactI18next)
          .init(
            {
              lng: localStorage.getItem("lang") || LANGUAGE,
              fallbackLng: "en",
              resources,
            },
            () => {
              seti18nReady(true);
            }
          );
      });
    }
  }, [data, isLoading, tenantKey]);

  return i18nReady && !isLoading;
};
