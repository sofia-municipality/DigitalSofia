import React, { useContext } from "react";
import { useTranslation } from "react-i18next";

import { usePayTaxData } from "../../../../../../../apiManager/apiHooks";
import { useGetFormatters } from "../../../../../components/Taxes/TaxesAccordion/hooks";
import { TaxAccordionContext } from "../../../../../components/Taxes/TaxesAccordion/context";
import Modal from "../../../../../components/Modal/Modal";
import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../components/buttons/SmCta";
import { convertToDecimal } from "../../../../../../../utils";
import { useDevice } from "../../../../../../../customHooks";

import styles from "./taxesPayCta.module.scss";

const PayCta = ({ className }) => {
  const { fetch: payData, error, isLoading, resetError } = usePayTaxData();
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const { numberFormatter } = useGetFormatters();
  const { taxAccordionContext, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  const {
    selectedItems = {},
    isPaymentInProgress,
    taxSubject,
  } = taxAccordionContext;
  const selectedTotal = Object.values(selectedItems).reduce((acc, item) => {
    acc += item.total || 0;
    return convertToDecimal(acc);
  }, 0);

  const onClick = () => {
    setTaxAccordionContext({ isPaymentInProgress: true });
    payData({ selectedItems, taxSubject });
  };

  return (
    <>
      {error ? (
        <Modal
          title={t("form.error.modal.title")}
          message={t("form.error.modal.message")}
          borderColor="red"
          modalOpen={!!error}
          textAlign="center"
          yesText={t("form.document.sign.evrotrust.modal.error.cta")}
          onYes={() => {
            setTaxAccordionContext({ isPaymentInProgress: false });
            resetError();
          }}
          showClose={false}
          showNo={false}
        />
      ) : null}
      <div
        className={`row justify-content-end align-items-center ${className}`}
      >
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
              onClick={onClick}
              size={SmCtaSizes.MEDIUM}
              disabled={isLoading || isPaymentInProgress}
              loading={isLoading || isPaymentInProgress}
            >
              {t("localTaxes.payment.payCta.label")}
            </SmCta>
          </div>
        ) : null}
      </div>
    </>
  );
};

export default PayCta;
