import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import VerifiedOutlinedIcon from "@mui/icons-material/VerifiedOutlined";
import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";

const SignDocumentSuccess = React.memo(
  ({ signitureType, referenceNumber, onSuccessClose }) => {
    const { t } = useTranslation();
    return signitureType === "consent" ? (
      <>
        <div className={styles.modalCloseCta} />
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title id="modal-title">
              {t("form.document.sign.modal.consent.success.title")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-blue text-white mb-5 mr-0">
                <VerifiedOutlinedIcon />
              </div>
            </div>
            <p className="sm-body-2-regular mb-5">
              {t("form.document.sign.modal.consent.success.description")}
            </p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={() => onSuccessClose()}
              className={styles.confirmCta}
            >
              {t("modal.cta.confirm")}
            </SmCta>
          </Modal.Footer>
        </div>
      </>
    ) : (
      <>
        <div className={styles.modalCloseCta} />
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title>
              {t("form.document.sign.evrotrust.modal.success.title")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            {referenceNumber ? (
              <div className="d-flex justify-content-center align-items-center w-100 mb-4">
                <span className={styles.referenceNumberLabel}>
                  {t(
                    "form.document.sign.evrotrust.modal.success.referenceNumber"
                  )}
                </span>
                <span className={styles.referenceNumber}>
                  {referenceNumber}
                </span>
              </div>
            ) : null}
            <div className="d-flex justify-content-center align-items-center w-100">
              <div className="sm-rounded-wrapper bg-sm-blue text-white mb-5 mr-0">
                <VerifiedOutlinedIcon />
              </div>
            </div>
            <p className="sm-body-2-regular mb-5">
              {t("form.document.sign.evrotrust.modal.success.description")}
            </p>
            <p className="sm-body-2-regular text-sm-red">
              {t("form.document.sign.evrotrust.modal.success.description.hint")}
            </p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={() => onSuccessClose()}
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

export default SignDocumentSuccess;
