import React, { useState, useEffect, Fragment } from "react";
import { Button, Row, Col } from "react-bootstrap";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import { getInputByType } from "../../utils";

import styles from "./editableInputs.module.scss";

const mapValueToInputs = (value = [], content, fieldId, props) => {
  const childContent = content[0].content;
  if (value.length) {
    return value.map((item, index) => {
      const elementContent = Array.isArray(childContent)
        ? childContent.map((child) =>
            Object.assign({}, child, {
              fieldId: `${fieldId}_${index}.${child.fieldId}`,
            })
          )
        : childContent;
      return {
        fieldId: `${fieldId}_${index}`,
        type: content[0].type,
        content: elementContent,
        props,
        defaultValue: item?.defaultValue || item || {},
      };
    });
  } else {
    const elementContent = Array.isArray(childContent)
      ? childContent.map((child) =>
          Object.assign({}, child, {
            fieldId: `${fieldId}_0.${child.fieldId}`,
          })
        )
      : childContent;
    return [
      {
        fieldId: `${fieldId}_0`,
        type: content[0].type,
        content: elementContent,
        props,
        defaultValue: {},
      },
    ];
  }
};

const EditableInputs = ({ defaultValue, content, fieldId, props }) => {
  const [inputs, setInputs] = useState([]);

  useEffect(() => {
    const defaultInputs = mapValueToInputs(
      defaultValue,
      content,
      fieldId,
      props
    );
    setInputs(defaultInputs);
  }, [content, defaultValue, fieldId, props]);

  const onAddClick = () => {
    const childContent = content[0].content;
    const elementContent = Array.isArray(childContent)
      ? childContent.map((child) =>
          Object.assign({}, child, {
            fieldId: `${fieldId}_${inputs.length}.${child.fieldId}`,
          })
        )
      : childContent;
    const newInput = {
      fieldId: `${fieldId}_${inputs.length}`,
      type: content[0].type,
      content: elementContent,
      props,
      defaultValue: {},
    };

    setInputs((state) => [...state, newInput]);

    const section = document.getElementById("main");
    section?.scrollTo(0, section.scrollHeight);
  };

  const removeItem = (index) => {
    const element = document.getElementById(`editable-element-group-${index}`);
    element.remove();
  };

  const isWithLabels = inputs?.some(
    (i) => i.label || i.content?.some((c) => c.label)
  );

  return (
    <>
      <Row>
        <Col>
          <Button
            variant="success"
            className={styles.addCta}
            onClick={onAddClick}
          >
            <AddIcon />
          </Button>
        </Col>
      </Row>
      {inputs.map((item, index) => (
        <div key={index} id={`editable-element-group-${index}`}>
          {getInputByType(
            item,
            item.defaultValue,
            inputs.length > 1 ? (
              <RemoveCta
                onClick={() => removeItem(index)}
                isWithLabels={isWithLabels}
              />
            ) : null
          )}
        </div>
      ))}
    </>
  );
};

const RemoveCta = ({ onClick, isWithLabels }) => (
  <div className={`form-group ${isWithLabels ? styles.removeCtaWrapper : ""}`}>
    <Button variant="danger" className={styles.removeCta} onClick={onClick}>
      <RemoveIcon />
    </Button>
  </div>
);

export default EditableInputs;
