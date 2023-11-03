import { useApiCall } from "./common";

import { sendProcessEvent } from "../services/applicationServices";

export const useSendProcessEvent = (params) =>
  useApiCall(sendProcessEvent, params, false);
