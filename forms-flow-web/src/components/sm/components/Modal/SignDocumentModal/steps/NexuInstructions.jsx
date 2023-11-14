import React, { useState, useRef } from "react";
import { useSelector } from "react-redux";
import Modal from "react-bootstrap/Modal";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";
import FileDownloadOutlinedIcon from "@mui/icons-material/FileDownloadOutlined";
import { saveAs } from "file-saver";

import { useDevice } from "../../../../../../customHooks";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../../buttons/SmCta";

import styles from "../signDocumentModal.module.scss";
import "../signDocumentModal.scss";

const ctasConfig = {
  [useDevice.SYSTEMS.WINDOWS]: {
    title: "form.document.sign.nexu.modal.instructions.download.windows",
    filePath: "/assets/nexu/setupNexU1.23forkBG_sign.exe",
    fileName: "setupNexU1.23forkBG_sign.exe",
  },
  [useDevice.SYSTEMS.MAC]: {
    title: "form.document.sign.nexu.modal.instructions.download.mac",
    filePath: "/assets/nexu/nexu-1.0-1.x86_64.rpm",
    fileName: "nexu-1.0-1.x86_64.rpm",
  },
  [useDevice.SYSTEMS.LINUX]: {
    title: "form.document.sign.nexu.modal.instructions.download.linux",
    filePath: "/assets/nexu/nexu-1.0.pkg",
    fileName: "nexu-1.0.pkg",
  },
};

const MANUAL = {
  filePath: "/assets/nexu/manual",
  fileName: "form.document.sign.nexu.modal.instructions.download.manual",
};

const useGetDownloadCtas = () => {
  const { currentOS } = useDevice();
  const { t } = useTranslation();
  const [showAll, setShowAll] = useState(false);
  const ctaRef = useRef();

  return () => (
    <div className="mt-3 d-flex justify-content-center flex-column">
      {Object.entries(ctasConfig).map(
        ([key, { title, filePath, fileName }], index) =>
          currentOS === key || showAll ? (
            <div key={index} className="mb-3">
              <SmCta
                refObj={currentOS === key ? ctaRef : null}
                size={SmCtaSizes.MEDIUM}
                type={SmCtaTypes.LINK}
                className={styles.confirmCta}
                onClick={() => saveAs(filePath, fileName)}
              >
                <span className="d-flex align-items-center">
                  <FileDownloadOutlinedIcon className="mr-3" />
                  <span>{t(title)}</span>
                </span>
              </SmCta>
            </div>
          ) : null
      )}
      {!showAll ? (
        <SmCta
          size={SmCtaSizes.MEDIUM}
          type={SmCtaTypes.LINK}
          className={styles.confirmCta}
          onClick={() => {
            setShowAll(true);
            ctaRef?.current?.focus();
          }}
        >
          {t("form.document.sign.nexu.modal.instructions.showAll.cta")}
        </SmCta>
      ) : null}
    </div>
  );
};

const SignDocumentNexuInstructions = React.memo(({ onClose }) => {
  const { t } = useTranslation();
  const renderDownloadCtas = useGetDownloadCtas();
  const userLanguage = useSelector((state) => state.user.lang);

  return (
    <>
      <div className={styles.modalCloseCta}>
        <SmCta
          size={SmCtaSizes.SMALL}
          type={SmCtaTypes.OUTLINE}
          onClick={onClose}
          accessibilityProps={{
            "aria-label": t("screen.reader.modal.close.cta"),
          }}
        >
          <CloseIcon />
        </SmCta>
      </div>
      <div className={styles.modalContentContainer}>
        <Modal.Header className={styles.modalHeader}>
          <Modal.Title id="modal-title">
            {t("form.document.sign.nexu.modal.instructions.title")}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body className={styles.modalBody}>
          <div className="d-flex justify-content-center align-items-center w-100">
            <div className="sm-rounded-wrapper bg-sm-yellow text-white mb-5 mr-0">
              <span style={{ fontSize: "25px" }}>i</span>
            </div>
          </div>
          <p>{t("form.document.sign.nexu.modal.instructions.description.1")}</p>
          <p className="mt-3">
            {t("form.document.sign.nexu.modal.instructions.description.2")}
          </p>
          {renderDownloadCtas()}
          <div className="mt-5 d-flex justify-content-center align-items-center">
            <SmCta
              size={SmCtaSizes.MEDIUM}
              type={SmCtaTypes.OUTLINE}
              className={styles.confirmCta}
              onClick={() => {
                saveAs(
                  `${MANUAL.filePath}_${userLanguage}.docx`,
                  t(MANUAL.fileName)
                );
              }}
              accessibilityProps={{
                "aria-label": t(
                  "screen.reader.form.document.sign.nexu.modal.instructions.download.manual"
                ),
              }}
            >
              <img
                alt=""
                src="/file_open.svg"
                className="mr-3"
                width="30"
                height="30"
              />
              <span>{t(MANUAL.fileName)}</span>
            </SmCta>
          </div>
        </Modal.Body>
        <Modal.Footer className={styles.modalFooter}>
          <SmCta
            size={SmCtaSizes.SMALL}
            type={SmCtaTypes.SECONDARY}
            onClick={onClose}
            className={styles.confirmCta}
          >
            {t("form.document.sign.nexu.modal.instructions.update.cta")}
          </SmCta>
        </Modal.Footer>
      </div>
    </>
  );
});

export default SignDocumentNexuInstructions;
