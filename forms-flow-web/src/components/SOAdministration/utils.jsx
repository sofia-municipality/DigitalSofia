import { Form, Row, Col } from "react-bootstrap";
import { Translation } from "react-i18next";

import TextArea from "./components/TextArea";
import FileInput from "./components/FileInput";
import EditableInputs from "./components/EditableInputs";
import RichText from "./components/RichText";

export const getInputByType = (
  { fieldId, type, content, fromApi, props = {} },
  defaultValue,
  actions
) => {
  if (type === "textarea") {
    return (
      <TextArea
        required
        name={fieldId}
        defaultValue={defaultValue}
        props={props}
      />
    );
  } else if (type === "file") {
    return (
      <FileInput name={fieldId} defaultValue={defaultValue} props={props} />
    );
  } else if (type === "object") {
    return (
      <Row>
        {content.map((item, index) => {
          const valueKey = item.fieldId.split(".")[1];
          return (
            <Col key={index} xs={12} lg>
              <Form.Group controlId={item.fieldId}>
                {item.label ? (
                  <Form.Label>
                    <Translation>{(t) => t(item.label)}</Translation>
                  </Form.Label>
                ) : null}
                {getInputByType(item, defaultValue?.[valueKey])}
                <Form.Text />
              </Form.Group>
            </Col>
          );
        })}
        {actions ? (
          <Col xs={12} lg={1}>
            {actions}
          </Col>
        ) : null}
      </Row>
    );
  } else if (type === "array") {
    return (
      <Row>
        {content.map((item, index) => (
          <Col key={index} xs={12} lg>
            <Form.Group controlId={item.fieldId}>
              {item.label ? (
                <Form.Label>
                  <Translation>{(t) => t(item.label)}</Translation>
                </Form.Label>
              ) : null}
              {getInputByType(item, defaultValue?.[index])}
              <Form.Text />
            </Form.Group>
          </Col>
        ))}
        {actions ? (
          <Col xs={12} lg={1}>
            {actions}
          </Col>
        ) : null}
      </Row>
    );
  } else if (type === "array_objects") {
    if (fromApi) {
      return (
        <EditableInputs
          defaultValue={defaultValue}
          props={props}
          fieldId={fieldId}
          content={content}
        />
      );
    }
    return (
      <>
        {content.map((item, index) => (
          <Form.Group key={index} controlId={item.fieldId}>
            {item.label ? (
              <Form.Label>
                <Translation>{(t) => t(item.label)}</Translation>
              </Form.Label>
            ) : null}
            {getInputByType(item, defaultValue?.[index])}
            <Form.Text />
          </Form.Group>
        ))}
      </>
    );
  } else if (type === "richtext") {
    return <RichText name={fieldId} defaultValue={defaultValue} />;
  } else {
    return (
      <Form.Control
        required
        name={fieldId}
        type={type}
        defaultValue={defaultValue}
        {...props}
      />
    );
  }
};
