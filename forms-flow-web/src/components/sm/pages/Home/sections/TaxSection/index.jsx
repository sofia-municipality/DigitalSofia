import React from "react";

import SmAnimatedCta, {
  AnimationDirection,
} from "../../../../components/buttons/SmAnimatedCta";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import styles from "./taxSection.module.scss";

const TaxSection = ({
  title,
  subtitle,
  description,
  image,
  ctaText,
  ctaAnimationDirection = AnimationDirection.RIGHT,
}) => (
  <section
    className={`container-fluid ${styles.section} ${styles.taxSection}`}
    aria-labelledby="taxes-section-title"
  >
    <div className={`row flex-column flex-md-row ${styles.sectionContent}`}>
      <div className={`col-md-5 col-xxl-4 ${styles.taxImgwrapper}`}>
        <img className={styles.taxImg} src={image} alt="" />
      </div>
      <div className={`col-md-7 col-xxl-8 ${styles.taxContent}`}>
        <h2 className={styles.sectionMainTitle} id="taxes-section-title">
          {title}
        </h2>
        <h3 className={styles.sectionSubtitle}>{subtitle}</h3>
        <p className={styles.sectionDescription}>{description}</p>
        <SmAnimatedCta
          className={styles.sectionMainCta}
          href={PAGE_ROUTES.LOCAL_TAXES_AND_FEES}
          animationDirection={ctaAnimationDirection}
          circleClassName="bg-sm-circle-border-green"
          borderClassName="sm-cta-border-yellow-green"
          isLink
        >
          {ctaText}
        </SmAnimatedCta>
      </div>
    </div>
  </section>
);

export default TaxSection;
