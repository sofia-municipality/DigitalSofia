import moment from "moment";
import "moment/locale/bg";
import { useSelector } from "react-redux";

import { useApiCall } from "./common";
import {
  getTaxData,
  payTaxData,
  getPaidTaxDataGroups,
  getPaidTaxData,
} from "../services/taxServices";
import { capitalizeFirstLetter } from "../../utils";
import { EPAYMENT_REDIRECT_URL } from "../../constants/constants";

export const useGetTaxReference = (limit = 300) =>
  useApiCall(getTaxData, limit);

const useGroupTaxDataByMonthYear = (data) => {
  const lang = useSelector((state) => state.user.lang);
  if (!data?.length) return [];

  const mappedData = data
    .sort((a, b) => new Date(b.created) - new Date(a.created))
    .reduce((acc, item) => {
      const date = moment(item.created).locale(lang);
      const month = capitalizeFirstLetter(date.format("MMMM"));
      const year = date.format("YYYY");

      const mappedDate = `${month} ${year}`;
      if (!acc[mappedDate]) {
        acc[mappedDate] = [];
      }

      acc[mappedDate].push(item);
      return acc;
    }, {});
  return mappedData;
};

export const useGetPaidTaxesGroups = (params) => {
  const { data = {}, ...rest } = useApiCall(
    getPaidTaxDataGroups,
    params,
    false
  );
  const { obligations, ...otherData } = data;
  const mappedData = useGroupTaxDataByMonthYear(obligations);
  return { data: { obligations: mappedData, ...otherData }, ...rest };
};

const groupByBatchNumber = (data = {}) => {
  const mappedData = Object.entries(data)
    .filter(([, value]) => value?.length)
    .reduce((acc, [itemKey, itemValue]) => {
      const batches = {};
      itemValue.forEach((record) => {
        if (!batches[record.partida_no]) {
          batches[record.partida_no] = { data: [] };
        }

        batches[record.partida_no].data.push(record);
      });

      acc[itemKey] = { data: batches };
      return acc;
    }, {});

  const paymentDate = Object.entries(data)
    .filter(([, value]) => value?.length)
    .map(([, value]) => value)?.[0]?.[0]?.created;

  return { payments: mappedData, paymentDate };
};

export const useGetPaidTaxesData = (reqData) => {
  const { data, ...rest } = useApiCall(getPaidTaxData, reqData, false);
  const mappedData = groupByBatchNumber(data);
  return { data: mappedData, ...rest };
};

export const usePayTaxData = (reqData) => {
  const { data, ...rest } = useApiCall(payTaxData, reqData, false);
  if (data) {
    window.location.href = EPAYMENT_REDIRECT_URL;
  }

  return { data, ...rest };
};
