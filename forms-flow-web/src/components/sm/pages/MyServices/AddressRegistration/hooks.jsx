import { useTranslation } from "react-i18next";
import { useSelector, useDispatch } from "react-redux";
import CloseIcon from "@mui/icons-material/Close";
import FileDownloadOutlinedIcon from "@mui/icons-material/FileDownloadOutlined";
import CheckOutlinedIcon from "@mui/icons-material/CheckOutlined";
import UpdateIcon from "@mui/icons-material/Update";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import moment from "moment";

import { useGetBaseUrl } from "../../../../../customHooks";
import { APPLICATION_STATUS } from "../../../../../constants/formEmbeddedConstants";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import { DRAFT_ENABLED } from "../../../../../constants/constants";
import { getProcessReq } from "../../../../../apiManager/services/bpmServices";
import { updateApplicationStatus } from "../../../../../apiManager/services/applicationServices";
import selectApplicationCreateAPI from "../../../../Form/Item/apiSelectHelper";
import { sendProcessEvent } from "../../../../../apiManager/services/applicationServices";
import { EDELIVERY_RETRY_EVENT } from "../../../../../constants/applicationConstants";
import { downloadFile } from "../../../../../utils";

import SmCta, {
  SmCtaSizes,
  SmCtaTypes,
} from "../../../components/buttons/SmCta";

import styles from "./hooks.module.scss";
import { useState } from "react";

