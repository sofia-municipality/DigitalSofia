import React, { useMemo, useContext } from "react";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

import {
  TAX_CATEGORIES,
  SM_NEW_DESIGN_ENABLED,
} from "../../../../../constants/constants";
import { useDevice } from "../../../../../customHooks";
import SimpleAccordion from "../../../components/Accordion/SimpleAccordion";
import TaxesCheckbox from "../TaxesCheckbox";
import { TaxAccordionContext } from "./context";
import { useGetFormatters } from "./hooks";
import { convertToDecimal } from "../../../../../utils";
import Content from "./TaxesAccordionContent";
import styles from "./taxesAccordion.module.scss";

const getAccordionTitle = (type) => {
  switch (type) {
    case TAX_CATEGORIES.REAL_ESTATE:
      return {
        icon: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/real_estate_taxes_icon.svg"
          : "/in_home_mode.svg",
        iconExpanded: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/real_estate_taxes_icon.svg"
          : "/in_home_mode_filled.svg",
        title: "localTaxes.reference.category.realEstate.title",
      };
    case TAX_CATEGORIES.HOUSEHOLD_WASTE:
      return {
        icon: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/house_hold_waste_taxes_icon.svg"
          : "/delete.svg",
        iconExpanded: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/house_hold_waste_taxes_icon.svg"
          : "/delete_filled.svg",
        title: "localTaxes.reference.category.houseHoldWaste.title",
      };
    case TAX_CATEGORIES.VEHICLE:
      return {
        icon: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/vehicle_taxes_icon.svg"
          : "/directions_car.svg",
        iconExpanded: SM_NEW_DESIGN_ENABLED
          ? "/assets/Images/vehicle_taxes_icon.svg"
          : "/directions_car_filled.svg",
        title: "localTaxes.reference.category.vehicle.title",
      };
    default:
      return {
        icon: "",
        title: "",
      };
  }
};

const Title = ({ total, title, icon, iconExpanded, isExpanded, showTotal }) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  return (
    <div className="d-flex align-items-center">
      <img
        src={isExpanded ? iconExpanded : icon}
        alt=""
        className={styles.icon}
      />
      <div className={styles.title}>
        <span>{t(title)}</span>
        {showTotal && (
          <span style={{ fontSize: 0 }}>
            {t("localTaxes.reference.category.total.label")}
            {numberFormatter.format(total)}
            {t("currency.lev.short")}
          </span>
        )}
      </div>
    </div>
  );
};

const getTotalItemsCount = (items, type) => {
  const data = Object.values(items[type]?.data || []).filter(
    (e) => e?.data?.length
  );

  const itemsCount = data.reduce((acc, item) => {
    acc += item?.data?.length;
    return acc;
  }, 0);

  return itemsCount;
};

const Total = ({
  type,
  className = "",
  selectEnabled,
  isExpanded,
  showMainTotalCheckbox = true,
}) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {}, allItems = {} } = taxAccordionContext;
  const total = allItems[type]?.total;
  const items = allItems[type]?.data;

  const shouldDisableCheckbox = () => {
    return Object.entries(items || {}).reduce((acc, [, value]) => {
      if (value?.data?.length > 0) {
        acc = false;
      }

      return acc;
    }, true);
  };

  const isChecked = () => {
    const selectedDataCount = getTotalItemsCount(selectedItems, type);
    const totalItemsCount = getTotalItemsCount(allItems, type);

    return selectedDataCount === totalItemsCount;
  };

  const onChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(selectedItems);

    if (!isChecked) {
      Object.keys(newSelectedItems[type].data).forEach((key) => {
        delete newSelectedItems[type].data[key].data;
        newSelectedItems[type].data[key].total = 0;
      });
      newSelectedItems[type].total = 0;
    } else {
      newSelectedItems[type].data = cloneDeep(items);
      newSelectedItems[type].total = total;
    }

    setTaxAccordionContext({ selectedItems: cloneDeep(newSelectedItems) });
  };
  return (
    <div
      className={`d-flex align-items-center justify-content-end 
      ${
        selectEnabled ? styles.totalWrapperWithSelect : styles.totalWrapper
      } ${className}`}
      aria-hidden="true"
    >
      <div
        className={`${styles.totalLabel} ${
          selectEnabled ? "" : styles.showOnHover
        }`}
      >
        {t("localTaxes.reference.category.total.label")}
      </div>
      <div className="d-flex align-items-center">
        <span className={styles.total}>{numberFormatter.format(total)}</span>
        <span className={styles.currency}>{t("currency.lev.short")}</span>
      </div>
      {selectEnabled && isExpanded && showMainTotalCheckbox ? (
        <TaxesCheckbox
          className="ml-2"
          onChange={onChange}
          isChecked={isChecked}
          shouldDisable={false}
          selectEnabled={selectEnabled}
          forceDisable={shouldDisableCheckbox()}
        />
      ) : null}
    </div>
  );
};

