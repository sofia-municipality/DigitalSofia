import React from "react";

import styles from "./mastHeadCtaSection.module.scss";
import SmCta, { SmCtaTypes } from "../../../../components/buttons/SmCta";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import { useTranslation } from "react-i18next";

const MastHeadCtaSection = ({
  addressCtaText,
  addressCtaLink = PAGE_ROUTES.ADDRESS_REGISTRATION,
  taxCtaText,
  taxCtaLink = PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
}) => {
  const { t } = useTranslation();
  return (
    <div
      className={`container-fluid ${styles.section} ${styles.mastheadCtasSection}`}
    >
      <div
        className={`row justify-content-between ${styles.buttonsContainer} 
        ${styles.sectionContent}`}
      >
        <div className={`col-md-6 ${styles.mastheadCtawrapper}`}>
          <SmCta
            href={addressCtaLink}
            className={styles.mastheadCta}
            isLink
            type={SmCtaTypes.TERTIARY}
          >
            {addressCtaText || t("masthead.cta.addressRegistration")}
          </SmCta>
        </div>
        <div className={`col-md-6 ${styles.mastheadCtawrapper}`}>
          <SmCta
            href={taxCtaLink}
            className={styles.mastheadCta}
            isLink
            type={SmCtaTypes.TERTIARY}
          >
            {taxCtaText || t("masthead.cta.localTaxes")}
          </SmCta>
        </div>
      </div>
    </div>
  );
};

export default MastHeadCtaSection;
