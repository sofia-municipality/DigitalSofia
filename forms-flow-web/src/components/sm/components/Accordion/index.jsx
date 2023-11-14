import React, { useState, useEffect } from "react";
import { Accordion, Button, Card } from "react-bootstrap";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import DOMPurify from "dompurify";

import styles from "./accordion.module.scss";

const AccordionCard = ({ title, content, eventKey, expanded = false }) => {
  const [isExpanded, setIsExpanded] = useState(expanded);
  const accordionHeaderId = `accordion-header-${eventKey}`;
  const accordionContentId = `accordion-panel-${eventKey}`;

  useEffect(() => {
    setIsExpanded(expanded);
  }, [expanded]);

  return (
    <div className={`${styles.smCard} ${isExpanded ? styles.expanded : ""}`}>
      <Card>
        <Card.Header>
          <Accordion.Toggle
            id={accordionHeaderId}
            className={styles.accordionCta}
            as={Button}
            eventKey={eventKey}
            onClick={() => setIsExpanded(!isExpanded)}
            aria-expanded={isExpanded ? "true" : "false"}
            aria-controls={accordionContentId}
          >
            <div className={styles.smCardHeader}>
              <span className={styles.headerTitle}>{title}</span>
              {isExpanded ? <RemoveIcon className={styles.icon} /> : null}
              {!isExpanded ? (
                <AddIcon className={`${styles.icon} ${styles.arrowDown}`} />
              ) : null}
            </div>
            <div className={styles.line} />
          </Accordion.Toggle>
        </Card.Header>
        <Accordion.Collapse eventKey={eventKey}>
          <Card.Body
            as="section"
            id={accordionContentId}
            aria-labelledby={accordionHeaderId}
            dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(content) }}
          />
        </Accordion.Collapse>
      </Card>
    </div>
  );
};

const SmAccordion = ({ className = "", cards = [], openCloseAll = "" }) => {
  const [eventKey, setEventKey] = useState(0);
  const [refreshAccordion, setRefreshAccordion] = useState(0);
  useEffect(() => {
    if (openCloseAll === "open") {
      setEventKey(1);
      setRefreshAccordion(1);
    } else {
      setEventKey(0);
      setRefreshAccordion(1);
    }
  }, [openCloseAll]);

  useEffect(() => {
    if (refreshAccordion === 1) {
      setRefreshAccordion(0);
    }
  }, [refreshAccordion]);
  return (
    <div className={className}>
      {refreshAccordion
        ? ""
        : cards.map((card, index) => (
            <Accordion
              key={index}
              defaultActiveKey={eventKey === 1 ? index + 1 : null}
            >
              <AccordionCard
                eventKey={index + 1}
                expanded={openCloseAll === "open"}
                {...card}
              />
            </Accordion>
          ))}
    </div>
  );
};
export default SmAccordion;
