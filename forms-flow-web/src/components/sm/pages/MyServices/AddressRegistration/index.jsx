import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import CachedIcon from "@mui/icons-material/Cached";

import PageContainer from "../../../components/PageContainer";
import CustomBreadcrumbs from "../../../components/Breadcrumbs/CustomBreadcrumbs";
import Pagination from "../../../components/Pagination";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import { APPLICATION_STATUS } from "../../../../../constants/formEmbeddedConstants";
import {
  SM_NEW_DESIGN_ENABLED,
  EPAYMENT_ACCESS_CODE_LOGIN_URL,
} from "../../../../../constants/constants";
import Loading from "../../../../../containers/Loading";
import { deleteApplicationById } from "../../../../../apiManager/services/applicationServices";
import {
  useGetDraftsAndSubmissions,
  useWithdrawApplication,
} from "../../../../../apiManager/apiHooks";
import Modal from "../../../components/Modal/Modal";
import BaseCta from "../../../components/buttons/BaseCta";
import SmCta, {
  SmCtaSizes,
  SmCtaTypes,
} from "../../../components/buttons/SmCta";

import { useGetFullAddress } from "./hooks";
import AddressRegistrationCard from "./components/AddressRegistrationCard";
import ResendInvitationModal from "./components/ResendInvitationModal";
import styles from "./myServices.addressRegistration.module.scss";

const limit = 10;

const getPersonNames = (data) => {
  let firstName;
  let middleName;
  let lastName;
  if (data?.behalf === "child") {
    firstName = data?.childFirstName;
    middleName = data?.childMiddleName;
    lastName = data?.childLastName;
  } else if (data?.behalf === "otherPerson") {
    firstName = data?.otherPersonFirstName;
    middleName = data?.otherPersonMiddleName;
    lastName = data?.otherPersonLastName;
  } else {
    firstName = data?.firstName;
    middleName = data?.middleName;
    lastName = data?.lastName;
  }

  return firstName || middleName || lastName
    ? firstName + " " + middleName + " " + lastName
    : null;
};

