import React from "react";
import { useTranslation } from "react-i18next";

import { SM_NEW_DESIGN_ENABLED } from "../../../../../constants/constants";
import BaseCta from "../BaseCta";

import styles from "./sm-cta.module.scss";

export const SmCtaTypes = {
  PRIMARY: "primary",
  SECONDARY: "secondary",
  TERTIARY: "tertiary",
  QUATERNARY: "quaternary",
  OUTLINE: "outline",
  TRANSPARENT: "transparent",
  LINK: "link",
  ERROR_OUTLINE: "error-outline",
  ERROR: "error",
  SUCCESS: "success",
};

export const SmCtaSizes = {
  SMALL: "small",
  MEDIUM: "medium",
  LARGE: "large",
};

const SmCta = ({
  children,
  className = "",
  isLink,
  href,
  hardRedirect,
  onClick = () => {},
  type = SmCtaTypes.PRIMARY,
  size = SmCtaSizes.LARGE,
  disabled = false,
  loading = false,
  target,
  accessibilityProps,
  refObj,
}) => {
  const { t } = useTranslation();

  return (
    <BaseCta
      className={`btn ${styles.smCta} ${styles[type]} ${
        styles[size]
      } ${className} ${loading ? styles.loading : ""} ${
        SM_NEW_DESIGN_ENABLED ? styles[`${type}-new-design`] : ""
      }`}
      isLink={isLink}
      href={href}
      hardRedirect={hardRedirect}
      onClick={onClick}
      disabled={disabled}
      target={target}
      accessibilityProps={accessibilityProps}
      refObj={refObj}
    >
      {loading ? (
        <div role="alert" aria-busy="true" style={{ fontSize: 0 }}>
          {t("screen.reader.loading.alert")}
        </div>
      ) : null}
      {children}
    </BaseCta>
  );
};

export default SmCta;
