import { httpGETRequest } from "../httpRequestHandler";
import API from "../endpoints";
import lodash from "lodash";

const mapRegixData = (data, mappers = []) => {
  const mappedData = {};
  mappers.forEach((mapper) => {
    lodash.set(
      mappedData,
      mapper.target,
      mapper.mapper(lodash.get(data, mapper.prop))
    );
  });

  return mappedData;
};

export const getUserPersonalDataRegix = async ({ params, url, mappers }) => {
  const res = await httpGETRequest(url || API.GET_REGIX_DATA, params);
  return mapRegixData(res.data, mappers);
};
