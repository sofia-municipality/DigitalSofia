import { useCallback } from "react";
import { useLocation } from "react-router-dom";
import { saveAs } from "file-saver";
import querystring from "querystring";

import * as formEmbeddedConstants from "../constants/formEmbeddedConstants";
import API from "../apiManager/endpoints";
import * as apiConfig from "../apiManager/endpoints/config";
import * as httpRequestHandler from "../apiManager/httpRequestHandler";
import {
  validatePersonalIdentifier,
  downloadFile,
  sendMobleAppMessage,
  downloadBase64File,
} from "../utils";
import { useDevice } from "./device";

export const useEnrichForm = () => {
  const { search } = useLocation();
  const deviceSize = useDevice();

  const enrichForm = useCallback(
    (formRef, saveDraft = () => {}, draftId) => {
      const searchParams = querystring.parse(search.replace("?", "")) || {};
      if (formRef.current) {
        if (draftId) {
          formRef.current.draftId = draftId;
        }

        formRef.current.httpRequestHandler = httpRequestHandler;
        formRef.current.validatePersonalIdentifier = validatePersonalIdentifier;
        formRef.current.saveAs = saveAs;
        formRef.current.searchParams = searchParams;
        formRef.current.saveDraft = saveDraft;
        formRef.current.deviceSize = deviceSize;
        formRef.current.constants = {
          api: API,
          apiConfig,
          ...formEmbeddedConstants,
        };
        formRef.current.downloadBase64File = downloadBase64File;
        formRef.current.downloadFile = downloadFile;
        formRef.current.sendMobleAppMessage = sendMobleAppMessage;
      }
    },
    [deviceSize, search]
  );

  return enrichForm;
};
