import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";

import { PAGE_ROUTES } from "../../../../../../../constants/navigation";
import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../components/buttons/SmCta";

import styles from "./foreigner.forbidden.modal.module.scss";
import "./foreigner.forbidden.modal.scss";

const ForeignerForbiddenModal = React.memo(({ modalOpen }) => {
  const { t } = useTranslation();
  return (
    <>
      <Modal
        show={modalOpen}
        className="foreignerForbbidenModal"
        aria-labelledby="modal-title"
      >
        <div className={styles.modalCloseCta}></div>
        <div className={styles.modalContentContainer}>
          <Modal.Header className={styles.modalHeader}>
            <Modal.Title id="modal-title">
              {t("foreigners.forbidden.modal.title")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className={styles.modalBody}>
            <p>{t("foreigners.forbidden.modal.description")}</p>
          </Modal.Body>
          <Modal.Footer className={styles.modalFooter}>
            <SmCta
              size={SmCtaSizes.MEDIUM}
              type={SmCtaTypes.SECONDARY}
              className={styles.mvrCta}
              href={t("foreigners.forbidden.modal.mvr.link")}
              isLink
              hardRedirect
            >
              {t("foreigners.forbidden.modal.mvr.link.text")}
            </SmCta>
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.OUTLINE}
              href={PAGE_ROUTES.FAQ}
              isLink
            >
              <span className="sm-cta-outline-underline">
                {t("foreigners.forbidden.modal.faq.link.text")}
              </span>
            </SmCta>
          </Modal.Footer>
        </div>
      </Modal>
    </>
  );
});

export default ForeignerForbiddenModal;
