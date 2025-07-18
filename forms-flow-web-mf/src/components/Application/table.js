import React from "react";
import { Link } from "react-router-dom";
import startCase from "lodash/startCase";
import {
  textFilter,
  selectFilter,
  customFilter,
  FILTER_TYPES,
} from "react-bootstrap-table2-filter";
import { Translation } from "react-i18next";
import DateRangePicker from "@wojtekmaj/react-daterange-picker";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import { toast } from "react-toastify";
import { HelperServices } from "@formsflow/service";

import {
  APPLICATION_STATUS_LABEL,
  DEFAULT_APPLICATION_STATUS_LABEL,
} from "../../constants/formEmbeddedConstants";
import { getFormSubmission } from "../../apiManager/services/FormServices";

let statusFilter, idFilter, nameFilter, modifiedDateFilter;

export const defaultSortedBy = [
  {
    dataField: "id",
    order: "desc", // or desc
  },
];

const getApplicationStatusOptions = (rows, t) => {
  const selectOptions = rows
    .filter((option) => APPLICATION_STATUS_LABEL[option])
    .map((option) => {
      return {
        value: option,
        label: t(APPLICATION_STATUS_LABEL[option]),
      };
    });
  return selectOptions;
};

const linkApplication = (cell, row, redirectUrl) => {
  return (
    <Link
      className="custom_primary_color"
      to={`${redirectUrl}application/${row.id}`}
      title={cell}
    >
      {cell}
    </Link>
  );
};

const ReceiptButton = ({ formId, name, receiptId }) => {
  const handleClick = async () => {
    const sliceSize = 512;

    const resp = await getFormSubmission(formId, receiptId);
    const dataUrl = resp.data.file[0].url;

    const byteCharacters = Buffer.from(dataUrl.split(',')[1], 'base64');
    const byteArrays = [];

    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      const slice = byteCharacters.subarray(offset, offset + sliceSize);

      const byteArray = Uint8Array.prototype.slice.call(slice);
      byteArrays.push(byteArray);
    }

    const blob = new Blob(byteArrays, { type: "application/pdf" });
    const blobUrl = URL.createObjectURL(blob);
  
    window.open(blobUrl, "_blank");
  };

  return (
    <button className="btn btn-link" key={receiptId} onClick={handleClick}>
      {name}
    </button>
  );
};

const Receipts = ({ row }) => {
  const receiptsList = row.receipts;

  return (
    <section>
      {receiptsList.map((rec) => (
        <ReceiptButton
          key={rec._id}
          formId={rec.form}
          name={rec.name}
          receiptId={rec._id}
        />
      ))}
    </section>
  );
};

function timeFormatter(cell) {
  const localdate = HelperServices?.getLocalDateAndTime(cell);
  return <label title={cell}>{localdate}</label>;
}

const nameFormatter = (cell) => {
  const name = startCase(cell);
  return (
    <label
      className="text-truncate w-100"
      style={{ maxWidth: "550px" }}
      title={name}
    >
      {startCase(name)}
    </label>
  );
};

const customStyle = { border: "1px solid #ced4da", fontStyle: "normal" };

const styleForValidationFail = { border: "1px solid red" };

export const columns_history = [
  {
    dataField: "applicationname",
    text: <Translation>{(t) => t("Application Name")}</Translation>,
    sort: true,
  },
  {
    dataField: "applicationstatus",
    text: <Translation>{(t) => t("Application Status")}</Translation>,
    sort: true,
  },
];

let applicationNotified = false;
const notifyValidationError = () => {
  if (!applicationNotified) {
    toast.error("Invalid application id");
    applicationNotified = true;
  }
};

