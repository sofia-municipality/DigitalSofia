import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import UpdateIcon from "@mui/icons-material/Update";

import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";

import { useCheckDocumentStatus } from "../hooks";
import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";

const SignDocumentSent = React.memo(
  ({
    signitureType,
    onSuccess,
    onReject,
    onFormSubmissionError,
    redirectUrl,
  }) => {
    const { t } = useTranslation();
    const { checkDocumentStatus, isLoading } = useCheckDocumentStatus({
      signitureType,
      onSuccess,
      onReject,
      onFormSubmissionError,
      redirectUrl,
    });

    return (
      <>
        <div className={styles.modalCloseCta} />
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title id="modal-title">
              {t("form.document.sign.evrotrust.modal.title")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-red text-white mb-5 mr-0">
                <UpdateIcon />
              </div>
            </div>
            <p>{t("form.document.sign.evrotrust.modal.description")}</p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={checkDocumentStatus}
              className={styles.confirmCta}
              disabled={isLoading}
              loading={isLoading}
            >
              {t("modal.cta.confirm")}
            </SmCta>
          </Modal.Footer>
        </div>
      </>
    );
  }
);

export default SignDocumentSent;
