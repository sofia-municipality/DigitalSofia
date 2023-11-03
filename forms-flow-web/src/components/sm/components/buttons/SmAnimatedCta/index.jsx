import React from "react";

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
}) => {
  const animationDirectionClass =
    animationDirection === AnimationDirection.RIGHT
      ? styles.right
      : styles.left;

  const ctaClassname = `${styles.btn} ${borderClassName}`;

  return (
    <div className={`${styles.btnContainer} ${className}`}>
      <span
        className={`${styles.circle} ${circleClassName} ${animationDirectionClass}`}
      />
      <span className={`${styles.btnLine} ${animationDirectionClass}`} />
      <BaseCta
        className={ctaClassname}
        isLink={isLink}
        href={href}
        hardRedirect={hardRedirect}
        onClick={onClick}
      >
        {children}
      </BaseCta>
    </div>
  );
};

export default SmAnimatedCta;
