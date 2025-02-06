import React, { useEffect, useState } from "react";
import { Accordion, Card, Button } from "react-bootstrap";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import styles from "./simpleAccordion.module.scss";

const SimpleAccordion = ({
  id = 0,
  forceOpenClose,
  Title,
  ExtraTitleInfo,
  SubTitle,
  Content,
  className,
  // Commented out because of new design
  // expandedClassName = "",
  // borderClassName = "",
  onExpand,
}) => {
  const accordionHeaderId = `accordion-header-${id}`;
  const accordionContentId = `accordion-panel-${id}`;
  const [isExpanded, setIsExpanded] = useState(false);

  useEffect(() => {
    if (forceOpenClose > 0) {
      setIsExpanded(true);
    } else {
      setIsExpanded(false);
    }
  }, [forceOpenClose]);

  return (
    <Accordion activeKey={isExpanded ? "0" : undefined} className={className}>
      {/* Commented out because of new design */}
      {/* <Card
        className={`${styles.accordionCard} ${className} ${borderClassName} ${
          isExpanded ? expandedClassName : ""
        }`}
      > */}
      <Accordion.Toggle
        id={accordionHeaderId}
        className={styles.accordionCta}
        as={Button}
        eventKey="0"
        onClick={() => {
          setIsExpanded(!isExpanded);
          onExpand && onExpand(!isExpanded);
        }}
        aria-expanded={isExpanded ? "true" : "false"}
        aria-controls={accordionContentId}
      >
        <div className={styles.accordionCardHeader}>
          <span className={styles.headerTitle}>
            <Title isExpanded={isExpanded} />
          </span>
          <div className="d-flex align-items-center">
            {ExtraTitleInfo ? <ExtraTitleInfo isExpanded={isExpanded} /> : null}
            {isExpanded ? (
              <KeyboardArrowDownIcon className={styles.icon} />
            ) : null}
            {!isExpanded ? (
              <KeyboardArrowUpIcon className={styles.icon} />
            ) : null}
          </div>
        </div>
        {SubTitle ? <SubTitle isExpanded={isExpanded} /> : null}
      </Accordion.Toggle>
      <Accordion.Collapse eventKey="0">
        <Card.Body
          as="section"
          className={styles.accordionBody}
          id={accordionContentId}
          aria-labelledby={accordionHeaderId}
        >
          <Content isExpanded={isExpanded} />
        </Card.Body>
      </Accordion.Collapse>
      {/* </Card> */}
    </Accordion>
  );
};

export default SimpleAccordion;
