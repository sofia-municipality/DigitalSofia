import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import moment from "moment";
import { cloneDeep } from "lodash";

import {
  useDevice,
  useOpenCloseAllItems,
} from "../../../../../../../customHooks";
import { useGetTaxReference } from "../../../../../../../apiManager/apiHooks";
import { PAGE_ROUTES } from "../../../../../../../constants/navigation";
import {
  TAX_CATEGORIES,
  PAYMENT_RETRY_ENABLED_STATUSES,
} from "../../../../../../../constants/constants";
import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../components/buttons/SmCta";
import { convertToDecimal } from "../../../../../../../utils";

import PayCta from "../TaxesPayCta";
import TaxesInfo from "../TaxesInfo";
import TaxesAccordion from "../../../../../components/Taxes/TaxesAccordion";
import { TaxAccordionContextProvider } from "../../../../../components/Taxes/TaxesAccordion/context";
import { TaxesContainerType } from "../../../../../components/Taxes/utils";

import styles from "./taxesContainer.module.scss";
import Loading from "../../../../../../../containers/Loading";

const now = new Date();

const TaxesContainer = ({
  title,
  subtitle,
  type = TaxesContainerType.REFERENCE,
  taxes,
  total,
  hasAlreadyGeneratedRequests,
}) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const taxRecords = Object.entries(taxes);
  const date = moment(now).format("DD.MM.YYYY");
  const time = moment(now).format("hh:mm:ss");
  const {
    onOpenCloseAllClick,
    onExpand,
    openCloseAll,
    shouldOpenCloseAll,
    forceOpenAll,
  } = useOpenCloseAllItems(taxRecords.length);

  useEffect(() => {
    if (total && type === TaxesContainerType.PAYMENT) {
      forceOpenAll();
    }
  }, [total, type, forceOpenAll]);

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
                {hasAlreadyGeneratedRequests ? (
                  <div
                    className={`col-12 ${styles.infoMessageWrapper} ${
                      type === TaxesContainerType.REFERENCE
                        ? styles.infoMessageWrapperReference
                        : ""
                    }`}
                  >
                    <div className={styles.infoMessage}>
                      <img
                        src="/assets/Images/pending_payment_request_icon.svg"
                        alt="Pending Payment Icon"
                        className={styles.infoMessageIcon}
                        width="20px"
                        height="20px"
                      />
                      <span className={styles.infoMessageText}>
                        {t("localTaxes.pending.payments.info.message")}
                      </span>
                    </div>
                  </div>
                ) : null}
                {!isPhone && taxRecords?.length ? (
                  <div className="col-12 d-flex justify-content-end mt-2">
                    <SmCta
                      className={styles.openAllLink}
                      type={SmCtaTypes.OUTLINE}
                      size={SmCtaSizes.SMALL}
                      onClick={onOpenCloseAllClick}
                      accessibilityProps={{
                        "aria-expanded":
                          shouldOpenCloseAll !== "open" ? "false" : "true",
                      }}
                    >
                      <span className="sm-cta-outline-underline">
                        {t(
                          shouldOpenCloseAll !== "open"
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
                            containerType={type}
                            onExpand={onExpand}
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

const filterAlreadyGeneratedRequests = (taxes) => {
  const mappedData = {};
  let hasAlreadyGeneratedRequests = false;
  Object.entries(taxes).forEach(([key, value]) => {
    if (!mappedData[key]) {
      mappedData[key] = { data: {}, total: value?.total };
    }

    Object.entries(value?.data).forEach(([batchKey, batchValue]) => {
      if (!mappedData[key].data[batchKey]) {
        mappedData[key].data[batchKey] = {};
      }

      const alreadyGeneratedRequests = batchValue.data.filter(
        (e) =>
          e.hasPaymentRequest &&
          !PAYMENT_RETRY_ENABLED_STATUSES.includes(e.status)
      );

      if (alreadyGeneratedRequests?.length) {
        hasAlreadyGeneratedRequests = true;
      }

      const totalAlreadyGeneratedRequests = alreadyGeneratedRequests.reduce(
        (total, request) => {
          total += convertToDecimal(request.residual + request.interest);
          return convertToDecimal(total);
        },
        0
      );

      mappedData[key].data[batchKey].data = batchValue.data.filter(
        (e) =>
          !e.hasPaymentRequest ||
          PAYMENT_RETRY_ENABLED_STATUSES.includes(e.status)
      );

      mappedData[key].data[batchKey].total = convertToDecimal(
        batchValue.total - totalAlreadyGeneratedRequests
      );

      mappedData[key].total = convertToDecimal(
        mappedData[key].total - totalAlreadyGeneratedRequests
      );
    });
  });

  return { data: mappedData, hasAlreadyGeneratedRequests };
};

const TaxesContainerWrapper = (props) => {
  const { data = {}, isLoading } = useGetTaxReference();
  const { obligations, taxSubject } = data;
  const { data: taxes = {} } = obligations || {};
  const { data: filteredTaxes, hasAlreadyGeneratedRequests } =
    filterAlreadyGeneratedRequests(taxes);
  const total = Object.values(filteredTaxes).reduce((total, item) => {
    total += item.total;
    return convertToDecimal(total);
  }, 0);

  return data && !isLoading ? (
    <TaxAccordionContextProvider
      defaultState={{
        selectedItems: cloneDeep(filteredTaxes),
        allItems: cloneDeep(filteredTaxes),
        taxSubject: cloneDeep(taxSubject),
      }}
    >
      <TaxesContainer
        total={total}
        taxes={taxes}
        hasAlreadyGeneratedRequests={hasAlreadyGeneratedRequests}
        {...props}
      />
    </TaxAccordionContextProvider>
  ) : (
    <Loading />
  );
};

export default TaxesContainerWrapper;
