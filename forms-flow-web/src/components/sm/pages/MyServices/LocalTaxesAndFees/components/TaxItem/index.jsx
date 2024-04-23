import React from "react";
import { useTranslation } from "react-i18next";
import moment from "moment";
import KeyboardArrowRightIcon from "@mui/icons-material/KeyboardArrowRight";

import { PAGE_ROUTES } from "../../../../../../../constants/navigation";
import NavLink from "../../../../../components/Navigation/NavLink";
import { useDevice } from "../../../../../../../customHooks";
import { getTaxRecordGroupStatusIcon } from "../../utils";
import styles from "./taxItem.module.scss";

const TaxDate = ({ date }) => {
  const { t } = useTranslation();
  return (
    <div className={styles.taxDateWrapper}>
      <span className={styles.taxDateLabel}>
        {t("myServices.localTaxes.date")}
      </span>
      <span className="sm-body-regular">
        {moment(date).format("DD.MM.YYYY")}
      </span>
    </div>
  );
};

const TaxItem = ({ created, status, id }) => {
  const { isPhone } = useDevice();
  const { t } = useTranslation();
  const {
    Icon,
    className: iconClassName,
    border,
    accessibleName,
  } = getTaxRecordGroupStatusIcon(status);
  const detailsUrl =
    PAGE_ROUTES.MY_SERVICES_LOCAL_TAXES_AND_FEES_DETAILS.replace(
      ":paymentId",
      id
    );

  return (
    <NavLink
      to={detailsUrl}
      className={`row no-gutters ${styles.localTaxesItem}`}
    >
      <div className={`col ${styles.taxItemContent}`}>
        <div className={styles.leftContent}>
          <div className="d-flex align-items-center">
            {Icon ? (
              <>
                <span style={{ fontSize: 0 }}>{t(accessibleName)}</span>
                {border ? (
                  <div className={`${styles.iconWrapper} ${iconClassName}`}>
                    <Icon className={styles.icon} />
                  </div>
                ) : (
                  <Icon
                    className={`${styles.iconWithoutWrapper} ${iconClassName}`}
                  />
                )}
              </>
            ) : null}
            <div className="sm-heading-5">
              {t("myServices.localTaxes.taxRecord.title")}
            </div>
          </div>
          {isPhone && <TaxDate date={created} />}
        </div>

        <div className="d-flex align-items-center">
          {!isPhone && <TaxDate date={created} />}
          <KeyboardArrowRightIcon className={styles.arrowIcon} />
        </div>
      </div>
    </NavLink>
  );
};

export default TaxItem;
