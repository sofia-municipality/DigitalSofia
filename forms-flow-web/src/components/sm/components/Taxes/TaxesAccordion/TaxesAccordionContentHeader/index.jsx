import React, { useContext } from "react";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

import { useGetFormatters } from "../hooks";
import { TAX_CATEGORIES } from "../../../../../../constants/constants";
import { convertToDecimal } from "../../../../../../utils";
import { filter } from "../../utils";
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
  let {
    transformedSelectedItems = {},
    allItems = {},
    showCheckBoxModalOnce,
  } = taxAccordionContext;

  const batchDataTotal = Number(
    transformedSelectedItems[type]?.batchesTotals[batchNumber]
  );

  const shouldDisableCheckbox = () => {
    const batchDataLength = Object.keys(allItems[type]?.batches).length;
    return !batchDataLength;
  };

  const isChecked = () => {
    const selectedBatchData = filter(
      transformedSelectedItems[type]?.batches,
      (key) => key.includes(batchNumber)
    );
    const batchData = filter(allItems[type]?.batches, (key) =>
      key.includes(batchNumber)
    );
    return (
      Object.keys(batchData).length === Object.keys(selectedBatchData).length
    );
  };

  const clearAndReCalculateTotals = (newSelectedItems) => {
    // Clean current totals for each batch number
    Object.keys(newSelectedItems[type].batchesTotals).forEach((key) => {
      newSelectedItems[type].batchesTotals[key] = 0;
    });

    // Clean group total
    newSelectedItems[type].total = 0;

    // Calculate all batches totals
    Object.entries(newSelectedItems[type].batches).forEach(([, value]) => {
      newSelectedItems[type].batchesTotals[value.partidaNo] += convertToDecimal(
        value.residual + value.interest
      );
    });

    // Calculate group total
    Object.entries(newSelectedItems[type].batchesTotals).forEach(
      ([, value]) => {
        newSelectedItems[type].total += convertToDecimal(value);
      }
    );

    return newSelectedItems;
  };

  const onChange = (e) => {
    const isChecked = e.target.checked;
    let newSelectedItems = cloneDeep(transformedSelectedItems);

    if (!isChecked) {
      // Lowest payOrder in the unselected batch group
      const lowestPayOrderInDeselectedBatchGroup =
        newSelectedItems[type].batchGroupPayOrderList[batchNumber][0]
          .payOrder || 0;

      // If all batches are in the current year select only in the current batch group
      if (allItems[type].onlyCurrentYearItemsForEachBatchGroup[batchNumber]) {
        newSelectedItems[type].batches = filter(
          newSelectedItems[type]?.batches,
          (key, value) => {
            return value.partidaNo !== batchNumber;
          }
        );
      } else {
        // Filter all batch groups based on deselected item's payOrder
        newSelectedItems[type].batches = filter(
          newSelectedItems[type]?.batches,
          (key, value) => {
            return (
              value.payOrder < lowestPayOrderInDeselectedBatchGroup &&
              value.partidaNo !== batchNumber
            );
          }
        );
      }

      newSelectedItems = clearAndReCalculateTotals(newSelectedItems);
    } else {
      // Highest payOrder in the selected batch group
      const highestPayOrderInSelectedBatchGroup =
        newSelectedItems[type].batchGroupPayOrderList[batchNumber][
          newSelectedItems[type].batchGroupPayOrderList[batchNumber].length - 1
        ].payOrder || 0;

      // If all batches are in the current year select only in the current batch group
      if (allItems[type].onlyCurrentYearItemsForEachBatchGroup[batchNumber]) {
        filter(allItems[type]?.batches, (key, value) => {
          if (value.partidaNo === batchNumber) {
            newSelectedItems[type].batches[key] = value;
          }
        });
      } else {
        // Filter all batch groups based on selected item's payOrder
        newSelectedItems[type].batches = filter(
          allItems[type]?.batches,
          (key, value) => {
            return value.payOrder <= highestPayOrderInSelectedBatchGroup;
          }
        );
      }

      newSelectedItems = clearAndReCalculateTotals(newSelectedItems);
    }

    let incrementShowCheckBoxModalOnce = showCheckBoxModalOnce;

    // Modal should be shown:
    // if there is more than one batch group
    // if current batch group contains not only current year batches
    if (
      !(
        allItems[type].onlyOneBatchForType ||
        allItems[type].onlyCurrentYearItemsForEachBatchGroup[batchNumber]
      )
    ) {
      ++incrementShowCheckBoxModalOnce;
    }

    setTaxAccordionContext({
      transformedSelectedItems: cloneDeep(newSelectedItems),
      showCheckBoxModalOnce: incrementShowCheckBoxModalOnce,
    });
  };

  return (
    <div
      className={`row no-gutters d-flex w-100 align-items-end ${styles.contentHeader}`}
    >
      <div className="col-8 col-md-5">
        {[TAX_CATEGORIES.REAL_ESTATE, TAX_CATEGORIES.HOUSEHOLD_WASTE].includes(
          type
        ) ? (
          <>
            <div className={styles.taxRecordIdentifierLabel}>
              {t("localTaxes.reference.category.content.header.address")}
            </div>
            <div className={styles.taxRecordIdentifier}>{identifier}</div>
          </>
        ) : [TAX_CATEGORIES.VEHICLE].includes(type) ? (
          <div className={styles.regNumberContainer}>
            <span className={styles.regNumber}>
              <span
                className={`sm-body-2-regular ${styles.regNumberBlueLine}`}
              ></span>
              <span className={styles.regNumberText}>{identifier}</span>
            </span>
          </div>
        ) : null}
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
