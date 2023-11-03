import React from "react";
import styles from "./addressRegistrationCardProgress.module.scss";
import { useTranslation } from "react-i18next";

const AddressRegistrationCardProgress = ({
  title,
  stepsCount,
  borderClassName,
  iconClassName,
  activeStepIndex,
}) => {
  const { t } = useTranslation();
  return (
    <div
      role="progressbar"
      aria-valuetext={title}
      aria-valuenow={activeStepIndex + 1}
      aria-valuemin="1"
      aria-valuemax={stepsCount + 1}
      aria-label={t("screen.reader.my.services.progress.bar")}
    >
      <p className={styles.title}>{title}</p>
      <div className={styles.progress}>
        {" "
          .repeat(stepsCount)
          .split(" ")
          .map((_, index) => {
            if (index === activeStepIndex) {
              return (
                <div className={styles.activeProgressStep} key={index}>
                  <span
                    id={"active"}
                    className={`${styles.progressStep} ${borderClassName}`}
                  />
                  <div className={`${styles.line} ${iconClassName}`} />
                </div>
              );
            } else {
              return <span key={index} className={styles.progressStep} />;
            }
          })}
      </div>
    </div>
  );
};
export default AddressRegistrationCardProgress;
