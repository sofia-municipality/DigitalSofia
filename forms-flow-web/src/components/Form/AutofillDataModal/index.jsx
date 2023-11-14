import React, { useEffect, useState, useRef } from "react";
import { useTranslation } from "react-i18next";
import Modal from "../../sm/components/Modal/Modal";

import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";
import { useAutofillPersonalData } from "../../../apiManager/apiHooks";

const AutofillDataModal = ({ formRef }) => {
  const { t } = useTranslation();
  const autoFillElement = useRef();
  const [isAutofillTriggered, setIsAutofillTriggered] = useState(false);
  const [autoFillRequestData, setAutoFillRequestData] = useState();
  const [autoFillErrorMessage, setAutoFillErrorMessage] = useState();
  const [notification, setNotification] = useState();

  const {
    fetch: fetchAutofillData,
    isLoading: isAutofillDataLoading,
    error: autofillError,
  } = useAutofillPersonalData();

  const onAutofillConfirm = async () => {
    const data = await fetchAutofillData(autoFillRequestData);
    if (data) {
      Object.entries(data).forEach(([key, value]) => {
        if (formRef.current?.component?.data) {
          formRef.current.component.data[key] = value;
        }
      });

      formRef.current.component.root.triggerRedraw();

      setIsAutofillTriggered(false);
      setTimeout(() => {
        const element = document.getElementById(autoFillElement.current.id);

        const input = element.getElementsByTagName("input")[0];
        input.focus();
      }, 300);

      setNotification(t("screen.reader.status.autofill.completed"));
    }
  };

  const onAutofillDecline = () => {
    autoFillElement.current.input.click();
    setAutoFillErrorMessage(null);
    setIsAutofillTriggered(false);
  };

  useEffect(() => {
    const handler = (e) => {
      setAutoFillRequestData(e.detail);
      autoFillElement.current = e.target.component;
      setIsAutofillTriggered(true);
    };

    document.addEventListener(CUSTOM_EVENT_TYPE.AUTO_FILL_DATA, handler);

    return () =>
      document.removeEventListener(CUSTOM_EVENT_TYPE.AUTO_FILL_DATA, handler);
  }, []);

  useEffect(() => {
    if (autofillError) {
      setAutoFillErrorMessage(autofillError);
    }
  }, [autofillError]);

  return (
    <>
      <div
        role="status"
        aria-live="polite"
        style={{ fontSize: 0 }}
        className="p-0 m-0"
      >
        {notification}
      </div>
      <Modal
        isLoading={isAutofillDataLoading}
        modalOpen={isAutofillTriggered}
        title={
          autoFillErrorMessage
            ? t("autofill.modal.error.title")
            : t("autofill.modal.title")
        }
        message={
          autoFillErrorMessage
            ? t(autoFillErrorMessage)
            : t("autofill.modal.description")
        }
        onNo={onAutofillDecline}
        onYes={autoFillErrorMessage ? onAutofillDecline : onAutofillConfirm}
        showNo={!autoFillErrorMessage}
      />
    </>
  );
};

export default AutofillDataModal;
