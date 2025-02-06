import React, { useState, useEffect, Fragment } from "react";
import { Button, Row, Col, Form } from "react-bootstrap";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import { Translation } from "react-i18next";

import styles from "./editableInputGroups.module.scss";

const mapValueToInputGroups = (value = [], content, fieldId, props) => {
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

const EditableInputGroups = ({ defaultValue, content, fieldId, props }) => {
  const [inputGroups, setInputGroups] = useState([]);

  useEffect(() => {
    const defaultInputs = mapValueToInputGroups(
      defaultValue,
      content,
      fieldId,
      props
    );
    setInputGroups(defaultInputs);
  }, [content, defaultValue, fieldId, props]);

  const onAddClick = () => {
    const childContent = content[0].content;
    const elementContent = Array.isArray(childContent)
      ? childContent.map((child) =>
          Object.assign({}, child, {
            fieldId: `${fieldId}_${inputGroups.length}.${child.fieldId}`,
          })
        )
      : childContent;
    const newGroup = {
      fieldId: `${fieldId}_${inputGroups.length}`,
      type: content[0].type,
      content: elementContent,
      props,
      defaultValue: {},
    };

    setInputGroups((state) => [...state, newGroup]);

    const section = document.getElementById("main");
    section?.scrollTo(0, section.scrollHeight);
  };

  const removeItem = (index) => {
    const element = document.getElementById(`editable-element-group-${index}`);
    element.remove();
  };

  const isWithLabels = inputGroups?.some(
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
      {inputGroups.map((item, index) => (
        <Row id={`editable-element-group-${index}`} key={index}>
          <Col xs={11}>
            <div className={`${styles.editableGroupContainer}`}>
              <Row>
                <Col>
                  {getInput(
                    index,
                    item,
                    "region",
                    true,
                    "Въведете име на район"
                  )}
                </Col>
                <Col>
                  {getInput(index, item, "link", true, "https://raion.bg")}
                </Col>
              </Row>
              <div className={`${styles.editableGroupContainer}`}>
                <Row>
                  <Col>
                    {getInput(
                      index,
                      item,
                      "mdtName",
                      true,
                      'МДТ "Име на район"'
                    )}
                  </Col>
                  <Col>
                    {getInput(index, item, "mdtPhone1", true, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "mdtPhone2", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "mdtPhone3", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "mdtPhone4", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "mdtPhone5", false, "020000000")}
                  </Col>
                </Row>
                <hr className="hr" />
                <Row>
                  <Col>
                    {getInput(
                      index,
                      item,
                      "graoName",
                      true,
                      'ГРАО "Име на район"'
                    )}
                  </Col>
                  <Col>
                    {getInput(index, item, "graoPhone1", true, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "graoPhone2", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "graoPhone3", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "graoPhone4", false, "020000000")}
                  </Col>
                  <Col>
                    {getInput(index, item, "graoPhone5", false, "020000000")}
                  </Col>
                </Row>
              </div>
            </div>
          </Col>
          <Col xs={1}>
            {inputGroups.length > 1 ? (
              <RemoveCta
                onClick={() => removeItem(index)}
                isWithLabels={isWithLabels}
              />
            ) : null}
          </Col>
        </Row>
      ))}
    </>
  );
};

const getInput = (
  index,
  item,
  fieldName,
  required = true,
  placeholder = ""
) => {
  const extractedItem = item.content.find(
    (contentItem) => contentItem.fieldId === `${item.fieldId}.${fieldName}`
  );
  const value = item.defaultValue[fieldName];
  return (
    <Form.Group key={index} controlId={`${item.fieldId}.${fieldName}`}>
      <Form.Label>
        <Translation>{(t) => t(extractedItem.label)}</Translation>
      </Form.Label>
      <Form.Control
        required={required}
        name={extractedItem.fieldId}
        type={extractedItem.type}
        defaultValue={value}
        placeholder={placeholder}
      />
      <Form.Text />
    </Form.Group>
  );
};

const RemoveCta = ({ onClick, isWithLabels }) => (
  <div className={`form-group ${isWithLabels ? styles.removeCtaWrapper : ""}`}>
    <Button variant="danger" className={styles.removeCta} onClick={onClick}>
      <RemoveIcon />
    </Button>
  </div>
);

export default EditableInputGroups;
