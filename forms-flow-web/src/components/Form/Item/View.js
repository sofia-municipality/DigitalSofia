import React, { useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import CloseIcon from "@mui/icons-material/Close";
import { push } from "connected-react-router";
import { connect, useDispatch, useSelector } from "react-redux";
import {
  selectRoot,
  resetSubmissions,
  saveSubmission,
  Form,
  selectError,
  Errors,
  getForm,
  Formio,
} from "react-formio";
import { useTranslation, Translation } from "react-i18next";
import isEqual from "lodash/isEqual";
import cloneDeep from "lodash/cloneDeep";

import { getFormTranslations } from "../../../utils";
import Loading from "../../../containers/Loading";
import {
  getProcessReq,
  getDraftReqFormat,
} from "../../../apiManager/services/bpmServices";
import {
  setFormFailureErrorData,
  setFormRequestData,
  setFormSubmissionError,
  setFormSubmissionLoading,
  setFormSuccessData,
  setMaintainBPMFormPagination,
} from "../../../actions/formActions";
import {
  publicApplicationStatus,
  updateApplicationStatus,
} from "../../../apiManager/services/applicationServices";
import LoadingOverlay from "react-loading-overlay";
import { CUSTOM_EVENT_TYPE } from "../../ServiceFlow/constants/customEventTypes";
import { toast } from "react-toastify";
import { setFormSubmitted } from "../../../actions/formActions";
import { fetchFormByAlias } from "../../../apiManager/services/bpmFormServices";
import { checkIsObjectId } from "../../../apiManager/services/formatterService";
import {
  draftCreate,
  draftUpdate,
  publicDraftCreate,
  publicDraftUpdate,
} from "../../../apiManager/services/draftService";
import { setPublicStatusLoading } from "../../../actions/applicationActions";
import { postCustomSubmission } from "../../../apiManager/services/FormServices";
import {
  CUSTOM_SUBMISSION_URL,
  CUSTOM_SUBMISSION_ENABLE,
  DRAFT_ENABLED,
  DRAFT_POLLING_RATE,
  DRAFT_FEEDBACK_ENABLED,
  DRAFT_SAVE_ON_EXIT_ENABLED,
  DRAFT_CREATE_ON_INIT_ENABLED,
  FORM_ALERTS_ENABLED,
} from "../../../constants/constants";
import { PAGE_ROUTES } from "../../../constants/navigation";
import * as formEmbeddedConstants from "../../../constants/formEmbeddedConstants";
import useInterval from "../../../customHooks/useInterval";
import { usePageTitleRef, useEnrichForm } from "../../../customHooks";
import selectApplicationCreateAPI from "./apiSelectHelper";
import {
  getApplicationCount,
  getFormProcesses,
} from "../../../apiManager/services/processServices";
import { setFormStatusLoading } from "../../../actions/processActions";
import SavingLoading from "../../Loading/SavingLoading";
import SmCta, { SmCtaTypes } from "../../sm/components/buttons/SmCta";
import SubmissionError from "../../sm/components/Modal/SubmissionErrorModal";
import AutofillDataModal from "../AutofillDataModal";
import FormSuccessModal from "../FormSuccessModal";
import FormSignDocumentModal from "../FormSignDocumentModal";
import FormErrorModal from "../FormErrorModal";
import { useGetBaseUrl, useFormRestrictionsCheck } from "../../../customHooks";
//import NotFound from "../../NotFound/";
import { renderPage } from "../../../helper/helper";

const View = React.memo((props) => {
  const isWebView = localStorage.getItem("hideNav");
  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );
  const [formStatus, setFormStatus] = React.useState("");
  const { t } = useTranslation();
  const lang = useSelector((state) => state.user.lang);
  const formStatusLoading = useSelector(
    (state) => state.process?.formStatusLoading
  );
  const isFormSubmissionLoading = useSelector(
    (state) => state.formDelete.isFormSubmissionLoading
  );
  const isPublicStatusLoading = useSelector(
    (state) => state.applications.isPublicStatusLoading
  );

  const isFormSubmitted = useSelector(
    (state) => state.formDelete.formSubmitted
  );
  const publicFormStatus = useSelector(
    (state) => state.formDelete.publicFormStatus
  );
  const draftSubmissionId = useSelector(
    (state) => state.draft.draftSubmission?.id
  );
  const draftSubmissionIdRef = useRef();
  // Holds the latest data saved by the server
  const processLoadError = useSelector(
    (state) => state.process?.processLoadError
  );
  const lastUpdatedDraft = useSelector((state) => state.draft.lastUpdated);
  const isPublic = !props.isAuthenticated;
  const redirectUrl = useGetBaseUrl();
  /**
   * `draftData` is used for keeping the uptodate form entry,
   * this will get updated on every change the form is having.
   */
  const [draftData, setDraftData] = useState({});
  const draftRef = useRef();
  const [isDraftCreated, setIsDraftCreated] = useState(false);
  const isDraftCreatedRef = useRef();

  const { formId } = useParams();
  const [validFormId, setValidFormId] = useState(undefined);

  const [showPublicForm, setShowPublicForm] = useState("checking");
  const [poll, setPoll] = useState(DRAFT_ENABLED);
  const exitType = useRef("UNMOUNT");
  const [draftSaved, setDraftSaved] = useState(false);
  const [notified, setNotified] = useState(false);
  const {
    isAuthenticated,
    submission,
    hideComponents,
    onSubmit,
    onCustomEvent,
    errors,
    options,
    form: { form, isActive, url, error },
    tenant,
  } = props;

  useFormRestrictionsCheck(form?.path, tenant);

  const [isValidResource, setIsValidResource] = useState(false);

  const formSubmitCallback = useRef();
  const setFormSubmitCallback = (func) => (formSubmitCallback.current = func);

  const headingRef = usePageTitleRef();

  const formRef = useRef();
  const enrichForm = useEnrichForm();

  const dispatch = useDispatch();
  /*
  Selecting which endpoint to use based on authentication status,
  public endpoint or authenticated endpoint.
  */
  const draftCreateMethod = isAuthenticated ? draftCreate : publicDraftCreate;
  const draftUpdateMethod = isAuthenticated ? draftUpdate : publicDraftUpdate;

  const getPublicForm = useCallback(
    (form_id, isObjectId, formObj) => {
      dispatch(setPublicStatusLoading(true));
      dispatch(
        publicApplicationStatus(form_id, (err) => {
          dispatch(setPublicStatusLoading(false));
          if (!err) {
            if (isPublic) {
              if (isObjectId) {
                dispatch(getForm("form", form_id));
                dispatch(setFormStatusLoading(false));
              } else {
                dispatch(
                  setFormRequestData(
                    "form",
                    form_id,
                    `${Formio.getProjectUrl()}/form/${form_id}`
                  )
                );
                dispatch(setFormSuccessData("form", formObj));
                dispatch(setFormStatusLoading(false));
              }
            }
          }
        })
      );
    },
    [dispatch, isPublic]
  );
  const getFormData = useCallback(() => {
    const isObjectId = checkIsObjectId(formId);
    if (isObjectId) {
      getPublicForm(formId, isObjectId);
      setValidFormId(formId);
    } else {
      dispatch(
        fetchFormByAlias(formId, async (err, formObj) => {
          if (!err) {
            const form_id = formObj._id;
            getPublicForm(form_id, isObjectId, formObj);
            setValidFormId(form_id);
          } else {
            dispatch(setFormFailureErrorData("form", err));
          }
        })
      );
    }
  }, [formId, dispatch, getPublicForm]);
  /**
   * Compares the current form data and last saved data
   * Draft is updated only if the form is updated from the last saved form data.
   */
  const saveDraft = (payload, exitType = exitType) => {
    if (exitType === "SUBMIT") return;
    let dataChanged = !isEqual(payload.data, lastUpdatedDraft.data);
    if (dataChanged) {
      if (draftSubmissionIdRef.current && isDraftCreatedRef.current) {
        setDraftSaved(false);
        dispatch(
          draftUpdateMethod(payload, draftSubmissionIdRef.current, (err) => {
            if (
              exitType === "UNMOUNT" &&
              !err &&
              isAuthenticated &&
              DRAFT_FEEDBACK_ENABLED
            ) {
              toast.success(t("Submission saved to draft."));
            }
            if (!err) {
              setDraftSaved(true);
            } else {
              setDraftSaved(false);
            }
          })
        );
      } else if (
        !isDraftCreated &&
        !DRAFT_CREATE_ON_INIT_ENABLED &&
        DRAFT_ENABLED
      ) {
        dispatch(draftCreateMethod(payload, setIsDraftCreated));
      }
    }
  };

  useEffect(() => {
    if (draftSubmissionId) {
      draftSubmissionIdRef.current = draftSubmissionId;
    }
  }, [draftSubmissionId]);

  useEffect(() => {
    if (isDraftCreated) {
      isDraftCreatedRef.current = isDraftCreated;
    }
  }, [isDraftCreated]);

  useEffect(() => {
    if (draftSubmissionId && formRef.current) {
      formRef.current.draftId = draftSubmissionId;
      formRef.current.data.draftId = draftSubmissionId;
    }
  }, [draftSubmissionId]);

  useEffect(() => {
    if (form._id && !error) setIsValidResource(true);
    return () => setIsValidResource(false);
  }, [error, form._id]);

  useEffect(() => {
    setTimeout(() => {
      setNotified(true);
    }, 5000);
  }, []);

  useEffect(() => {
    if (isDraftCreated) {
      setDraftSaved(true);
    }
  }, [isDraftCreated]);

  /**
   * Will create a draft application when the form is selected for entry.
   */
  useEffect(() => {
    if (
      validFormId &&
      DRAFT_ENABLED &&
      DRAFT_CREATE_ON_INIT_ENABLED &&
      isValidResource &&
      ((isAuthenticated && formStatus === "active") ||
        (!isAuthenticated && publicFormStatus?.status == "active"))
    ) {
      let payload = getDraftReqFormat(validFormId, draftData?.data);
      dispatch(draftCreateMethod(payload, setIsDraftCreated));
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [validFormId, formStatus, publicFormStatus, isValidResource]);

  /**
   * We will repeatedly update the current state to draft table
   * on purticular interval
   */
  useInterval(
    () => {
      let payload = getDraftReqFormat(validFormId, { ...draftData?.data });
      saveDraft(payload);
    },
    poll ? DRAFT_POLLING_RATE : null
  );

  /**
   * Save the current state when the component unmounts.
   * Save the data before submission to handle submission failure.
   */
  useEffect(() => {
    return () => {
      let payload = getDraftReqFormat(validFormId, draftRef.current?.data);
      if (poll && DRAFT_SAVE_ON_EXIT_ENABLED)
        saveDraft(payload, exitType.current);
    };
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [validFormId, draftSubmissionId, isDraftCreated, poll, exitType.current]);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(setFormStatusLoading(true));
      dispatch(
        getFormProcesses(formId, (err, data) => {
          if (!err) {
            dispatch(getApplicationCount(data.id));
            setFormStatus(data.status);
            dispatch(setFormStatusLoading(false));
          } else {
            dispatch(setFormStatusLoading(false));
          }
        })
      );
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  useEffect(() => {
    if (isPublic) {
      getFormData();
    } else {
      setValidFormId(formId);
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPublic, dispatch, getFormData]);

  useEffect(() => {
    if (publicFormStatus) {
      if (
        publicFormStatus.anonymous === true &&
        publicFormStatus.status === "active"
      ) {
        setShowPublicForm(true);
      } else {
        setShowPublicForm(false);
      }
    }
  }, [publicFormStatus]);

  const manuallySaveDraft = ({ submission }) => {
    if (DRAFT_ENABLED) {
      const { data } = submission;
      const draftData = cloneDeep(data);
      setDraftData(draftData);
      draftRef.current = draftData;
      let payload = getDraftReqFormat(validFormId, draftData);
      saveDraft(payload, exitType.current);
    }
  };

  if (isActive || isPublicStatusLoading || formStatusLoading) {
    return (
      <div data-testid="loading-view-component">
        <Loading />
      </div>
    );
  }

  if (isFormSubmitted && !isAuthenticated) {
    return (
      <div className="text-center pt-5">
        <h1>{t("Thank you for your response.")}</h1>
        <p>{t("saved successfully")}</p>
      </div>
    );
  }

  if (isPublic && !showPublicForm) {
    return (
      <div className="alert alert-danger mt-4" role="alert">
        {t("Form not available")}
      </div>
    );
  }

  return (
    <div className="container overflow-y-auto form-view-wrapper">
      {DRAFT_ENABLED &&
        DRAFT_FEEDBACK_ENABLED &&
        isAuthenticated &&
        isValidResource &&
        (formStatus === "active" ||
          (publicFormStatus?.anonymous === true &&
            publicFormStatus?.status === "active")) && (
          <>
            <span className="pr-2  mr-2 d-flex justify-content-end align-items-center">
              {!notified && (
                <span className="text-primary">
                  <i className="fa fa-info-circle mr-2" aria-hidden="true"></i>
                  {t(
                    "Unfinished applications will be saved to Applications/Drafts."
                  )}
                </span>
              )}

              {notified && poll && (
                <SavingLoading
                  text={
                    draftSaved
                      ? t("Saved to Applications/Drafts")
                      : t("Saving...")
                  }
                  saved={draftSaved}
                />
              )}
            </span>
          </>
        )}
      <div className="d-flex align-items-center justify-content-center">
        <div
          className={`main-header ${
            form.display === "wizard" ? "main-header-wizzard" : ""
          }`}
        >
          <SubmissionError
            modalOpen={props.submissionError.modalOpen}
            message={props.submissionError.message}
            onConfirm={props.onConfirm}
          ></SubmissionError>
          <div
            className={`form-header ${
              form.display === "wizard" ? "form-wizard-header" : ""
            }`}
          >
            {form.title ? (
              <h3 tabIndex="-1" ref={headingRef}>
                {t(form.title)}
              </h3>
            ) : (
              ""
            )}
            <SmCta
              type={SmCtaTypes.OUTLINE}
              className="form-close-cta"
              isLink
              href={
                isWebView
                  ? showRequestServiceLink
                    ? PAGE_ROUTES.ADDRESS_REGISTRATION
                    : PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION
                  : PAGE_ROUTES.ADDRESS_REGISTRATION
              }
              disabled={isFormSubmissionLoading}
              accessibilityProps={{
                "aria-label": t("screen.reader.modal.close.cta"),
              }}
            >
              <span className="sm-cta-outline-underline">
                {t("form.header.actions.close")}
              </span>
              <CloseIcon className="form-close-cta-icon" />
            </SmCta>
          </div>
        </div>
      </div>
      <Errors errors={errors} />
      <LoadingOverlay
        active={isFormSubmissionLoading}
        spinner={<Loading />}
        className={`col-12 ${
          form.display === "wizard" ? "form-wizzard-wrapper" : ""
        }`}
      >
        <div>
          {isPublic || formStatus === "active" ? (
            <>
              {formEmbeddedConstants.REACT_APP_SHOW_AUTO_FULFILLMENT_CHECKBOX ? (
                <AutofillDataModal formRef={formRef} />
              ) : null}
              <FormSuccessModal formRef={formRef} />
              <FormErrorModal />
              <FormSignDocumentModal
                formRef={formRef}
                setFormSubmitCallback={setFormSubmitCallback}
                onInit={() => {
                  if (DRAFT_ENABLED) {
                    const updatedDraftData = cloneDeep(
                      draftData.data || draftData
                    );
                    updatedDraftData.applicationStatus =
                      formEmbeddedConstants.APPLICATION_STATUS.SIGN_DOCUMENT_PENDING;
                    setDraftData(updatedDraftData);
                    draftRef.current = updatedDraftData;
                    let payload = getDraftReqFormat(
                      validFormId,
                      updatedDraftData
                    );
                    saveDraft(payload, exitType.current);
                  }
                }}
              />
              <Form
                form={form}
                submission={submission}
                url={url}
                options={{
                  ...options,
                  language: lang,
                  i18n: getFormTranslations(),
                }}
                hideComponents={hideComponents}
                onChange={(data) => {
                  setDraftData(data);
                  draftRef.current = data;
                }}
                onSubmit={(data) => {
                  setPoll(false);
                  exitType.current = "SUBMIT";
                  onSubmit(data, form._id, isPublic, formSubmitCallback);
                }}
                onCustomEvent={(evt) => onCustomEvent(evt, redirectUrl)}
                onNextPage={manuallySaveDraft}
                formReady={(form) => {
                  formRef.current = form;
                  enrichForm(formRef, manuallySaveDraft);
                }}
              />
            </>
          ) : (
            renderPage(formStatus, processLoadError)
          )}
        </div>
      </LoadingOverlay>
    </div>
  );
});

// eslint-disable-next-line no-unused-vars
const doProcessActions = (submission, ownProps, formSubmitCallback) => {
  return (dispatch, getState) => {
    const state = getState();
    let form = state.form?.form;
    let isAuth = state.user.isAuthenticated;
    const tenantKey = state.tenants?.tenantId;
    const redirectUrl = useGetBaseUrl.getBaseUrl(tenantKey);
    const origin = `${window.location.origin}${redirectUrl}`;
    dispatch(resetSubmissions("submission"));
    const data = getProcessReq(form, submission._id, origin);
    let draft_id = state.draft.draftSubmission?.id;
    const applicationId = state.draft.draftSubmission?.applicationId;
    let isDraftCreated = draft_id ? true : false;
    const applicationCreateAPI = selectApplicationCreateAPI(
      isAuth,
      isDraftCreated,
      DRAFT_ENABLED && isDraftCreated
    );

    dispatch(
      // eslint-disable-next-line no-unused-vars
      applicationCreateAPI(data, draft_id ? draft_id : null, (err, res) => {
        dispatch(setFormSubmissionLoading(false));
        if (!err) {
          if (formSubmitCallback?.current) {
            formSubmitCallback.current(null, res);
            dispatch(setFormSubmitted(true));
            if (isAuth) {
              dispatch(setMaintainBPMFormPagination(true));
            }
          } else {
            FORM_ALERTS_ENABLED &&
              toast.success(
                <Translation>{(t) => t("Submission Saved")}</Translation>
              );
            dispatch(setFormSubmitted(true));
            if (isAuth) {
              dispatch(setMaintainBPMFormPagination(true));
              let redirectTarget;
              if (form.type === "resource") {
                redirectTarget = `${redirectUrl}form`;
              } else {
                redirectTarget = `${PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
                  ":tenantId",
                  tenantKey
                )}`;
              }
              dispatch(push(redirectTarget));
            }
          }
        } else {
          formSubmitCallback.current && formSubmitCallback.current(err);
          updateApplicationStatus({
            applicationId: submission.data.applicationId || applicationId,
            formUrl: data.formUrl,
            applicationStatus:
              formEmbeddedConstants.APPLICATION_STATUS.SUBMISSION_ERROR,
          })
            .then(() => {
              dispatch(
                setFormSubmissionError({
                  modalOpen: true,
                })
              );
            })
            .catch(() => {
              dispatch(
                setFormSubmissionError({
                  modalOpen: true,
                })
              );
            });
        }
      })
    );
  };
};

const onSubmissionErrorConfirm = () => {
  return (dispatch, getState) => {
    const state = getState();
    const tenantKey = state.tenants?.tenantId;
    const ErrorDetails = { modalOpen: false, message: "" };
    dispatch(setFormSubmissionError(ErrorDetails));
    const redirectUrl = `${PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
      ":tenantId",
      tenantKey
    )}`;
    dispatch(push(redirectUrl));
  };
};

const mapStateToProps = (state) => {
  return {
    user: state.user.userDetail,
    tenant: state?.tenants?.tenantId,
    form: selectRoot("form", state),
    isAuthenticated: state.user.isAuthenticated,
    errors: [selectError("form", state), selectError("submission", state)],
    options: {
      noAlerts: !FORM_ALERTS_ENABLED,
      i18n: {
        en: {
          error: <Translation>{(t) => t("Message")}</Translation>,
        },
      },
    },
    submissionError: selectRoot("formDelete", state).formSubmissionError,
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    onSubmit: (submission, formId, isPublic, formSubmitCallback) => {
      !formSubmitCallback?.current && dispatch(setFormSubmissionLoading(true));
      // this is callback function for submission
      const callBack = (err, submission) => {
        if (!err) {
          dispatch(doProcessActions(submission, ownProps, formSubmitCallback));
        } else {
          dispatch(setFormSubmissionLoading(false));
          formSubmitCallback.current && formSubmitCallback.current(err);
          dispatch(
            setFormSubmissionError({
              modalOpen: true,
            })
          );
        }
      };
      if (CUSTOM_SUBMISSION_URL && CUSTOM_SUBMISSION_ENABLE) {
        postCustomSubmission(submission, formId, isPublic, callBack);
      } else {
        dispatch(saveSubmission("submission", submission, formId, callBack));
      }
    },
    onCustomEvent: (customEvent, redirectUrl) => {
      switch (customEvent.type) {
        case CUSTOM_EVENT_TYPE.CUSTOM_SUBMIT_DONE:
          toast.success("Submission Saved.");
          dispatch(push(`${redirectUrl}form`));
          break;
        case CUSTOM_EVENT_TYPE.CANCEL_SUBMISSION:
          dispatch(push(`${redirectUrl}form`));
          break;
        default:
          return;
      }
    },
    onConfirm: () => dispatch(onSubmissionErrorConfirm()),
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(View);