export const columns = (
  applicationStatus,
  lastModified,
  callback,
  t,
  redirectUrl,
  invalidFilters
) => {
  if (invalidFilters.APPLICATION_ID) {
    notifyValidationError();
  } else {
    applicationNotified = false;
  }
  return [
    {
      dataField: "id",
      text: <Translation>{(t) => t("Application ID")}</Translation>,
      formatter: (cell, row) => linkApplication(cell, row, redirectUrl),
      headerClasses: "classApplicationId",
      sort: true,
      filter: textFilter({
        delay: 800,
        placeholder: `\uf002 ${t("Application ID")}`, // custom the input placeholder
        caseSensitive: false, // default is false, and true will only work when comparator is LIKE
        className: "icon-search",
        style: invalidFilters.APPLICATION_ID
          ? styleForValidationFail
          : customStyle,
        getFilter: (filter) => {
          idFilter = filter;
        },
      }),
    },
    {
      dataField: "applicationName",
      text: <Translation>{(t) => t("Application Name")}</Translation>,
      sort: true,
      headerClasses: "classApplicationName",
      formatter: nameFormatter,
      filter: textFilter({
        delay: 800,
        placeholder: `\uf002 ${t("Application Name")}`, // custom the input placeholder
        caseSensitive: false, // default is false, and true will only work when comparator is LIKE
        className: "icon-search",
        style: customStyle,
        getFilter: (filter) => {
          nameFilter = filter;
        },
      }),
    },
    {
      dataField: "applicationStatus",
      text: <Translation>{(t) => t("Application Status")}</Translation>,
      sort: true,
      formatter: (cell) => {
        return t(
          APPLICATION_STATUS_LABEL[cell] || DEFAULT_APPLICATION_STATUS_LABEL
        );
      },
      filter:
        applicationStatus?.length > 0 &&
        selectFilter({
          options: getApplicationStatusOptions(applicationStatus, t),
          style: customStyle,
          placeholder: `${t("All")}`,
          defaultValue: `${t("All")}`,
          caseSensitive: false, // default is false, and true will only work when comparator is LIKE
          getFilter: (filter) => {
            statusFilter = filter;
          },
        }),
    },
    {
      dataField: "receipts",
      text: <Translation>{(t) => t("Link To Receipts")}</Translation>,
      headerClasses: "receiptsColumn",
      formatter: (_, row) => <Receipts row={row} />,
    },

    {
      dataField: "modified",
      text: <Translation>{(t) => t("Last Modified")}</Translation>,
      formatter: timeFormatter,
      sort: true,
      filter: customFilter({
        type: FILTER_TYPES.DATE,
      }),
      // eslint-disable-next-line no-unused-vars
      filterRenderer: (onFilter, column) => {
        return (
          <DateRangePicker
            onChange={(selectedRange) => {
              callback(selectedRange);
              onFilter(selectedRange);
            }}
            value={lastModified}
            maxDate={new Date()}
            minDate={new Date("January 1, 0999 01:01:00")}
            dayPlaceholder="dd"
            monthPlaceholder="mm"
            yearPlaceholder="yyyy"
            calendarAriaLabel={t("Select the date")}
            dayAriaLabel="Select the day"
            clearAriaLabel="Click to clear"
            locale="bg-BG"
          />
        );
      },
    },
  ];
};

const customTotal = (from, to, total) => (
  <span className="react-bootstrap-table-pagination-total">
    <Translation>
      {(t) => t("so.translations.table.total.text", { from, to, total })}
    </Translation>
  </span>
);
export const customDropUp = ({
  options,
  currSizePerPage,
  onSizePerPageChange,
}) => {
  return (
    <DropdownButton
      drop="up"
      variant="secondary"
      title={currSizePerPage}
      style={{ display: "inline" }}
    >
      {options.map((option) => (
        <Dropdown.Item
          key={option.text}
          type="button"
          onClick={() => onSizePerPageChange(option.page)}
        >
          {option.text}
        </Dropdown.Item>
      ))}
    </DropdownButton>
  );
};
const getpageList = (count) => {
  const list = [
    {
      text: "5",
      value: 5,
    },
    {
      text: "25",
      value: 25,
    },
    {
      text: "50",
      value: 50,
    },
    {
      text: "100",
      value: 100,
    },
    {
      text: "All",
      value: count,
    },
  ];
  return list;
};

export const getoptions = (count, page, countPerPage) => {
  return {
    expandRowBgColor: "rgb(173,216,230)",
    pageStartIndex: 1,
    alwaysShowAllBtns: true, // Always show next and previous button
    withFirstAndLast: true, // Hide the going to First and Last page button
    hideSizePerPage: false, // Hide the sizePerPage dropdown always
    // hidePageListOnlyOnePage: true, // Hide the pagination list when only one page
    paginationSize: 7, // the pagination bar size.
    prePageText: "<",
    nextPageText: ">",
    showTotal: true,
    Total: count,
    paginationTotalRenderer: customTotal,
    disablePageTitle: true,
    sizePerPage: countPerPage,
    page: page,
    totalSize: count,
    sizePerPageList: getpageList(count),
    sizePerPageRenderer: customDropUp,
  };
};
export const clearFilter = () => {
  statusFilter("");
  idFilter("");
  nameFilter("");
  modifiedDateFilter("");
};
