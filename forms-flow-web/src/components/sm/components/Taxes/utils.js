import { convertToDecimal } from "../../../../utils";
import { cloneDeep } from "lodash";

export const TaxesContainerType = {
  REFERENCE: "reference",
  PAYMENT: "payment",
  STATUS: "status",
};

export const filter = (obj, fun) =>
  Object.entries(obj).reduce(
    (prev, [key, value]) => ({
      ...prev,
      ...(fun(key, value) ? { [key]: value } : {}),
    }),
    {}
  );

/***** TaxesCheckbox methods *****/

/*** isDisabled ***/

/*
   Methods for disabling checkboxes based on different rules
*/

// Skip missing payOrders and enable next checkbox in a row
export const skipMissingPayOrdersAndEnableNextCheckbox = (
  item,
  missedPayOrders,
  selectedBatches,
  previousObligation
) => {
  // Find last on if missing payOrders are in a row, also if not
  // find just the right one based on item.payOrder === element.next.payOrder
  const foundLastItemInCaseOfMissingInARow = missedPayOrders[
    item.partidaNo
  ].findLast((element) => item.payOrder === element.next.payOrder);

  const isPreviousSelected =
    selectedBatches[foundLastItemInCaseOfMissingInARow.previous.key];

  if (item.payOrder === foundLastItemInCaseOfMissingInARow.next.payOrder) {
    if (isPreviousSelected) {
      previousObligation = true;
    } else {
      previousObligation = false;
    }
  }
  return previousObligation;
};

// CURRENT YEAR HIGHEST PAYORDER JUST FOR BATCH GROUP
export const currentYearHighestPayOrder = (
  item,
  selectedBatches,
  currentYearBatchSelected,
  previousObligation,
  allBatches,
  minPayOrder,
  doNotDisableMinPayOrder
) => {
  const previousKey = `${item.partidaNo}-${item.payOrder}-${item.instNo - 1}-${
    item.taxPeriodYear
  }`;
  const hasPreviousInstNumber = allBatches[previousKey];

  const isPreviousInstNumberSelected = selectedBatches[previousKey];
  // Check if previous installment number is selected

  if (hasPreviousInstNumber) {
    if (isPreviousInstNumberSelected) {
      currentYearBatchSelected = true;
    } else if (item.instNo > 1) {
      currentYearBatchSelected = false;
      previousObligation = false;
      if (item.payOrder === minPayOrder) {
        doNotDisableMinPayOrder = false;
      }
    }
  }
  return {
    previousObligation: previousObligation,
    currentYearBatchSelected: currentYearBatchSelected,
    doNotDisableMinPayOrder: doNotDisableMinPayOrder,
  };
};

export const notCurrentYearHighestPayOrder = (
  item,
  selectedBatches,
  disableDueToMaxPayOrder,
  previousObligation,
  allBatches
) => {
  const hasPreviousInstallmentNumber =
    allBatches[
      `${item.partidaNo}-${item.payOrder}-${item.instNo - 1}-${
        item.taxPeriodYear
      }`
    ];

  const isPreviousInstallmentNumberSelected =
    selectedBatches[
      `${item.partidaNo}-${item.payOrder}-${item.instNo - 1}-${
        item.taxPeriodYear
      }`
    ];

  if (hasPreviousInstallmentNumber) {
    // Check if previous installment number is selected
    if (isPreviousInstallmentNumberSelected) {
      disableDueToMaxPayOrder = true;
    } else if (item.instNo > 1) {
      disableDueToMaxPayOrder = false;
      previousObligation = false;
    }
  }
  return {
    previousObligation: previousObligation,
    currentYearBatchSelected: disableDueToMaxPayOrder,
  };
};
/*** End of methods used in isDisabled ***/

/*** defaultOnChange ***/
/*
   Methods for different scenarios invoked on checkbox checked
*/

// Current year and maxPayOrder in the current batch group
export const selectCurrentYearMaxPayOrder = (type, newSelectedItems, item) => {
  const itemKey = `${item.partidaNo}-${item.payOrder}-${item.instNo}-${item.taxPeriodYear}`;
  newSelectedItems[type].batches[itemKey] = cloneDeep(item);
  newSelectedItems = reCalculateTotals(type, newSelectedItems, item);
  return newSelectedItems;
};

