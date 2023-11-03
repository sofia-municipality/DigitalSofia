import { WEB_BASE_URL } from "./config";

const API = {
    GET_LANGUAGES: `${WEB_BASE_URL}/translations/languages`,
    GET_LANGUAGES_FALLBACK: `${window.location.origin}/languageConfig/languageData.json`,
    GET_LANGUAGE_TRANSLATIONS: `${WEB_BASE_URL}/translations/languages/<language>`,
}

export default API;