import { useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { saveAs } from "file-saver";
import { useLocation } from "react-router-dom";
import querystring from "querystring";

import * as formEmbeddedConstants from "../constants/formEmbeddedConstants";
import API from "../apiManager/endpoints";
import * as apiConfig from "../apiManager/endpoints/config";
import * as httpRequestHandler from "../apiManager/httpRequestHandler";
import { useCheckFormRestrictions } from "../apiManager/apiHooks";
import {
  validatePersonalIdentifier,
  sendMobleAppMessage,
  downloadBase64File,
  downloadFile,
} from "../utils";
import { openCloseForbiddenModal } from "../actions/roleActions";
import { ForbiddenModalTypes } from "../components/sm/components/Modal/ForbiddenModal";
import { useDevice } from "./device";
import {
  MULTITENANCY_ENABLED,
  SERVICES_ASSURANCE_LEVEL,
  CHECK_ASSURANCE_LEVEL_ENABLED,
  FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES,
  PERMANENT_ADDRESS_FORM_PATH,
  CURRENT_ADDRESS_FORM_PATH,
} from "../constants/constants";
import { useCheckUserAssuranceLevel } from "./user";

export const useEnrichForm = () => {
  const { search } = useLocation();
  const deviceSize = useDevice();

  const enrichForm = useCallback(
    (formRef, saveDraft = () => {}, draftId) => {
      const searchParams = querystring.parse(search.replace("?", "")) || {};
      if (formRef?.current) {
        if (draftId) {
          formRef.current.draftId = draftId;
          formRef.current.data.draftId = draftId;
        }

        formRef.current.httpRequestHandler = httpRequestHandler;
        formRef.current.validatePersonalIdentifier = validatePersonalIdentifier;
        formRef.current.saveAs = saveAs;
        formRef.current.downloadBase64File = downloadBase64File;
        formRef.current.downloadFile = downloadFile;
        formRef.current.sendMobleAppMessage = sendMobleAppMessage;
        formRef.current.searchParams = searchParams;
        formRef.current.saveDraft = saveDraft;
        formRef.current.deviceSize = deviceSize;
        formRef.current.constants = {
          api: API,
          apiConfig,
          ...formEmbeddedConstants,
        };
      }
    },
    [deviceSize, search]
  );

  return enrichForm;
};

export const useCheckUserAssuranceByFormPath = () => {
  const checkAssuranceLevel = useCheckUserAssuranceLevel();

  return useCallback(
    (path) => {
      const formAssuranceLevel = SERVICES_ASSURANCE_LEVEL[path];
      if (formAssuranceLevel) {
        return checkAssuranceLevel(path);
      }

      return { isPassed: true };
    },
    [checkAssuranceLevel]
  );
};

export const useFormRestrictionsCheck = (path, tenantKey, onMount = true) => {
  const dispatch = useDispatch();
  const { search } = window.location;
  const { behalf } = querystring.parse(search.replace("?", "")) || {};
  const { fetch: checkFormRestrictions } = useCheckFormRestrictions();
  const checkAssuranceLevel = useCheckUserAssuranceByFormPath();
  const userPersonIdenfitier = useSelector(
    (state) => state.user?.userDetail?.personIdentifier || ""
  );

  const checkRestrictions = useCallback(
    async (path, tenantKey, passedBehalf) => {
      if (path) {
        const formBehalf = passedBehalf || behalf;
        let strippedPath = path;
        if (MULTITENANCY_ENABLED && tenantKey) {
          strippedPath = path.replace(tenantKey + "-", "");
        }
        if (
          (strippedPath === PERMANENT_ADDRESS_FORM_PATH ||
            strippedPath === CURRENT_ADDRESS_FORM_PATH) &&
          (!formBehalf ||
            formBehalf ===
              FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.MY_BEHALF) &&
          userPersonIdenfitier.split("-")?.[0] !== "PNOBG"
        ) {
          dispatch(
            openCloseForbiddenModal({
              isOpen: true,
            })
          );

          return false;
        }

        if (CHECK_ASSURANCE_LEVEL_ENABLED) {
          const { isPassed, requiredAssuranceLevel } = checkAssuranceLevel(
            strippedPath,
            tenantKey
          );

          if (!isPassed) {
            dispatch(
              openCloseForbiddenModal({
                isOpen: true,
                type: ForbiddenModalTypes.ASSURANCE_LEVEL,
                requiredAssuranceLevel,
              })
            );

            return false;
          }

          const formRestrictions = await checkFormRestrictions(strippedPath);
          if (formRestrictions) {
            dispatch(
              openCloseForbiddenModal({
                isOpen: true,
                type: ForbiddenModalTypes.CHILD_FORM_IN_PROGRESS,
              })
            );

            return false;
          }
        }
      }
      return true;
    },
    [
      checkAssuranceLevel,
      checkFormRestrictions,
      dispatch,
      behalf,
      userPersonIdenfitier,
    ]
  );

  useEffect(() => {
    if (onMount) {
      checkRestrictions(path, tenantKey);
    }
  }, [checkRestrictions, onMount, path, tenantKey]);

  return checkRestrictions;
};
