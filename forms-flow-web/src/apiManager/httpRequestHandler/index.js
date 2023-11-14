import axios from "axios";

import UserService from "../../services/UserService";
import { LANGUAGE } from "../../constants/constants";

// const qs = require("querystring");

const getDefaultHeaders = (isBearer, token, language = LANGUAGE) => ({
  Authorization: isBearer ? `Bearer ${token || UserService.getToken()}` : token,
  "Accept-Language": language,
});

export const httpGETRequest = (
  url,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.get(url, {
    params: data,
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpGETBlobRequest = (
  url,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.get(url, {
    params: data,
    responseType: "blob",
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpPOSTRequest = (
  url,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.post(url, data, {
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpPOSTBlobRequest = (
  url,
  params,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.post(url, data, {
    params: params,
    responseType: "blob",
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpPOSTRequestWithoutToken = (url, data) => {
  return axios.post(url, data, {
    headers: {
      "Content-Type": "application/json",
    },
  });
};

export const httpGETRequestWithoutToken = (
  url,
  data,
  language = LANGUAGE,
  headers = {}
) => {
  return axios.get(url, {
    params: data,
    headers: {
      "Content-Type": "application/json",
      "Accept-Language": language,
      ...headers,
    },
  });
};

export const httpPOSTRequestWithHAL = (url, data, token, isBearer = true) => {
  return axios.post(url, data, {
    headers: {
      Authorization: isBearer
        ? `Bearer ${token || UserService.getToken()}`
        : token,
      Accept: "application/hal+json",
    },
  });
};

export const httpPUTRequest = (
  url,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.put(url, data, {
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpPATCHRequest = (
  url,
  data,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.patch(url, data, {
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpDELETERequest = (
  url,
  token,
  isBearer = true,
  headers = null,
  language
) => {
  return axios.delete(url, {
    headers: !headers ? getDefaultHeaders(isBearer, token, language) : headers,
  });
};

export const httpPUTRequestWithoutToken = (url, data) => {
  return axios.put(url, data, {
    headers: {
      "Content-Type": "application/json",
    },
  });
};
