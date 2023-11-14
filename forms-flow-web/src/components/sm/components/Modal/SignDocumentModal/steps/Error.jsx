import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";

import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";

import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";

const SignDocumentError = React.memo(({ onClose }) => {
  const { t } = useTranslation();

  return (
    <>
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
            {t("form.document.sign.evrotrust.modal.error.title")}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body className={styles.modalBody}>
          <div className="d-flex justify-content-center align-items-center w-100">
            <div className="sm-rounded-wrapper bg-sm-red text-white mb-5 mr-0">
              <CloseIcon />
            </div>
          </div>
          <p>{t("form.document.sign.evrotrust.modal.error.description.1")}</p>
          <p>{t("form.document.sign.evrotrust.modal.error.description.2")}</p>
        </Modal.Body>
        <Modal.Footer className={styles.modalFooter}>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.SECONDARY}
            onClick={onClose}
            className={styles.confirmCta}
          >
            {t("form.document.sign.evrotrust.modal.error.cta")}
          </SmCta>
        </Modal.Footer>
      </div>
    </>
  );
});

export default SignDocumentError;
