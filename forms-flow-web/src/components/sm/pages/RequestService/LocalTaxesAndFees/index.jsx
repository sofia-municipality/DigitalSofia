import React from "react";
import { useTranslation } from "react-i18next";

import { PAGE_ROUTES } from "../../../../../constants/navigation";
import { SM_NEW_DESIGN_ENABLED } from "../../../../../constants/constants";
import PageContainer from "../../../components/PageContainer";
import SectionCards from "../../../components/SectionCards";
import CustomBreadcrumbs from "../../../components/Breadcrumbs/CustomBreadcrumbs";

import styles from "./localTaxesAndFees.module.scss";

const sectionCards = [
  {
    link: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE,
    iconSrc: SM_NEW_DESIGN_ENABLED
      ? "/assets/Images/tax-reference-card-icon.svg"
      : "/quick_reference.svg",
    iconActiveSrc: "/quick_reference_filled.svg",
    title: "localTaxesAndFees.card.1.title",
    subtitle: "localTaxesAndFees.card.1.subtitle",
  },
  {
    link: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT,
    iconSrc: SM_NEW_DESIGN_ENABLED
      ? "/assets/Images/tax-payment-card-icon.svg"
      : "/credit_card.svg",
    iconActiveSrc: "/credit_card_filled.svg",
    title: "localTaxesAndFees.card.2.title",
    subtitle: "localTaxesAndFees.card.2.subtitle",
  },
];

const LocalTaxesAndFees = () => {
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
          <SectionCards items={sectionCards} />
        </div>
      </div>
    </PageContainer>
  );
};
export default LocalTaxesAndFees;
