from enum import Enum


class BaseEnum(Enum):
    def __eq__(self, value: object) -> bool:
        return self.value == value


class DocumentStatusesEnum(BaseEnum):
    PENDING = "Pending"
    DELIVERING = "Delivering"
    ON_HOLD = "On hold"
    SIGNED = "Signed"
    GENERATED = "Generated"
    EXPIRED = "Expired"
    REJECTED = "Rejected"


class SignatureSourceEnum(BaseEnum):
    DIGITAL_SOFIA = "digitalSofia"


class FormIOPathEnum(BaseEnum):
    RECEIPTS = "receipts"
    GENERATED_FILES = "generated-files"


class ReceiptTypeEnum(BaseEnum):
    SUBMISSION_ACCEPTANCE = "SubmissionAcceptance"
    CONSIGMENT_ACCEPTANCE = "ConsignmentAcceptance"
    CONTENT_HANDOVER = "ContentHandover"
    CONTENT_HANDOVER_FAILURE = "ContentHandoverFailure"


class ApplicationStatusEnum(BaseEnum):
    DRAFT = "Draft"
    WITHDRAWN = "withdrawn"
    E_DELIVERY_ERROR = "edeliveryError"
    SIGN_DOCUMENT_PENDING = "signDocumentPending"
    SIGNATURE_NEEDED = "signitureNeeded"
    CANCELLED_THIRD_PARTY_SIGNATURE = "cancelledThirdPartySigniture"
    REJECTED = "rejected"
    COMPLETED = "Completed"
    PAID = "paid"
    EXPIRED_INVITATION = "expiredInvitation"
    WAITING_FOR_THIRD_PARTY_SIGNATURE = "waitingForThirdPartySigniture"
    FORM_SUBMITTED = "formSubmitted"
    WAITING_FOR_PAYMENT = "waitingForPayment"
    WITHDRAWN_INVITATION = "withdrawn"
    SUBMISSION_ERROR = "submissionError"
    CANCELLED_PAYMENT = "cancelledPayment",
    CANCELLED = "canceled"
