import { useTranslation } from "react-i18next";
import moment from "moment";

import { useDevice } from "../../../../../../../customHooks";
import TaxesCheckbox from "../TaxesCheckbox";

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

  return {
    numberFormatter,
    interestFormatter,
    totalFormatter,
  };
};

export const useGetColumnsConfig = (selectEnabled, type) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const { numberFormatter, interestFormatter, totalFormatter } =
    useGetFormatters();

  const totalAmountFormatter = (col, row, _, formatExtraData) => {
    if (formatExtraData.selectEnabled && formatExtraData.isPhone) {
      const value = totalFormatter(col, row);
      return (
        <div className="d-flex">
          <span className="mr-2">{value}</span>
          <TaxesCheckbox item={row} type={type} />
        </div>
      );
    } else {
      return totalFormatter(col, row);
    }
  };

  const columns = [
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

  if (selectEnabled && !isPhone) {
    columns.push({
      dataField: "debtinstalmentId",
      align: "right",
      headerAlign: "right",
      text: "",
      formatter: (_, row) => <TaxesCheckbox item={row} type={type} />,
    });
  }

  return columns;
};
