import React from "react";
import { Form, Button, Row, Col } from "react-bootstrap";
import { useTranslation } from "react-i18next";

import TextArea from "../../components/TextArea";
import RichText from "../../components/RichText";

import styles from "./faqForm.module.scss";

const FAQForm = ({
  title = "",
  content = "",
  isFavoured = false,
  onSubmit = () => {},
}) => {
  const { t } = useTranslation();
  const onFormSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const payload = Object.fromEntries(formData.entries());
    payload.isFavoured = !!payload.isFavoured;
    onSubmit(payload);
  };

  return (
    <Form className={styles.form} onSubmit={onFormSubmit}>
      <Form.Group controlId="title">
        <Form.Label>{t("faqForm.question")}</Form.Label>
        <TextArea name="title" required defaultValue={title} />
      </Form.Group>
      <Form.Group controlId="title">
        <Form.Label>{t("faqForm.answer")}</Form.Label>
        <RichText name="content" defaultValue={content} />
      </Form.Group>
      <Form.Group controlId="isFavoured">
        <Form.Check
          type="checkbox"
          name="isFavoured"
          label={t("faqForm.isFavoured")}
          defaultChecked={isFavoured}
        />
      </Form.Group>
      <Row>
        <Col className={styles.submitCtaWrapper}>
          <Button variant="primary" type="submit">
            {t("form.submitCta")}
          </Button>
        </Col>
      </Row>
    </Form>
  );
};

export default FAQForm;
