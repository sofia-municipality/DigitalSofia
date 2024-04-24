import React from "react";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";

import NavLink from "../../Navigation/NavLink";
import { usePageTitleRef } from "../../../../../customHooks";
import { SM_NEW_DESIGN_ENABLED } from "../../../../../constants/constants";

import styles from "./customBreadcrumbs.module.scss";

const CustomBreadcrumbs = ({ className, link, linkText, title }) => {
  const headingRef = usePageTitleRef();

  return (
    <div className={className}>
      <NavLink
        className={`${styles.backLink} ${
          SM_NEW_DESIGN_ENABLED ? styles.backLinkNewDesign : ""
        }`}
        to={link}
      >
        {SM_NEW_DESIGN_ENABLED ? (
          <span className={styles.backIconWrapper}>
            <ChevronLeftIcon
              className={styles.backIcon}
              width="20px"
              height="20px"
            />
          </span>
        ) : null}
        <span>{linkText}</span>
      </NavLink>
      <div
        className={`${styles.breadcrumbsWrapper} ${
          SM_NEW_DESIGN_ENABLED ? styles.breadcrumbsWrapperNewDesign : ""
        }`}
      >
        <div className={styles.breadcrumbs}>
          <div className={styles.line} />
          <div className={styles.circle} />
        </div>
        <h1 className={styles.pageTitle} tabIndex="-1" ref={headingRef}>
          {title}
        </h1>
      </div>
    </div>
  );
};
export default CustomBreadcrumbs;
