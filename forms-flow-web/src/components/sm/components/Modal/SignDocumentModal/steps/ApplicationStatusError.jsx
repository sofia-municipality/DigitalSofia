import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";

import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";
import { useNavigateTo } from "../../../../../../customHooks";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import { APPLICATION_STATUS } from "../../../../../../constants/formEmbeddedConstants";

import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";

const useErrorDetails = (applicationStatus, onClose) => {
  const redirectToMyServices = useNavigateTo(
    PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION
  );

  switch (applicationStatus) {
    case APPLICATION_STATUS.EDELIVERY_ERROR:
      return {
        title: "form.error.modal.title",
        description: "form.error.modal.message",
        ctaText: "form.document.sign.evrotrust.modal.error.cta",
        handleClose: () => {
          onClose && onClose();
          redirectToMyServices();
        },
      };
    default:
      return {
        title: "form.error.modal.title",
        description: "form.error.modal.message",
        ctaText: "form.document.sign.evrotrust.modal.error.cta",
        handleClose: () => {
          onClose && onClose();
        },
      };
  }
};

const SignDocumentApplicationStatusError = React.memo(
  ({ onClose, applicationStatusForcedError }) => {
    const { t } = useTranslation();
    const { title, description, ctaText, handleClose } = useErrorDetails(
      applicationStatusForcedError,
      onClose
    );

    return (
      <>
        <div className={styles.modalCloseCta}>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.OUTLINE}
            onClick={handleClose}
            accessibilityProps={{
              "aria-label": t("screen.reader.modal.close.cta"),
            }}
          >
            <CloseIcon />
          </SmCta>
        </div>
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title id="modal-title">{t(title)}</Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-red text-white mb-5 mr-0">
                <CloseIcon />
              </div>
            </div>
            <p>{t(description)}</p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={handleClose}
              className={styles.confirmCta}
            >
              {t(ctaText)}
            </SmCta>
          </Modal.Footer>
        </div>
      </>
    );
  }
);

export default SignDocumentApplicationStatusError;
