// Commented code is hidden based on last designs
import React from "react";
// import React, { useContext } from "react";
import { useTranslation } from "react-i18next";

// import { TaxAccordionContext } from "../context";
// import { useGetFormatters } from "../hooks";
import styles from "./taxesAccordionContentFooter.module.scss";
// import { useDevice } from "../../../../../../customHooks";

const ContentFooter = ({ batchNumber }) => {
  const { t } = useTranslation();
  // const { isPhone } = useDevice();
  // const { numberFormatter } = useGetFormatters();
  // const { taxAccordionContext = {} } = useContext(TaxAccordionContext);

  // const { transformedSelectedItems = {} } = taxAccordionContext;
  // const selectedAmount = transformedSelectedItems[type]?.batchesTotals?.[batchNumber]?.total;

  return (
    <div className="mt-3 d-flex justify-content-between">
      <div>
        <span className={styles.batchNumberLabel}>
          {t("localTaxes.reference.category.content.footer.batchNumber")}
        </span>
        <span className={styles.batchNumber}>{batchNumber}</span>
      </div>
      {/* {selectEnabled && !isPhone ? (
        <div className="col-12 col-md-7 d-flex justify-content-end align-items-center">
          <span className={styles.selectedAmountLabel}>
            {t("localTaxes.payment.total.selected.amount.label")}
          </span>
          <span className={styles.selectedAmount}>
            {numberFormatter.format(selectedAmount)}
          </span>
          <span className={styles.currency}>{t("currency.lev.short")}</span>
        </div>
      ) : null} */}
    </div>
  );
};

export default ContentFooter;
