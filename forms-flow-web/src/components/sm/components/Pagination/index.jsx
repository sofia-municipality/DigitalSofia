import React, { useLayoutEffect } from "react";
import Pagination from "react-js-pagination";
import KeyboardDoubleArrowLeftIcon from "@mui/icons-material/KeyboardDoubleArrowLeft";
import KeyboardDoubleArrowRightIcon from "@mui/icons-material/KeyboardDoubleArrowRight";
import { useDevice } from "../../../../customHooks";
import styles from "./pagination.module.scss";
import { useTranslation } from "react-i18next";

const SmPagination = ({
  totalItems,
  limit,
  changePage,
  selectedPage,
  range = 10,
  mobileRange = 5,
}) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  useLayoutEffect(() => {
    const pageEls = Array.from(
      document.getElementsByClassName("accessible-pagination-page-link")
    );

    const pages = pageEls.filter((e) => {
      const classes = Array.from(e.classList);
      return (
        !classes.includes("pagination-page-link-next") &&
        !classes.includes("pagination-page-link-previous")
      );
    });

    const previousPage = pageEls.find((e) => {
      const classes = Array.from(e.classList);
      return classes.includes("pagination-page-link-previous");
    });

    previousPage &&
      (previousPage.ariaLabel = t(
        "screen.reader.pagination.navigation.goToPreviousPage"
      ));

    const nextPage = pageEls.find((e) => {
      const classes = Array.from(e.classList);
      return classes.includes("pagination-page-link-next");
    });

    nextPage &&
      (nextPage.ariaLabel = t(
        "screen.reader.pagination.navigation.goToNextPage"
      ));

    pages.forEach((e) => {
      const ariaLabel = e.ariaLabel;
      const regexArgs = /\d+/.exec(ariaLabel);
      const number = regexArgs?.[0];
      const classes = Array.from(e.classList);
      if (classes.includes("pagination-page-link-active")) {
        e.ariaCurrent = "true";
        e.ariaLabel = `${t(
          "screen.reader.pagination.navigation.current"
        )} ${number}`;
      } else {
        e.ariaLabel = `${t(
          "screen.reader.pagination.navigation.goToPage"
        )} ${number}`;
      }
    });
  }, [t]);

  return (
    <nav
      role="navigation"
      aria-label={t("screen.reader.pagination.navigation")}
    >
      <Pagination
        innerClass="pagination justify-content-center"
        itemsCountPerPage={limit}
        activePage={selectedPage}
        totalItemsCount={totalItems}
        pageRangeDisplayed={isPhone ? mobileRange : range}
        itemClass={styles.pageWrapper}
        linkClass={`${styles.page} accessible-pagination-page-link`}
        linkClassPrev={"pagination-page-link-previous"}
        linkClassNext={"pagination-page-link-next"}
        activeLinkClass={`${styles.activePage} pagination-page-link-active`}
        activeClass={styles.activePageWrapper}
        onChange={changePage}
        hideFirstLastPages={true}
        prevPageText={<KeyboardDoubleArrowLeftIcon />}
        nextPageText={<KeyboardDoubleArrowRightIcon />}
      />
    </nav>
  );
};

export default SmPagination;
