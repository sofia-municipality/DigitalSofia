import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { push } from "connected-react-router";

import { PAGE_ROUTES } from "../../../constants/navigation";
import SignDocumentModal from "../../sm/components/Modal/SignDocumentModal";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";
import { APPLICATION_STATUS } from "../../../constants/formEmbeddedConstants";

const FormSignDocumentModal = ({
  formRef,
  setFormSubmitCallback,
  onInit,
  handleSubmissionError = false,
}) => {
  const dispatch = useDispatch();
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const [isSignDocumentTriggered, setIsSignDocumentTriggered] = useState(false);
  const [signDocumentParams, setSignDocumentParams] = useState();

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
      closeModal={() => setIsSignDocumentTriggered(false)}
      onInit={() => {
        if (formRef.current?.component?.data) {
          formRef.current.component.data.applicationStatus =
            APPLICATION_STATUS.SIGN_DOCUMENT_PENDING;
        }

        onInit && onInit(signDocumentParams);
      }}
      onSuccess={(signedDocument, shouldKeepApplicationStatus) => {
        return new Promise((resolve, reject) => {
          const formSubmitCallback = (err) => {
            if (!err) {
              resolve();
            } else {
              reject(err);
            }
          };
          setFormSubmitCallback(formSubmitCallback);
          if (formRef?.current?.component?.data) {
            if (!shouldKeepApplicationStatus) {
              formRef.current.component.data.applicationStatus =
                signDocumentParams.applicationStatus ||
                APPLICATION_STATUS.FORM_SUBMITTED;
            }

            if (signedDocument) {
              formRef.current.component.data["pdfData"]["url"] = signedDocument;
            }
            if (!signDocumentParams.noAutosubmit) {
              formRef.current.component.emit("submitButton");
            }
          }
        });
      }}
      onSuccessClose={onSignDocumentModalClose}
      onRejectClose={onSignDocumentModalClose}
      onReject={() => {
        return new Promise((resolve, reject) => {
          const formSubmitCallback = (err) => {
            if (!err) {
              resolve();
            } else {
              reject(err);
            }
          };
          setFormSubmitCallback(formSubmitCallback);
          if (formRef?.current?.component?.data) {
            formRef.current.component.data.applicationStatus =
              APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE;
            formRef.current.component.emit("submitButton");
          }
        });
      }}
      onFormSubmissionError={
        handleSubmissionError ? null : () => setIsSignDocumentTriggered(false)
      }
    />
  ) : null;
};

export default FormSignDocumentModal;
