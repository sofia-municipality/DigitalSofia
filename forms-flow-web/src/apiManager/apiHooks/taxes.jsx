import { useApiCall } from "./common";
import { getTaxData } from "../services/taxServices";

export const useGetTaxReference = (limit = 300) =>
  useApiCall(getTaxData, limit);
