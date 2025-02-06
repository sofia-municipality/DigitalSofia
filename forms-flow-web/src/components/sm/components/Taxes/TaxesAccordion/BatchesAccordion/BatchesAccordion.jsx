import React, { useEffect, useState } from "react";
import { Accordion, Button } from "react-bootstrap";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import styles from "./batchesAccordion.module.scss";

const BatchesAccordion = ({
  id = 0,
  forceOpenClose,
  Title,
  Content,
  className,
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
            {isExpanded ? (
              <KeyboardArrowUpIcon className={styles.icon} />
            ) : null}
            {!isExpanded ? (
              <KeyboardArrowDownIcon className={styles.icon} />
            ) : null}
          </div>
        </div>
      </Accordion.Toggle>
      <Accordion.Collapse eventKey="0">
        <Content isExpanded={isExpanded} className={styles.innerContent} />
      </Accordion.Collapse>
    </Accordion>
  );
};

export default BatchesAccordion;
