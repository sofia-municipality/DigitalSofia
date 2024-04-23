import React, { useContext } from "react";
import { cloneDeep } from "lodash";
import { useTranslation } from "react-i18next";

import { PAYMENT_RETRY_ENABLED_STATUSES } from "../../../../../constants/constants";
import { convertToDecimal } from "../../../../../utils";
import { TaxAccordionContext } from "../TaxesAccordion/context";
import styles from "./taxesCheckbox.module.scss";

const TaxesCheckbox = ({
  className = "",
  item = {},
  isChecked,
  onChange,
  type,
  shouldDisable = true,
  selectEnabled,
  forceDisable = false,
}) => {
  const { t } = useTranslation();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {}, allItems = {} } = taxAccordionContext;

  const isDisabled = () => {
    const batchData = allItems[type]?.data?.[item.partidaNo]?.data || [];
    const selectedBatchData =
      selectedItems[type]?.data?.[item.partidaNo]?.data || [];
    const previousObligation = selectedBatchData.find(
      (e) => e.payOrder === item.payOrder - 1
    );

    const payOrderList = batchData.map((e) => e.payOrder);
    const minPayOrder = payOrderList?.length ? Math.min(...payOrderList) : 0;

    return (
      !(!!previousObligation || item.payOrder === minPayOrder) && shouldDisable
    );
  };

  const defaultIsChecked = () => {
    const batchData = selectedItems[type]?.data?.[item.partidaNo]?.data;
    const selectedItem = (batchData || []).find(
      (e) =>
        e.payOrder === item.payOrder &&
        e.debtInstalmentId === item.debtInstalmentId
    );

    return !!selectedItem;
  };

  const defaultOnOnChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(selectedItems);

    const batchData = selectedItems[type]?.data?.[item.partidaNo]?.data;
    if (!isChecked) {
      const newData = batchData.filter(
        (b) =>
          b.payOrder <= item.payOrder &&
          b.debtInstalmentId !== item.debtInstalmentId
      );

      const unselectedItems =
        batchData.filter(
          (b) =>
            b.payOrder > item.payOrder ||
            b.debtInstalmentId === item.debtInstalmentId
        ) || [];

      newSelectedItems[type].data[item.partidaNo].data = newData;

      const newAmount = unselectedItems.reduce((acc, i) => {
        acc += convertToDecimal(i.residual + i.interest);
        return convertToDecimal(acc);
      }, 0);

      newSelectedItems[type].data[item.partidaNo].total = convertToDecimal(
        newSelectedItems[type].data[item.partidaNo].total - newAmount
      );

      newSelectedItems[type].total = convertToDecimal(
        newSelectedItems[type].total - newAmount
      );
    } else {
      const newBatchData = newSelectedItems[type].data[item.partidaNo];
      if (!newBatchData.data) {
        newSelectedItems[type].data[item.partidaNo].data = [];
      }

      if (!newBatchData.total) {
        newSelectedItems[type].data[item.partidaNo].total = 0;
      }

      const newAmount = convertToDecimal(item.residual + item.interest);

      newSelectedItems[type].data[item.partidaNo].data.push(cloneDeep(item));
      newSelectedItems[type].data[item.partidaNo].total = convertToDecimal(
        newSelectedItems[type].data[item.partidaNo].total + newAmount
      );

      newSelectedItems[type].total = convertToDecimal(
        newSelectedItems[type].total + newAmount
      );
    }

    setTaxAccordionContext({ selectedItems: cloneDeep(newSelectedItems) });
  };

  const onCheck = onChange || defaultOnOnChange;
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