const SelectedTotal = ({ className = "", isExpanded, type }) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext = {} } = useContext(TaxAccordionContext);

  const getTotalSelectedAmount = () => {
    const { selectedItems = {} } = taxAccordionContext;
    const batchSelectedItems = selectedItems[type]?.data || {};
    const total = Object.values(batchSelectedItems).reduce((acc, item) => {
      acc += item.total || 0;
      return convertToDecimal(acc);
    }, 0);

    return total;
  };

  return !isExpanded ? (
    <div
      className={`d-flex align-items-center justify-content-end 
      ${styles.totalWrapperWithSelect} ${
        SM_NEW_DESIGN_ENABLED ? "" : "mt-4"
      } ${className}`}
      aria-hidden="true"
    >
      <div className={styles.totalLabel}>
        {t("localTaxes.payment.total.selected.amount.label")}
      </div>
      <div className={styles.selectedTotalWrapper}>
        <span className={`${styles.total} ${styles.selectedTotal}`}>
          {numberFormatter.format(getTotalSelectedAmount())}
        </span>
        <span className={styles.currency}>{t("currency.lev.short")}</span>
      </div>
    </div>
  ) : null;
};

const TaxesAccordion = ({
  id,
  type,
  forceOpenClose,
  data = {},
  containerType,
  selectEnabled = false,
  showTotal = true,
  onExpand,
}) => {
  const { title, icon, iconExpanded } = getAccordionTitle(type);
  const { isPhone } = useDevice();
  const { data: items, total } = data;
  const { taxAccordionContext = {} } = useContext(TaxAccordionContext);
  const { selectedItems = {} } = taxAccordionContext;
  const isSelectedForPayment = !!selectedItems?.[type]?.total && selectEnabled;

  const TitleComponent = useMemo(
    () =>
      ({ isExpanded }) =>
        (
          <Title
            total={total}
            title={title}
            icon={icon}
            iconExpanded={iconExpanded}
            isExpanded={isExpanded}
            showTotal={showTotal}
          />
        ),
    [total, title, icon, iconExpanded, showTotal]
  );

  const ExtraTitleInfoComponent = useMemo(
    () =>
      !isPhone && showTotal
        ? ({ isExpanded }) => (
            <Total
              type={type}
              isExpanded={isExpanded}
              total={total}
              selectEnabled={selectEnabled}
              showMainTotalCheckbox={false}
            />
          )
        : null,
    [type, isPhone, total, selectEnabled, showTotal]
  );

  const SubTitleComponent = useMemo(
    () =>
      isPhone && showTotal
        ? ({ isExpanded }) => (
            <>
              <Total
                type={type}
                className="mt-4"
                isExpanded={isExpanded}
                total={total}
                selectEnabled={selectEnabled}
              />
              {selectEnabled ? (
                <SelectedTotal type={type} isExpanded={isExpanded} />
              ) : null}
            </>
          )
        : selectEnabled
        ? ({ isExpanded }) => (
            <SelectedTotal type={type} isExpanded={isExpanded} />
          )
        : null,
    [isPhone, total, type, selectEnabled, showTotal]
  );

  const ContentComponent = useMemo(
    () => () =>
      (
        <Content
          data={items}
          type={type}
          selectEnabled={selectEnabled}
          showTotal={showTotal}
          containerType={containerType}
        />
      ),
    [items, type, selectEnabled, showTotal, containerType]
  );

  return (
    <SimpleAccordion
      id={id}
      forceOpenClose={forceOpenClose}
      className={`${styles.taxesAccordion} ${
        isSelectedForPayment ? styles.selected : ""
      } ${SM_NEW_DESIGN_ENABLED ? styles.taxesAccordionNewDesign : ""}`}
      expandedClassName={styles.expanded}
      Title={TitleComponent}
      ExtraTitleInfo={ExtraTitleInfoComponent}
      SubTitle={SubTitleComponent}
      Content={ContentComponent}
      onExpand={onExpand}
    />
  );
};

export default TaxesAccordion;
