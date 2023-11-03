import React, { useState } from "react";
import { Accordion, Card, Button } from "react-bootstrap";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";

import styles from "./addressRegistrationCardAccordion.module.scss";

const AddressRegistrationCardAccordion = ({
  id,
  title,
  content,
  className,
  borderClassName,
}) => {
  const accordionHeaderId = `accordion-header-${id}`;
  const accordionContentId = `accordion-panel-${id}`;
  const [isExpanded, setIsExpanded] = useState(false);
  return (
    <Accordion>
      <Card
        className={`${styles.accordionCard} ${className} ${borderClassName}`}
      >
        <Accordion.Toggle
          id={accordionHeaderId}
          className={styles.accordionCta}
          as={Button}
          eventKey="0"
          onClick={() => setIsExpanded(!isExpanded)}
          aria-expanded={isExpanded ? "true" : "false"}
          aria-controls={accordionContentId}
        >
          <div className={styles.accordionCardHeader}>
            <span className={styles.headerTitle}>{title}</span>
            {isExpanded ? <RemoveIcon className={styles.icon} /> : null}
            {!isExpanded ? <AddIcon className={styles.icon} /> : null}
          </div>
        </Accordion.Toggle>
        <Accordion.Collapse eventKey="0">
          <Card.Body
            as="section"
            className={styles.accordionBody}
            id={accordionContentId}
            aria-labelledby={accordionHeaderId}
          >
            {content}
          </Card.Body>
        </Accordion.Collapse>
      </Card>
    </Accordion>
  );
};

export default AddressRegistrationCardAccordion;
