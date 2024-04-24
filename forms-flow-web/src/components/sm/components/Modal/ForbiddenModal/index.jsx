import React from "react";
import { useSelector, useDispatch } from "react-redux";
import Modal from "react-bootstrap/Modal";

import { openCloseForbiddenModal } from "../../../../../actions/roleActions";
import {
  ForeignerForbiddenContent,
  AssuranceLevelContent,
  ChildFormInProgressContent,
} from "./content";
import styles from "./forbidden.modal.module.scss";
import "./forbidden.modal.scss";

export const ForbiddenModalTypes = {
  FOREIGNER_FORBIDDEN: "FOREIGNER_FORBIDDEN",
  ASSURANCE_LEVEL: "ASSURANCE_LEVEL",
  CHILD_FORM_IN_PROGRESS: "CHILD_FORM_IN_PROGRESS",
};

const useModalContent = (type) => {
  switch (type) {
    case ForbiddenModalTypes.FOREIGNER_FORBIDDEN:
      return ForeignerForbiddenContent;
    case ForbiddenModalTypes.ASSURANCE_LEVEL:
      return AssuranceLevelContent;
    case ForbiddenModalTypes.CHILD_FORM_IN_PROGRESS:
      return ChildFormInProgressContent;
    default:
      return null;
  }
};

const ForbiddenModal = () => {
  const dispatch = useDispatch();
  const {
    isOpen,
    type = ForbiddenModalTypes.FOREIGNER_FORBIDDEN,
    requiredAssuranceLevel,
  } = useSelector((state) => state.user.forbiddenModal) || {};

  const Content = useModalContent(type);
  const onClose = () => {
    dispatch(openCloseForbiddenModal({ isOpen: false }));
  };

  return isOpen ? (
    <Modal
      show={isOpen}
      className="foreignerForbbidenModal"
      aria-labelledby="modal-title"
    >
      <div className={styles.modalCloseCta}></div>
      <div className={styles.modalContentContainer}>
        <Content
          onClose={onClose}
          requiredAssuranceLevel={requiredAssuranceLevel}
        />
      </div>
    </Modal>
  ) : null;
};

export default ForbiddenModal;
