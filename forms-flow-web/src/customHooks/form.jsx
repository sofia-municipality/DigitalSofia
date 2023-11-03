import { saveAs } from "file-saver";
import { useLocation } from "react-router-dom";
import querystring from "querystring";

import * as formEmbeddedConstants from "../constants/formEmbeddedConstants";
import API from "../apiManager/endpoints";
import * as httpRequestHandler from "../apiManager/httpRequestHandler";
import { validatePersonalIdentifier } from "../utils";
import { useDevice } from "./device";
import { useCallback } from "react";

export const useEnrichForm = () => {
  const { search } = useLocation();
  const deviceSize = useDevice();

  const enrichForm = useCallback(
    (formRef, saveDraft = () => {}, draftId) => {
      const searchParams = querystring.parse(search.replace("?", "")) || {};
      if (formRef.current.component?.parent) {
        if (draftId) {
          formRef.current.component.parent.draftId = draftId;
          formRef.current.component.data.draftId = draftId;
        }

        formRef.current.component.parent.httpRequestHandler =
          httpRequestHandler;
        formRef.current.component.parent.validatePersonalIdentifier =
          validatePersonalIdentifier;
        formRef.current.component.parent.saveAs = saveAs;
        formRef.current.component.parent.searchParams = searchParams;
        formRef.current.component.parent.saveDraft = saveDraft;
        formRef.current.component.parent.deviceSize = deviceSize;
        formRef.current.component.parent.constants = {
          api: API,
          ...formEmbeddedConstants,
        };
      }
    },
    [deviceSize, search]
  );

  return enrichForm;
};