const MyServicesAddressRegistration = () => {
  const { t } = useTranslation();
  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );
  const getFullAddress = useGetFullAddress();
  const {
    fetch: withdrawInvitation,
    isLoading: isWithdrawInvitationLoading,
    error: withdrawInvitationError,
    resetError: resetWithdrawInvitationError,
  } = useWithdrawApplication();
  const [currentPage, setCurrentPage] = useState(1);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteError, setDeleteError] = useState();
  const [isDeleteInProgress, setIsDeleteInProgress] = useState(false);
  const [notification, setNotification] = useState();
  const [isResendInvitationModalOpen, setIsResendInvitationModalOpen] =
    useState();
  const [isWithdrawInvitationModalOpen, setIsWithdrawInvitationModalOpen] =
    useState();
  const [isPayModalOpen, setIsPayModalOpen] = useState(false);
  const [paymentCode, setPaymentCode] = useState(false);
  const [attemptedDeleteId, setAttemptedDeleteId] = useState(null);
  const [resendInvitationParams, setResendInvitationParams] = useState(null);
  const [withdrawInvitationParams, setWithdrawInvitationParams] =
    useState(null);
  const statuses = Object.values(APPLICATION_STATUS);
  const {
    fetch: fetchServices,
    data: { items = [], total: totalItems } = {},
    isLoading,
  } = useGetDraftsAndSubmissions();

  useEffect(() => {
    fetchServices({ pageNo: currentPage, limit });
  }, [fetchServices, currentPage]);

  const setPaginationPage = (index) => {
    setCurrentPage(index);
    document.getElementById("app").scrollTo(0, 0);
  };

  const data = items.filter(
    (item) =>
      statuses.includes(item.formioData?.applicationStatus) ||
      statuses.includes(item.status)
  );

  const refetchServices = () => {
    if (items.length === 1 && currentPage > 1) {
      setPaginationPage(currentPage - 1);
    } else {
      fetchServices({ pageNo: currentPage, limit });
      document.getElementById("app").scrollTo(0, 0);
    }
  };

  const onDeleteConfirm = async () => {
    setIsDeleteInProgress(true);
    try {
      await deleteApplicationById(attemptedDeleteId);
      setIsDeleteModalOpen(false);
      setAttemptedDeleteId(null);
      setDeleteError(null);
      setIsDeleteInProgress(false);
      refetchServices();
      setNotification(t("screen.reader.status.delete.success"));
    } catch (err) {
      setDeleteError(err);
      setIsDeleteInProgress(false);
    }
  };

  const onDeleteModalClose = () => {
    setIsDeleteModalOpen(false);
    setDeleteError(null);
  };

  const onWithdrawInvitationConfirm = async () => {
    const { applicationId, invitee } = withdrawInvitationParams;
    await withdrawInvitation({ applicationId, role: invitee });

    onWithdrawModalClose();
    refetchServices();
    setNotification(t("screen.reader.status.withdraw.success"));
  };

  const onWithdrawModalClose = () => {
    setIsWithdrawInvitationModalOpen(false);
    setWithdrawInvitationParams(null);
    resetWithdrawInvitationError();
  };

  return (
    <PageContainer>
      <div
        role="status"
        aria-live="polite"
        style={{ fontSize: 0 }}
        className="p-0 m-0"
      >
        {notification}
      </div>
      <div className={styles.addressWrapper}>
        <div className={styles.addressContainer}>
          {!isLoading ? (
            <>
              <Modal
                title={t("myServices.delete.modal.title")}
                message={t(
                  deleteError
                    ? "myServices.delete.modal.error.message"
                    : "myServices.delete.modal.message"
                )}
                borderColor={deleteError ? "red" : "blue"}
                isLoading={isDeleteInProgress}
                modalOpen={isDeleteModalOpen}
                onYes={deleteError ? onDeleteModalClose : onDeleteConfirm}
                onNo={onDeleteModalClose}
                showClose={true}
                showNo={!deleteError}
              />
              {isWithdrawInvitationModalOpen ? (
                <Modal
                  title={t("myServices.withdraw.modal.title")}
                  message={t(
                    withdrawInvitationError
                      ? "myServices.withdraw.modal.error.message"
                      : "myServices.withdraw.modal.message"
                  )}
                  borderColor={withdrawInvitationError ? "red" : "blue"}
                  isLoading={isWithdrawInvitationLoading}
                  modalOpen={isWithdrawInvitationModalOpen}
                  onYes={
                    withdrawInvitationError
                      ? onWithdrawModalClose
                      : onWithdrawInvitationConfirm
                  }
                  onNo={onWithdrawModalClose}
                  showClose={true}
                  showNo={!withdrawInvitationError}
                />
              ) : null}
              {isPayModalOpen ? (
                <Modal
                  title={t("myServices.pay.modal.title")}
                  message={(() => (
                    <span>
                      {t("myServices.pay.modal.paymentCode")}
                      <strong>{paymentCode}</strong>
                    </span>
                  ))()}
                  description={t("myServices.pay.modal.description")}
                  textAlign="center"
                  borderColor="green"
                  modalOpen={isPayModalOpen}
                  yesText={t("myServices.pay.modal.cta")}
                  onYes={() => {
                    window.location.href = `${EPAYMENT_ACCESS_CODE_LOGIN_URL}?code=${paymentCode}`;
                  }}
                  noText={t("myServices.pay.modal.back")}
                  onNo={() => {
                    setPaymentCode(null);
                    setIsPayModalOpen(false);
                  }}
                />
              ) : null}
              {isResendInvitationModalOpen ? (
                <ResendInvitationModal
                  modalOpen={isResendInvitationModalOpen}
                  onClose={() => setIsResendInvitationModalOpen(false)}
                  {...resendInvitationParams}
                />
              ) : null}
              <CustomBreadcrumbs
                className={styles.breadCrumbs}
                link={PAGE_ROUTES.MY_SERVICES}
                linkText={t("myServices.backLinkText")}
                title={t("addressRegistratrion.title")}
              />
              <div className={styles.refreshCtaWrapper}>
                <SmCta
                  size={SmCtaSizes.MEDIUM}
                  type={SmCtaTypes.OUTLINE}
                  className={styles.refreshCta}
                  onClick={refetchServices}
                >
                  <span className="sm-cta-outline-underline">
                    {t("refreshData.cta")}
                  </span>
                  <CachedIcon
                    className={styles.refreshCtaIcon}
                    width="20"
                    height="15"
                  />
                </SmCta>
              </div>
              <div className={styles.contentWrapper}>
                {data?.length ? (
                  <ul
                    className={styles.servicesWrapper}
                    aria-label={`${t("scrren.reader.my.services.list")}${
                      totalItems > limit
                        ? `. ${t(
                            "screen.reader.pagination.navigation.current"
                          )} ${currentPage}`
                        : ""
                    }`}
                  >
                    {data.map((item, index) => (
                      <li key={index} className="m-0">
                        <AddressRegistrationCard
                          id={index}
                          draftId={item.draftId || item.formioData?.draftId}
                          applicationId={item.applicationId}
                          formId={item.formioFormId}
                          submissionId={item.formioSubmissionId}
                          behalf={item.formioData?.behalf}
                          names={getPersonNames(item.formioData)}
                          formName={item.formioName}
                          formPath={item.path}
                          status={
                            item.status === APPLICATION_STATUS.SUBMISSION_ERROR
                              ? item.status
                              : item.formioData?.applicationStatus ||
                                item.status
                          }
                          formioApplicationStatus={
                            item.formioData?.applicationStatus
                          }
                          entryNumber={item.formioData?.reference_number}
                          resultingCertificateUrl={
                            item.formioData?.resultingCertificateUrl
                          }
                          address={getFullAddress(
                            item.formioData?.address,
                            item.formioData?.streetNumber,
                            item.formioData?.region,
                            item.formioData?.entrance,
                            item.formioData?.floorNumber,
                            item.formioData?.appartmentNumber
                          )}
                          property={item.formioData?.property}
                          childCustody={item.formioData?.childCustody}
                          trusteeFirstName={item.formioData?.trusteeFirstName}
                          trusteeLastName={item.formioData?.trusteeLastName}
                          ownerFirstName={
                            item.formioData?.propertyOwnerFirstName
                          }
                          ownerLastName={item.formioData?.propertyOwnerLastName}
                          ownerPdfUrl={item.formioData?.propertyOwnerPdfUrl}
                          ownerSignutureDate={
                            item.formioData?.propertyOwnerSignutureDate
                          }
                          trusteePdfUrl={item.formioData?.trusteePdfUrl}
                          trusteeSignitureDate={
                            item.formioData?.trusteeSignitureDate
                          }
                          ownerRejectionDate={
                            item.formioData?.propertyOwnerRejectionDate
                          }
                          trusteeRejectionDate={
                            item.formioData?.trusteeRejectionDate
                          }
                          ownerInvitationExpiredDate={
                            item.formioData?.propertyOwnerInvitationExpiredDate
                          }
                          trusteeInvitationExpiredDate={
                            item.formioData?.trusteeInvitationExpiredDate
                          }
                          ownerInvitationWithdrawnDate={
                            item.formioData
                              ?.propertyOwnerInvitationWithdrawnDate
                          }
                          trusteeInvitationWithdrawnDate={
                            item.formioData?.trusteeInvitationWithdrawnDate
                          }
                          paymentCode={
                            item?.camundaData?.paymentAccessCode?.value
                          }
                          submitterTaskId={item.formioData?.submitterTaskId}
                          processInstanceId={item.processInstanceId}
                          onDelete={(id) => {
                            setAttemptedDeleteId(id);
                            setIsDeleteModalOpen(true);
                          }}
                          onInvitationResend={(params) => {
                            setResendInvitationParams(params);
                            setIsResendInvitationModalOpen(true);
                          }}
                          onWithdraw={(params) => {
                            setWithdrawInvitationParams(params);
                            setIsWithdrawInvitationModalOpen(true);
                          }}
                          onSubmissionResend={() => {
                            refetchServices();
                          }}
                          onPayInitiated={(code) => {
                            setPaymentCode(code);
                            setIsPayModalOpen(true);
                          }}
                        />
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className={styles.noServicesFound}>
                    <span className={styles.noServicesText}>
                      {t("myServices.noServices")}
                    </span>
                  </div>
                )}
                {totalItems > limit ? (
                  <div className="container-fluid">
                    <div className="row">
                      <div className="col-12 mt-4">
                        <Pagination
                          limit={limit}
                          totalItems={totalItems}
                          selectedPage={currentPage}
                          changePage={setPaginationPage}
                        ></Pagination>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            </>
          ) : (
            <Loading />
          )}
        </div>
        {SM_NEW_DESIGN_ENABLED && showRequestServiceLink ? (
          <div className={styles.stickyMobileBottomNav}>
            <BaseCta
              className={styles.stickyMobileBottomNavLink}
              isLink
              href={PAGE_ROUTES.REQUEST_SERVICE}
            >
              <img
                width="40px"
                height="40px"
                className={styles.stickyMobileBottomNavPageIcon}
                alt=""
                src="/assets/Images/request-service-link-icon.svg"
              />
              <span>{t("myServices.link")}</span>
            </BaseCta>
          </div>
        ) : null}
      </div>
    </PageContainer>
  );
};

export default MyServicesAddressRegistration;
