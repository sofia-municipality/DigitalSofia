import React, { useState, useEffect, useContext } from "react";
import { useTranslation } from "react-i18next";
import moment from "moment";
import { cloneDeep } from "lodash";

import { useDevice } from "../../../../../../../customHooks";
import { useGetTaxReference } from "../../../../../../../apiManager/apiHooks";
import { PAGE_ROUTES } from "../../../../../../../constants/navigation";
import { TAX_CATEGORIES } from "../../../../../../../constants/constants";
import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../components/buttons/SmCta";

import TaxesInfo from "../TaxesInfo";
import TaxesAccordion from "../TaxesAccordion";
import {
  TaxAccordionContextProvider,
  TaxAccordionContext,
} from "../TaxesAccordion/context";
import { useGetFormatters } from "../TaxesAccordion/hooks";

import styles from "./taxesContainer.module.scss";
import Loading from "../../../../../../../containers/Loading";

const now = new Date();

export const TaxesContainerType = {
  REFERENCE: "reference",
  PAYMENT: "payment",
};

const TaxesContainer = ({
  title,
  subtitle,
  type = TaxesContainerType.REFERENCE,
  taxes,
  total,
}) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const { numberFormatter } = useGetFormatters();
  const [openCloseAll, setOpenCloseAll] = useState("");
  const taxRecords = Object.entries(taxes);
  const date = moment(now).format("DD.MM.YYYY");
  const time = moment(now).format("hh:mm:ss");

  const { taxAccordionContext } = useContext(TaxAccordionContext);
  const { selectedItems = {} } = taxAccordionContext;
  const selectedTotal = Object.values(selectedItems).reduce((acc, item) => {
    acc += item.total || 0;
    return acc;
  }, 0);

  useEffect(() => {
    if (total && type === TaxesContainerType.PAYMENT) {
      setOpenCloseAll("open");
    }
  }, [total, type]);

  const PayCta = ({ className }) => (
    <div className={`row justify-content-end align-items-center ${className}`}>
      <div className="col-12 col-md-auto d-flex justify-content-end">
        <div className={styles.paySumTotalLabel}>
          <span className="sm-heading-5 mr-5">
            {t("localTaxes.payment.payCta.title")}
          </span>
          <span className={styles.paySumTotal}>
            {numberFormatter.format(selectedTotal)}
          </span>
          <span className={styles.currency}>{t("currency.lev.short")}</span>
        </div>
      </div>
      {selectedTotal ? (
        <div
          className={`col-12 col-md-auto ${
            isPhone ? "mt-4" : ""
          } d-flex justify-content-end`}
        >
          <SmCta
            className="px-4"
            type={SmCtaTypes.SECONDARY}
            onClick={() => console.log(selectedItems)}
            size={SmCtaSizes.MEDIUM}
          >
            {t("localTaxes.payment.payCta.label")}
          </SmCta>
        </div>
      ) : null}
    </div>
  );

  return (
    <div className={`${styles.localTaxesContainer}`}>
      <div className={`${styles.localTaxesContent} pr-md-5 pl-md-5`}>
        <div className={styles.content}>
          <div>
            <TaxesInfo
              date={date}
              time={time}
              total={total}
              title={title}
              subtitle={subtitle}
            />
            <div className="container-fluid">
              {type === TaxesContainerType.PAYMENT &&
              !isPhone &&
              taxRecords?.length ? (
                <PayCta className={"my-4"} />
              ) : null}
              <div className="row">
                {!isPhone && taxRecords?.length ? (
                  <div className="col-12 d-flex justify-content-end mt-2">
                    <SmCta
                      className={styles.openAllLink}
                      type={SmCtaTypes.OUTLINE}
                      size={SmCtaSizes.SMALL}
                      onClick={() => {
                        openCloseAll !== "open"
                          ? setOpenCloseAll("open")
                          : setOpenCloseAll("close");
                      }}
                      accessibilityProps={{
                        "aria-expanded":
                          openCloseAll !== "open" ? "false" : "true",
                      }}
                    >
                      <span className="sm-cta-outline-underline">
                        {t(
                          openCloseAll !== "open"
                            ? "faqs.openAll.cta.text"
                            : "faqs.closeAll.cta.text"
                        )}
                      </span>
                    </SmCta>
                  </div>
                ) : null}
                <div
                  className={`col-12 ${styles.accordionWrapper} ${
                    type === TaxesContainerType.PAYMENT
                      ? styles.accordionPaymentWrapper
                      : ""
                  }`}
                >
                  {taxRecords?.length ? (
                    taxRecords.map(([key, value], index) => {
                      if (Object.values(TAX_CATEGORIES).includes(key)) {
                        return (
                          <TaxesAccordion
                            id={index}
                            key={key}
                            data={value}
                            type={key}
                            forceOpenClose={openCloseAll}
                            selectEnabled={type === TaxesContainerType.PAYMENT}
                          />
                        );
                      }
                    })
                  ) : (
                    <div className={styles.noTaxDataFound}>
                      <span className={styles.noTaxDataFoundText}>
                        {t("localTaxes.reference.noTaxData")}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className={styles.referenceFooter}>
          <div>
            {taxRecords?.length ? (
              <div className="container-fluid">
                {type === TaxesContainerType.PAYMENT ? (
                  <PayCta />
                ) : (
                  <div className="row">
                    <div className="col-12 d-flex justify-content-end">
                      <SmCta
                        type={SmCtaTypes.OUTLINE}
                        isLink
                        href={PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT}
                      >
                        <span className="sm-cta-outline-underline">
                          {t("localTaxes.reference.pay.cta")}
                        </span>
                      </SmCta>
                    </div>
                  </div>
                )}
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
};

const TaxesContainerWrapper = (props) => {
  const { data = {}, isLoading } = useGetTaxReference();
  const { obligations } = data;
  const { total, data: taxes = {} } = obligations || {};

  return data && !isLoading ? (
    <TaxAccordionContextProvider
      defaultState={{ selectedItems: cloneDeep(taxes) }}
    >
      <TaxesContainer total={total} taxes={taxes} {...props} />
    </TaxAccordionContextProvider>
  ) : (
    <Loading />
  );
};

export default TaxesContainerWrapper;
