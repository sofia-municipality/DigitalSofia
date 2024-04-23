import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import Modal from "../../sm/components/Modal/Modal";

import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";

const FormSuccessModal = ({ formRef, onInit = () => {} }) => {
  const { t } = useTranslation();
  const [formSuccessModalParams, setFormSuccessModalParams] = useState();
  const [isFormSuccessModalTriggered, setIsFormSuccessModalTriggered] =
    useState(false);

  useEffect(() => {
    const handler = async (e) => {
      const params = e.detail || {};
      setFormSuccessModalParams(params);
      setIsFormSuccessModalTriggered(true);
      onInit(params);
    };

    document.addEventListener(
      CUSTOM_EVENT_TYPE.SUBMIT_FORM_WITH_SUCCESS_MODAL,
      handler
    );

    return () =>
      document.removeEventListener(
        CUSTOM_EVENT_TYPE.SUBMIT_FORM_WITH_SUCCESS_MODAL,
        handler
      );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onFormSuccessModalClose = async () => {
    if (formRef.current) {
      formRef.current.emit("submitButton");
    }

    setIsFormSuccessModalTriggered(false);
  };

  return isFormSuccessModalTriggered ? (
    <Modal
      modalOpen={isFormSuccessModalTriggered}
      title={formSuccessModalParams?.title && t(formSuccessModalParams?.title)}
      message={
        formSuccessModalParams?.message && t(formSuccessModalParams?.message)
      }
      description={
        formSuccessModalParams?.description &&
        t(formSuccessModalParams?.description)
      }
      yesText={
        formSuccessModalParams?.yesText && t(formSuccessModalParams?.yesText)
      }
      iconType={formSuccessModalParams?.iconType}
      iconColor={formSuccessModalParams?.iconColor}
      borderColor={formSuccessModalParams?.borderColor}
      modalSize={formSuccessModalParams?.modalSize}
      textAlign={formSuccessModalParams?.textAlign}
      showNo={false}
      onYes={onFormSuccessModalClose}
    />
  ) : null;
};

export default FormSuccessModal;
