/* istanbul ignore file */
//import { httpGETRequest, httpPOSTRequest } from "../httpRequestHandler";
import { httpGETRequest, httpPOSTRequest } from "../httpRequestHandler";
import API from "../endpoints/index";
import { replaceUrl } from "../../helper/helper";

export const fetchFormAuthorizationDetials = (formId) => {
  const url = replaceUrl(
    API.HANDLE_AUTHORIZATION_FOR_DESIGNER,
    "<resource_id>",
    formId
  );
  return httpGETRequest(url);
};

export const handleAuthorization = (data, formId) => {
  const url = replaceUrl(
    API.HANDLE_AUTHORIZATION_FOR_DESIGNER,
    "<resource_id>",
    formId
  );
  return httpPOSTRequest(url, data);
};

export const getUserRoles = () => {
  const url = API.USER_ROLES;
  return httpGETRequest(url);
};

export const getClientList = (id) => {
  let url = API.CLIENT_LIST;
  if (id) {
    url += `/${id}`;
  }
  return httpGETRequest(url);
};

export const getReviewerList = (id) => {
  let url = API.APPLICATION_LIST;
  if (id) {
    url += `/${id}`;
  }
  return httpGETRequest(url);
};
