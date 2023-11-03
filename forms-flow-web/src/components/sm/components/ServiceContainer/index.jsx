import React from "react";
import { useSelector } from "react-redux";
import { useDevice, usePageTitleRef } from "../../../../customHooks";

import BaseCta from "../buttons/BaseCta";

import styles from "./serviceContainer.module.scss";

const ServiceContainer = ({
  title,
  TitleIcon,
  titleIconClass,
  link,
  linkText,
  LinkIcon,
  linkIconClass,
  children,
}) => {
  const hideNav = localStorage.getItem("hideNav");
  const { isPhone } = useDevice();
  const headingRef = usePageTitleRef();
  const isAuth = useSelector((state) => state.user.isAuthenticated);

  return (
    <div className={styles.serviceWrapper}>
      <div className={styles.serviceContainer}>
        <div className="row no-gutters d-flex justify-content-between align-items-center mb-4">
          <div className="col-auto">
            <div className={styles.pageTitleWrapper}>
              {TitleIcon ? (
                <TitleIcon className={`${titleIconClass} ${styles.icon}`} />
              ) : null}
              <h1
                className={`sm-heading-3 ${styles.pageTitle}`}
                tabIndex="-1"
                ref={headingRef}
              >
                {title}
              </h1>
            </div>
          </div>
          {isAuth && link && !isPhone ? (
            <div className="col-auto">
              <BaseCta className={styles.link} isLink href={link}>
                {LinkIcon ? (
                  <LinkIcon className={`${linkIconClass} ${styles.icon}`} />
                ) : null}
                <span>{linkText}</span>
              </BaseCta>
            </div>
          ) : null}
        </div>
        {children}
      </div>
      {isPhone && !hideNav ? (
        <div className={styles.stickyMobileBottomNav}>
          <BaseCta className={styles.link} isLink href={link}>
            {LinkIcon ? (
              <LinkIcon className={`${linkIconClass} ${styles.icon}`} />
            ) : null}
            <span>{linkText}</span>
          </BaseCta>
        </div>
      ) : null}
    </div>
  );
};
export default ServiceContainer;