// Not current year and regular scenario
export const regularSelectAndNotCurrentYearMaxPayOrder = (
  allItems,
  type,
  newSelectedItems,
  item,
  currentYear,
  maxPayOrder,
  missedPayOrders
) => {
  const foundLastItemInCaseOfMissingInARow = missedPayOrders[
    item.partidaNo
  ].findLast((element) => item.payOrder === element.next.payOrder);

  Object.entries(allItems[type].batches).forEach(([batchNumber, batch]) => {
    // Not Current year and maxPayOrder in the whole tax group with exact installment number
    const notCurrentYearMaxPayOrder =
      Number(item.taxPeriodYear) !== currentYear &&
      Number(item.taxPeriodYear) === Number(batch.taxPeriodYear) &&
      item.payOrder === maxPayOrder &&
      batch.payOrder === maxPayOrder;

    if (notCurrentYearMaxPayOrder) {
      // If is the same installment number
      if (item.instNo === batch.instNo) {
        newSelectedItems[type].batches[batchNumber] = cloneDeep(batch);
        newSelectedItems = reCalculateTotals(type, newSelectedItems, batch);
      }
      // Check all items equals checked item's payOrder
      // Or handle pre-selection of missed payOrder in other batches where the same persist
    } else if (
      batch.payOrder === item.payOrder ||
      (foundLastItemInCaseOfMissingInARow &&
        batch.payOrder <= foundLastItemInCaseOfMissingInARow.next.payOrder)
    ) {
      // Preselect other batches with same payOrder
      newSelectedItems[type].batches[batchNumber] = cloneDeep(batch);
      // Recalculate totals
      newSelectedItems = reCalculateTotals(type, newSelectedItems, batch);
      // For preselecting missing payOrders in other batches
    }
  });
  return newSelectedItems;
};

/* 
  Methods for different scenarios invoked on checkbox unchecked
*/

// Unselect current year highest payOrder in each batch group by installment number
export const unselectCurrentYearHighestPayOrder = (
  type,
  newSelectedItems,
  item,
  currentYear,
  highestPayOrderInBatchGroup
) => {
  const selectedItemsInstNo = [];
  filter(newSelectedItems[type]?.batches, (batchNumber, batch) => {
    if (
      Number(batch.taxPeriodYear) === currentYear &&
      batch.payOrder === highestPayOrderInBatchGroup &&
      batch.partidaNo === item.partidaNo
    ) {
      selectedItemsInstNo.push(batch.instNo);
    }
  });
  selectedItemsInstNo.sort();
  // Edge case when only one item is in current year and it's installment number is higher than 1
  if (selectedItemsInstNo.length === 1 && selectedItemsInstNo[0] > 1) {
    // eslint-disable-next-line max-len
    const currentItemKey = `${item.partidaNo}-${item.payOrder}-${item.instNo}-${item.taxPeriodYear}`;

    newSelectedItems[type].batchesTotals[item.partidaNo] = convertToDecimal(
      newSelectedItems[type].batchesTotals[item.partidaNo] -
        convertToDecimal(item.residual + item.interest)
    );
    delete newSelectedItems[type].batches[currentItemKey];
  } else {
    const fromTheLastOne = selectedItemsInstNo[selectedItemsInstNo.length - 1];
    for (let i = fromTheLastOne; i >= item.instNo; i--) {
      const itemKey = `${item.partidaNo}-${item.payOrder}-${i}-${item.taxPeriodYear}`;
      const pickedItem = newSelectedItems[type].batches[itemKey];
      // remove (uncheck) the batch data
      newSelectedItems[type].batchesTotals[pickedItem.partidaNo] =
        convertToDecimal(
          newSelectedItems[type].batchesTotals[pickedItem.partidaNo] -
            convertToDecimal(pickedItem.residual + pickedItem.interest)
        );
      delete newSelectedItems[type].batches[itemKey];
    }
  }
  return newSelectedItems;
};

