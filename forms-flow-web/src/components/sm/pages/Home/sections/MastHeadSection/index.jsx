import React, { useEffect, useContext } from "react";
import { useInView } from "react-intersection-observer";

import { SmContext } from "../../../../context";
import styles from "./mastHeadSection.module.scss";
import { useDevice } from "../../../../../../customHooks";

const bullets = [
  "bg-sm-circle-border-red",
  "bg-sm-circle-border-green",
  "bg-sm-circle-border-blue",
];

const MastHeadSection = ({ title, description, features = [] }) => {
  const { isMobile } = useDevice();
  const { ref: mainSectionRef, inView: isMainSectionVisible } = useInView({
    rootMargin: "-200px 0px 0px 0px",
    initialInView: true,
  });
  const { smContext, setSmContext } = useContext(SmContext);
  const { smallNav, isNavExpanded } = smContext;

  useEffect(() => {
    if (!isMainSectionVisible !== smallNav && !isNavExpanded)
      setSmContext({ smallNav: !isMainSectionVisible });
  }, [isMainSectionVisible, smallNav, setSmContext, isNavExpanded]);

  const featuresContent = features.slice(0, 3).map((feat, index) => ({
    text: feat,
    border: bullets[index],
  }));

  return (
    <div
      className={`container-fluid ${styles.section} ${styles.mainSection}`}
      ref={isMobile ? mainSectionRef : null}
    >
      <div className={`row ${styles.sectionContent}`}>
        <div className={`col-md-6 ${styles.leftContainer}`}>
          <h1 className={styles.title} ref={!isMobile ? mainSectionRef : null}>
            {title}
          </h1>
          <p className={styles.desc}>{description}</p>
        </div>
        <div
          className={`col-md-6 d-flex justify-content-center ${styles.rightContainer}`}
        >
          <div className={styles.circleWrapper}>
            <div className={styles.circleContent}>
              {featuresContent.map((bullet, index) => (
                <p key={index} className={styles.bullet}>
                  <span className={`${styles.circle} ${bullet.border}`}></span>
                  <span className={styles.bulletText}>{bullet.text}</span>
                </p>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MastHeadSection;
