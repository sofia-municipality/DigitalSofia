import { RequestService } from "@formsflow/service";
import { setSelectLanguages } from "../../actions/languageSetAction";
import API from "../endpoints";

const mapLanguages = (languages = []) =>
  languages.map((lang) => ({
    name: lang.language?.toLowerCase(),
    value: lang.languageShort,
    label: lang.languageLong,
  }));

export const fetchSelectLanguages = (tenantKey, withFallback = true) => {
  return async (dispatch) => {
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
      dispatch(setSelectLanguages(languages));
      return languages;
    } catch (err) {
      if (!withFallback) throw err;
      return await dispatch(fetchFallbackLanguages());
    }
  };
};

export const fetchFallbackLanguages = () => {
  return async (dispatch) => {
    const res = await fetch(
      `${process.env.PUBLIC_URL}/languageConfig/languageData.json`,
      {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
      }
    );
    const data = await res.json();
    dispatch(setSelectLanguages(data));
    return data;
  };
};
