import { httpGETRequest, httpPOSTRequest } from "../httpRequestHandler";
import API from "../endpoints";

export const getTaxData = async (limit) => {
  const res = await httpGETRequest(API.GET_TAX_DATA, { limit });
  return res.data;
};

export const getPaidTaxDataGroups = async ({ pageNo = 1, limit = 10 }) => {
  const url = `${API.GET_PAY_TAX_GROUP_DATA}?page=${pageNo}&limit=${limit}`;
  const res = await httpGETRequest(url);
  return res.data;
};

export const getPaidTaxData = async ({ id, refetch }) => {
  let url = API.GET_PAY_TAX_DATA.replace("<taxGroupId>", id);
  if (refetch) {
    url += "?refetch=true";
  }
  const res = await httpGETRequest(url);
  return res.data;
};

const mapTaxDataForPayment = ({ selectedItems, taxSubject }) => {
  const batchData = Object.values(selectedItems).map((batch) => batch.data);
  const mappedData = batchData.reduce((accBatch, item) => {
    const taxRecords = Object.values(item)
      .filter((item) => item?.data)
      .map((item) => item.data);

    const taxes = taxRecords.reduce((accTax, item) => {
      accTax = [...accTax, ...item];
      return accTax;
    }, []);

    accBatch = [...accBatch, ...taxes];
    return accBatch;
  }, []);

  return {
    payment_requests: mappedData,
    taxSubjectId: taxSubject?.taxSubjectId,
  };
};

export const payTaxData = async (data) => {
  const mappedData = mapTaxDataForPayment(data);
  const res = await httpPOSTRequest(API.PAY_TAX_DATA, mappedData);
  return res.data;
};
