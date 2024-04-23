import React from "react";
import { Modal } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import SmCta, { SmCtaSizes, SmCtaTypes } from "../../../buttons/SmCta";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import { useLogin } from "../../../../../../customHooks";

import styles from "../forbidden.modal.module.scss";

export const ForeignerForbiddenContent = ({ onClose }) => {
  const { t } = useTranslation();
  return (
    <>
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
          onClick={onClose}
          isLink
        >
          <span className="sm-cta-outline-underline">
            {t("foreigners.forbidden.modal.faq.link.text")}
          </span>
        </SmCta>
      </Modal.Footer>
    </>
  );
};

export const AssuranceLevelContent = ({ requiredAssuranceLevel }) => {
  const { t } = useTranslation();
  const login = useLogin();

  return (
    <>
      <Modal.Body className={styles.modalBody}>
        <p>{t("assurance.forbidden.modal.description")}</p>
      </Modal.Body>
      <Modal.Footer className={styles.modalFooter}>
        <SmCta
          className="px-4"
          size={SmCtaSizes.MEDIUM}
          type={SmCtaTypes.SECONDARY}
          onClick={() => {
            login(
              {
                prompt: "login",
                customParams: {
                  requested_assurance_level: requiredAssuranceLevel,
                },
              },
              true
            );
          }}
        >
          {t("assurance.forbidden.modal.cta")}
        </SmCta>
      </Modal.Footer>
    </>
  );
};

export const ChildFormInProgressContent = ({ onClose }) => {
  const { t } = useTranslation();

  return (
    <>
      <Modal.Body className={styles.modalBody}>
        <p>{t("childForm.inProgress.forbidden.modal.description")}</p>
      </Modal.Body>
      <Modal.Footer className={styles.modalFooter}>
        <SmCta
          className="px-4"
          size={SmCtaSizes.MEDIUM}
          type={SmCtaTypes.SECONDARY}
          href={PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION}
          isLink
          onClick={onClose}
        >
          {t("childForm.inProgress.forbidden.modal.cta")}
        </SmCta>
      </Modal.Footer>
    </>
  );
};
