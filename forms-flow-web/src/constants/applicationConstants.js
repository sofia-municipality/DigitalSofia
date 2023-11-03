/* istanbul ignore file */
import { APPLICATION_STATUS } from "./formEmbeddedConstants";
import API from "../apiManager/endpoints";

export const RESUBMITTED_STATUS_EVENT = "application_resubmitted";
export const ACKNOWLEDGED_EVENT = "application_acknowledged";
export const INVITATION_RESUBMITTED = "invitation_resubmitted";

//export const RETURNED_STATUS = "Returned";

// These are the old application status values used for checking the "Resubmit" status (backward compatibility).
export const RESUBMIT_STATUS = "Resubmit";
export const AWAITING_ACKNOWLEDGEMENT = "Awaiting Acknowledgement";
//export const NEW_STATUS = "New";

export const CLIENT_EDIT_STATUS = [
  AWAITING_ACKNOWLEDGEMENT,
  RESUBMIT_STATUS,
  APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE,
  APPLICATION_STATUS.WITHDRAWN_INVITATION,
  APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE,
  APPLICATION_STATUS.EXPIRED_INVITATION,
];

export const UPDATE_EVENT_STATUS = [
  RESUBMIT_STATUS,
  AWAITING_ACKNOWLEDGEMENT,
  APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE,
  APPLICATION_STATUS.WITHDRAWN_INVITATION,
  APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE,
  APPLICATION_STATUS.EXPIRED_INVITATION,
];

export const getProcessDataReq = (
  applicationDetail,
  submission,
  processMessage
) => {
  let url;
  const data = {
    messageName: "",
    processInstanceId: applicationDetail.processInstanceId,
  };

  // Check if the application is a resubmit, and if so, use the event name
  if (applicationDetail.isResubmit) {
    url = API.APPLICATION_EVENT_UPDATE.replace(
      "<application_id>",
      applicationDetail.id
    );
    data.data = submission;
    data.messageName = applicationDetail.eventName;
  } else {
    url = API.BPM_SENT_MESSAGE;
    switch (applicationDetail.applicationStatus) {
      case AWAITING_ACKNOWLEDGEMENT:
        data.messageName = ACKNOWLEDGED_EVENT;
        break;
      case APPLICATION_STATUS.WITHDRAWN_INVITATION:
      case RESUBMIT_STATUS:
        data.messageName = RESUBMITTED_STATUS_EVENT;
        break;
      case APPLICATION_STATUS.EXPIRED_INVITATION:
      case APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE:
        data.messageName = processMessage || INVITATION_RESUBMITTED;
        break;
      case APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE:
        data.messageName = processMessage || RESUBMITTED_STATUS_EVENT;
        break;
      default:
        return null; //TODO check
    }
  }

  return { data, url };
};
