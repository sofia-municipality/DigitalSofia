import React, { useRef, useState } from "react";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

import FileService from "../../../../services/FileService";
import {
  useFetchAdminTranslations,
  useCreateTranslation,
  useUpdateTranslation,
} from "../../../../apiManager/apiHooks";
import TranslationUploadProgressModal from "../TranslationUploadProgressModal";
import TranslationUploadOptionsModal, {
  optionsConfig,
} from "../TranslationUploadOptionsModal";

const FAILED_UPLOAD_RETRY_COUNT = 1;

const TranslationUploadCta = ({ total, onClose, className = "" }) => {
  const { t } = useTranslation();
  const uploadFormNode = useRef();
  const [uploadedFilesCount, setUploadedFilesCount] = useState(0);
  const [selectedOption, setSelectedOption] = useState();
  const [totalTranslationsCount, setTotalTranslationsCount] = useState();
  const [isUploadTranslationsModalOpen, setIsUploadTranslationsModalOpen] =
    useState();
  const [
    isUploadTranslationsOptionsModalOpen,
    setIsUploadTranslationsOptionsModalOpen,
  ] = useState();
  const retryCount = useRef(0);
  const failedTranslations = useRef([]);
  const [errorsCount, setErrosCount] = useState();
  const { fetch: fetchAllTranslations } = useFetchAdminTranslations();
  const { fetch: addTranslation } = useCreateTranslation();
  const { fetch: updateTranslation } = useUpdateTranslation();

  const uploadClick = (e) => {
    e.preventDefault();
    setIsUploadTranslationsOptionsModalOpen(true);
    return false;
  };

  const importTranslations = (apiCalls) => {
    setTimeout(() => {
      apiCalls.reduce((prev, cur, index) => {
        return prev
          .then((data) => {
            if (index > 0 && data) {
              setUploadedFilesCount((prevState) => prevState + 1);
            }

            return new Promise((resolve) => {
              setTimeout(() => resolve(), 300);
            }).then(async () => {
              return cur();
            });
          })
          .catch((err) => {
            console.log(err);
          });
      }, Promise.resolve());
    }, 1000);
  };

  const translationApiCall = (payload, apiCall) => {
    return new Promise((resolve, reject) => {
      apiCall(payload)
        .then((res) => resolve(res))
        .catch((e) => {
          failedTranslations.current.push(() =>
            translationApiCall(payload, apiCall)
          );
          reject(e);
        });
    });
  };

  const fileUploaded = async (evt) => {
    let allTranslations;
    try {
      const translationsRes = await fetchAllTranslations({
        limit: total,
        currentPage: 1,
        searchData: {},
      });
      allTranslations = translationsRes.data;
    } catch (err) {
      toast.error(t("so.translations.uknown.error"));
    }

    FileService.uploadFile(evt, async (fileContent) => {
      evt.target.value = null;

      const newTranslations = fileContent.filter(
        (e) => !allTranslations.find((b) => e.identifier === b.data.identifier)
      );
      let existingTranslations = fileContent.filter(
        (e) => !!allTranslations.find((b) => e.identifier === b.data.identifier)
      );

      if (selectedOption === optionsConfig.VALUE_CHANGED) {
        existingTranslations = existingTranslations.filter((e) => {
          const item = allTranslations.find(
            (b) => e.identifier === b.data.identifier
          );
          return item.data.translation !== e.translation;
        });
      }

      existingTranslations = existingTranslations.map((e) =>
        Object.assign({}, e, {
          id: allTranslations.find((b) => e.identifier === b.data.identifier)
            ?._id,
        })
      );

      const newTranslationsApiCalls = newTranslations.map(
        (e) => () => translationApiCall(e, addTranslation)
      );
      const existingTranslationsApiCalls = existingTranslations.map(
        (e) => () => translationApiCall(e, updateTranslation)
      );

      const failedTranslationsHandler = () => {
        if (failedTranslations.current.length) {
          if (
            failedTranslations.current.length ===
              newTranslationsApiCalls.length +
                existingTranslationsApiCalls.length ||
            retryCount.current === FAILED_UPLOAD_RETRY_COUNT
          ) {
            setErrosCount(failedTranslations.current.length);
          } else {
            const apiCalls = [...failedTranslations.current];
            failedTranslations.current = [];
            retryCount.current += 1;
            importTranslations([...apiCalls, failedTranslationsHandler]);
          }
        }
      };

      const apiCalls = [
        ...newTranslationsApiCalls,
        ...existingTranslationsApiCalls,
        failedTranslationsHandler,
      ];

      setTotalTranslationsCount(apiCalls.length - 1);
      setIsUploadTranslationsModalOpen(true);
      importTranslations(apiCalls);
    });
  };

  return (
    <>
      <button
        className={`btn btn-primary btn-md d-flex align-items-center ${className}`}
        onClick={uploadClick}
        title={t("Upload json translations only")}
      >
        <i className="fa fa-upload fa-lg mr-1" aria-hidden="true" />
        <span>{t("so.translatons.upload.cta")}</span>
      </button>
      <input
        type="file"
        className="d-none"
        multiple={false}
        accept=".json,application/json"
        onChange={(e) => {
          fileUploaded(e);
        }}
        ref={uploadFormNode}
      />
      {isUploadTranslationsOptionsModalOpen ? (
        <TranslationUploadOptionsModal
          isOpen={isUploadTranslationsOptionsModalOpen}
          onConfirm={(option) => {
            setSelectedOption(option);
            setIsUploadTranslationsOptionsModalOpen(false);
            uploadFormNode.current?.click();
          }}
          onClose={() => {
            setIsUploadTranslationsOptionsModalOpen(false);
          }}
        />
      ) : null}
      {isUploadTranslationsModalOpen ? (
        <TranslationUploadProgressModal
          isOpen={isUploadTranslationsModalOpen}
          totalCount={totalTranslationsCount}
          count={uploadedFilesCount}
          errorsCount={errorsCount}
          onClose={() => {
            setIsUploadTranslationsModalOpen(false);
            setErrosCount(null);
            onClose();
          }}
        />
      ) : null}
    </>
  );
};

export default TranslationUploadCta;
