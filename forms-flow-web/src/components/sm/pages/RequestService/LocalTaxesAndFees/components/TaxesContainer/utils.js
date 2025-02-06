import { convertToDecimal } from "../../../../../../../utils";

const currentYear = new Date().getFullYear();

export const buildBatchesTotals = (
  mappedData,
  type,
  batchNumber,
  batchValue,
  totalAlreadyGeneratedRequests
) => {
  mappedData[type]["batchesTotals"][batchNumber] = convertToDecimal(
    batchValue.total - totalAlreadyGeneratedRequests
  );
};

// Build batches object with key [partidaNo-payOrder-instNo-txtPeriodYear]
export const buildBatches = (mappedData, type, batchNumber, batchData) => {
  mappedData[type]["batches"][
    // eslint-disable-next-line max-len
    `${batchNumber}-${batchData.payOrder}-${batchData.instNo}-${batchData.taxPeriodYear}`
  ] = batchData;
};

// Create group list from available pay orders
export const buildPayOrderGroupList = (mappedData, type, batchData) => {
  if (!mappedData[type].payOrderGroupList.includes(batchData.payOrder)) {
    mappedData[type].payOrderGroupList.push(batchData.payOrder);
  }
};

// Creates batchGroupPayOrderList array for batchNumber
export const buildBatchGroupPayOrderList = (
  mappedData,
  type,
  batchNumber,
  batchData
) => {
  if (!mappedData[type].batchGroupPayOrderList[batchNumber]) {
    mappedData[type].batchGroupPayOrderList[batchNumber] = [];
  }

  if (
    !mappedData[type].batchGroupPayOrderList[batchNumber].includes(
      batchData.payOrder
    )
  ) {
    // Populate it with payOrders
    mappedData[type].batchGroupPayOrderList[batchNumber].push({
      // eslint-disable-next-line max-len
      itemKey: `${batchNumber}-${batchData.payOrder}-${batchData.instNo}-${batchData.taxPeriodYear}`,
      payOrder: batchData.payOrder,
    });
  }
};

// Identify the minimum payOrder
export const createMinimumPayOrder = (mappedData, type) => {
  mappedData[type].minPayOrder = mappedData[type].payOrderGroupList?.length
    ? Math.min(...mappedData[type].payOrderGroupList)
    : 0;
};

// Identify the maximum payOrder
export const createMaximumPayOrder = (mappedData, type) => {
  mappedData[type].maxPayOrder =
    mappedData[type].payOrderGroupList[
      mappedData[type].payOrderGroupList.length - 1
    ];
};

// Build list for each type with highest payOrder and it's installment numbers
export const buildBatchGroupAllHighestPayOrderPaymentsList = (
  mappedData,
  type
) => {
  Object.entries(mappedData[type].batches).forEach(
    ([batchNumber, batchValue]) => {
      if (batchValue.payOrder === mappedData[type].maxPayOrder) {
        if (
          !mappedData[type].batchGroupHighestPayOrderAllPaymentsList[
            batchNumber.split("-")[0]
          ]
        ) {
          mappedData[type].batchGroupHighestPayOrderAllPaymentsList[
            batchNumber.split("-")[0]
          ] = [];
        }
        if (
          !mappedData[type].batchGroupHighestPayOrderAllPaymentsList[
            batchNumber.split("-")[0]
          ].includes(batchValue.instNo)
        ) {
          mappedData[type].batchGroupHighestPayOrderAllPaymentsList[
            batchNumber.split("-")[0]
          ].push(batchValue.instNo);
        }
      }
    }
  );
};

// Combine all missed payOrders per each tax group
export const combineAllMissedPayOrdersForEachTaxGroup = (mappedData, type) => {
  let extractedData = [];
  Object.values(mappedData[type].missedPayOrders).map((mP) => {
    mP.map((mP) => {
      if (mP && mP.missing && !extractedData.includes(mP.missing)) {
        extractedData.push(mP.missing);
      }
    });
  });
  mappedData[type].allMissedPayOrdersCombined = extractedData;
};

// Find missing payOrders for each batch group
export const findMissingPayOrdersForBatchGroup = (
  mappedData,
  type,
  batchNumber
) => {
  // Creates missedPayOrdersList array for batchNumber
  if (!mappedData[type].missedPayOrders[batchNumber]) {
    mappedData[type].missedPayOrders[batchNumber] = [];
  }
  const batchGroupPayOrderList =
    mappedData[type].batchGroupPayOrderList[batchNumber] || [];
  // Sort the array to ensure it is in ascending order
  batchGroupPayOrderList.sort((a, b) => a.payOrder - b.payOrder);

  for (let i = 1; i < batchGroupPayOrderList.length; i++) {
    let currentPayOrder = batchGroupPayOrderList[i].payOrder;
    let previousPayOrder = batchGroupPayOrderList[i - 1].payOrder;
    let currentKey = batchGroupPayOrderList[i].itemKey;
    let previousKey = batchGroupPayOrderList[i - 1].itemKey;

    // Check if there's a gap between the current and previous numbers
    if (currentPayOrder - previousPayOrder > 1) {
      // Add all missing numbers to the missingNumbers array
      for (let j = previousPayOrder + 1; j < currentPayOrder; j++) {
        mappedData[type].missedPayOrders[batchNumber].push({
          previous: {
            payOrder: previousPayOrder,
            key: previousKey,
          },
          missing: j,
          next: {
            payOrder: currentPayOrder,
            key: currentKey,
          },
        });
      }
    }
  }
};

export const checkIfOnlyOneBatchForType = (mappedData, type, batchNumber) => {
  mappedData[type].onlyOneBatchForType = Object.values(
    mappedData[type].batches
  ).every((item) => batchNumber === item.partidaNo);
};

export const checkIfOnlyCurrentYearBatches = (mappedData, type) => {
  mappedData[type].onlyCurrentYearBatches = Object.values(
    mappedData[type].batches
  ).every((item) => Number(item.taxPeriodYear) === currentYear);
};

export const checkIfOnlyCurrentYearItemsInCurrentBatch = (
  mappedData,
  type,
  batchNumber
) => {
  mappedData[type].onlyCurrentYearItemsForEachBatchGroup[batchNumber] =
    Object.values(mappedData[type].batches)
      .filter((item) => batchNumber === item.partidaNo)
      .every((item) => Number(item.taxPeriodYear) === currentYear);
};
