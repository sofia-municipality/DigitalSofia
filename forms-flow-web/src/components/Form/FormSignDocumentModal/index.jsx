import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { push } from "connected-react-router";

import { PAGE_ROUTES } from "../../../constants/navigation";
import SignDocumentModal from "../../sm/components/Modal/SignDocumentModal";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";
import {
  APPLICATION_STATUS,
  APPLICATION_STATUS_FORCED_ERROR,
} from "../../../constants/formEmbeddedConstants";

const FormSignDocumentModal = ({
  formRef,
  setFormSubmitCallback,
  onInit,
  handleSubmissionError = false,
}) => {
  const isWebView = localStorage.getItem("hideNav");
  const dispatch = useDispatch();
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const [isSignDocumentTriggered, setIsSignDocumentTriggered] = useState(false);
  const [signDocumentParams, setSignDocumentParams] = useState();
  const [isFormSubmitted, setIsFormSubmitted] = useState(false);

  const onSignDocumentModalClose = () => {
    setIsSignDocumentTriggered(false);
    dispatch(
      push(
        `${
          signDocumentParams.redirectUrl ||
          PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
            ":tenantId",
            tenantKey
          )
        }`
      )
    );
  };

  useEffect(() => {
    const handler = (e) => {
      setSignDocumentParams(e.detail);
      setIsSignDocumentTriggered(true);
    };

    document.addEventListener(CUSTOM_EVENT_TYPE.SIGN_DOCUMENT, handler);

    return () =>
      document.removeEventListener(CUSTOM_EVENT_TYPE.SIGN_DOCUMENT, handler);
  }, []);

  return isSignDocumentTriggered ? (
    <SignDocumentModal
      modalOpen={isSignDocumentTriggered}
      params={signDocumentParams}
      hideStatusModals={isWebView}
      closeModal={() => setIsSignDocumentTriggered(false)}
      onInit={() => {
        if (formRef.current?.data) {
          formRef.current.data.applicationStatus =
            APPLICATION_STATUS.SIGN_DOCUMENT_PENDING;
        }

        onInit && onInit(signDocumentParams);
      }}
      onSubmit={() => {
        return new Promise((resolve, reject) => {
          const formSubmitCallback = (err, res) => {
            if (err) {
              reject(err);
            } else if (
              APPLICATION_STATUS_FORCED_ERROR.includes(res?.applicationStatus)
            ) {
              reject(res?.applicationStatus);
            } else {
              resolve();
              setIsFormSubmitted(true);
            }
          };

          setFormSubmitCallback(formSubmitCallback);
          if (formRef.current?.data) {
            formRef.current.data.applicationStatus =
              APPLICATION_STATUS.SIGN_DOCUMENT_PENDING;

            if (!signDocumentParams.noAutosubmit) {
              formRef.current.emit("submitButton");
            }
          }
        });
      }}
      onSuccess={(signedDocument, shouldKeepApplicationStatus) => {
        return new Promise((resolve, reject) => {
          if (isFormSubmitted) {
            resolve();
          } else {
            const formSubmitCallback = (err, res) => {
              if (err) {
                reject(err);
              } else if (
                APPLICATION_STATUS_FORCED_ERROR.includes(res?.applicationStatus)
              ) {
                reject(res?.applicationStatus);
              } else {
                resolve();
              }
            };
            setFormSubmitCallback(formSubmitCallback);
            if (formRef?.current?.data) {
              if (!shouldKeepApplicationStatus) {
                formRef.current.data.applicationStatus =
                  signDocumentParams.applicationStatus ||
                  APPLICATION_STATUS.FORM_SUBMITTED;
              }

              if (signedDocument && formRef.current.data?.pdfData) {
                formRef.current.data.pdfData.url = signedDocument;
              }
              if (!signDocumentParams.noAutosubmit) {
                formRef.current.emit("submitButton");
              }
            }
          }
        });
      }}
      onSuccessClose={onSignDocumentModalClose}
      onRejectClose={onSignDocumentModalClose}
      onReject={() => {
        return new Promise((resolve, reject) => {
          if (isFormSubmitted) {
            resolve();
          } else {
            const formSubmitCallback = (err) => {
              if (!err) {
                resolve();
              } else {
                reject(err);
              }
            };
            setFormSubmitCallback(formSubmitCallback);
            if (formRef?.current?.data) {
              formRef.current.data.applicationStatus =
                APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE;
              formRef.current.emit("submitButton");
            }
          }
        });
      }}
      onFormSubmissionError={
        handleSubmissionError ? null : () => setIsSignDocumentTriggered(false)
      }
      onFormSubmissionSuccess={() => {
        setIsSignDocumentTriggered(false);
        const showRequestServiceLink = sessionStorage.getItem(
          "showRequestServiceLink"
        );

        const url = showRequestServiceLink
          ? PAGE_ROUTES.REQUEST_SERVICE
          : PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION;

        dispatch(push(url.replace(":tenantId", tenantKey)));
      }}
    />
  ) : null;
};

export default FormSignDocumentModal;
