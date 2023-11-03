import React, { useMemo, useContext } from "react";
import { useTranslation } from "react-i18next";
import { cloneDeep } from "lodash";

import { TAX_CATEGORIES } from "../../../../../../../constants/constants";
import { useDevice } from "../../../../../../../customHooks";
import SimpleAccordion from "../../../../../components/Accordion/SimpleAccordion";
import TaxesCheckbox from "../TaxesCheckbox";
import { TaxAccordionContext } from "./context";
import { useGetFormatters } from "./hooks";
import Content from "./TaxesAccordionContent";
import styles from "./taxesAccordion.module.scss";

const getAccordionTitle = (type) => {
  switch (type) {
    case TAX_CATEGORIES.REAL_ESTATE:
      return {
        icon: "/in_home_mode.svg",
        iconExpanded: "/in_home_mode_filled.svg",
        title: "localTaxes.reference.category.realEstate.title",
      };
    case TAX_CATEGORIES.HOUSEHOLD_WASTE:
      return {
        icon: "/delete.svg",
        iconExpanded: "/delete_filled.svg",
        title: "localTaxes.reference.category.houseHoldWaste.title",
      };
    case TAX_CATEGORIES.VEHICLE:
      return {
        icon: "/directions_car.svg",
        iconExpanded: "/directions_car_filled.svg",
        title: "localTaxes.reference.category.vehicle.title",
      };
    default:
      return {
        icon: "",
        title: "",
      };
  }
};

const Title = ({ total, title, icon, iconExpanded, isExpanded }) => {
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
        <span style={{ fontSize: 0 }}>
          {t("localTaxes.reference.category.total.label")}
          {numberFormatter.format(total)}
          {t("currency.lev.short")}
        </span>
      </div>
    </div>
  );
};

const Total = ({
  type,
  total,
  className = "",
  selectEnabled,
  isExpanded,
  items,
  showMainTotalCheckbox = true,
}) => {
  const { t } = useTranslation();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const { selectedItems = {} } = taxAccordionContext;

  const isChecked = () => {
    const selectedData = Object.values(selectedItems[type]?.data || []).filter(
      (e) => e?.data?.length
    );

    const totalItems = Object.values(items || []).filter(
      (e) => e?.data?.length
    );
    return selectedData?.length === totalItems.length;
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
      className={`d-flex align-items-center justify-content-end mr-3 ${className}`}
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
      return acc;
    }, 0);

    return total;
  };

  return !isExpanded ? (
    <div
      className={`d-flex align-items-center justify-content-end mr-3 mt-4 ${className}`}
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
  selectEnabled = false,
}) => {
  const { title, icon, iconExpanded } = getAccordionTitle(type);
  const { isPhone } = useDevice();
  const { data: items, total } = data;
  const { taxAccordionContext } = useContext(TaxAccordionContext);
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
          />
        ),
    [total, title, icon, iconExpanded]
  );

  const ExtraTitleInfoComponent = useMemo(
    () =>
      !isPhone
        ? ({ isExpanded }) => (
            <Total
              type={type}
              items={items}
              isExpanded={isExpanded}
              total={total}
              selectEnabled={selectEnabled}
              showMainTotalCheckbox={false}
            />
          )
        : null,
    [type, items, isPhone, total, selectEnabled]
  );

  const SubTitleComponent = useMemo(
    () =>
      isPhone
        ? ({ isExpanded }) => (
            <>
              <Total
                type={type}
                items={items}
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
    [isPhone, total, type, selectEnabled, items]
  );

  const ContentComponent = useMemo(
    () => () =>
      <Content data={items} type={type} selectEnabled={selectEnabled} />,
    [items, type, selectEnabled]
  );

  return (
    <SimpleAccordion
      id={id}
      forceOpenClose={forceOpenClose}
      className={`${styles.taxesAccordion} ${
        isSelectedForPayment ? styles.selected : ""
      }`}
      expandedClassName={styles.expanded}
      Title={TitleComponent}
      ExtraTitleInfo={ExtraTitleInfoComponent}
      SubTitle={SubTitleComponent}
      Content={ContentComponent}
    />
  );
};

export default TaxesAccordion;
