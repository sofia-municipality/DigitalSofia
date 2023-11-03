import React, { useState } from "react";
import { Modal, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";

export const optionsConfig = {
  VALUE_CHANGED: 1,
  ALL: 2,
};

const useGetSelectOptions = () => {
  const { t } = useTranslation();

  return [
    {
      label: t("so.translations.options.select.value.valueChanged"),
      value: optionsConfig.VALUE_CHANGED,
    },
    {
      label: t("so.translations.options.select.value.all"),
      value: optionsConfig.ALL,
    },
  ];
};

const TranslationUploadOptionsModal = ({ isOpen, onClose, onConfirm }) => {
  const { t } = useTranslation();
  const options = useGetSelectOptions();
  const [selected, setSelected] = useState(optionsConfig.VALUE_CHANGED);

  const handleChange = (e) => {
    setSelected(e.target.value);
  };

  return (
    <Modal show={isOpen} onHide={onClose}>
      <Modal.Header closeButton>
        <Modal.Title>
          {t("so.translations.upload.options.modal.title")}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>{t("so.translations.upload.options.modal.description")}</p>
        <div className="input-group">
          <select
            className="form-control select mr-2 mt-2"
            title={t("so.translations.options.select.title")}
            onChange={(e) => {
              handleChange(e);
            }}
            aria-label={t("so.translations.options.select.title")}
          >
            {options.map(({ label, value }, index) => (
              <option key={index + 1} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="primary" onClick={() => onConfirm(selected)}>
          {t("modal.cta.confirm")}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default TranslationUploadOptionsModal;
