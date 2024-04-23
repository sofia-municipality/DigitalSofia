import { useState } from "react";

import { getUserPersonalDataRegix } from "../services/regix";
import {
  signDocument,
  getDocumentStatus,
} from "../services/documentSignServices";
import {
  fetchDraftsAndSubmissions,
  updateSubmissionData,
  getFormSubmissionsByPath,
  getFormSubmissionByFormPath,
  updateFormSubmissionByFormPath,
  createFormSubmissionByFormPath,
  deleteFormSubmissionByFormPath,
  checkFormRestrictions,
} from "../services/FormServices";
import {
  checkApplicationPermissions,
  updateApplicationStatus,
} from "../services/applicationServices";
import { getBPMUserTaskDetail } from "../services/bpmTaskServices";
import { getFormIdSubmissionIdFromURL } from "../services/formatterService";

import { useApiCall } from "./common";

const mapError = (err = {}) => {
  switch (err.status) {
    case 403:
      return "autofill.error.invalid.card";
    default:
      return "autofill.error.default.message";
  }
};

export const useAutofillPersonalData = () => {
  const [isLoading, setIsLoading] = useState();
  const [data, setData] = useState();
  const [error, setError] = useState();
  const fetch = async (params) => {
    try {
      setError(null);
      setIsLoading(true);
      const res = await getUserPersonalDataRegix(params);
      setData(res);
      setIsLoading(false);
      return res;
    } catch (err) {
      const errorMsg = params.error || mapError(err.response);
      setError(errorMsg);
      setIsLoading(false);
    }
  };

  return { fetch, data, isLoading, error };
};

export const useSignDocument = () => {
  const [isLoading, setIsLoading] = useState();
  const [data, setData] = useState();
  const [error, setError] = useState();
  const fetch = async (params) => {
    try {
      setError(null);
      setIsLoading(true);
      const res = await signDocument(params);
      setData(res);
      setIsLoading(false);
      return res;
    } catch (err) {
      setError(err);
      setIsLoading(false);
      throw err;
    }
  };

  return { fetch, data, isLoading, error };
};

export const useGetDocumentSignStatus = () => {
  const [isLoading, setIsLoading] = useState();
  const [data, setData] = useState();
  const [error, setError] = useState();
  const fetch = async (params) => {
    try {
      setError(null);
      setIsLoading(true);
      const res = await getDocumentStatus(params);
      setData(res);
      setIsLoading(false);
      return res;
    } catch (err) {
      setError(err);
      setIsLoading(false);
      throw err;
    }
  };

  return { fetch, data, isLoading, error };
};

export const useGetDraftsAndSubmissions = (params) =>
  useApiCall(fetchDraftsAndSubmissions, params, false);

export const useCheckApplicationPermissions = (params) =>
  useApiCall(checkApplicationPermissions, params, false);

export const useGetBPMUserTaskDetail = (params) =>
  useApiCall(getBPMUserTaskDetail, params);

export const useUpdateApplicationStatus =
  () =>
  ({ applicationId, applicationStatus, formUrl }) => {
    const { formId, submissionId } = getFormIdSubmissionIdFromURL(formUrl);
    const updateApplicationStatusInBE = () =>
      updateApplicationStatus({
        applicationId,
        applicationStatus,
        formUrl,
      });
    const updateApplicationStatusInSubmission = () =>
      updateSubmissionData(submissionId, formId, { applicationStatus });

    const apiCall = Promise.all([
      updateApplicationStatusInBE(),
      updateApplicationStatusInSubmission(),
    ]);

    return apiCall;
  };

export const useGetFormSubmissionsByPath = (params, onMount) =>
  useApiCall(getFormSubmissionsByPath, params, onMount);

export const useGetFormSubmissionByFormPath = (params) =>
  useApiCall(getFormSubmissionByFormPath, params, false);

export const useUpdateFormSubmissionByFormPath = (params) =>
  useApiCall(updateFormSubmissionByFormPath, params, false);

export const useCreateFormSubmissionByFormPath = (params) =>
  useApiCall(createFormSubmissionByFormPath, params, false);

export const useDeleteFormSubmissionByFormPath = (params) =>
  useApiCall(deleteFormSubmissionByFormPath, params, false);

export const useCheckFormRestrictions = (params) =>
  useApiCall(checkFormRestrictions, params, false);
