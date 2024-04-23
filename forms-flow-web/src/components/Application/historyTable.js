import React from "react";
import {
  getFormUrl,
  getLocalDateTime,
} from "../../apiManager/services/formatterService";
import { Translation } from "react-i18next";

export const defaultSortedBy = [
  {
    dataField: "name",
    order: "asc", // or desc
  },
];

const linkSubmision = (row, redirectUrl) => {
  const { formId, submissionId } = row;
  const url = getFormUrl(formId, submissionId, redirectUrl);
  return (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
    <div title={url} onClick={() => window.open(url, "_blank")}>
      <span className="btn btn-primary btn-sm form-btn">
        <span>
          <i className="fa fa-eye" aria-hidden="true"></i>&nbsp;
        </span>
        <Translation>{(t) => t("View Submission")}</Translation>
      </span>
    </div>
  );
};

function timeFormatter(cell) {
  const localDate = getLocalDateTime(cell);
  return <label title={cell}>{localDate}</label>;
}

// History table columns
export const columns_history = (redirectUrl) => [
  {
    dataField: "applicationStatus",
    text: <Translation>{(t) => t("Status")}</Translation>,
    sort: true,
  },
  {
    dataField: "created",
    text: <Translation>{(t) => t("Created")}</Translation>,
    sort: true,
    formatter: timeFormatter,
  },
  {
    dataField: "submittedBy",
    text: <Translation>{(t) => t("Submitted By")}</Translation>,
    sort: true,
  },
  {
    dataField: "formId",
    text: <Translation>{(t) => t("Submissions")}</Translation>,
    formatter: (cell, row) => linkSubmision(row, redirectUrl),
  },
];
const customTotal = (from, to, total) => (
  <span className="react-bootstrap-table-pagination-total" role="main">
    <Translation>
      {(t) => t("so.translations.table.total.text", { from, to, total })}
    </Translation>
  </span>
);

export const getoptions = (count) => {
  return {
    expandRowBgColor: "rgb(173,216,230)",
    pageStartIndex: 1,
    alwaysShowAllBtns: true, // Always show next and previous button
    withFirstAndLast: false, // Hide the going to First and Last page button
    hideSizePerPage: true, // Hide the sizePerPage dropdown always
    // hidePageListOnlyOnePage: true, // Hide the pagination list when only one page
    paginationSize: 7, // the pagination bar size.
    prePageText: "<<",
    nextPageText: ">>",
    showTotal: true,
    Total: count,
    paginationTotalRenderer: customTotal,
    disablePageTitle: true,
    sizePerPage: 5,
  };
};
