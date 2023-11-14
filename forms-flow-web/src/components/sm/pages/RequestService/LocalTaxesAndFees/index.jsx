import React from "react";
import { useTranslation } from "react-i18next";
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";

import { PAGE_ROUTES } from "../../../../../constants/navigation";
import PageContainer from "../../../components/PageContainer";
import NavLink from "../../../components/Navigation/NavLink";
import CustomBreadcrumbs from "../../../components/Breadcrumbs/CustomBreadcrumbs";

import { useDevice } from "../../../../../customHooks";

import styles from "./localTaxesAndFees.module.scss";

const LocalTaxesAndFees = () => {
  const { isPhone } = useDevice();
  const { t } = useTranslation();

  return (
    <PageContainer>
      <div className={styles.localTaxesWrapper}>
        <div className={styles.localTaxesContainer}>
          <CustomBreadcrumbs
            className={styles.breadcrumbs}
            link={PAGE_ROUTES.REQUEST_SERVICE}
            linkText={t("addressRegistratrion.backLinkText")}
            title={t("localTaxesAndFees.title")}
          />
          <div className={styles.localTaxesSectionWrapper}>
            <NavLink
              to={PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE}
              className={`row no-gutters ${styles.localTaxesSection}`}
            >
              <div className={`col ${styles.serviceLinkWrapper}`}>
                <div className={styles.serviceLink}>
                  <img
                    className={styles.icon}
                    src="/quick_reference.svg"
                    alt=""
                  />
                  <img
                    className={`${styles.icon} ${styles.iconActive}`}
                    src="/quick_reference_filled.svg"
                    alt=""
                  />
                  <div className={styles.content}>
                    <h2
                      className={`${
                        isPhone ? "sm-heading-5" : "sm-heading-4"
                      } text-sm-indigo-dark`}
                    >
                      {t("localTaxesAndFees.card.1.title")}
                    </h2>
                    <p className="sm-body-2-regular text-sm-indigo-4">
                      {t("localTaxesAndFees.card.1.subtitle")}
                    </p>
                  </div>
                </div>
                <KeyboardArrowRightIcon className={styles.arrowIcon} />
              </div>
            </NavLink>
            <NavLink
              to={PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT}
              className={`row no-gutters ${styles.localTaxesSection}`}
            >
              <div className={`col ${styles.serviceLinkWrapper}`}>
                <div className={styles.serviceLink}>
                  <img className={styles.icon} src="/credit_card.svg" alt="" />
                  <img
                    className={`${styles.icon} ${styles.iconActive}`}
                    src="/credit_card_filled.svg"
                    alt=""
                  />
                  <div className={styles.content}>
                    <h2
                      className={`${
                        isPhone ? "sm-heading-5" : "sm-heading-4"
                      } text-sm-indigo-dark`}
                    >
                      {t("localTaxesAndFees.card.2.title")}
                    </h2>
                    <p className="sm-body-2-regular text-sm-indigo-4">
                      {t("localTaxesAndFees.card.2.subtitle")}
                    </p>
                  </div>
                </div>
                <KeyboardArrowRightIcon className={styles.arrowIcon} />
              </div>
            </NavLink>
          </div>
        </div>
      </div>
    </PageContainer>
  );
};
export default LocalTaxesAndFees;
