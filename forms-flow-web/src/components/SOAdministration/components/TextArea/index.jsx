import React from "react";
import { Form } from "react-bootstrap";

import styles from "./textArea.module.scss";

const MIN_TEXTAREA_HEIGHT = 32;

const TextArea = ({ props, ...rest }) => {
  const textareaRef = React.useRef(null);
  const onTextAreaChange = () => {
    // Reset height - important to shrink on delete
    textareaRef.current.style.height = "inherit";
    // Set height
    textareaRef.current.style.height = `${Math.max(
      textareaRef.current.scrollHeight,
      MIN_TEXTAREA_HEIGHT
    )}px`;
  };

  return (
    <Form.Control
      className={styles.textarea}
      ref={textareaRef}
      as="textarea"
      onChange={onTextAreaChange}
      {...rest}
      {...props}
    />
  );
};

export default TextArea;
