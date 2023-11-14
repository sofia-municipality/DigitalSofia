import React from "react";
import { useTranslation } from "react-i18next";
import Modal from "react-bootstrap/Modal";
import CloseIcon from "@mui/icons-material/Close";

import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../../components/buttons/SmCta";
import { useSendProcessEvent } from "../../../../../../../../apiManager/apiHooks";

import { STEPS } from "./steps";
import styles from "../resendInvitationModal.module.scss";

const ConfirmStep = ({
  invitee,
  processInstanceId,
  setCurrentStep,
  onClose,
}) => {
  const { t } = useTranslation();
  const { fetch: sendProcessEvent, isLoading } = useSendProcessEvent();

  const onResendInvitationConfirm = async () => {
    const messageName = `${invitee}_invitation_resend`;
    try {
      await sendProcessEvent({ messageName, processInstanceId });
      setCurrentStep(STEPS.SUCCESS);
    } catch (err) {
      setCurrentStep(STEPS.ERROR);
    }
  };

  return (
    <div>
      <div className={styles.modalCloseCta}>
        <SmCta
          size={SmCtaSizes.SMALL}
          type={SmCtaTypes.OUTLINE}
          onClick={onClose}
          accessibilityProps={{
            "aria-label": t("screen.reader.modal.close.cta"),
          }}
        >
          <CloseIcon />
        </SmCta>
      </div>
      <div className={styles.modalContentContainer}>
        <Modal.Header className={styles.modalHeader}>
          <Modal.Title id="modal-title">
            {t("myServices.resendInvitation.modal.title")}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body className={styles.modalBody}>
          {t("myServices.resendInvitation.modal.message")}
        </Modal.Body>
        <Modal.Footer className={styles.modalFooter}>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.OUTLINE}
            onClick={onClose}
            disabled={isLoading}
          >
            {t("modal.cta.cancel")}
          </SmCta>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.SECONDARY}
            onClick={onResendInvitationConfirm}
            className={styles.confirmCta}
            loading={isLoading}
            disabled={isLoading}
          >
            {t("myServices.resendInvitation.modal.yes.cta")}
          </SmCta>
        </Modal.Footer>
      </div>
    </div>
  );
};

export default ConfirmStep;
