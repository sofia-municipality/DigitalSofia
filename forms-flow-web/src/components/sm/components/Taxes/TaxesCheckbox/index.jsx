import React, { useContext } from "react";
import { cloneDeep } from "lodash";
import { useTranslation } from "react-i18next";
import { PAYMENT_RETRY_ENABLED_STATUSES } from "../../../../../constants/constants";
import { TaxAccordionContext } from "../TaxesAccordion/context";
import styles from "./taxesCheckbox.module.scss";
import {
  regularSelectAndNotCurrentYearMaxPayOrder,
  selectCurrentYearMaxPayOrder,
  unselectCurrentYearHighestPayOrder,
  unselectNotCurrentYearHighestPayOrder,
  unselectItemRegularScenario,
  clearAndReCalculateTotals,
  skipMissingPayOrdersAndEnableNextCheckbox,
  currentYearHighestPayOrder,
  notCurrentYearHighestPayOrder,
} from "../utils";

const TaxesCheckbox = ({
  className = "",
  item = {},
  isChecked,
  onChange,
  type,
  selectEnabled,
  forceDisable = false,
}) => {
  const { t } = useTranslation();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  let {
    allItems = {},
    transformedSelectedItems = {},
    showCheckBoxModalOnce,
  } = taxAccordionContext;
  const currentYear = new Date().getFullYear();

  // This is default method to disable each batch item
  const isDisabled = () => {
    if (type && item !== undefined) {
      const {
        batchGroupPayOrderList,
        maxPayOrder,
        minPayOrder,
        missedPayOrders,
        batches: selectedBatches,
      } = transformedSelectedItems[type];
      const { batches: allBatches } = allItems[type];
      const highestPayOrderInItemsBatchGroup =
        batchGroupPayOrderList[item.partidaNo][
          batchGroupPayOrderList[item.partidaNo].length - 1
        ].payOrder || 0;

      let previousObligation = false;
      let currentYearBatchSelected = false;
      let disableDueToMaxPayOrder = false;
      let doNotDisableMinPayOrder = false;

      // Do not disable item payOrder if it is lowest
      if (item.payOrder === minPayOrder) {
        doNotDisableMinPayOrder = true;
      }

      if (
        Object.keys(missedPayOrders[item.partidaNo]).length > 0 &&
        missedPayOrders[item.partidaNo].some(
          (mP) => item.payOrder === mP.next.payOrder
        )
      ) {
        previousObligation = skipMissingPayOrdersAndEnableNextCheckbox(
          item,
          missedPayOrders,
          selectedBatches,
          previousObligation
        );
      }

      const entries = Object.entries(selectedBatches);
      for (let i = 0; i < entries.length; i++) {
        const currentBatch = entries[i];
        // Check if there is an earlier payOrder that is not selected
        if (currentBatch[1].payOrder === item.payOrder - 1) {
          previousObligation = true;
        }
      }

      // CURRENT YEAR HIGHEST PAYORDER JUST FOR BATCH GROUP
      // Check if the current item is part of the current year and the previous installment is selected
      if (
        Number(item.taxPeriodYear) === currentYear &&
        item.payOrder === highestPayOrderInItemsBatchGroup
      ) {
        const result = currentYearHighestPayOrder(
          item,
          selectedBatches,
          currentYearBatchSelected,
          previousObligation,
          allBatches,
          minPayOrder,
          doNotDisableMinPayOrder
        );
        currentYearBatchSelected = result.currentYearBatchSelected;
        previousObligation = result.previousObligation;
        doNotDisableMinPayOrder = result.doNotDisableMinPayOrder;
      }

      // NOT CURRENT YEAR HIGHEST PAYORDER FOR ALL BATCH GROUPS IN ONE PAYMENT TYPE
      // Disable items if it's not current year and the max payOrder for all the batch groups is selected
      if (
        item.payOrder === maxPayOrder &&
        Number(item.taxPeriodYear) !== currentYear
      ) {
        const result = notCurrentYearHighestPayOrder(
          item,
          selectedBatches,
          disableDueToMaxPayOrder,
          previousObligation,
          allBatches
        );
        disableDueToMaxPayOrder = result.disableDueToMaxPayOrder;
        previousObligation = result.previousObligation;
      }

      //  Determine disable condition based on rules
      return !(
        doNotDisableMinPayOrder ||
        previousObligation ||
        currentYearBatchSelected ||
        disableDueToMaxPayOrder
      );
    }
    return false;
  };

  const defaultIsChecked = () => {
    // eslint-disable-next-line max-len
    const currentItem = `${item.partidaNo}-${item.payOrder}-${item.instNo}-${item.taxPeriodYear}`;
    return !!transformedSelectedItems[type].batches[currentItem];
  };

  // This is default onChange method used only for each batch item
  const defaultOnChange = (e) => {
    const isChecked = e.target.checked;
    let newSelectedItems = cloneDeep(transformedSelectedItems);
    // Current year and highest payOrder in the batch group only!
    const highestPayOrderInBatchGroup =
      transformedSelectedItems[type].batchGroupPayOrderList[item.partidaNo][
        transformedSelectedItems[type].batchGroupPayOrderList[item.partidaNo]
          .length - 1
      ].payOrder || 0;
    const { maxPayOrder, missedPayOrders } = transformedSelectedItems[type];

    // Unchecked
    if (!isChecked) {
      const unselectCurrentYearHighestPayOrderRule =
        Number(item.taxPeriodYear) === currentYear &&
        item.payOrder === highestPayOrderInBatchGroup;

      const unselectNotCurrentYearHighestPayOrderRule =
        Number(item.taxPeriodYear) !== currentYear &&
        item.payOrder === maxPayOrder;

      // If unselect current year highest payOrder only in current batch group
      if (unselectCurrentYearHighestPayOrderRule) {
        newSelectedItems = unselectCurrentYearHighestPayOrder(
          type,
          newSelectedItems,
          item,
          currentYear,
          highestPayOrderInBatchGroup
        );

        // If unselect not current year highest payOrder with exact installment number
      } else if (unselectNotCurrentYearHighestPayOrderRule) {
        newSelectedItems = unselectNotCurrentYearHighestPayOrder(
          type,
          newSelectedItems,
          item,
          currentYear,
          maxPayOrder,
          allItems
        );
        // Regular uncheck
      } else {
        newSelectedItems = unselectItemRegularScenario(
          type,
          newSelectedItems,
          item
        );
      }

      newSelectedItems = clearAndReCalculateTotals(type, newSelectedItems);
    } else {
      // Checked
      // Current year and maxPayOrder in the current batch group with exact installment number
      const currentYearHighestPayOrderCheck =
        Number(item.taxPeriodYear) === currentYear &&
        item.payOrder === highestPayOrderInBatchGroup;

      if (currentYearHighestPayOrderCheck) {
        newSelectedItems = selectCurrentYearMaxPayOrder(
          type,
          newSelectedItems,
          item
        );
      } else {
        newSelectedItems = regularSelectAndNotCurrentYearMaxPayOrder(
          allItems,
          type,
          newSelectedItems,
          item,
          currentYear,
          maxPayOrder,
          missedPayOrders
        );
      }
    }

    let incrementShowCheckBoxModalOnce = showCheckBoxModalOnce;
    if (!allItems[type].onlyCurrentYearItemsForEachBatchGroup[item.partidaNo]) {
      ++incrementShowCheckBoxModalOnce;
    }

    setTaxAccordionContext({
      transformedSelectedItems: cloneDeep(newSelectedItems),
      showCheckBoxModalOnce: incrementShowCheckBoxModalOnce,
    });
  };

  const onCheck = onChange || defaultOnChange;
  const isCheckboxChecked = isChecked || defaultIsChecked;

  return item.hasPaymentRequest &&
    !PAYMENT_RETRY_ENABLED_STATUSES.includes(item.status) ? (
    <div className={styles.iconWrapper}>
      <img
        src="/assets/Images/pending_payment_request_icon.svg"
        alt="Pending Payment Icon"
        className="text-sm-red"
        width="20px"
        height="20px"
      />
    </div>
  ) : selectEnabled ? (
    <div className={`sm-checkbox ${styles.taxesCheckbox} ${className}`}>
      <label className={`form-check-label ${styles.checkboxLabel}`}>
        <input
          type="checkbox"
          className="mt-0"
          disabled={isDisabled() || forceDisable}
          checked={isCheckboxChecked() && !forceDisable}
          onChange={onCheck}
          onClick={(e) => {
            e.stopPropagation();
          }}
        />
        <div style={{ fontSize: 0, width: 0, height: 0 }}>
          {t("tax.checkbox.label")}
        </div>
      </label>
    </div>
  ) : null;
};

export default TaxesCheckbox;
