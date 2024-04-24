import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import Modal from "../../sm/components/Modal/Modal";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";
import { useNavigateTo } from "../../../customHooks";
import { PAGE_ROUTES } from "../../../constants/navigation";

const FormErrorModal = () => {
  const { t } = useTranslation();
  const navigateToMyServices = useNavigateTo(
    PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION
  );
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onFormErrorModalClose = async () => {
    formErrorModalParams.onClose
      ? formErrorModalParams.onClose()
      : navigateToMyServices();

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
