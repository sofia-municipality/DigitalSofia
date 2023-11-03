import React, { useEffect, useState } from "react";

import SignDocumentModal from "../SignDocumentModal";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";

const FormSignDocumentModal = ({ formRef }) => {
  const [isSignDocumentTriggered, setIsSignDocumentTriggered] = useState(false);
  const [signDocumentParams, setSignDocumentParams] = useState();

  const onSignDocumentModalClose = () => {
    setIsSignDocumentTriggered(false);
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
      onSuccess={(signedDocument) => {
        return new Promise((resolve) => {
          if (formRef?.current?.component?.data && signedDocument) {
            formRef.current.component.data["pdfData"]["url"] = signedDocument;
          }

          resolve();
        });
      }}
      onSuccessClose={onSignDocumentModalClose}
    />
  ) : null;
};

export default FormSignDocumentModal;
