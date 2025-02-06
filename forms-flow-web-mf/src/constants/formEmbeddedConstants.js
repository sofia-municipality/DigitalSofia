export const APPLICATION_STATUS = {
  NEW: "New",
  DRAFT: "Draft",
  DRAFT_IN_PROGRESS: "draftInProgress",
  DRAFT_FILLED: "draftFilled",
  WAITING_FOR_THIRD_PARTY_SIGNUTURE: "waitingForThirdPartySigniture",
  CANCELLED_THIRD_PARTY_SIGNUTURE: "cancelledThirdPartySigniture",
  EXPIRED_INVITATION: "expiredInvitation",
  WITHDRAWN_INVITATION: "withdrawn",
  SIGNITURE_NEEDED: "signitureNeeded",
  SIGN_DOCUMENT_PENDING: "signDocumentPending",
  FORM_SUBMITTED: "formSubmitted",
  MORE_DOCUMENTS_NEEDED: "moreDocumentsNeeded",
  REJECTED: "rejected",
  WAITING_FOR_PAYMENT: "waitingForPayment",
  PAID: "paid",
  COMPLETED: "Completed",
  CANCELED: "canceled",
  CANCELLED_PAYMENT: "cancelledPayment",
  SUBMISSION_ERROR: "submissionError",
  EDELIVERY_ERROR: "edeliveryError",
};

export const APPLICATION_STATUS_LABEL = {
  [APPLICATION_STATUS.DRAFT_IN_PROGRESS]:
    "myServices.status.draftInProgress.label",
  [APPLICATION_STATUS.DRAFT_FILLED]: "myServices.status.draftFilled.label",
  [APPLICATION_STATUS.WITHDRAWN_INVITATION]:
    "myServices.status.draftFilled.label",
  [APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE]:
    "myServices.status.waitingForThirdPartySigniture.label",
  [APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE]:
    "myServices.status.cancelledThirdPartySigniture.label",
  [APPLICATION_STATUS.EXPIRED_INVITATION]:
    "myServices.status.expiredInvitation.label",
  [APPLICATION_STATUS.SIGNITURE_NEEDED]:
    "myServices.status.signitureNeeded.label",
  [APPLICATION_STATUS.SIGN_DOCUMENT_PENDING]:
    "myServices.status.waitingForSigniture.label",
  [APPLICATION_STATUS.FORM_SUBMITTED]: "myServices.status.formSubmitted.label",
  [APPLICATION_STATUS.MORE_DOCUMENTS_NEEDED]:
    "myServices.status.moreDocumentsNeeded.label",
  [APPLICATION_STATUS.REJECTED]: "myServices.status.rejected.label",
  [APPLICATION_STATUS.WAITING_FOR_PAYMENT]:
    "myServices.status.waitingForPayment.label",
  [APPLICATION_STATUS.PAID]: "myServices.status.paid.label",
  [APPLICATION_STATUS.COMPLETED]: "myServices.status.completed.label",
  [APPLICATION_STATUS.CANCELED]: "myServices.status.draftFilled.label",
  [APPLICATION_STATUS.NEW]: "myServices.status.formSubmitted.label",
  [APPLICATION_STATUS.DRAFT]: "myServices.status.draftInProgress.label",
  [APPLICATION_STATUS.SUBMISSION_ERROR]:
    "myServices.status.submissionError.label",
  [APPLICATION_STATUS.EDELIVERY_ERROR]:
    "myServices.status.submissionError.label",
  [APPLICATION_STATUS.CANCELLED_PAYMENT]:
    "myServices.status.formSubmitted.label",
};

export const DEFAULT_APPLICATION_STATUS_LABEL = "myServices.status.formSubmitted.label";
