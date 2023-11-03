import React from "react";

import styles from "./mobileAppSection.module.scss";
import { useTranslation } from "react-i18next";

const MobileAppSection = ({
  title,
  subtitle,
  description,
  image,
  ctaItems = [],
}) => {
  const { t } = useTranslation();
  const googlePlayCta = ctaItems.find((cta) => cta.ctaId) === "googleplay";
  const appStoreCta = ctaItems.find((cta) => cta.ctaId) === "appstore";

  return (
    <section
      className={`container-fluid ${styles.section} ${styles.mobileAppSection}`}
      aria-labelledby="mobileApp-section-title"
    >
      <div className={styles.sectionContent}>
        <div className="row flex-column flex-md-row">
          <div className={`col-md-6 ${styles.mobileAppContent}`}>
            <h2
              className={`${styles.sectionMainTitle} text-white`}
              id="mobileApp-section-title"
            >
              {title}
            </h2>
            <h3 className={`${styles.sectionSubtitle} text-white`}>
              {subtitle}
            </h3>
            <p className={`${styles.sectionDescription} text-white`}>
              {description}
            </p>
          </div>
          <div className={`col-md-6 ${styles.digiSofiaImgWrapper}`}>
            <img className={styles.digiSofiaImg} src={image} alt="" />
          </div>
        </div>
        <div className="row flex-column flex-md-row">
          <div className="col-auto text-center">
            <a
              href={googlePlayCta?.ctaHref || "/"}
              aria-label={t("screen.reader.digitall.sofia.googlePlay.link")}
            >
              <img src="/googleplay.png" alt="Google Play icon" />
            </a>
          </div>
          <div className="col-auto text-center mt-3 mt-md-0">
            <a
              href={appStoreCta?.ctaHref || "/"}
              aria-label={t("screen.reader.digitall.sofia.appleStore.link")}
            >
              <img src="/appstore.png" alt="Apple store icon" />
            </a>
          </div>
        </div>
      </div>
    </section>
  );
};

export default MobileAppSection;
