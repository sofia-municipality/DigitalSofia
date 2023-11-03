import React, { useContext } from "react";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

import { useGetFormatters } from "../hooks";
import { TAX_CATEGORIES } from "../../../../../../../../constants/constants";
import TaxesCheckbox from "../../TaxesCheckbox";
import { TaxAccordionContext } from "../context";
import styles from "./taxesAccordionContentHeader.module.scss";

const TaxesAccordionContentHeader = ({
  identifier,
  total,
  type,
  selectEnabled,
  batchNumber,
  items,
}) => {
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {} } = taxAccordionContext;
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();

  const isChecked = () => {
    const batchData = selectedItems[type]?.data?.[batchNumber]?.data;
    return batchData?.length === items.length;
  };

  const onChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(selectedItems);

    if (!isChecked) {
      delete newSelectedItems[type].data[batchNumber].data;
      newSelectedItems[type].data[batchNumber].total = 0;
      newSelectedItems[type].total = newSelectedItems[type].total - total;
    } else {
      const alreadySelectedAmount =
        selectedItems[type]?.data?.[batchNumber]?.total || 0;

      newSelectedItems[type].data[batchNumber].data = cloneDeep(items);
      newSelectedItems[type].data[batchNumber].total = total;
      newSelectedItems[type].total =
        newSelectedItems[type].total + (total - alreadySelectedAmount);
    }

    setTaxAccordionContext({ selectedItems: cloneDeep(newSelectedItems) });
  };

  return (
    <div
      className={`row no-gutters d-flex w-100 align-items-end ${styles.contentHeader}`}
    >
      <div className="col-8 col-md-5">
        {[TAX_CATEGORIES.REAL_ESTATE, TAX_CATEGORIES.HOUSEHOLD_WASTE].includes(
          type
        ) ? (
          <div className={styles.taxRecordIdentifierLabel}>
            {t("localTaxes.reference.category.content.header.address")}
          </div>
        ) : null}
        <div className={styles.taxRecordIdentifier}>{identifier}</div>
      </div>
      <div className="col-12 col-md-7 d-flex justify-content-end align-items-center">
        <span className={styles.taxRecordTotalLabel}>
          {t("localTaxes.reference.category.content.header.total.label")}
        </span>
        <span className={styles.taxRecordTotal}>
          {numberFormatter.format(total)}
        </span>
        <span className={styles.currency}>{t("currency.lev.short")}</span>
        {selectEnabled ? (
          <TaxesCheckbox
            className={styles.checkbox}
            onChange={onChange}
            isChecked={isChecked}
          />
        ) : null}
      </div>
    </div>
  );
};

export default TaxesAccordionContentHeader;
