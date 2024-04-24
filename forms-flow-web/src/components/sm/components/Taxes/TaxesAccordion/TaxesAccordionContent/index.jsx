import React from "react";
import BootstrapTable from "react-bootstrap-table-next";
import { useTranslation } from "react-i18next";

import {
  TAX_CATEGORIES_IDENTIFIER_PROP,
  PAYMENT_RETRY_ENABLED_STATUSES,
} from "../../../../../../constants/constants";
import { useDevice } from "../../../../../../customHooks";

import { TaxesContainerType } from "../../utils";
import { useGetColumnsConfig } from "../hooks";
import ContentHeader from "../TaxesAccordionContentHeader";
import ContentFooter from "../TaxesAccordionContentFooter";
import styles from "./taxesAccordionContent.module.scss";

const TaxesAccordionContent = ({
  type,
  data = {},
  selectEnabled,
  showTotal,
  containerType,
}) => {
  const { isPhone } = useDevice();
  const { t } = useTranslation();
  const columns = useGetColumnsConfig(selectEnabled, type, containerType);
  const rowStyle = (row) => {
    const style = {};
    if (
      row.hasPaymentRequest &&
      !PAYMENT_RETRY_ENABLED_STATUSES.includes(row.status)
    ) {
      style.outline = "#D04925 1px solid";
    }

    return style;
  };

  return Object.entries(data).map(([key, value]) => {
    const { data: items = [], total } = value;
    const identifier =
      items[0]?.[TAX_CATEGORIES_IDENTIFIER_PROP[type]] ||
      items[0]?.propertyAddress ||
      items[0]?.additional_data;

    return !isPhone ? (
      <section className={styles.taxContentWrapper} aria-label={key}>
        <ContentHeader
          identifier={identifier}
          total={total}
          type={type}
          batchNumber={key}
          selectEnabled={selectEnabled}
          showTotal={showTotal}
        />
        <div className={styles.taxRecord}>
          <BootstrapTable
            wrapperClasses={styles.taxRecordTable}
            keyField={
              containerType === TaxesContainerType.STATUS
                ? "payment_id"
                : "debtInstalmentId"
            }
            data={items}
            columns={columns}
            rowStyle={rowStyle}
            bordered={false}
            headerWrapperClasses={styles.tableHeader}
            bodyClasses={styles.tableBody}
          />
        </div>
        <ContentFooter
          batchNumber={key}
          selectEnabled={selectEnabled}
          type={type}
        />
      </section>
    ) : (
      <section className={styles.taxContentWrapper} aria-label={key}>
        <ContentHeader
          identifier={identifier}
          total={total}
          type={type}
          batchNumber={key}
          selectEnabled={selectEnabled}
          showTotal={showTotal}
        />
        <ul
          className={styles.taxesMobileWrapper}
          aria-label={t("screen.reader.localTaxesAndFees.list")}
        >
          {items.map((item, index) => (
            <li key={index} style={rowStyle(item)}>
              <section
                className={styles.taxRecordMobile}
                aria-label={`${t(
                  "screen.reader.localTaxesAndFees.list.item"
                )} ${index + 1}`}
              >
                {columns.map((col, index) => {
                  const value = item[col.dataField];
                  const formatter = col.formatter || ((val) => val);
                  return (
                    <div key={index} className={styles.mobileRow}>
                      <div>{col.text}</div>
                      <div>
                        {formatter(
                          value,
                          item,
                          index,
                          col.formatExtraData || {}
                        )}
                      </div>
                    </div>
                  );
                })}
              </section>
            </li>
          ))}
        </ul>
        <ContentFooter
          batchNumber={key}
          selectEnabled={selectEnabled}
          type={type}
        />
      </section>
    );
  });
};

export default TaxesAccordionContent;
