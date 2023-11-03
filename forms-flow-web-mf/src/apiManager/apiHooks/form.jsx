import {
  getFormSubmissionsByPath,
  getFormSubmissionByFormPath,
  updateFormSubmissionByFormPath,
  createFormSubmissionByFormPath,
  deleteFormSubmissionByFormPath,
} from "../services/FormServices";

import { useApiCall } from "./common";

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
