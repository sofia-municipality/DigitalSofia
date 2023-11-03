import { useApiCall } from "./common";

import { withdrawApplication } from "../services/applicationServices";

export const useWithdrawApplication = (params) =>
  useApiCall(withdrawApplication, params, false);
