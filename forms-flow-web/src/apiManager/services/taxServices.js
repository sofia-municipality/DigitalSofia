import { httpGETRequest } from "../httpRequestHandler";
import API from "../endpoints";

export const getTaxData = async (limit) => {
  const res = await httpGETRequest(API.GET_TAX_DATA, { limit });
  return res.data;
};
