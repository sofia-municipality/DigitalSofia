import { useTranslation } from "react-i18next";
import moment from "moment";

import { PAYMENT_STATUS } from "../../../../../constants/constants";
import { useDevice } from "../../../../../customHooks";
import TaxesCheckbox from "../TaxesCheckbox";
import { TaxesContainerType } from "../utils";

const getPaymentStatusTranslation = (status) => {
  switch (status) {
    case PAYMENT_STATUS.PENDING:
      return "myServices.localTaxes.status.pending";
    case PAYMENT_STATUS.PAID:
      return "myServices.localTaxes.status.paid";
    case PAYMENT_STATUS.EXPIRED:
      return "myServices.localTaxes.status.expired";
    case PAYMENT_STATUS.CANCELED:
      return "myServices.localTaxes.status.canceled";
    case PAYMENT_STATUS.IN_PROCESS:
      return "myServices.localTaxes.status.inProcess";
    default:
      return "myServices.localTaxes.status.pending";
  }
};

export const useGetFormatters = () => {
  const { t } = useTranslation();

  const numberFormatter = new Intl.NumberFormat("bg-BG", {
    minimumFractionDigits: 2,
  });

  const interestFormatter = (cell) => {
    const interest = Number.parseFloat(cell);
    return (
      <span
        className={interest ? "text-sm-red-6" : ""}
        style={interest ? { fontWeight: 700 } : {}}
      >
        {numberFormatter.format(cell)}
      </span>
    );
  };

  const totalFormatter = (_, row) => {
    const total = row.residual + row.interest;
    return (
      <span>
        {numberFormatter.format(total)} {t("currency.lev.short")}
      </span>
    );
  };

  const currencyFormatter = (cell) => (
    <span>
      {numberFormatter.format(cell)} {t("currency.lev.short")}
    </span>
  );

  return {
    numberFormatter,
    interestFormatter,
    totalFormatter,
    currencyFormatter,
  };
};

export const useGetColumnsConfig = (selectEnabled, type, containerType) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const {
    numberFormatter,
    interestFormatter,
    totalFormatter,
    currencyFormatter,
  } = useGetFormatters();

  const totalAmountFormatter = (col, row, _, formatExtraData) => {
    if (formatExtraData.selectEnabled && formatExtraData.isPhone) {
      const value = totalFormatter(col, row);
      return (
        <div className="d-flex">
          <span className="mr-2">{value}</span>
          <TaxesCheckbox
            selectEnabled={formatExtraData.selectEnabled}
            item={row}
            type={type}
          />
        </div>
      );
    } else {
      return totalFormatter(col, row);
    }
  };

  const referencePaymentColumns = [
    {
      dataField: "taxPeriodYear",
      align: "center",
      headerAlign: "center",
      text: t("localTaxes.reference.category.content.col.year"),
    },
    {
      dataField: "payOrder",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.payOrder"),
    },
    {
      dataField: "instNo",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.instNo"),
    },
    {
      dataField: "termPayDate",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.termPayDate"),
      formatter: (cell) => moment(cell).format("DD.MM.YYYY"),
    },
    {
      dataField: "residual",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.residual"),
      formatter: (cell) => numberFormatter.format(cell),
    },
    {
      dataField: "interest",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.interest"),
      formatter: interestFormatter,
    },
    {
      dataField: "value",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.value"),
      formatter: totalAmountFormatter,
      formatExtraData: {
        isPhone,
        selectEnabled,
      },
    },
  ];

  const statusColumns = [
    {
      dataField: "tax_period_year",
      align: "left",
      headerAlign: "left",
      text: t("localTaxes.status.category.content.col.year"),
    },
    {
      dataField: "pay_order",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.reference.category.content.col.instNo"),
    },
    {
      dataField: "amount",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.status.category.content.col.sum"),
      formatter: currencyFormatter,
    },
    {
      dataField: "created",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.status.category.content.col.date"),
      formatter: (cell) => moment(cell).format("DD.MM.YYYY"),
    },
    {
      dataField: "status",
      align: "center",
      headerAlign: "center",
      text: t("localTaxes.status.category.content.col.status"),
      formatter: (cell) => t(getPaymentStatusTranslation(cell)),
    },
  ];

  const columns =
    containerType === TaxesContainerType.STATUS
      ? statusColumns
      : referencePaymentColumns;

  if (!isPhone && containerType !== TaxesContainerType.STATUS) {
    columns.push({
      dataField: "debtInstalmentId",
      align: "right",
      headerAlign: "right",
      text: t("localTaxes.payment.category.content.action"),
      formatter: (_, row) => (
        <TaxesCheckbox selectEnabled={selectEnabled} item={row} type={type} />
      ),
      headerFormatter: (cell) => (
        <span style={{ fontSize: 0 }}>{cell.text}</span>
      ),
    });
  }

  return columns;
};
