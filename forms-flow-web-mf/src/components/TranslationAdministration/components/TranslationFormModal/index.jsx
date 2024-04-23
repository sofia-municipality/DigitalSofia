import React from "react";
import { useSelector } from "react-redux";
import { Modal, Button, Form } from "react-bootstrap";
import { useTranslation } from "react-i18next";

import TextArea from "../TextArea";

const TranslationFormModal = ({
  type = "edit",
  title,
  isOpen,
  onClose,
  data = {},
  onSubmit = () => {},
}) => {
  const { t } = useTranslation();
  const { key, language, translation, id } = data;
  const selectLanguages = useSelector((state) => state.user.selectLanguages);

  const onFormSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData.entries());
    const payload = type === "edit" ? { ...data, id, language, key } : data;
    onSubmit(payload);
  };

  return (
    <Modal show={isOpen} onHide={onClose}>
      <Modal.Header closeButton>
        <Modal.Title>{t(title)}</Modal.Title>
      </Modal.Header>
      <Form onSubmit={onFormSubmit}>
        <Modal.Body>
          <Form.Group controlId="language">
            <Form.Control
              as="select"
              name="language"
              defaultValue={language}
              disabled={type === "edit"}
            >
              {selectLanguages.map(({ label }, index) => (
                <option key={index} value={label}>
                  {label}
                </option>
              ))}
            </Form.Control>
          </Form.Group>
          <Form.Group controlId="key">
            <Form.Label>{t("so.translations.form.field.key.label")}</Form.Label>
            <TextArea name="key" required defaultValue={key} disabled={type === "edit"} />
          </Form.Group>
          <Form.Group controlId="translation">
            <Form.Label>
              {t("so.translations.form.field.translation.label")}
            </Form.Label>
            <TextArea name="translation" defaultValue={translation} />
          </Form.Group>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={onClose}>
            {t("so.translations.form.cancel.cta")}
          </Button>
          <Button variant="primary" type="submit">
            {t(`so.translations.form.${type}.cta`)}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default TranslationFormModal;
