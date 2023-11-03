import React from "react";
import { useTranslation } from "react-i18next";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import TaskOutlinedIcon from "@mui/icons-material/TaskOutlined";

import PageContainer from "../../components/PageContainer";
import ServiceContainer from "../../components/ServiceContainer";
import SmAnimatedCta, {
  AnimationDirection,
} from "../../components/buttons/SmAnimatedCta";

import { PAGE_ROUTES } from "../../../../constants/navigation";

import styles from "./requestService.module.scss";

const cards = [
  {
    iconSrc: "/where_to_vote.svg",
    iconActiveSrc: "/where_to_vote_filled.svg",
    title: "requestService.addressRegistration.title",
    subTitle: "requestService.addressRegistration.subtitle",
    description: "requestService.addressRegistration.description",
    ctaText: "requestService.addressRegistration.cta",
    ctaProps: {
      isLink: true,
      href: PAGE_ROUTES.ADDRESS_REGISTRATION,
    },
  },
  {
    iconSrc: "/account_balance.svg",
    iconActiveSrc: "/account_balance_filled.svg",
    title: "requestService.localTaxes.title",
    subTitle: "requestService.localTaxes.subtitle",
    description: "requestService.localTaxes.description",
    ctaProps: {
      isLink: true,
      href: PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
      animationDirection: AnimationDirection.RIGHT,
      circleClassName: "bg-sm-circle-border-green",
      borderClassName: "sm-cta-border-yellow-green",
    },
    ctaText: "requestService.localTaxes.cta",
  },
];

const RequestService = () => {
  const { t } = useTranslation();
  return (
    <PageContainer>
      <ServiceContainer
        title={t("requestService.title")}
        TitleIcon={EditOutlinedIcon}
        titleIconClass="text-sm-orange"
        link={PAGE_ROUTES.MY_SERVICES}
        linkText={t("requestService.link")}
        LinkIcon={TaskOutlinedIcon}
        linkIconClass="text-sm-blue"
      >
        <div className={`row no-gutters ${styles.createSection}`}>
          {cards.map(
            (
              {
                iconSrc,
                iconActiveSrc,
                title,
                subTitle,
                description,
                ctaText,
                ctaProps = {},
              },
              index
            ) => (
              <div key={index} className={`col ${styles.sectionCard}`}>
                <div>
                  <div className={styles.imgWrapper}>
                    <img
                      src="/iconBorder.svg"
                      alt=""
                      className={styles.imgBorder}
                    />
                    <img className={styles.icon} src={iconSrc} alt="" />
                    <img
                      className={`${styles.icon} ${styles.iconActive}`}
                      src={iconActiveSrc}
                      alt=""
                    />
                  </div>
                  <h1 className={styles.title}>{t(title)}</h1>
                  <h2 className={styles.subTitle}>{t(subTitle)}</h2>
                  <p className={styles.description}>{t(description)}</p>
                </div>
                <SmAnimatedCta className={styles.cta} {...ctaProps}>
                  {t(ctaText)}
                </SmAnimatedCta>
              </div>
            )
          )}
        </div>
      </ServiceContainer>
    </PageContainer>
  );
};
export default RequestService;
