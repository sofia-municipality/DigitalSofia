import React, { useEffect, useContext, useState } from "react";
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
import useCheckForPayment from "../../../../../../../customHooks/check-for-payment";
import {
  buildBatchesTotals,
  buildBatches,
  buildPayOrderGroupList,
  buildBatchGroupPayOrderList,
  createMinimumPayOrder,
  createMaximumPayOrder,
  buildBatchGroupAllHighestPayOrderPaymentsList,
  combineAllMissedPayOrdersForEachTaxGroup,
  findMissingPayOrdersForBatchGroup,
  checkIfOnlyOneBatchForType,
  checkIfOnlyCurrentYearBatches,
  checkIfOnlyCurrentYearItemsInCurrentBatch,
} from "./utils";
import { TaxAccordionContext } from "../../../../../components/Taxes/TaxesAccordion/context";
import Modal from "../../../../../components/Modal/Modal";
import { useLocation, useHistory } from "react-router-dom";

const TaxesContainer = ({
  title,
  subtitle,
  type = TaxesContainerType.REFERENCE,
  taxes,
  hasAlreadyGeneratedRequests,
}) => {
  const { t } = useTranslation();
  const { isPhone } = useDevice();
  const taxRecords = Object.entries(taxes);
  const now = new Date();
  const date = moment(now).format("DD.MM.YYYY");
  const time = moment(now).format("hh:mm:ss");
  const { onOpenCloseAllClick, onExpand, openCloseAll, shouldOpenCloseAll } =
    useOpenCloseAllItems(taxRecords.length);
  const { fetch: checkForInitiatedPayment, CheckForPaymentModal } =
    useCheckForPayment();

  const { taxAccordionContext = {}, setTaxAccordionContext } =
    useContext(TaxAccordionContext);
  let {
    allItems = {},
    transformedSelectedItems = {},
    showCheckBoxModalOnce,
  } = taxAccordionContext;
  const [total, setTotal] = useState(0);
  const [showModal, setShowModal] = useState(0);

  useEffect(() => {
    const handleTotalChange = () => {
      const newTotal = Object.values(transformedSelectedItems).reduce(
        (sum, item) => {
          return convertToDecimal(sum + item.total);
        },
        0
      );
      setTotal(newTotal);
    };

    const handleShowCheckBoxModalOnce = () => {
      const storageItem = localStorage.getItem("showModal");
      setShowModal(showCheckBoxModalOnce);
      if (!storageItem && showCheckBoxModalOnce === 2) {
        localStorage.setItem("showModal", showCheckBoxModalOnce);
        setShowModal(showCheckBoxModalOnce);
      } else if (Number(storageItem) === 2) {
        setShowModal(storageItem);
      } else {
        setShowModal(showCheckBoxModalOnce);
      }
    };

    handleShowCheckBoxModalOnce();
    handleTotalChange();
  }, [showCheckBoxModalOnce, transformedSelectedItems]);

  // Define the order of the keys
  const order = ["vehicle", "real_estate", "household_waste"];

  // Sort the entries based on the predefined order
  const sortedTaxRecords = taxRecords.sort((a, b) => {
    return order.indexOf(a[0]) - order.indexOf(b[0]);
  });

  return (
    <>
      {CheckForPaymentModal}
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
                {type === TaxesContainerType.PAYMENT ? (
                  <div className="row">
                    <div className={`${styles.infoMessageWrapper} col-12`}>
                      <div className={`${styles.infoMessage} ${styles.imGrey}`}>
                        <img
                          src="/assets/Images/info-icon.svg"
                          alt="Information Icon"
                          className={styles.infoMessageIcon}
                          width="24px"
                          height="24px"
                        />
                        <span className={styles.infoMessageText}>
                          {t("localTaxes.payment.checkbox.modal.description")}
                        </span>
                      </div>
                    </div>
                  </div>
                ) : null}
                {/* Commented out section because of the new design */}
                {/* {type === TaxesContainerType.PAYMENT &&
                !isPhone &&
                taxRecords?.length ? (
                  <PayCta
                    className={"my-4"}
                    checkForInitiatedPayment={checkForInitiatedPayment}
                  />
                ) : null} */}
                <div className="row">
                  {hasAlreadyGeneratedRequests ? (
                    <div
                      className={`col-12 ${styles.infoMessageWrapper} ${
                        type === TaxesContainerType.REFERENCE
                          ? styles.infoMessageWrapperReference
                          : ""
                      }`}
                    >
                      <div className={`${styles.infoMessage} ${styles.imRed}`}>
                        <img
                          src="/assets/Images/pending_payment_request_icon.svg"
                          alt="Pending Payment Icon"
                          className={styles.infoMessageIcon}
                          width="24px"
                          height="24px"
                        />
                        <span className={styles.infoMessageText}>
                          {t("localTaxes.pending.payments.info.message")}
                        </span>
                      </div>
                    </div>
                  ) : null}
                  {!isPhone && sortedTaxRecords?.length ? (
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
                    {sortedTaxRecords?.length ? (
                      sortedTaxRecords.map(([key, value], index) => {
                        if (Object.values(TAX_CATEGORIES).includes(key)) {
                          return (
                            <TaxesAccordion
                              id={index}
                              key={key}
                              data={value}
                              type={key}
                              forceOpenClose={openCloseAll}
                              selectEnabled={
                                type === TaxesContainerType.PAYMENT
                              }
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
                    <PayCta
                      checkForInitiatedPayment={checkForInitiatedPayment}
                    />
                  ) : (
                    <div className="row">
                      <div className="col-12 d-flex justify-content-end">
                        <SmCta
                          type={SmCtaTypes.OUTLINE}
                          {...(hasAlreadyGeneratedRequests
                            ? {
                                onClick: async (e) => {
                                  e.preventDefault();
                                  await checkForInitiatedPayment();

                                  window.location.href =
                                    PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT;
                                },
                              }
                            : {
                                isLink: true,
                                href: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT,
                              })}
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
      {Number(showModal) === 1 ? (
        <Modal
          title={
            <img
              src="/assets/Images/info-icon.svg"
              alt="Information Icon"
              width="40px"
              height="40px"
            />
          }
          message={t(`localTaxes.payment.checkbox.modal.description`)}
          description={t(
            `localTaxes.payment.checkbox.modal.additional.description`
          )}
          borderColor="blue"
          modalOpen={Number(showModal) === 1}
          textAlign="center"
          yesText={t(`localTaxes.payment.checkbox.modal.understand`)}
          onYes={() => {
            setTaxAccordionContext({
              showCheckBoxModalOnce: 2,
            });
          }}
          onNo={() => {
            setTaxAccordionContext({
              transformedSelectedItems: cloneDeep(allItems),
              showCheckBoxModalOnce: 2,
            });
          }}
          showClose={false}
          modalSize="lg"
        />
      ) : null}
    </>
  );
};

const transformData = (taxes) => {
  const mappedData = {};
  let hasAlreadyGeneratedRequests = false;
  Object.entries(taxes).forEach(([type, batchData]) => {
    if (!mappedData[type]) {
      mappedData[type] = {
        batches: {},
        total: batchData.total,
        payOrderGroupList: [],
        minPayOrder: 0,
        maxPayOrder: 0,
        batchesTotals: {},
        batchGroupPayOrderList: {},
        batchGroupHighestPayOrderAllPaymentsList: {},
        missedPayOrders: {},
        allMissedPayOrdersCombined: [],
        onlyOneBatchForType: false,
        onlyCurrentYearBatches: false,
        onlyCurrentYearItemsForEachBatchGroup: {},
      };
      // Populate main data
      Object.entries(batchData.data).forEach(([batchNumber, batchValue]) => {
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

        // Add new object with totals of each batch
        buildBatchesTotals(
          mappedData,
          type,
          batchNumber,
          batchValue,
          totalAlreadyGeneratedRequests
        );

        mappedData[type].total = convertToDecimal(
          mappedData[type].total - totalAlreadyGeneratedRequests
        );

        batchValue.data.map((batchData) => {
          if (
            !batchData.hasPaymentRequest ||
            PAYMENT_RETRY_ENABLED_STATUSES.includes(batchData.status)
          ) {
            // Build batches object with key [partidaNo-payOrder-instNo-txtPeriodYear]
            buildBatches(mappedData, type, batchNumber, batchData);

            // Create group list from available pay orders
            buildPayOrderGroupList(mappedData, type, batchData);

            // Creates batchGroupPayOrderList array for batchNumber
            buildBatchGroupPayOrderList(
              mappedData,
              type,
              batchNumber,
              batchData
            );
          }
        });
        // Find and create property object missing payOrders for each batch group
        findMissingPayOrdersForBatchGroup(mappedData, type, batchNumber);

        // Add property if a tax group has only one batch group
        checkIfOnlyOneBatchForType(mappedData, type, batchNumber);

        // Check if all the batches are from current year
        checkIfOnlyCurrentYearBatches(mappedData, type);

        // Check if all the batches in each batch group are from the current year
        checkIfOnlyCurrentYearItemsInCurrentBatch(
          mappedData,
          type,
          batchNumber
        );
      });

      // Sort the payOrder group list
      mappedData[type].payOrderGroupList.sort((a, b) => a - b);

      // Identify the minimum payOrder
      createMinimumPayOrder(mappedData, type);

      // Identify the maximum payOrder
      createMaximumPayOrder(mappedData, type);

      // Build list for each type with highest payOrder and it's installment numbers
      buildBatchGroupAllHighestPayOrderPaymentsList(mappedData, type);

      // Combine all missed payOrders per each tax group
      combineAllMissedPayOrdersForEachTaxGroup(mappedData, type);
    }
  });
  return { data: mappedData, hasAlreadyGeneratedRequests };
};

const TaxesContainerWrapper = (props) => {
  const { t } = useTranslation();
  const {
    data = {},
    isLoading,
    error,
    resetError,
    fetch: getTaxReference,
  } = useGetTaxReference();
  const { obligations, taxSubject } = data;
  const { data: taxes = {} } = obligations || {};
  const { data: transformedSelectedItems, hasAlreadyGeneratedRequests } =
    transformData(taxes);

  const location = useLocation();
  const history = useHistory();

  useEffect(() => {
    // eslint-disable-next-line
    const unListen = history.listen((location, action) => {
      if (location.pathname !== "/local-taxes-fees/payment") {
        const storageItem = localStorage.getItem("showModal");
        if (storageItem) {
          localStorage.removeItem("showModal");
        }
      }
    });

    // Cleanup the listener on component unmount
    return () => {
      unListen();
    };
  }, [history]);

  useEffect(() => {
    if (location.pathname === "/local-taxes-fees/payment") {
      const storageItem = localStorage.getItem("showModal");
      if (storageItem) {
        localStorage.removeItem("showModal");
      }
    }
  }, [location]);

  if (error) {
    return (
      <Modal
        title={t("form.error.modal.title")}
        message={t("form.error.modal.message")}
        borderColor="red"
        modalOpen={!!error}
        textAlign="center"
        yesText={t("form.document.sign.evrotrust.modal.error.cta")}
        onYes={async () => {
          await getTaxReference();
          resetError();
        }}
        showClose={false}
        showNo={false}
      />
    );
  }

  return data && !isLoading ? (
    <TaxAccordionContextProvider
      defaultState={{
        allItems: cloneDeep(transformedSelectedItems),
        taxSubject: cloneDeep(taxSubject),
        transformedSelectedItems: cloneDeep(transformedSelectedItems),
        showCheckBoxModalOnce: 0,
      }}
    >
      <TaxesContainer
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
