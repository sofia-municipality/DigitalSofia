import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";

import { checkForPayment } from "../../apiManager/services/taxServices";
import { useApiCall } from "../../apiManager/apiHooks/common";
import Modal from "../../components/sm/components/Modal/Modal";
import { useLogin } from "../user";
import { EPAYMENT_ACCESS_CODE_LOGIN_URL } from "../../constants/constants";

const useCheckForPayment = () => {
  const login = useLogin();
  const { t } = useTranslation();
  const { error, fetch, resetError, isLoading } = useApiCall(
    checkForPayment,
    {},
    false
  );

  useEffect(() => {
    if (error?.response?.status === 401) {
      login();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [error]);

  return {
    fetch: fetch,
    error,
    isLoading,
    CheckForPaymentModal:
      error && error.response.status !== 401 ? (
        <Modal
          title={
            <img
              src="/assets/Images/pending_payment_request_icon.svg"
              alt="Pending Payment Icon"
              width="20px"
              height="20px"
            />
          }
          message={t(`form.error.modal.${error?.response.data.key}`)}
          borderColor="red"
          modalOpen={!!error}
          textAlign="center"
          yesText={t(
            `${
              error?.response?.data?.key === "pending_payment_request"
                ? "form.document.sign.epayment.modal.error.cta"
                : "modal.cta.confirm"
            }`
          )}
          onYes={() => {
            if (error?.response?.data?.key === "pending_payment_request") {
              window.location.href = `${EPAYMENT_ACCESS_CODE_LOGIN_URL}?code=${
                error?.response?.data?.accessCode || ""
              }`;
            } else {
              resetError();
            }
          }}
          onNo={() => resetError()}
          showClose={false}
          showNo={error?.response?.data?.key === "pending_payment_request"}
        />
      ) : null,
  };
};

export default useCheckForPayment;
