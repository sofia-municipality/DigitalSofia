import { React, useState } from "react";
import {
  PERMANENT_ADDRESS_FORM_PATH,
  CURRENT_ADDRESS_FORM_PATH,
} from "../../../../../../../constants/constants";
import SmCta from "../../../../../components/buttons/SmCta";
import { APPLICATION_STATUS_LABEL } from "../../../../../../../constants/formEmbeddedConstants";
import AddressRegistrationCardHeader from "../AddressRegistrationCardHeader";
import AddressRegistrationCardAccordion from "../AddressRegistrationCardAccordion";
import { useGetAddressCardProps } from "../../hooks";
import styles from "./addressRegistrationCard.module.scss";
import { useTranslation } from "react-i18next";
import Modal from "../../../../../components/Modal/Modal";
import { draftChildEligibilityCheck } from "../../../../../../../apiManager/services/draftService";
import { SERVICES_IDS } from "../../../../../../../constants/constants";
import { useHistory } from "react-router-dom";
import { useSelector } from "react-redux";

const AddressRegistrationCard = ({
  id,
  draftId,
  applicationId,
  formId,
  submissionId,
  behalf,
  names,
  formName,
  formPath = "",
  status,
  resultingCertificateUrl,
  formioApplicationStatus,
  entryNumber,
  address,
  property,
  childCustody,
  trusteeFirstName,
  trusteeLastName,
  ownerFirstName,
  ownerLastName,
  ownerPdfUrl,
  ownerSignutureDate,
  trusteePdfUrl,
  trusteeSignitureDate,
  submitterTaskId,
  ownerRejectionDate,
  trusteeRejectionDate,
  ownerInvitationExpiredDate,
  trusteeInvitationExpiredDate,
  ownerInvitationWithdrawnDate,
  trusteeInvitationWithdrawnDate,
  paymentCode,
  processInstanceId,
  stepsCount = 8,
  onDelete = () => {},
  onInvitationResend = () => {},
  onWithdraw = () => {},
  onSubmissionResend = () => {},
  onPayInitiated = () => {},
}) => {
  const [isChildEligibilityModalTriggered, setChildEligibilityModalTriggered] =
    useState(false);
  const history = useHistory();

  const { t } = useTranslation();
  const props = useGetAddressCardProps({
    t,
    status,
    formioApplicationStatus,
    draftId,
    applicationId,
    formId,
    submissionId,
    behalf,
    resultingCertificateUrl,
    onDelete,
    property,
    childCustody,
    trusteeFirstName,
    trusteeLastName,
    ownerFirstName,
    ownerLastName,
    entryNumber,
    ownerPdfUrl,
    ownerSignutureDate,
    trusteePdfUrl,
    trusteeSignitureDate,
    submitterTaskId,
    ownerRejectionDate,
    trusteeRejectionDate,
    ownerInvitationExpiredDate,
    trusteeInvitationExpiredDate,
    ownerInvitationWithdrawnDate,
    trusteeInvitationWithdrawnDate,
    paymentCode,
    onInvitationResend,
    processInstanceId,
    onWithdraw,
    onSubmissionResend,
    onPayInitiated,
  });
  const user = useSelector((state) => state.user.userDetail);

  if (!Object.keys(props).length) {
    return null;
  }

  const {
    accordionTitle,
    accordionContent,
    Icon,
    borderClassName,
    iconClassName,
    activeStepIndex,
    ctas,
  } = props;

  const fullFormName = formPath?.includes(PERMANENT_ADDRESS_FORM_PATH)
    ? t(`pernament.address.form.title.${behalf || "myBehalf"}`)
    : formPath?.includes(CURRENT_ADDRESS_FORM_PATH)
    ? t(`current.address.form.title.${behalf || "myBehalf"}`)
    : formName;

  let formType = "";
  if (formPath?.includes(CURRENT_ADDRESS_FORM_PATH)) {
    formType = CURRENT_ADDRESS_FORM_PATH;
  } else if (formPath?.includes(PERMANENT_ADDRESS_FORM_PATH)) {
    formType = PERMANENT_ADDRESS_FORM_PATH;
  }

  const checkIfAlreadyChildEligibilityStarted = async (cta) => {
    const dataPayload = {
      serviceId: SERVICES_IDS[formType],
      personIdentifier: user.personIdentifier?.replace("PNOBG-", ""),
    };
    try {
      const res = await draftChildEligibilityCheck(dataPayload);
      if (res.status === 200) {
        history.push(cta.customHref);
      }
    } catch (error) {
      if (
        error &&
        error.response &&
        error.response.status &&
        error.response.status === 422
      ) {
        handleModal(true);
      }
    }
  };

  const handleModal = (value) => {
    setChildEligibilityModalTriggered(value);
  };

  ctas.map((cta) => {
    if (cta.isDraft && behalf === "myBehalf") {
      cta.onClick = () => {
        checkIfAlreadyChildEligibilityStarted(cta);
      };
    } else {
      return {
        ...cta,
      };
    }
  });

  return (
    <section
      className={styles.addressCardWrapper}
      aria-label={`${t("scrren.reader.my.services.list.item")} ${id + 1}`}
    >
      <AddressRegistrationCardHeader
        names={names}
        formName={fullFormName}
        entryNumber={entryNumber}
        stepsCount={stepsCount}
        statusLabel={t(APPLICATION_STATUS_LABEL[status])}
        Icon={Icon}
        borderClassName={borderClassName}
        iconClassName={iconClassName}
        activeStepIndex={activeStepIndex}
      />
      <AddressRegistrationCardAccordion
        id={id}
        title={accordionTitle}
        content={accordionContent}
        className={styles.accordion}
        borderClassName={borderClassName}
      />
      <div className={styles.addressCardFooter}>
        <div className={styles.address}>{address}</div>
        <div className={styles.ctasWrapper}>
          {ctas.map(
            ({ type, href, text, Icon, onClick, className, ...rest }, index) =>
              type === "text" ? (
                <span key={index} className={className}>
                  {text}
                </span>
              ) : (
                <SmCta
                  key={index}
                  className={`${styles.addressCta} ${className ?? ""}`}
                  type={type}
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
      </div>
      <Modal
        modalOpen={isChildEligibilityModalTriggered}
        description={t("addressRegistratrion.childEligibilityCheck")}
        showNo={false}
        onYes={() => {
          handleModal(false);
        }}
      />
    </section>
  );
};

export default AddressRegistrationCard;
