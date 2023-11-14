import React from "react";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";
import UpdateIcon from "@mui/icons-material/Update";
import VerifiedOutlinedIcon from "@mui/icons-material/VerifiedOutlined";

import styles from "./modal.module.scss";
import "./modal.scss";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../buttons/SmCta";

const IconTypes = {
  CLOSE: "close",
  PENDING: "pending",
  SUCCESS: "sucess",
};

const Icons = {
  [IconTypes.CLOSE]: CloseIcon,
  [IconTypes.PENDING]: UpdateIcon,
  [IconTypes.SUCCESS]: VerifiedOutlinedIcon,
};

const SmModal = React.memo((props) => {
  const { t } = useTranslation();
  const {
    isLoading = false,
    modalOpen = false,
    onYes,
    onNo,
    message,
    description,
    yesText = t("modal.cta.confirm"),
    noText = t("modal.cta.cancel"),
    title,
    showYes = true,
    showNo = true,
    showClose = false,
    iconType,
    iconColor = "red",
    borderColor = "blue",
    modalSize,
    textAlign,
  } = props;

  const Icon = iconType && Icons[iconType];

  return (
    <Modal
      size={modalSize}
      show={modalOpen}
      aria-labelledby="modal-title"
      className={`smModal ${borderColor ? `smModal-${borderColor}` : ""} ${
        isLoading ? "smModal-loading" : ""
      }`}
    >
      <div className={styles.modalCloseCta}>
        {showNo || showClose ? (
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.OUTLINE}
            onClick={onNo}
            disabled={isLoading}
            accessibilityProps={{
              "aria-label": t("screen.reader.modal.close.cta"),
            }}
          >
            <CloseIcon />
          </SmCta>
        ) : null}
      </div>
      <div className={styles.modalContentContainer}>
        {title ? (
          <Modal.Header
            className={`${styles.modalHeader} ${
              textAlign ? styles[`modalHeader-${textAlign}`] : ""
            }`}
          >
            <Modal.Title id="modal-title">{title}</Modal.Title>
          </Modal.Header>
        ) : null}
        <Modal.Body
          className={`${styles.modalBody} ${
            textAlign ? styles[`modalBody-${textAlign}`] : ""
          }`}
        >
          {Icon ? (
            <div className="d-flex justify-content-center align-items-center w-100">
              <div
                className={`sm-rounded-wrapper bg-sm-${iconColor} text-white mb-5 mr-0`}
              >
                <Icon />
              </div>
            </div>
          ) : null}
          <p>{message}</p>
          {description ? <p>{description}</p> : null}
        </Modal.Body>
        <Modal.Footer className={styles.modalFooter}>
          {showNo ? (
            <SmCta
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.OUTLINE}
              onClick={onNo}
              disabled={isLoading}
            >
              {noText}
            </SmCta>
          ) : null}
          {showYes ? (
            <SmCta
              disabled={isLoading}
              loading={isLoading}
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.SECONDARY}
              onClick={onYes}
              className={styles.confirmCta}
            >
              {yesText}
            </SmCta>
          ) : null}
        </Modal.Footer>
      </div>
    </Modal>
  );
});

export default SmModal;
