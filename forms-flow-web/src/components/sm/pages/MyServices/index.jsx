import React from "react";
import { useTranslation } from "react-i18next";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import TaskOutlinedIcon from "@mui/icons-material/TaskOutlined";

import PageContainer from "../../components/PageContainer";
import ServiceContainer from "../../components/ServiceContainer";
import SectionCards from "../../components/SectionCards";

import { PAGE_ROUTES } from "../../../../constants/navigation";
import { SM_NEW_DESIGN_ENABLED } from "../../../../constants/constants";

import styles from "./myServices.module.scss";

const PageIcon = () => (
  <img
    className={styles.pageIcon}
    width="70px"
    height="70px"
    alt=""
    src="/assets/Images/my-services-icon.svg"
  />
);

const RequestServiceLinkIcon = () => (
  <img
    width="40px"
    height="40px"
    className={styles.requestServiceLinkIcon}
    alt=""
    src="/assets/Images/request-service-link-icon.svg"
  />
);

const sectionCards = [
  {
    link: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    iconSrc: SM_NEW_DESIGN_ENABLED
      ? "/assets/Images/address-registration-card-icon.svg"
      : "/where_to_vote.svg",
    iconActiveSrc: "/where_to_vote_filled.svg",
    title: "myServices.card.1.title",
    subtitle: "myServices.card.1.subtitle",
  },
  {
    link: PAGE_ROUTES.MY_SERVICES_LOCAL_TAXES_AND_FEES,
    iconSrc: SM_NEW_DESIGN_ENABLED
      ? "/assets/Images/local-taxes-card-icon.svg"
      : "/account_balance.svg",
    iconActiveSrc: "/account_balance_filled.svg",
    title: "myServices.card.2.title",
    subtitle: "myServices.card.2.subtitle",
  },
];

const MyServices = () => {
  const { t } = useTranslation();
  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );

  return (
    <PageContainer>
      <ServiceContainer
        title={t("myServices.title")}
        TitleIcon={SM_NEW_DESIGN_ENABLED ? PageIcon : TaskOutlinedIcon}
        titleIconClass="text-sm-blue"
        link={PAGE_ROUTES.REQUEST_SERVICE}
        linkText={t("myServices.link")}
        LinkIcon={
          SM_NEW_DESIGN_ENABLED ? RequestServiceLinkIcon : EditOutlinedIcon
        }
        linkIconClass="text-sm-orange"
        linkClass={SM_NEW_DESIGN_ENABLED ? styles.requestServicePageLink : ""}
        forceShowNavLink={SM_NEW_DESIGN_ENABLED && showRequestServiceLink}
      >
        <SectionCards items={sectionCards} />
      </ServiceContainer>
    </PageContainer>
  );
};
export default MyServices;
