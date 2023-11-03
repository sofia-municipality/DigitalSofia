import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../Buttons/SmCta";

const SignDocumentSuccess = React.memo(
  ({ onSuccessClose }) => {
    const { t } = useTranslation();
    return (
      <>
        <div className={styles.modalCloseCta} />
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title>
              {t("form.document.sign.modal.success.title")}
            </Modal.Title>
          </Modal.Header>
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
