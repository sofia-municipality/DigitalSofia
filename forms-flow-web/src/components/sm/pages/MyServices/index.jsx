import React from "react";
import { useTranslation } from "react-i18next";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import TaskOutlinedIcon from "@mui/icons-material/TaskOutlined";
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";

import PageContainer from "../../components/PageContainer";
import ServiceContainer from "../../components/ServiceContainer";
import NavLink from "../../components/Navigation/NavLink";

import { PAGE_ROUTES } from "../../../../constants/navigation";
import { useDevice } from "../../../../customHooks";

import styles from "./myServices.module.scss";

const MyServices = () => {
  const { isPhone } = useDevice();
  const { t } = useTranslation();

  return (
    <PageContainer>
      <ServiceContainer
        title={t("myServices.title")}
        TitleIcon={TaskOutlinedIcon}
        titleIconClass="text-sm-blue"
        link={PAGE_ROUTES.REQUEST_SERVICE}
        linkText={t("myServices.link")}
        LinkIcon={EditOutlinedIcon}
        linkIconClass="text-sm-orange"
      >
        <div className={styles.myServicesSectionWrapper}>
          <NavLink
            to={PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION}
            className={`row no-gutters ${styles.myServicesSection}`}
          >
            <div className={`col ${styles.serviceLinkWrapper}`}>
              <div className={styles.serviceLink}>
                <img className={styles.icon} src="/where_to_vote.svg" alt="" />
                <img
                  className={`${styles.icon} ${styles.iconActive}`}
                  src="/where_to_vote_filled.svg"
                  alt=""
                />
                <div className={styles.content}>
                  <h2
                    className={`${
                      isPhone ? "sm-heading-5" : "sm-heading-4"
                    } text-sm-indigo-dark`}
                  >
                    {t("myServices.card.1.title")}
                  </h2>
                  <p className="sm-body-2-regular text-sm-indigo-4">
                    {t("myServices.card.1.subtitle")}
                  </p>
                </div>
              </div>
              <KeyboardArrowRightIcon className={styles.arrowIcon} />
            </div>
          </NavLink>
          <NavLink
            to="/"
            className={`row no-gutters ${styles.myServicesSection}`}
          >
            <div className={`col ${styles.serviceLinkWrapper}`}>
              <div className={styles.serviceLink}>
                <img
                  className={styles.icon}
                  src="/account_balance.svg"
                  alt=""
                />
                <img
                  className={`${styles.icon} ${styles.iconActive}`}
                  src="/account_balance_filled.svg"
                  alt=""
                />
                <div className={styles.content}>
                  <h2
                    className={`${
                      isPhone ? "sm-heading-5" : "sm-heading-4"
                    } text-sm-indigo-dark`}
                  >
                    {t("myServices.card.2.title")}
                  </h2>
                  <p className="sm-body-2-regular text-sm-indigo-4">
                    {t("myServices.card.2.subtitle")}
                  </p>
                </div>
              </div>
              <KeyboardArrowRightIcon className={styles.arrowIcon} />
            </div>
          </NavLink>
        </div>
      </ServiceContainer>
    </PageContainer>
  );
};
export default MyServices;
