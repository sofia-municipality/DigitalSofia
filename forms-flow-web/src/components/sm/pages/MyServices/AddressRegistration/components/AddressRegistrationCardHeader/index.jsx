import React from "react";

import { useDevice } from "../../../../../../../customHooks";
import AddressRegistrationCardProgress from "../AddressRegistrationCardProgress";
import styles from "./addressRegistrationCardHeader.module.scss";
import { useTranslation } from "react-i18next";

const AddressRegistrationCardHeader = ({
  names,
  formName,
  entryNumber,
  stepsCount,
  statusLabel,
  Icon,
  borderClassName,
  iconClassName,
  activeStepIndex,
}) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  return isPhone ? (
    <div className={styles.addressCardHeaderMobile}>
      <div className={styles.iconNamesWrapper}>
        <div className={`${styles.iconWrapperMobile} ${iconClassName} mr-0`}>
          <Icon className={styles.mobileIcon} />
        </div>
        <p className={styles.names}>{names}</p>
      </div>
      <div className={styles.namesWrapper}>
        <p className={styles.formName}>{formName}</p>
        {entryNumber ? (
          <div className={styles.entryNumberMobile}>
            <span className={styles.entryNumberLabel}>
              {t("myServices.reference.number.label")}
            </span>
            <span className={styles.entryNumberValue}>{entryNumber}</span>
          </div>
        ) : null}
        <div className={styles.statusProgressWrapper}>
          <AddressRegistrationCardProgress
            title={statusLabel}
            stepsCount={stepsCount}
            borderClassName={borderClassName}
            iconClassName={iconClassName}
            activeStepIndex={activeStepIndex}
          />
        </div>
      </div>
    </div>
  ) : (
    <div className={styles.addressCardHeader}>
      <div className={styles.iconImageWrapper}>
        <div className={`${styles.iconWrapper} ${iconClassName}`}>
          <Icon />
        </div>
        <div className={styles.namesWrapper}>
          <p className={styles.names}>{names}</p>
          <p className={styles.formName}>{formName}</p>
          <AddressRegistrationCardProgress
            title={statusLabel}
            stepsCount={stepsCount}
            borderClassName={borderClassName}
            iconClassName={iconClassName}
            activeStepIndex={activeStepIndex}
          />
        </div>
      </div>
      {entryNumber ? (
        <div className={styles.entryNumber}>
          <span className={styles.entryNumberLabel}>
            {t("myServices.reference.number.label")}
          </span>
          <span className={styles.entryNumberValue}>{entryNumber}</span>
        </div>
      ) : null}
    </div>
  );
};

export default AddressRegistrationCardHeader;
