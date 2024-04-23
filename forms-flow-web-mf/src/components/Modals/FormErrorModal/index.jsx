import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import Modal from "../Modal";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";

const FormErrorModal = () => {
  const { t } = useTranslation();
  const [formErrorModalParams, setFormErrorModalParams] = useState();
  const [isFormErrorModalTriggered, setIsFormErrorModalTriggered] =
    useState(false);

  useEffect(() => {
    const handler = async (e) => {
      const params = e.detail || {};
      setFormErrorModalParams(params);
      setIsFormErrorModalTriggered(true);
    };

    document.addEventListener(CUSTOM_EVENT_TYPE.OPEN_FORM_ERROR_MODAL, handler);

    return () =>
      document.removeEventListener(
        CUSTOM_EVENT_TYPE.OPEN_FORM_ERROR_MODAL,
        handler
      );
  }, []);

  const onFormErrorModalClose = async () => {
    formErrorModalParams.onClose
      && formErrorModalParams.onClose();
     
    setIsFormErrorModalTriggered(false);
  };

  return isFormErrorModalTriggered ? (
    <Modal
      modalOpen={isFormErrorModalTriggered}
      title={t(formErrorModalParams?.title || "form.error.modal.title")}
      message={t(formErrorModalParams?.message || "form.error.modal.message")}
      description={
        formErrorModalParams?.description &&
        t(formErrorModalParams?.description)
      }
      yesText={
        formErrorModalParams?.yesText && t(formErrorModalParams?.yesText)
      }
      iconType={formErrorModalParams?.iconType || "close"}
      iconColor={formErrorModalParams?.iconColor || "red"}
      borderColor={formErrorModalParams?.borderColor || "red"}
      modalSize={formErrorModalParams?.modalSize}
      textAlign={formErrorModalParams?.textAlign}
      showNo={false}
      onYes={onFormErrorModalClose}
    />
  ) : null;
};

export default FormErrorModal;
