import React, { useContext } from "react";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

import { useGetFormatters } from "../hooks";
import { TAX_CATEGORIES } from "../../../../../../constants/constants";
import { convertToDecimal } from "../../../../../../utils";
import TaxesCheckbox from "../../TaxesCheckbox";
import { TaxAccordionContext } from "../context";
import styles from "./taxesAccordionContentHeader.module.scss";

const TaxesAccordionContentHeader = ({
  identifier,
  type,
  selectEnabled,
  batchNumber,
  showTotal,
}) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {}, allItems = {} } = taxAccordionContext;
  const batchData = allItems[type]?.data?.[batchNumber]?.data;
  const batchDataTotal = allItems[type]?.data?.[batchNumber]?.total;

  const shouldDisableCheckbox = () => {
    const batchData = allItems[type]?.data?.[batchNumber]?.data;
    return !batchData?.length;
  };

  const isChecked = () => {
    const selectedBatchData = selectedItems[type]?.data?.[batchNumber]?.data;
    const batchData = allItems[type]?.data?.[batchNumber]?.data;
    return batchData?.length === selectedBatchData?.length;
  };

  const onChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(selectedItems);

    if (!isChecked) {
      delete newSelectedItems[type].data[batchNumber].data;
      newSelectedItems[type].data[batchNumber].total = 0;
      newSelectedItems[type].total = convertToDecimal(
        newSelectedItems[type].total - batchDataTotal
      );
    } else {
      const alreadySelectedAmount =
        selectedItems[type]?.data?.[batchNumber]?.total || 0;

      newSelectedItems[type].data[batchNumber].data = cloneDeep(batchData);
      newSelectedItems[type].data[batchNumber].total = batchDataTotal;
      newSelectedItems[type].total = convertToDecimal(
        newSelectedItems[type].total +
          convertToDecimal(batchDataTotal - alreadySelectedAmount)
      );
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
      {showTotal && (
        <div className="col-12 col-md-7 d-flex justify-content-end align-items-center">
          <span className={styles.taxRecordTotalLabel}>
            {t("localTaxes.reference.category.content.header.total.label")}
          </span>
          <span className={styles.taxRecordTotal}>
            {numberFormatter.format(batchDataTotal)}
          </span>
          <span className={styles.currency}>{t("currency.lev.short")}</span>
          {selectEnabled ? (
            <TaxesCheckbox
              className={styles.checkbox}
              onChange={onChange}
              isChecked={isChecked}
              shouldDisable={false}
              selectEnabled={selectEnabled}
              forceDisable={shouldDisableCheckbox()}
            />
          ) : null}
        </div>
      )}
    </div>
  );
};

export default TaxesAccordionContentHeader;