export const useGetAddressCardProps = ({
  t,
  status,
  formioApplicationStatus,
  draftId,
  applicationId,
  formId,
  submissionId,
  behalf,
  property,
  childCustody,
  resultingCertificateUrl,
  trusteeFirstName,
  trusteeLastName,
  ownerFirstName,
  ownerLastName,
  entryNumber,
  onDelete = () => {},
  // Add reject reason when BE retrieves it
  // rejectReason,
  ownerPdfUrl,
  ownerSignutureDate,
  trusteePdfUrl,
  trusteeSignitureDate,
  submitterTaskId,
  ownerRejectionDate,
  trusteeRejectionDate,
  ownerInvitationExpiredDate,
  trusteeInvitationExpiredDate,
  paymentCode,
  onInvitationResend,
  processInstanceId,
  onWithdraw,
  ownerInvitationWithdrawnDate,
  trusteeInvitationWithdrawnDate,
  onSubmissionResend,
  onPayInitiated,
}) => {
  const dispatch = useDispatch();
  const baseUrl = useGetBaseUrl();
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const [isButtonClicked, setIsButtonClicked] = useState(false);

  const thirdPartySignitureContentProps = {
    t,
    applicationId,
    behalf,
    formId,
    submissionId,
    childCustody,
    property,
    ownerFirstName,
    ownerLastName,
    ownerPdfUrl,
    ownerSignutureDate,
    ownerRejectionDate,
    trusteeFirstName,
    trusteeLastName,
    trusteeSignitureDate,
    trusteeRejectionDate,
    trusteePdfUrl,
    ownerInvitationExpiredDate,
    trusteeInvitationExpiredDate,
    onInvitationResend,
    processInstanceId,
    onWithdraw,
    ownerInvitationWithdrawnDate,
    trusteeInvitationWithdrawnDate,
  };

  switch (status) {
    case APPLICATION_STATUS.DRAFT_IN_PROGRESS:
    case APPLICATION_STATUS.DRAFT_FILLED:
      return {
        activeStepIndex: 0,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t("myServices.status.draft.accordion.title"),
        accordionContent: t("myServices.status.draft.accordion.content"),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
          {
            type: SmCtaTypes.SECONDARY,
            text: t("myServices.continue.cta"),
            isLink: true,
            href: `${PAGE_ROUTES.DRAFT_EDIT.replace(":formId", formId).replace(
              ":draftId",
              draftId
            )}?behalf=${behalf}`,
          },
        ],
      };
    case APPLICATION_STATUS.CANCELED:
    case APPLICATION_STATUS.WITHDRAWN_INVITATION:
      return {
        activeStepIndex: 0,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t("myServices.status.draft.accordion.title"),
        accordionContent: t("myServices.status.draft.accordion.content"),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
          {
            type: SmCtaTypes.SECONDARY,
            text: t("myServices.continue.cta"),
            isLink: true,
            href: `${PAGE_ROUTES.SUBMISSION_EDIT.replace(
              ":formId",
              formId
            ).replace(":submissionId", submissionId)}?behalf=${behalf}`,
          },
        ],
      };
    case APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE:
      return {
        activeStepIndex: 1,
        Icon: UpdateIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t(
          "myServices.status.waitingForThirdPartySigniture.accordion.title"
        ),
        accordionContent: renderWaitingForThidPartySignitureContent(
          thirdPartySignitureContentProps
        ),
        ctas: [
          {
            type: "text",
            text: t("myServices.waitingForSigniture.cta"),
            className: styles.waitingForSignText,
          },
        ],
      };
    case APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE:
      return {
        activeStepIndex: 2,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t(
          "myServices.status.cancelledThirdPartySigniture.accordion.title"
        ),
        accordionContent: renderCancelledThidPartySignitureContent(
          thirdPartySignitureContentProps
        ),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
        ],
      };
    case APPLICATION_STATUS.EXPIRED_INVITATION:
      return {
        activeStepIndex: 2,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t(
          "myServices.status.expiredThirdPartySigniture.accordion.title"
        ),
        accordionContent: renderExpiredThidPartySignitureContent(
          thirdPartySignitureContentProps
        ),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
        ],
      };
    case APPLICATION_STATUS.SIGNITURE_NEEDED:
      return {
        activeStepIndex: 3,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t("myServices.status.signitureNeeded.accordion.title"),
        accordionContent: t(
          "myServices.status.signitureNeeded.accordion.content"
        ),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
          {
            type: SmCtaTypes.SECONDARY,
            text: t("myServices.continue.cta"),
            isLink: true,
            href: PAGE_ROUTES.USER_TASK.replace(":taskId", submitterTaskId),
          },
        ],
      };
    case APPLICATION_STATUS.SIGN_DOCUMENT_PENDING:
      return {
        activeStepIndex: 3,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t(
          "myServices.status.waitingForSigniture.accordion.title"
        ),
        accordionContent: t(
          "myServices.status.waitingForSigniture.accordion.content"
        ),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
        ],
      };
    case APPLICATION_STATUS.SUBMISSION_ERROR:
      return {
        activeStepIndex: 3,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t("myServices.status.submission.error.title"),
        accordionContent: t("myServices.status.submission.error.content"),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.status.submission.error.tryAgain.cta"),
            onClick: () => {
              const apiCall = selectApplicationCreateAPI(
                isAuth,
                draftId,
                DRAFT_ENABLED && draftId
              );

              const origin = `${window.location.origin}${baseUrl}`;
              const data = getProcessReq({ _id: formId }, submissionId, origin);

              dispatch(
                apiCall(data, draftId ? draftId : null, (err) => {
                  if (!err) {
                    updateApplicationStatus({
                      applicationId,
                      applicationStatus: formioApplicationStatus,
                      formUrl: data.formUrl,
                    })
                      .then(() => {
                        setIsButtonClicked(false);
                        onSubmissionResend();
                      })
                      .catch(() => {
                        setIsButtonClicked(false);
                      });
                  }
                })
              );
            },
            loading: isButtonClicked,
            disabled: isButtonClicked,
          },
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
        ],
      };
    case APPLICATION_STATUS.EDELIVERY_ERROR:
      return {
        activeStepIndex: 3,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t("myServices.status.submission.error.title"),
        accordionContent: t("myServices.status.submission.error.content"),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.status.submission.error.tryAgain.cta"),
            onClick: () => {
              setIsButtonClicked(true);
              sendProcessEvent({
                messageName: EDELIVERY_RETRY_EVENT,
                processInstanceId,
              })
                .then(() => {
                  setIsButtonClicked(false);
                  onSubmissionResend();
                })
                .catch(() => {
                  setIsButtonClicked(false);
                });
            },
            loading: isButtonClicked,
            disabled: isButtonClicked,
          },
        ],
      };
    case APPLICATION_STATUS.NEW:
    case APPLICATION_STATUS.FORM_SUBMITTED:
    case APPLICATION_STATUS.CANCELLED_PAYMENT:
      return {
        activeStepIndex: 4,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t("myServices.status.formSubmitted.accordion.title"),
        accordionContent: renderFormSubmitSuccessContent(t, entryNumber),
        ctas: [],
      };
    case APPLICATION_STATUS.MORE_DOCUMENTS_NEEDED:
      return {
        activeStepIndex: 5,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t(
          "myServices.status.moreDocumentsNeeded.accordion.title"
        ),
        accordionContent: t(
          "myServices.status.moreDocumentsNeeded.accordion.content"
        ),
        ctas: [
          {
            type: SmCtaTypes.SECONDARY,
            text: t("myServices.uploadDocument.cta"),
            href: "/",
          },
        ],
      };
    case APPLICATION_STATUS.REJECTED:
      return {
        activeStepIndex: 5,
        Icon: CloseIcon,
        borderClassName: "bg-sm-circle-border-red",
        iconClassName: "bg-sm-red",
        accordionTitle: t("myServices.status.rejected.accordion.title"),
        accordionContent: renderFormSubmissionRejectedContent(t),
        // Add reject reason when BE retrieves it
        // accordionContent: renderFormSubmissionRejectedContent(t, rejectReason),
        ctas: [
          {
            type: SmCtaTypes.OUTLINE,
            text: t("myServices.delete.cta"),
            onClick: () => {
              onDelete(applicationId);
            },
          },
        ],
      };
    case APPLICATION_STATUS.WAITING_FOR_PAYMENT:
      return {
        activeStepIndex: 6,
        Icon: EditOutlinedIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t(
          "myServices.status.waitingForPayment.accordion.content.title"
        ),
        accordionContent: t(
          "myServices.status.waitingForPayment.accordion.content.content"
        ),
        ctas: [
          {
            type: SmCtaTypes.SECONDARY,
            text: t("myServices.pay.cta"),
            onClick: () => onPayInitiated(paymentCode),
          },
        ],
      };
    case APPLICATION_STATUS.PAID:
      return {
        activeStepIndex: 7,
        Icon: UpdateIcon,
        borderClassName: "bg-sm-circle-border-blue",
        iconClassName: "bg-sm-blue",
        accordionTitle: t("myServices.status.paid.accordion.title"),
        accordionContent: t("myServices.status.paid.accordion.content"),
        ctas: [],
      };
    case APPLICATION_STATUS.COMPLETED:
      return {
        activeStepIndex: 8,
        Icon: CheckOutlinedIcon,
        borderClassName: "bg-sm-circle-border-green",
        iconClassName: "bg-sm-green",
        accordionTitle: t("myServices.status.completed.accordion.title"),
        accordionContent: t("myServices.status.completed.accordion.content"),
        ctas: [
          {
            type: SmCtaTypes.SUCCESS,
            text: t("myServices.downloadDocument.cta"),
            Icon: FileDownloadOutlinedIcon,
            onClick: () => {
              const fileName = entryNumber
                ? `${entryNumber}.pdf`
                : "Удостоверение.pdf";
              downloadFile(resultingCertificateUrl, fileName);
            },
          },
        ],
      };
    default:
      return {};
  }
};

