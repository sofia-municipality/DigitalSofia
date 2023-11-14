import React from "react";
import { Modal, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";

const TranslationDeleteModal = ({ isOpen, onClose, onDelete = () => {} }) => {
  const { t } = useTranslation();

  return (
    <Modal show={isOpen} onHide={onClose}>
      <Modal.Header closeButton>
        <Modal.Title>{t("so.translations.delete.modal.title")}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>{t("so.translations.delete.modal.description")}</p>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={onClose}>
          {t("so.translations.delete.modal.cancel.cta")}
        </Button>
        <Button variant="danger" onClick={onDelete}>
          {t("so.translations.delete.modal.delete.cta")}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default TranslationDeleteModal;
