import React from "react";
import { useTranslation } from "react-i18next";
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";

import { SM_NEW_DESIGN_ENABLED } from "../../../../constants/constants";
import { useDevice } from "../../../../customHooks";
import NavLink from "../Navigation/NavLink";
import styles from "./sectionCards.module.scss";

export const SectionCard = ({
  link,
  iconSrc,
  iconActiveSrc,
  title,
  subtitle,
}) => {
  const { isPhone } = useDevice();
  const { t } = useTranslation();

  return (
    <NavLink
      to={link}
      className={`row no-gutters ${
        SM_NEW_DESIGN_ENABLED
          ? styles.servicesSectionNewDesign
          : styles.servicesSection
      }`}
    >
      {SM_NEW_DESIGN_ENABLED ? (
        <div
          className={`col ${styles.serviceLinkWrapper} ${styles.serviceLinkWrapperNewDesign}`}
        >
          <div>
            <img className={styles.iconNewDesign} src={iconSrc} alt="" />
            <h2
              className={`${
                isPhone ? "sm-heading-5" : "sm-heading-4"
              } text-sm-indigo-dark`}
            >
              {t(title)}
            </h2>
            <p className="sm-body-2-regular text-sm-indigo-4">{t(subtitle)}</p>
          </div>
          <KeyboardArrowRightIcon className={styles.arrowIconNewDesign} />
        </div>
      ) : (
        <div className={`col ${styles.serviceLinkWrapper}`}>
          <div className={styles.serviceLink}>
            <img className={styles.icon} src={iconSrc} alt="" />
            <img
              className={`${styles.icon} ${styles.iconActive}`}
              src={iconActiveSrc}
              alt=""
            />
            <div className={styles.content}>
              <h2
                className={`${
                  isPhone ? "sm-heading-5" : "sm-heading-4"
                } text-sm-indigo-dark`}
              >
                {t(title)}
              </h2>
              <p className="sm-body-2-regular text-sm-indigo-4">
                {t(subtitle)}
              </p>
            </div>
          </div>
          <KeyboardArrowRightIcon className={styles.arrowIcon} />
        </div>
      )}
    </NavLink>
  );
};

const SectionCards = ({ items }) => (
  <div
    className={
      SM_NEW_DESIGN_ENABLED
        ? styles.servicesSectionWrapperNewDesign
        : styles.servicesSectionWrapper
    }
  >
    {SM_NEW_DESIGN_ENABLED ? (
      <div className={styles.servicesSectionRow}>
        {items.map((item, index) => (
          <SectionCard key={index} {...item} />
        ))}
      </div>
    ) : (
      items.map((item, index) => <SectionCard key={index} {...item} />)
    )}
  </div>
);

export default SectionCards;
