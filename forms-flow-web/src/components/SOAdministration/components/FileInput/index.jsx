import React, { useState, useEffect } from "react";
import { Form, Row, Col } from "react-bootstrap";

const FileInput = ({ defaultValue, name, props, ...rest }) => {
  const [value, setValue] = useState(defaultValue);

  const onChange = async (e) => {
    setValue(await getBase64(e.target.files[0]));
  };

  useEffect(() => {
    const attachDefaultFile = async () => {
      if (defaultValue) {
        const fileRes = await fetch(defaultValue);
        const fileBlob = await fileRes.blob();
        const file = new File([fileBlob], "default_file");
        file.base64Url = defaultValue;
        let container = new DataTransfer();
        container.items.add(file);
        document.getElementById(name).files = container.files;
      }
    };

    attachDefaultFile();
  }, [defaultValue, name]);

  return (
    <Row>
      <Col xs="auto">
        <img src={value} alt="" width="40" />
      </Col>
      <Col className="d-flex align-items-center">
        <Form.Control
          type="file"
          name={name}
          required
          {...rest}
          {...props}
          onChange={onChange}
        />
      </Col>
    </Row>
  );
};

export const getBase64 = (file) => {
  return new Promise((resolve) => {
    let baseURL = "";
    let reader = new FileReader();
    reader.readAsDataURL(file);

    reader.onload = () => {
      baseURL = reader.result;
      file.base64Url = baseURL;
      resolve(baseURL);
    };
  });
};

export default FileInput;
