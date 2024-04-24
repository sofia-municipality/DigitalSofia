import React from "react";

import { SM_NEW_DESIGN_ENABLED } from "../../../../../constants/constants";
import BaseCta from "../BaseCta";
import styles from "./animated-cta.module.scss";

export const AnimationDirection = {
  LEFT: "left",
  RIGHT: "right",
};

const SmAnimatedCta = ({
  children,
  className = null,
  circleClassName = "bg-sm-circle-border-red",
  borderClassName = "sm-cta-border-red-yellow",
  animationDirection = AnimationDirection.LEFT,
  isLink,
  href,
  hardRedirect,
  onClick,
  accessibilityProps,
}) => {
  const animationDirectionClass =
    animationDirection === AnimationDirection.RIGHT
      ? styles.right
      : styles.left;

  const ctaClassname = `${styles.btn} ${
    SM_NEW_DESIGN_ENABLED ? styles.btnNewDesign : ""
  } ${borderClassName} `;

  return (
    <div className={`${styles.btnContainer} ${className}`}>
      <span
        className={`${styles.circle} ${
          SM_NEW_DESIGN_ENABLED ? styles.circleNewDesign : ""
        } ${circleClassName} ${animationDirectionClass} `}
      />
      <span
        className={`${styles.btnLine} ${
          SM_NEW_DESIGN_ENABLED ? styles.btnLineNewDesign : ""
        } ${animationDirectionClass}`}
      />
      <BaseCta
        className={ctaClassname}
        isLink={isLink}
        href={href}
        hardRedirect={hardRedirect}
        onClick={onClick}
        accessibilityProps={accessibilityProps}
      >
        {children}
      </BaseCta>
    </div>
  );
};

export default SmAnimatedCta;
