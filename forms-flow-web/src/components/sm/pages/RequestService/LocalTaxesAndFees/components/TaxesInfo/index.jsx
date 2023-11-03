import React from "react";
import { useTranslation } from "react-i18next";
import { useSelector } from "react-redux";
import CloseIcon from "@mui/icons-material/Close";

import { usePageTitleRef } from "../../../../../../../customHooks";
import { PAGE_ROUTES } from "../../../../../../../constants/navigation";
import SmCta, { SmCtaTypes } from "../../../../../components/buttons/SmCta";
import { useGetFormatters } from "../TaxesAccordion/hooks";

import styles from "./taxesInfo.module.scss";

const TaxesInfo = ({
  date,
  time,
  total,
  title = "localTaxes.reference.taxInfo.title",
  subtitle = "localTaxes.reference.taxInfo.subtitle",
}) => {
  const { t } = useTranslation();
  const headingRef = usePageTitleRef();
  const { numberFormatter } = useGetFormatters();
  const userDetails = useSelector((state) => state.user.userDetail);
  const { fullName, name, personIdentifier = "" } = userDetails || {};

  return (
    <div className="container-fluid">
      <div className="row">
        <div className={`col-12 ${styles.infoWrapper}`}>
          <div className={styles.info}>
            <div className="d-flex justify-content-between align-items-center">
              <div className="d-flex flex-column">
                <h1 className={styles.title} tabIndex="-1" ref={headingRef}>
                  {t(title)}
                </h1>
                <p className={styles.subtitle}>{t(subtitle)}</p>
              </div>
              <SmCta
                type={SmCtaTypes.OUTLINE}
                className={styles.closeCta}
                isLink
                href={PAGE_ROUTES.LOCAL_TAXES_AND_FEES}
                accessibilityProps={{
                  "aria-label": t("screen.reader.modal.close.cta"),
                }}
              >
                <span className="sm-cta-outline-underline">
                  {t("localTaxes.reference.close.cta")}
                </span>
                <CloseIcon className={styles.closeCtaIcon} />
              </SmCta>
            </div>
            <div className={`${styles.namesWrapper} mt-4`}>
              <span className={styles.names}>{fullName || name}</span>
              <span className={styles.personIdentifierLabel}>
                {t("localTaxes.reference.taxInfo.personIdentifierLabel")}
              </span>
              <span className={styles.personIdentifier}>
                {personIdentifier.replace("PNOBG-", "")}
              </span>
            </div>
            <div className="row no-gutters w-100 mt-3 align-items-baseline">
              <div className={`col-12 col-md-6 ${styles.dateTimeWrapper}`}>
                <span className={styles.dateTimeLabel}>
                  {t("localTaxes.reference.taxInfo.dateTimeLabel")}
                </span>
                <span className={styles.dateTime}>
                  <span className="mr-4">{date}</span>
                  <span>{time}</span>
                </span>
              </div>
              {total ? (
                <div
                  className={`col-12 col-md-6 d-flex justify-content-end 
              mt-4 mt-md-0`}
                >
                  <div className={styles.totalWrapper}>
                    <span className={styles.totalLabel}>
                      {t("localTaxes.reference.taxInfo.total")}
                    </span>
                    <span className={styles.total}>
                      {numberFormatter.format(total)}
                    </span>
                    <span className={styles.currency}>
                      {t("currency.lev.short")}
                    </span>
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaxesInfo;
