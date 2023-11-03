import React from "react";
import NavLink from "../../Navigation/NavLink";
import { usePageTitleRef } from "../../../../../customHooks";

import styles from "./customBreadcrumbs.module.scss";

const CustomBreadcrumbs = ({ className, link, linkText, title }) => {
  const headingRef = usePageTitleRef();

  return (
    <div className={className}>
      <NavLink className={styles.backLink} to={link}>
        {linkText}
      </NavLink>
      <div className={styles.breadcrumbsWrapper}>
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
