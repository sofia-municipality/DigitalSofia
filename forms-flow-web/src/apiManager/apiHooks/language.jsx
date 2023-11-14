import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";

import { MULTITENANCY_ENABLED } from "../../constants/constants";
import { fetchSelectLanguages } from "../services/languageServices";

import {
  useGetFormSubmissionsByPath,
  useUpdateFormSubmissionByFormPath,
  useCreateFormSubmissionByFormPath,
  useDeleteFormSubmissionByFormPath,
} from "./form";
import { useApi } from "./common";

export const useFetchLanguages = (tenantKey) => {
  const dispatch = useDispatch();
  return useApi(
    useCallback(
      () => dispatch(fetchSelectLanguages(tenantKey)),
      [dispatch, tenantKey]
    )
  );
};

const useGetFormPath = () => {
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  let path = "language-translations";
  if (MULTITENANCY_ENABLED && tenantKey) {
    path = tenantKey + "-" + path;
  }

  return path;
};

export const useFetchAdminTranslations = () => {
  const { fetch, isLoading, data } = useGetFormSubmissionsByPath(null, false);
  const path = useGetFormPath();

  const getTranslations = useCallback(
    ({ limit, currentPage, searchData }) => {
      let payload = {
        path,
        sort: "-created",
        limit,
        skip: (currentPage - 1) * limit,
      };

      if (Object.keys(searchData).length) {
        let filter = {};
        Object.entries(searchData)
          .filter(([, value]) => !!value)
          .forEach(
            ([key, value]) => (filter[`data.${key}__regex`] = `/${value}/i`)
          );

        payload = { ...payload, ...filter };
      }

      return fetch(payload);
    },
    [fetch, path]
  );

  return {
    fetch: getTranslations,
    isLoading,
    data,
  };
};

const mapTranslationPayload = ({ language, key, translation }) => {
  return {
    language,
    key,
    identifier: `${language}-${key}`,
    status: "public",
    translation,
    submit: true,
  };
};

export const useUpdateTranslation = () => {
  const { fetch, isLoading, data } = useUpdateFormSubmissionByFormPath();
  const path = useGetFormPath();

  const updateTranslation = useCallback(
    ({ id: submissionId, ...params }) => {
      const payload = mapTranslationPayload(params);
      return fetch({
        path,
        submissionId,
        data: payload,
      });
    },
    [fetch, path]
  );

  return {
    fetch: updateTranslation,
    isLoading,
    data,
  };
};

export const useCreateTranslation = () => {
  const { fetch, isLoading, data } = useCreateFormSubmissionByFormPath();
  const path = useGetFormPath();

  const createTranslation = useCallback(
    (params) => {
      const payload = mapTranslationPayload(params);
      return fetch({
        path,
        data: payload,
      });
    },
    [fetch, path]
  );

  return {
    fetch: createTranslation,
    isLoading,
    data,
  };
};

export const useDeleteTranslation = () => {
  const { fetch, isLoading, data } = useDeleteFormSubmissionByFormPath();
  const path = useGetFormPath();

  const deleteTranslation = useCallback(
    (submissionId) => {
      return fetch({
        path,
        submissionId,
      });
    },
    [fetch, path]
  );

  return {
    fetch: deleteTranslation,
    isLoading,
    data,
  };
};
