import { useCallback } from "react";
import { useApi } from "./common";
import {
  fetchFAQ,
  updateFAQ,
  getFAQ,
  addFAQ,
  deleteFAQ,
} from "../services/faqServices";

export const useFetchFAQ = (page, perPage, language, isFavoured) =>
  useApi(
    useCallback(async () => {
      const res = await fetchFAQ(page, perPage, language, isFavoured);
      return res?.data;
    }, [page, perPage, language, isFavoured])
  );

export const useGetFAQ = (id, language) =>
  useApi(useCallback(() => getFAQ(id, language), [id, language]));

export const useUpdateFAQ = () => (id, payload, language, withNotification) =>
  updateFAQ(id, payload, language, withNotification);

export const useAddFAQ = () => (payload, language) => addFAQ(payload, language);

export const useDeleteFAQ = () => (id) => deleteFAQ(id);
