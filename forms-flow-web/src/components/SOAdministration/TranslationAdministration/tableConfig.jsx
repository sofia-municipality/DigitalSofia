import { Dropdown, DropdownButton } from "react-bootstrap";
import { Translation } from "react-i18next";

const customDropUp = ({ options, currSizePerPage, onSizePerPageChange }) => {
  return (
    <DropdownButton
      drop="down"
      variant="secondary"
      title={currSizePerPage}
      style={{ display: "inline" }}
    >
      {options.map((option) => (
        <Dropdown.Item
          key={option.text}
          type="button"
          onClick={() => {
            onSizePerPageChange(option.page);
          }}
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

const customTotal = (from, to, total) => (
  <span className="react-bootstrap-table-pagination-total" role="main">
    <Translation>
      {(t) => t("so.translations.table.total.text", { from, to, total })}
    </Translation>
  </span>
);

export const getoptions = (pageNo, limit, totalForms, isMobile) => {
  return {
    expandRowBgColor: "rgb(173,216,230)",
    pageStartIndex: 1,
    alwaysShowAllBtns: true,
    withFirstAndLast: !isMobile,
    hideSizePerPage: false,
    hidePageListOnlyOnePage: true,
    paginationSize: isMobile ? 3 : 7,
    prePageText: "<",
    nextPageText: ">",
    showTotal: !isMobile,
    Total: totalForms,
    disablePageTitle: true,
    sizePerPage: limit,
    page: pageNo,
    totalSize: totalForms,
    paginationTotalRenderer: customTotal,
    sizePerPageList: getpageList(totalForms),
    sizePerPageRenderer: customDropUp,
  };
};
