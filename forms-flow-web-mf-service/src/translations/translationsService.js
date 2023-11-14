import i18n from "i18next";
import reactI18n from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import RequestService from '../request/requestService'
import API from '../endpoints'
import fallbackTranslations from "../resourceBundles";
import { MULTI_LANGUAGE_ENABLED, LANGUAGE } from "../constants"

const mapLanguages = (languages = []) =>
  languages.map((lang) => ({
    name: lang.language?.toLowerCase(),
    value: lang.languageShort,
    label: lang.languageLong,
  }));

const convertArrayToObject = (data) =>
  data.reduce((acc, item) => {
    Object.entries(item).forEach(([key, value]) => (acc[key] = value));
    return acc;
  }, {});

export const fetchLanguages = async (tenantKey, isMultitenancyEnabled, withFallback = true) => {
  if(isMultitenancyEnabled && !tenantKey) {
    return await fetchFallbackLanguages();
  } else {
    const headers = {};
    if (tenantKey) {
      headers["X-Tenant-Key"] = tenantKey;
    }
    try {
      const res = await RequestService.httpGETRequestWithoutToken(
        API.GET_LANGUAGES,
        undefined,
        headers
      );
      const languages = mapLanguages(res.data);
      return languages;
    } catch (err) {
      if (!withFallback) throw err;
      return await fetchFallbackLanguages();
    };
  }
};

export const fetchFallbackLanguages = async () => {
  const res = await fetch(
   API.GET_LANGUAGES_FALLBACK,
    {
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    }
  );
  const data = await res.json();
  return data;
};

export const getLanguage = async (language, tenantKey) => {
  const headers = {};
  if (tenantKey) {
    headers["X-Tenant-Key"] = tenantKey;
  }
  const url = API.GET_LANGUAGE_TRANSLATIONS.replace("<language>", language);
  const res = await RequestService.httpGETRequestWithoutToken(
    url,
    undefined,
    headers
  );
  return res.data;
};

export const loadTranslations = async (tenantKey, isMultitenancyEnabled, callback) => {
  const data = await fetchLanguages(tenantKey, isMultitenancyEnabled);

  if (data) {
    const getTranslations = data.map(async (lang) => {
      const languageCode = lang.name.toLowerCase();
      if(isMultitenancyEnabled && !tenantKey) {
        return Promise.resolve({
          [languageCode]: {
            translation: fallbackTranslations[languageCode],
          },
        });
      } else {
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
      }
    });

    const res = await Promise.all(getTranslations);
    const resources = convertArrayToObject(res);
      i18n
        .use(LanguageDetector)
        .use(reactI18n.initReactI18next)
        .init(
          {
            lng: MULTI_LANGUAGE_ENABLED ? localStorage.getItem("lang") || LANGUAGE : LANGUAGE,
            fallbackLng: "en",
            resources,
          },
          callback
        );
  }
};

export const changeLanguage = (language) => i18n.changeLanguage(language);

export const getFormTranslations = () =>
  Object.entries(i18n.options.resources).reduce((acc, [key, value]) => {
    acc[key] = value.translation;
    return acc;
  }, {});