const renderFormSubmissionRejectedContent = (t) => (
  <div>
    <p>{`${t("myServices.status.rejected.accordion.content.title")}`}</p>
    {/* Add reject reason when BE is ready */}
    {/* )} ${rejectReason}`}</p> */}
    <p>{t("myServices.status.rejected.accordion.content.description")}</p>
  </div>
);

const renderFormSubmitSuccessContent = (t, entryNumber) => (
  <div>
    <p>{t("myServices.status.formSubmitted.accordion.content.title")}</p>
    <p>{t("myServices.status.formSubmitted.accordion.content.description")}</p>
    <p>{`${t(
      "myServices.status.formSubmitted.accordion.content.referenceNumber"
    )} ${entryNumber}`}</p>
  </div>
);

const renderItemStatus = (
  t,
  {
    date,
    rejectedActionCtas,
    successActionCtas,
    expiredActionCtas,
    withdrawnActionCtas,
    isWithdrawn,
    isRejected,
    isSuccess,
    isExpired,
  }
) => {
  let iconSrc;
  let dateLabel;
  let actionCtas;
  if (isSuccess) {
    iconSrc = "/check_with_circle.svg";
    dateLabel =
      "myServices.status.waitingForThirdPartySigniture.signiture.success.date.label";
    actionCtas = successActionCtas;
  } else if (isExpired) {
    iconSrc = "/circle_with_x.svg";
    dateLabel =
      "myServices.status.expiredThirdPartySigniture.signiture.expired.date.label";
    actionCtas = expiredActionCtas;
  } else if (isRejected) {
    iconSrc = "/circle_with_x.svg";
    dateLabel =
      "myServices.status.cancelledThirdPartySigniture.signiture.rejected.date.label";
    actionCtas = rejectedActionCtas;
  } else if (isWithdrawn) {
    iconSrc = "/circle_with_x.svg";
    dateLabel =
      "myServices.status.cancelledThirdPartySigniture.signiture.withdrawn.date.label";
    actionCtas = withdrawnActionCtas;
  }

  return (
    <div className="">
      <div className="d-flex align-items-start">
        <img className="mr-3" alt="" width="20" height="20" src={iconSrc} />
        <span className={styles.signedOnText}>{`${t(dateLabel)} ${moment(
          date
        ).format("DD.MM.YYYY")}`}</span>
      </div>
      {actionCtas?.length ? (
        <div className="mt-3">
          {actionCtas.map(
            ({ href, text, Icon, onClick, className, ...rest }, index) => (
              <SmCta
                key={index}
                className={className ?? ""}
                isLink={!!href}
                href={href}
                onClick={onClick}
                {...rest}
              >
                {Icon ? <Icon className="mr-3" /> : null}
                <span>{text}</span>
              </SmCta>
            )
          )}
        </div>
      ) : null}
    </div>
  );
};

