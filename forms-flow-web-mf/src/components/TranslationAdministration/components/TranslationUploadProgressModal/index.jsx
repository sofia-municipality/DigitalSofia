import React from "react";
import { Modal, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";

const TranslationUploadProgressModal = ({
  isOpen,
  onClose,
  count,
  totalCount,
  errorsCount,
}) => {
  const { t } = useTranslation();

  return (
    <Modal show={isOpen}>
      <Modal.Header>
        <Modal.Title>{t("so.translations.upload.modal.title")}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {count === 0 && totalCount === 0 ? (
          <p>{t("so.translations.upload.modal.no.new.translations")}</p>
        ) : (
          <>
            <p>
              {t("so.translations.upload.modal.progress", {
                count,
                totalCount,
              })}
            </p>
            <div className="progress">
              <div
                className="progress-bar"
                role="progressbar"
                aria-valuenow={count}
                aria-label="upload-status"
                aria-valuemax={`${totalCount}`}
                style={{
                  width: `${
                    totalCount
                      ? `${Math.floor((count / totalCount) * 100)}%`
                      : "0%"
                  }`,
                  backgroundColor: "#4d53fa"
                }}
              ></div>
            </div>
          </>
        )}

        {errorsCount ? (
          <p className="sm-heading-5 text-sm-red-6">
            {t(`Failed translations ${errorsCount}`)}
          </p>
        ) : null}
      </Modal.Body>

      <Modal.Footer>
        <Button
          variant="primary"
          onClick={onClose}
          disabled={count !== totalCount && !errorsCount}
        >
          {t("modal.cta.confirm")}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default TranslationUploadProgressModal;
