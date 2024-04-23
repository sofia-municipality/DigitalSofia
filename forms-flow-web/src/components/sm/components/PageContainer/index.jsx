import React, { useEffect } from "react";
import { useLocation, useHistory } from "react-router-dom";
import { matchPath } from "react-router";
import styles from "./pageContainer.module.scss";

import {
  NavLinksSections,
  ROUTES_WITH_NAV_ANIMATION,
} from "../../../../constants/navigation";

const PageContainer = ({ children }) => {
  const { action } = useHistory();
  const hideNav = localStorage.getItem("hideNav");
  const { pathname, hash, key } = useLocation() || {};
  const isRouteWithNavAnimation = ROUTES_WITH_NAV_ANIMATION.some((route) =>
    matchPath(pathname, { path: route, exact: true })
  );

  useEffect(() => {
    if (hash === "") {
      const element = document.getElementById("app");
      element?.scrollTo(0, 0);
    } else {
      if (action === "POP") {
        window.history.replaceState(
          "",
          document.title,
          window.location.pathname + window.location.search
        );
      } else {
        setTimeout(() => {
          const id = hash.replace("#", "");
          const element = document.getElementById(id);
          if (element) {
            if (id !== NavLinksSections.MAIN_CONTENT) {
              element.scrollIntoView({
                behavior: "smooth",
                block: "start",
              });
            }

            element.focus({ preventScroll: true });
          } else {
            const element = document.getElementById("app");
            element.scrollTo(0, 0);
          }
        }, 100);
      }
    }
  }, [pathname, hash, key, action]);

  return (
    <div
      className={`${styles.pageContainer} ${
        isRouteWithNavAnimation ? styles.withNavAnimation : ""
      } ${hideNav ? "p-0" : ""}`}
      id={NavLinksSections.MAIN_CONTENT}
      tabIndex="-1"
    >
      {children}
    </div>
  );
};

export default PageContainer;
