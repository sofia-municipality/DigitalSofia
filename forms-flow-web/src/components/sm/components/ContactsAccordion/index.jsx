import React from "react";
import SimpleAccordion from "../Accordion/SimpleAccordion";
import styles from "./contactsAccordion.module.scss";
import { Row, Col } from "react-bootstrap";
import { useTranslation } from "react-i18next";

const ContactsAccordion = ({
  id,
  forceOpenClose,
  data = {},
  onExpand,
  className,
}) => {
  const { t } = useTranslation();

  const TitleComponent = () => (
    <section>
      <Row>
        <Col>
          <a
            href={data.link}
            target="_blank"
            rel="noreferrer"
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.mail")}
          >
            {data.region}
          </a>
        </Col>
      </Row>
    </section>
  );

  const ContentComponent = () => (
    <section className={`${styles.regionContainer}`}>
      <Row>
        <Col>{data.mdtName}</Col>
        <Col>
          <a
            href={`tel:${data.mdtPhone1}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.mdtPhone1}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.mdtPhone2}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.mdtPhone2}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.mdtPhone3}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.mdtPhone3}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.mdtPhone4}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.mdtPhone4}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.mdtPhone5}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.mdtPhone5}
          </a>
        </Col>
      </Row>
      <hr className="hr" />
      <Row>
        <Col>{data.graoName}</Col>
        <Col>
          <a
            href={`tel:${data.graoPhone1}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.graoPhone1}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.graoPhone2}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.graoPhone2}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.graoPhone3}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.graoPhone3}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.graoPhone4}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.graoPhone4}
          </a>
        </Col>
        <Col>
          <a
            href={`tel:${data.graoPhone5}`}
            className="linkSystemBlue"
            aria-label={t("contacts.page.aria.link.tel")}
          >
            {data.graoPhone5}
          </a>
        </Col>
      </Row>
    </section>
  );

  return (
    <SimpleAccordion
      id={id}
      forceOpenClose={forceOpenClose}
      Title={TitleComponent}
      Content={ContentComponent}
      onExpand={onExpand}
      className={className}
    />
  );
};

export default ContactsAccordion;
