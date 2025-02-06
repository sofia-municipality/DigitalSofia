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
// Commented out because of new design
// const getTotalItemsCount = (items, type) => {
//   const data = Object.keys(items[type]?.batches).length;

//   // Object.values(items[type]?.batches || []).filter(
//   //   (e) => e?.data?.length
//   // );

//   const itemsCount = data.reduce((acc, item) => {
//     acc += item?.data?.length;
//     return acc;
//   }, 0);

//   return itemsCount;
// };

const Total = ({
  type,
  className = "",
  selectEnabled,
  // isExpanded,
  showMainTotalCheckbox = true,
}) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { transformedSelectedItems = {}, allItems = {} } = taxAccordionContext;
  const total = transformedSelectedItems[type]?.total;
  const items = allItems[type]?.batches;

  const shouldDisableCheckbox = () => {
    return Object.entries(items || {}).reduce((acc, [, value]) => {
      if (value) {
        acc = false;
      }

      return acc;
    }, true);
  };

  const isChecked = () => {
    const selectedDataCount = Object.keys(
      transformedSelectedItems[type]?.batches
    ).length;
    const totalItemsCount = Object.keys(allItems[type]?.batches).length;
    return selectedDataCount === totalItemsCount;
  };

  const onChange = (e) => {
    const isChecked = e.target.checked;
    const newSelectedItems = cloneDeep(transformedSelectedItems);

    if (!isChecked) {
      newSelectedItems[type].batches = {};
      newSelectedItems[type].total = 0;

      Object.keys(newSelectedItems[type].batchesTotals).forEach((key) => {
        newSelectedItems[type].batchesTotals[key] = 0;
      });
    } else {
      newSelectedItems[type].batches = cloneDeep(items);
      newSelectedItems[type].total = allItems[type].total;
      Object.keys(newSelectedItems[type].batchesTotals).forEach((key) => {
        newSelectedItems[type].batchesTotals[key] =
          allItems[type].batchesTotals[key];
      });
    }

    setTaxAccordionContext({
      transformedSelectedItems: cloneDeep(newSelectedItems),
    });
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
      {selectEnabled && showMainTotalCheckbox ? (
        <TaxesCheckbox
          className="ml-2"
          onChange={onChange}
          isChecked={isChecked}
          selectEnabled={selectEnabled}
          forceDisable={shouldDisableCheckbox()}
        />
      ) : null}
    </div>
  );
};

const SelectedTotal = ({ className = "" }) => {
  // Commented out because of new design
  // const { t } = useTranslation();
  // const { numberFormatter } = useGetFormatters();
  // const { taxAccordionContext = {} } = useContext(TaxAccordionContext);

  // const getTotalSelectedAmount = () => {
  //   const { transformedSelectedItems = {} } = taxAccordionContext;
  //   const batchSelectedItemsTotals =
  //     transformedSelectedItems[type]?.batchesTotals || {};
  //   const total = Object.values(batchSelectedItemsTotals).reduce(
  //     (acc, item) => {
  //       acc += item || 0;
  //       return convertToDecimal(acc);
  //     },
  //     0
  //   );
  //   return total;
  // };

  return (
    <div
      className={`d-flex align-items-center justify-content-end 
      ${styles.totalWrapperWithSelect} ${
        SM_NEW_DESIGN_ENABLED ? "" : "mt-4"
      } ${className}`}
      aria-hidden="true"
    >
      {/*   With the new design we hide this part fo the application*/}
      {/* <div className={styles.totalLabel}>
        {t("localTaxes.payment.total.selected.amount.label")}
      </div>
      <div className={styles.selectedTotalWrapper}>
        <span className={`${styles.total} ${styles.selectedTotal}`}>
          {numberFormatter.format(getTotalSelectedAmount())}
        </span>
        <span className={styles.currency}>{t("currency.lev.short")}</span>
      </div> */}
    </div>
  );
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
  const { transformedSelectedItems = {} } = taxAccordionContext;
  const isSelectedForPayment =
    !!transformedSelectedItems?.[type]?.total && selectEnabled;

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
              showMainTotalCheckbox={true}
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
