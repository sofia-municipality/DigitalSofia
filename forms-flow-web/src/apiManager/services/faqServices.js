/* istanbul ignore file */
import {
  httpGETRequest,
  httpPUTRequest,
  httpPOSTRequest,
  httpDELETERequest,
} from "../httpRequestHandler";
import { Translation } from "react-i18next";
import { toast } from "react-toastify";
import API from "../endpoints";

export const fetchFAQ = (
  page,
  perPage,
  language,
  isFavoured,
  sortBy = "id",
  sortOrder = "desc"
) => {
  return httpGETRequest(
    `${API.GET_FAQS}`,
    {
      pageNo: page,
      limit: perPage,
      sortBy,
      sortOrder,
      isFavoured,
    },
    null,
    true,
    null,
    language
  );
};

export const getFAQ = (id, language) => {
  const url = API.GET_FAQ.replace("<faq_id>", id);
  return httpGETRequest(url, null, null, true, null, language)
    .then((res) => {
      return res.data;
    })
    .catch((error) => {
      throw error;
    });
};

export const updateFAQ = async (
  id,
  payload,
  language,
  withNotification = true
) => {
  const url = API.GET_FAQ.replace("<faq_id>", id);
  try {
    const res = await httpPUTRequest(url, payload, null, true, null, language);
    withNotification &&
      toast.success(
        <Translation>{(t) => t("Successfully Updated")}</Translation>
      );
    return res.data;
  } catch (error) {
    toast.error(
      <Translation>{(t_1) => t_1("Something went wrong")}</Translation>
    );
    throw error;
  }
};

export const addFAQ = async (payload, language) => {
  try {
    const res = await httpPOSTRequest(
      API.GET_FAQS,
      payload,
      null,
      true,
      null,
      language
    );
    toast.success(<Translation>{(t) => t("Successfully Added")}</Translation>);
    return res.data;
  } catch (error) {
    toast.error(
      <Translation>{(t_1) => t_1("Something went wrong")}</Translation>
    );
    throw error;
  }
};

export const deleteFAQ = async (id) => {
  const url = API.GET_FAQ.replace("<faq_id>", id);
  try {
    const res = await httpDELETERequest(url);
    toast.success(
      <Translation>{(t) => t("Successfully Deleted")}</Translation>
    );
    return res.data;
  } catch (error) {
    toast.error(
      <Translation>{(t_1) => t_1("Something went wrong")}</Translation>
    );
    throw error;
  }
};