const renderThidPartySignitureContent = ({
  t,
  applicationId,
  behalf,
  formId,
  submissionId,
  childCustody,
  property,
  ownerFirstName,
  ownerLastName,
  ownerPdfUrl,
  ownerSignutureDate,
  ownerRejectionDate,
  trusteeFirstName,
  trusteeLastName,
  trusteeSignitureDate,
  trusteeRejectionDate,
  trusteePdfUrl,
  description,
  subdescription,
  signitureGroupTitle,
  ownerInvitationExpiredDate,
  trusteeInvitationExpiredDate,
  ownerInvitationWithdrawnDate,
  trusteeInvitationWithdrawnDate,
  onInvitationResend,
  processInstanceId,
  onWithdraw,
}) => {
  const getRejectedActionCtas = (invitee) => [
    {
      type: SmCtaTypes.QUATERNARY,
      size: SmCtaSizes.SMALL,
      text: t("myServices.checkData.cta"),
      href: `${PAGE_ROUTES.SUBMISSION_EDIT.replace(":formId", formId).replace(
        ":submissionId",
        submissionId
      )}?behalf=${behalf}&invitee=${invitee}`,
    },
  ];

  const getWithdrawnActionCtas = (invitee) => [
    {
      type: SmCtaTypes.QUATERNARY,
      size: SmCtaSizes.SMALL,
      text: t("myServices.checkData.cta"),
      href: `${PAGE_ROUTES.SUBMISSION_EDIT.replace(":formId", formId).replace(
        ":submissionId",
        submissionId
      )}?behalf=${behalf}&invitee=${invitee}`,
    },
  ];

  const getExpiredActionCtas = (invitee) => [
    {
      type: SmCtaTypes.QUATERNARY,
      size: SmCtaSizes.SMALL,
      text: t("myServices.continue.cta"),
      href: `${PAGE_ROUTES.SUBMISSION_EDIT.replace(":formId", formId).replace(
        ":submissionId",
        submissionId
      )}?behalf=${behalf}&invitee=${invitee}`,
    },
  ];
  const successActionCtas = [];
  const pendingSignatures = [];
  if (property === "anotherPersonProperty" || property === "liveWithOwner") {
    pendingSignatures.push({
      type: "owner",
      title: `${ownerFirstName + " " + ownerLastName}`,
      isSuccess: !!ownerPdfUrl,
      isRejected: !!ownerRejectionDate,
      isExpired: !!ownerInvitationExpiredDate,
      isWithdrawn: !!ownerInvitationWithdrawnDate,
      date:
        ownerRejectionDate ||
        ownerInvitationExpiredDate ||
        ownerInvitationWithdrawnDate ||
        ownerSignutureDate,
      rejectedActionCtas: getRejectedActionCtas("owner"),
      withdrawnActionCtas: getWithdrawnActionCtas("owner"),
      successActionCtas,
      expiredActionCtas: getExpiredActionCtas("owner"),
    });
  }

  if (childCustody === "sharedCustody") {
    pendingSignatures.push({
      type: "trustee",
      title: `${trusteeFirstName + " " + trusteeLastName}`,
      isSuccess: !!trusteePdfUrl,
      isRejected: !!trusteeRejectionDate,
      isExpired: !!trusteeInvitationExpiredDate,
      isWithdrawn: !!trusteeInvitationWithdrawnDate,
      date:
        trusteeRejectionDate ||
        trusteeInvitationExpiredDate ||
        trusteeInvitationWithdrawnDate ||
        trusteeSignitureDate,
      rejectedActionCtas: getRejectedActionCtas("trustee"),
      withdrawnActionCtas: getWithdrawnActionCtas("trustee"),
      successActionCtas,
      expiredActionCtas: getExpiredActionCtas("trustee"),
    });
  }

  return (
    <div>
      <p className="sm-body-2-regular mb-0">{t(description)}</p>
      {subdescription ? (
        <p className="sm-body-2-regular mb-0">{t(subdescription)}</p>
      ) : null}
      {signitureGroupTitle ? (
        <p className="sm-body-2-regular mb-0 mt-4">{t(signitureGroupTitle)}</p>
      ) : null}
      {pendingSignatures.map((item, index) => {
        return (
          <div key={index} className={`${styles.pendingSignatureCard}`}>
            <p className="sm-heading-5 text-sm-indigo-dark">{item.title}</p>
            {item.isSuccess ||
            item.isRejected ||
            item.isExpired ||
            item.isWithdrawn ? (
              renderItemStatus(t, item)
            ) : (
              <div className={styles.pendingSignatureCardCtasWrapper}>
                <SmCta
                  type={SmCtaTypes.OUTLINE}
                  size={SmCtaSizes.SMALL}
                  className="p-0"
                  onClick={() =>
                    onWithdraw({
                      invitee: item.type,
                      processInstanceId,
                      applicationId,
                    })
                  }
                >
                  <span className="sm-cta-outline-underline">
                    {t(
                      "myServices.status.waitingForThirdPartySigniture.accordion.cancelInvitation.cta"
                    )}
                  </span>
                </SmCta>
                <SmCta
                  type={SmCtaTypes.QUATERNARY}
                  size={SmCtaSizes.SMALL}
                  onClick={() =>
                    onInvitationResend({
                      invitee: item.type,
                      processInstanceId,
                    })
                  }
                >
                  {t(
                    "myServices.status.waitingForThirdPartySigniture.accordion.resendInvitation.cta"
                  )}
                </SmCta>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};

const renderWaitingForThidPartySignitureContent = (props) =>
  renderThidPartySignitureContent({
    ...props,
    description:
      "myServices.status.waitingForThirdPartySigniture.accordion.content.description",
    subdescription:
      "myServices.status.waitingForThirdPartySigniture.accordion.content.subdescription",
    signitureGroupTitle:
      "myServices.status.waitingForThirdPartySigniture.accordion.content.title",
  });

const renderCancelledThidPartySignitureContent = (props) =>
  renderThidPartySignitureContent({
    ...props,
    description:
      "myServices.status.cancelledThirdPartySigniture.accordion.content.description",
    subdescription:
      "myServices.status.cancelledThirdPartySigniture.accordion.content.subdescription",
    signitureGroupTitle:
      "myServices.status.cancelledThirdPartySigniture.accordion.content.title",
  });

const renderExpiredThidPartySignitureContent = (props) =>
  renderThidPartySignitureContent({
    ...props,
    description:
      "myServices.status.expiredThirdPartySigniture.accordion.content",
    signitureGroupTitle:
      "myServices.status.expiredThirdPartySigniture.accordion.content.title",
  });

export const useGetFullAddress = () => {
  const { t } = useTranslation();
  return (
    address,
    streetNumber,
    region,
    entrance,
    floorNumber,
    appartmentNumber
  ) => {
    const mappedAddress = address?.name_pa || address;
    const addressString =
      typeof mappedAddress === "string" ? mappedAddress : "";

    if (!addressString) return "";
    let fullAddress = `${addressString}, № ${streetNumber?.building_number}`;
    if (entrance) {
      fullAddress += `, ${t("address.entrance.short")} ${entrance}`;
    }

    if (floorNumber) {
      fullAddress += `, ${t("address.floorNumber.short")} ${floorNumber}`;
    }

    if (appartmentNumber) {
      fullAddress += `, ${t(
        "address.appartmentNumber.short"
      )} ${appartmentNumber}`;
    }

    fullAddress += `, София, ${region?.name}`;

    return fullAddress;
  };
};
