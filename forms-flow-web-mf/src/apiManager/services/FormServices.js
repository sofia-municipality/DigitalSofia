import { RequestService } from "@formsflow/service";
import API from "../endpoints";
import { setCustomSubmission } from "../../actions/checkListActions";
import { replaceUrl } from "../../helper/helper";

export const formCreate = (formData) => {
  return RequestService.httpPOSTRequest(API.FORM_DESIGN, formData);
};

export const formUpdate = (form_id,formData) => {
  return RequestService.httpPUTRequest(`${API.FORM_DESIGN}/${form_id}`, formData);
};

export const getFormHistory = (form_id) => {
  return RequestService.httpGETRequest(`${API.FORM_HISTORY}/${form_id}`);
};


export const postCustomSubmission = (data, formId, isPublic, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const url = isPublic ? API.PUBLIC_CUSTOM_SUBMISSION : API.CUSTOM_SUBMISSION;
  const submissionUrl = replaceUrl(url, "<form_id>", formId);
  RequestService.httpPOSTRequest(`${submissionUrl}`, data)
    .then((res) => {
      if (res.data) {
        done(null, res.data);
      } else {
        done("Error Posting data", null);
      }
    })
    .catch((err) => {
      done(err, null);
    });
};

export const updateCustomSubmission = (data, formId, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const submissionUrl = replaceUrl(API.CUSTOM_SUBMISSION, "<form_id>", formId);
  RequestService.httpPUTRequest(`${submissionUrl}/${data._id}`, data)
    .then((res) => {
      if (res.data) {
        done(null, res.data);
      } else {
        done("Error updating data", null);
      }
    })
    .catch((err) => {
      done(err, null);
    });
};

export const getCustomSubmission = (submissionId, formId, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const submissionUrl = replaceUrl(API.CUSTOM_SUBMISSION, "<form_id>", formId);

  return (dispatch) => {
    RequestService.httpGETRequest(`${submissionUrl}/${submissionId}`, {})
      .then((res) => {
        if (res.data) {
          dispatch(setCustomSubmission(res.data));
        } else {
          dispatch(setCustomSubmission({}));
        }
      })
      .catch((err) => {
        done(err, null);
      });
  };
};

export const getFormSubmissionsByPath = async ({ path, ...data }) => {
  const URL = API.GET_FORM_SUBMISSIONS_BY_PATH.replace("<form_path>", path);
  const res = await RequestService.httpGETRequest(URL, data, null, false, {
    "X-Jwt-Token": localStorage.getItem("formioToken"),
  });

  const contentRange = res.headers["content-range"];
  const totalItemsCount = contentRange.split("/")[1];

  return {
    data: res.data,
    total: totalItemsCount,
  };
};

export const getFormSubmissionByFormPath = async ({ path, submissionId }) => {
  const URL = API.GET_FORM_SUBMISSION_BY_FORM_PATH.replace(
    "<form_path>",
    path
  ).replace("<submission_id>", submissionId);
  const res = await RequestService.httpGETRequest(URL, null, null, false, {
    "X-Jwt-Token": localStorage.getItem("formioToken"),
  });
  return res.data;
};

export const updateFormSubmissionByFormPath = async ({
  path,
  submissionId,
  data,
}) => {
  const URL = API.GET_FORM_SUBMISSION_BY_FORM_PATH.replace(
    "<form_path>",
    path
  ).replace("<submission_id>", submissionId);
  const res = await RequestService.httpPUTRequest(
    URL,
    { data, state: "submitted" },
    null,
    false,
    {
      "X-Jwt-Token": localStorage.getItem("formioToken"),
    }
  );
  return res.data;
};

export const deleteFormSubmissionByFormPath = async ({
  path,
  submissionId,
}) => {
  const URL = API.GET_FORM_SUBMISSION_BY_FORM_PATH.replace(
    "<form_path>",
    path
  ).replace("<submission_id>", submissionId);
  const res = await RequestService.httpDELETERequest(URL, null, null, false, {
    "X-Jwt-Token": localStorage.getItem("formioToken"),
  });
  return res.data;
};

export const createFormSubmissionByFormPath = async ({ path, data }) => {
  const URL = API.GET_FORM_SUBMISSIONS_BY_PATH.replace("<form_path>", path);
  const res = await RequestService.httpPOSTRequest(
    URL,
    { data, state: "submitted" },
    null,
    false,
    {
      "X-Jwt-Token": localStorage.getItem("formioToken"),
    }
  );
  return res.data;
};

export const getFormSubmissionByUrl = async (url) => {
  const res = await RequestService.httpGETRequest(url, null, null, false, {
    "X-Jwt-Token": localStorage.getItem("formioToken"),
  });
  return res.data;
};
