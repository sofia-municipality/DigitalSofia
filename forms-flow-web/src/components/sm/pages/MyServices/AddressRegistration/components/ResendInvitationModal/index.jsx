import React, { useState } from "react";
import Modal from "react-bootstrap/Modal";

import { STEPS } from "./steps/steps";
import ConfirmStep from "./steps/Confirm";
import SuccessStep from "./steps/Success";
import ErrorStep from "./steps/Error";
import "./resendInvitationModal.scss";

const renderCurrentStep = ({ currentStep, ...rest }) => {
  switch (currentStep) {
    case STEPS.SUCCESS:
      return <SuccessStep {...rest} />;
    case STEPS.ERROR:
      return <ErrorStep {...rest} />;
    default:
      return <ConfirmStep {...rest} />;
  }
};

const ResendInvitationModal = React.memo(
  ({ modalOpen = false, onClose = () => {}, ...rest }) => {
    const [currentStep, setCurrentStep] = useState(STEPS.CONFIRM);

    return currentStep ? (
      <Modal
        aria-labelledby="modal-title"
        show={modalOpen}
        className={`resendInvitationModal resendInvitationModal-${currentStep}`}
      >
        {renderCurrentStep({
          currentStep,
          onClose,
          setCurrentStep,
          ...rest,
        })}
      </Modal>
    ) : null;
  }
);

export default ResendInvitationModal;
