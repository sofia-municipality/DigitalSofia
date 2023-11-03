import React from "react";
import { useTranslation } from "react-i18next";

import LoginButton from "../../../../components/buttons/LoginButton";
import styles from "./hyiSection.module.scss";

const HowItWorksSection = ({ id, title, subtitle, items = [], ctaText }) => {
  const { t } = useTranslation();
  return (
    <section
      tabIndex="-1"
      id={id}
      className={`container-fluid ${styles.section} ${styles.howSection}`}
      aria-labelledby="hyi-section-title"
    >
      <div className={`row flex-column ${styles.sectionContent}`}>
        <div className={`col-12 ${styles.stepContent}`}>
          <h2 className={styles.sectionMainTitle} id="hyi-section-title">
            {title}
          </h2>
          <h3 className={styles.sectionSubtitle}>{subtitle}</h3>
        </div>
        <div className={`col-12 ${styles.stepsContainer}`}>
          <div className="container-fluid">
            <div
              className={`row flex-column flex-md-row ${
                items.length < 6 ? "justify-content-between" : ""
              }`}
            >
              {items.map((step, index) => (
                <div
                  key={index}
                  className={`col col-md-2 d-flex flex-column 
                      align-items-center text-center ${styles.step}`}
                >
                  <img src={step.image} width="50px" height="50px" alt="" />
                  <p className={styles.stepDescription}>{step.description}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div
          className={`col-12 d-flex justify-content-center ${styles.howLoginCta}`}
        >
          <LoginButton>{ctaText || t("login.ctaText")}</LoginButton>
        </div>
      </div>
    </section>
  );
};

export default HowItWorksSection;
