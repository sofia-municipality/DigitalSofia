import React from "react";
import { useTranslation } from "react-i18next";

import TaxItem from "../TaxItem";
import styles from "./taxSection.module.scss";

const TaxesSection = ({ items, title }) => {
  const { t } = useTranslation();
  return (
    <section
      className={styles.taxSection}
      aria-label={`${t("myServices.localTaxesGroup.section.label")} ${title}`}
    >
      <h2 className={styles.taxSectionHeading}>{title}</h2>
      <div className={styles.taxSectionContent}>
        {items.map((item, index) => (
          <TaxItem key={index} {...item} />
        ))}
      </div>
    </section>
  );
};

export default TaxesSection;
