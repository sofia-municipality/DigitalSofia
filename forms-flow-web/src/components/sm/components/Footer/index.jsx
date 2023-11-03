import React from "react";
import { useTranslation } from "react-i18next";

import { PAGE_ROUTES } from "../../../../constants/navigation";
import NavLink from "../Navigation/NavLink";
import styles from "./footer.module.scss";

const content = [
  {
    text: "footer.personal.data",
    link: PAGE_ROUTES.PERSONAL_DATA,
  },
  {
    text: "footer.terms",
    link: PAGE_ROUTES.TERMS_AND_CONDITIONS,
  },
  {
    text: "footer.cookies",
    link: PAGE_ROUTES.COOKIE_POLICY,
  },
];

const Footer = () => {
  const { t } = useTranslation();
  return (
    <div className="container-fluid">
      <div
        className={`${styles.smFooter} 
      row flex-column-reverse flex-lg-row align-items-center justify-content-center py-5`}
      >
        <div className="col-auto mt-4 mt-lg-0">
          <div className="container-fluid">
            <div className="row flex-column flex-lg-row">
              <div className="col-auto">
                <div className="container-flex">
                  <div
                    className="row no-gutters justify-content-center flex-nowrap"
                    style={{ gap: "25px" }}
                  >
                    {content.map((item, index) => (
                      <div key={index} className="col-auto">
                        <NavLink className={styles.footerLink} to={item.link}>
                          {t(item.text)}
                        </NavLink>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
              <div className={`col-auto ${styles.copyright} mt-4 mt-lg-0`}>
                {t("footer.allRights")}
              </div>
            </div>
          </div>
        </div>
        <div className="col-auto d-flex flex-column align-items-center justify-content-center">
          <img
            src="/digital-sofia-footer-logo.svg"
            alt="Sofia Manucipality Logo"
            width="160"
            height="180"
          />
          <div className="mt-2">sofia.bg</div>
        </div>
      </div>
    </div>
  );
};

export default Footer;
