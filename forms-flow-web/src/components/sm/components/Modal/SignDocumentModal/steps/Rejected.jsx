import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";

import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";

import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";

const SignDocumentRejected = React.memo(
  ({ onRejectClose, rejectModalTitle, rejectModalDescription }) => {
    const { t } = useTranslation();
    return (
      <>
        <div className={styles.modalCloseCta}>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.OUTLINE}
            onClick={onRejectClose}
            accessibilityProps={{
              "aria-label": t("screen.reader.modal.close.cta"),
            }}
          >
            <CloseIcon />
          </SmCta>
        </div>
        <div className={styles.modalContentContainer}>
          {rejectModalTitle ? (
            <Modal.Header className={styles.modalHeader}>
              <Modal.Title id="modal-title">{rejectModalTitle}</Modal.Title>
            </Modal.Header>
          ) : null}
          <Modal.Body className={styles.modalBody}>
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-red text-white mb-5 mr-0">
                <CloseIcon />
              </div>
            </div>
            {rejectModalDescription ? (
              <p className="sm-body-2-regular mb-5">{rejectModalDescription}</p>
            ) : null}
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={() => onRejectClose()}
              className={styles.confirmCta}
            >
              {t("modal.cta.confirm")}
            </SmCta>
          </Modal.Footer>
        </div>
      </>
    );
  }
);

export default SignDocumentRejected;
