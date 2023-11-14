import React, { useContext } from "react";
import { cloneDeep } from "lodash";

import { TaxAccordionContext } from "../TaxesAccordion/context";
import styles from "./taxesCheckbox.module.scss";

const TaxesCheckbox = ({
  className = "",
  item = {},
  isChecked,
  onChange,
  type,
}) => {
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {} } = taxAccordionContext;

  const defaultIsChecked = () => {
    const batchData = selectedItems[type]?.data?.[item.partidaNo]?.data;
    const selectedItem = (batchData || []).find(
      (e) => e.debtinstalmentId === item.debtinstalmentId
    );

    return !!selectedItem;
  };

  const defaultOnOnChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(selectedItems);

    const batchData = selectedItems[type]?.data?.[item.partidaNo]?.data;
    if (!isChecked) {
      const newData = batchData.filter(
        (e) => e.debtinstalmentId !== item.debtinstalmentId
      );

      newSelectedItems[type].data[item.partidaNo].data = newData;

      const newAmount = item.residual + item.interest;
      newSelectedItems[type].data[item.partidaNo].total =
        newSelectedItems[type].data[item.partidaNo].total - newAmount;

      newSelectedItems[type].total = newSelectedItems[type].total - newAmount;
    } else {
      const newBatchData = newSelectedItems[type].data[item.partidaNo];
      if (!newBatchData.data) {
        newSelectedItems[type].data[item.partidaNo].data = [];
      }

      if (!newBatchData.total) {
        newSelectedItems[type].data[item.partidaNo].total = 0;
      }

      const newAmount = item.residual + item.interest;

      newSelectedItems[type].data[item.partidaNo].data.push(cloneDeep(item));
      newSelectedItems[type].data[item.partidaNo].total =
        newSelectedItems[type].data[item.partidaNo].total + newAmount;

      newSelectedItems[type].total = newSelectedItems[type].total + newAmount;
    }

    setTaxAccordionContext({ selectedItems: cloneDeep(newSelectedItems) });
  };

  const onCheck = onChange || defaultOnOnChange;
  const isCheckboxChecked = isChecked || defaultIsChecked;

  return (
    <div className={`sm-checkbox ${className}`}>
      <label className={`form-check-label ${styles.checkboxLabel}`}>
        <input
          type="checkbox"
          className="mt-0"
          checked={isCheckboxChecked()}
          onChange={onCheck}
          onClick={(e) => {
            e.stopPropagation();
          }}
        />
      </label>
    </div>
  );
};

export default TaxesCheckbox;
