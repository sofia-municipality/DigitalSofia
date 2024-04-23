import {
  httpPOSTRequest,
  httpGETRequest,
  httpPUTRequest,
  httpPATCHRequest,
  httpDELETERequest,
} from "../httpRequestHandler";
// httpGETRequest,
import API from "../endpoints";
import { setCustomSubmission } from "../../actions/checkListActions";
import { replaceUrl } from "../../helper/helper";

export const formCreate = (formData) => {
  return httpPOSTRequest(API.FORM_DESIGN, formData);
};

export const formUpdate = (form_id, formData) => {
  return httpPUTRequest(`${API.FORM_DESIGN}/${form_id}`, formData);
};

export const getFormHistory = (form_id) => {
  return httpGETRequest(`${API.FORM_HISTORY}/${form_id}`);
};

export const postCustomSubmission = (data, formId, isPublic, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const url = isPublic ? API.PUBLIC_CUSTOM_SUBMISSION : API.CUSTOM_SUBMISSION;
  const submissionUrl = replaceUrl(url, "<form_id>", formId);
  httpPOSTRequest(`${submissionUrl}`, data)
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
  httpPUTRequest(`${submissionUrl}/${data._id}`, data)
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

const getSubmissionDataRequest = (data = {}) => {
  return Object.entries(data).map(([key, value]) => ({
    op: "replace",
    path: `/data/${key}`,
    value,
  }));
};

export const updateSubmissionData = async (
  submissionId,
  formId,
  data,
  ...rest
) => {
  const done = rest.length ? rest[0] : () => {};
  const submissionUrl = API.UPDATE_SUBMISSION_DATA.replace(
    "<form_id>",
    formId
  ).replace("<submission_id>", submissionId);

  try {
    const res = await httpPATCHRequest(
      submissionUrl,
      getSubmissionDataRequest(data),
      null,
      false,
      {
        "X-Jwt-Token": localStorage.getItem("formioToken"),
      }
    );
    if (res.data) {
      done(null, res.data);
    } else {
      done("Error updating data", null);
    }
  } catch (err) {
    done(err, null);
  }
};

export const deleteSubmission = async (submissionId, formId, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const submissionUrl = API.DELETE_SUBMISSION.replace(
    "<form_id>",
    formId
  ).replace("<submission_id>", submissionId);

  try {
    const res = await httpDELETERequest(submissionUrl, null, false, {
      "X-Jwt-Token": localStorage.getItem("formioToken"),
    });
    if (res.data) {
      done(null, res.data);
    } else {
      done("Error deleting data", null);
    }
  } catch (err) {
    done(err, null);
  }
};

export const getCustomSubmission = (submissionId, formId, ...rest) => {
  const done = rest.length ? rest[0] : () => {};
  const submissionUrl = replaceUrl(API.CUSTOM_SUBMISSION, "<form_id>", formId);

  return (dispatch) => {
    httpGETRequest(`${submissionUrl}/${submissionId}`, {})
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

const getRequestedFormProps = () => {
  const requestedFormioProps = [
    "draftId",
    "behalf",
    "applicationStatus",
    "reference_number",
    "resultingCertificateUrl",
    "address",
    "streetNumber",
    "region",
    "entrance",
    "floorNumber",
    "appartmentNumber",
    "property",
    "childCustody",
    "childFirstName",
    "childMiddleName",
    "childLastName",
    "otherPersonFirstName",
    "otherPersonMiddleName",
    "otherPersonLastName",
    "firstName",
    "middleName",
    "lastName",
    "trusteeFirstName",
    "trusteeLastName",
    "propertyOwnerFirstName",
    "propertyOwnerLastName",
    "propertyOwnerPdfUrl",
    "propertyOwnerSignutureDate",
    "trusteePdfUrl",
    "trusteeSignitureDate",
    "propertyOwnerRejectionDate",
    "trusteeRejectionDate",
    "propertyOwnerInvitationExpiredDate",
    "trusteeInvitationExpiredDate",
    "propertyOwnerInvitationWithdrawnDate",
    "trusteeInvitationWithdrawnDate",
    "submitterTaskId",
  ];

  const requiredCamundaProps = ["paymentAccessCode"];

  return `formioFields=${requestedFormioProps.join(
    ","
  )}&bpmFields=${requiredCamundaProps.join(",")}`;
};

export const fetchDraftsAndSubmissions = async ({
  pageNo = 1,
  limit = 20,
} = {}) => {
  const URL = `${
    API.GET_SERVICES
  }?pageNo=${pageNo}&limit=${limit}&${getRequestedFormProps()}`;
  const res = await httpGETRequest(URL);
  return res.data;
};

export const getFormSubmissionsByPath = async ({ path, ...data }) => {
  const URL = API.GET_FORM_SUBMISSIONS_BY_PATH.replace("<form_path>", path);
  const res = await httpGETRequest(URL, data, null, false, {
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
  const res = await httpGETRequest(URL, null, null, false, {
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
  const res = await httpPUTRequest(
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
  const res = await httpDELETERequest(URL, null, false, {
    "X-Jwt-Token": localStorage.getItem("formioToken"),
  });
  return res.data;
};

export const createFormSubmissionByFormPath = async ({ path, data }) => {
  const URL = API.GET_FORM_SUBMISSIONS_BY_PATH.replace("<form_path>", path);
  const res = await httpPOSTRequest(
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

export const checkFormRestrictions = () => {
  return false;
};
