import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";

import SmCta, { SmCtaTypes, SmCtaSizes } from "../../buttons/SmCta";
import styles from "./submissionErrorModal.module.scss";
import "./submissionErrorModal.scss";

const SubmissionErrorModal = React.memo(
  ({ modalOpen = false, onConfirm, title, message }) => {
    const { t } = useTranslation();
    return (
      <Modal
        size="lg"
        aria-labelledby="modal-title"
        show={modalOpen}
        className="submissionErrorModal"
      >
        <div className={styles.modalCloseCta}></div>
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title id="modal-title">
              {title || t("submission.modal.error.title")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-red text-white mb-5 mr-0">
                <CloseIcon />
              </div>
            </div>
            <p>{message || t("submission.modal.error.description")}</p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={onConfirm}
              className={styles.confirmCta}
            >
              {t("form.document.sign.evrotrust.modal.error.cta")}
            </SmCta>
          </Modal.Footer>
        </div>
      </Modal>
    );
  }
);

export default SubmissionErrorModal;