// Unselect not current year highest payOrder in all batches in one tax group by installment number
export const unselectNotCurrentYearHighestPayOrder = (
  type,
  newSelectedItems,
  item,
  currentYear,
  maxPayOrder,
  allItems
) => {
  let selectedItemsInDifferentBatchesByInstNo = {};
  const batches = newSelectedItems[type]?.batches;
  const allItemsBatches = allItems[type]?.batches;

  const currentItem = `${item.partidaNo}-${item.payOrder}-${item.instNo}-${item.taxPeriodYear}`;
  const matchBatchFromSameBatchAndSamePayOrder = `${item.partidaNo}-${item.payOrder}`;
  const matches = Object.keys(allItemsBatches).filter((elMatch) =>
    elMatch.includes(matchBatchFromSameBatchAndSamePayOrder)
  );
  if (matches.length <= 1 && matches.includes(currentItem)) {
    // remove (uncheck) the batch data
    newSelectedItems[type].batchesTotals[item.partidaNo] = convertToDecimal(
      newSelectedItems[type].batchesTotals[item.partidaNo] -
        convertToDecimal(item.residual + item.interest)
    );

    delete newSelectedItems[type].batches[currentItem];
  } else {
    for (const batch in batches) {
      if (
        Number(batches[batch].taxPeriodYear) !== currentYear &&
        batches[batch].payOrder === maxPayOrder &&
        item.taxPeriodYear === batches[batch].taxPeriodYear
      ) {
        if (
          !selectedItemsInDifferentBatchesByInstNo[batches[batch].partidaNo]
        ) {
          selectedItemsInDifferentBatchesByInstNo[batches[batch].partidaNo] =
            [];
        }
        selectedItemsInDifferentBatchesByInstNo[batches[batch].partidaNo].push(
          batches[batch].instNo
        );
      }
    }

    for (const batchNumber in selectedItemsInDifferentBatchesByInstNo) {
      selectedItemsInDifferentBatchesByInstNo[batchNumber].sort(
        (a, b) => a - b
      );

      // Check and delete batches for each batch based on installment number in descending order
      for (
        let j = selectedItemsInDifferentBatchesByInstNo[batchNumber].length;
        j >= item.instNo;
        j--
      ) {
        const batchKey = `${batchNumber}-${item.payOrder}-${j}-${item.taxPeriodYear}`;
        const pickedBatch = newSelectedItems[type].batches[batchKey];
        if (pickedBatch) {
          // remove (uncheck) the batch data
          newSelectedItems[type].batchesTotals[pickedBatch.partidaNo] =
            convertToDecimal(
              newSelectedItems[type].batchesTotals[pickedBatch.partidaNo] -
                convertToDecimal(pickedBatch.residual + pickedBatch.interest)
            );

          delete newSelectedItems[type].batches[batchKey];
        }
      }
    }
  }
  return newSelectedItems;
};

export const unselectItemRegularScenario = (type, newSelectedItems, item) => {
  Object.entries(newSelectedItems[type].batches).forEach(
    ([batchNumber, batch]) => {
      if (batch.payOrder >= item.payOrder) {
        // Calculate new amount for the total of the group
        newSelectedItems[type].batchesTotals[batch.partidaNo] =
          convertToDecimal(
            newSelectedItems[type].batchesTotals[batch.partidaNo] -
              convertToDecimal(batch.residual + batch.interest)
          );

        // Remove (uncheck) the batch data
        delete newSelectedItems[type].batches[batchNumber];
      }
    }
  );
  return newSelectedItems;
};

// Recalculate totals
export const reCalculateTotals = (type, newSelectedItems, batch) => {
  // Recalculate the new amount
  const newAmount = convertToDecimal(batch.residual + batch.interest);

  // Add the new amount to batch group total
  convertToDecimal(
    (newSelectedItems[type].batchesTotals[batch.partidaNo] += newAmount)
  );

  // Clean group total
  newSelectedItems[type].total = 0;
  // Calculate group total
  Object.entries(newSelectedItems[type].batchesTotals).forEach(([, value]) => {
    newSelectedItems[type].total += convertToDecimal(value);
  });

  return newSelectedItems;
};

export const clearAndReCalculateTotals = (type, newSelectedItems) => {
  // If no selected batches clear totals
  if (Object.keys(newSelectedItems[type].batches).length === 0) {
    newSelectedItems[type].total = 0;

    for (let key in newSelectedItems[type].batchesTotals) {
      newSelectedItems[type].batchesTotals[key] = 0;
    }
  } else {
    // Clean group total
    newSelectedItems[type].total = 0;

    // Calculate group total
    Object.entries(newSelectedItems[type].batchesTotals).forEach(
      ([, value]) => {
        newSelectedItems[type].total += convertToDecimal(value);
      }
    );
  }
  return newSelectedItems;
};

/*** End of methods used in defaultOnChange ***/
