import React, { useEffect, useRef, useState } from "react";
import { connect, useDispatch, useSelector } from "react-redux";

import CloseIcon from "@mui/icons-material/Close";
import {
  selectRoot,
  resetSubmissions,
  saveSubmission,
  Form,
  selectError,
  Errors,
} from "react-formio";
import { push } from "connected-react-router";
import { useParams } from "react-router-dom";
import { useTranslation, Translation } from "react-i18next";
import LoadingOverlay from "react-loading-overlay";
import { toast } from "react-toastify";
import isEqual from "lodash/isEqual";
import cloneDeep from "lodash/cloneDeep";

import { getFormTranslations } from "../../utils";
import useInterval from "../../customHooks/useInterval";
import { CUSTOM_EVENT_TYPE } from "../ServiceFlow/constants/customEventTypes";
import selectApplicationCreateAPI from "../Form/Item/apiSelectHelper";
import {
  setFormSubmissionError,
  setFormSubmissionLoading,
  setFormSubmitted,
} from "../../actions/formActions";
import { updateApplicationStatus } from "../../apiManager/services/applicationServices";
import { postCustomSubmission } from "../../apiManager/services/FormServices";
import {
  getProcessReq,
  getDraftReqFormat,
} from "../../apiManager/services/bpmServices";
import {
  deleteDraftbyId,
  draftUpdate,
} from "../../apiManager/services/draftService";
import {
  CUSTOM_SUBMISSION_URL,
  CUSTOM_SUBMISSION_ENABLE,
  DRAFT_ENABLED,
  DRAFT_POLLING_RATE,
  DRAFT_FEEDBACK_ENABLED,
  DRAFT_SAVE_ON_EXIT_ENABLED,
  FORM_ALERTS_ENABLED,
  DRAFT_DELETE_CTA_ENABLED,
} from "../../constants/constants";
import { PAGE_ROUTES } from "../../constants/navigation";
import * as formEmbeddedConstants from "../../constants/formEmbeddedConstants";
import Loading from "../../containers/Loading";
import SavingLoading from "../Loading/SavingLoading";
import Confirm from "../../containers/Confirm";
import { setDraftDelete } from "../../actions/draftActions";
import SmCta, { SmCtaTypes } from "../sm/components/buttons/SmCta";
import SubmissionError from "../sm/components/Modal/SubmissionErrorModal";
import { usePageTitleRef, useEnrichForm } from "../../customHooks";
import AutofillDataModal from "../Form/AutofillDataModal";
import FormSuccessModal from "../Form/FormSuccessModal";
import FormSignDocumentModal from "../Form/FormSignDocumentModal";
import FormErrorModal from "../Form/FormErrorModal";
import { useGetBaseUrl, useFormRestrictionsCheck } from "../../customHooks";
import { setFormStatusLoading } from "../../actions/processActions";
import { getFormProcesses } from "../../apiManager/services/processServices";
import { textTruncate } from "../../helper/helper";

const View = React.memo((props) => {
  const isWebView = localStorage.getItem("hideNav");
  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );
  const { t } = useTranslation();
  const lang = useSelector((state) => state.user.lang);
  const isFormSubmissionLoading = useSelector(
    (state) => state.formDelete.isFormSubmissionLoading
  );
  const isPublicStatusLoading = useSelector(
    (state) => state.applications.isPublicStatusLoading
  );

  const isFormSubmitted = useSelector(
    (state) => state.formDelete.formSubmitted
  );

  const draftDelete = useSelector((state) => state.draft?.draftDelete);

  const isPublic = !props.isAuthenticated;
  const redirectUrl = useGetBaseUrl();
  const draftSubmission = useSelector((state) => state.draft.submission);
  const draftSubmissionIdRef = useRef();
  const [draftSaved, setDraftSaved] = useState(false);
  const [showNotification, setShowNotification] = useState(false);
  /**
   * `draftData` is used for keeping the uptodate form entry,
   * this will get updated on every change the form is having.
   */
  const [draftData, setDraftData] = useState(draftSubmission?.data);
  // Holds the latest data saved by the server
  const lastUpdatedDraft = useSelector((state) => state.draft.lastUpdated);
  const draftRef = useRef();
  const { formId, draftId } = useParams();
  const [poll, setPoll] = useState(DRAFT_ENABLED);
  const exitType = useRef("UNMOUNT");
  const {
    isAuthenticated,
    submission,
    hideComponents,
    onSubmit,
    onCustomEvent,
    errors,
    options,
    form: { form, isActive, url },
    tenant,
  } = props;
  const dispatch = useDispatch();
  useFormRestrictionsCheck(form?.path, tenant);

  const formSubmitCallback = useRef();
  const setFormSubmitCallback = (func) => (formSubmitCallback.current = func);

  const headingRef = usePageTitleRef();

  const formRef = useRef();
  const enrichForm = useEnrichForm();

  const saveDraft = (payload, exitType = exitType) => {
    if (exitType === "SUBMIT" || processData?.status !== "active") return;
    let dataChanged = !isEqual(payload.data, lastUpdatedDraft.data);
    if (draftSubmissionIdRef.current) {
      if (String(draftSubmissionIdRef.current) !== String(draftId)) return;
      if (dataChanged) {
        setDraftSaved(false);
        if (!showNotification) setShowNotification(true);
        dispatch(
          draftUpdate(payload, draftSubmissionIdRef.current, (err) => {
            if (exitType === "UNMOUNT" && !err && DRAFT_FEEDBACK_ENABLED) {
              toast.success(t("Submission saved to draft."));
            }
            if (!err) {
              setDraftSaved(true);
            } else {
              setDraftSaved(false);
            }
          })
        );
      }
    }
  };
  const formStatusLoading = useSelector(
    (state) => state.process?.formStatusLoading
  );

  const processData = useSelector((state) => state.process?.formProcessList);

  /**
   * We will repeatedly update the current state to draft table
   * on purticular interval
   */
  useInterval(
    () => {
      let payload = getDraftReqFormat(formId, { ...draftData });
      saveDraft(payload);
    },
    poll ? DRAFT_POLLING_RATE : null
  );

  useEffect(() => {
    if (draftSubmission?.id) {
      draftSubmissionIdRef.current = draftSubmission?.id;
    }
  }, [draftSubmission?.id]);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(setFormStatusLoading(true));
      dispatch(
        getFormProcesses(formId, () => {
          dispatch(setFormStatusLoading(false));
        })
      );
    }
  }, [isAuthenticated, formId, dispatch]);

  useEffect(() => {
    return () => {
      let payload = getDraftReqFormat(formId, draftRef.current);
      if (poll && DRAFT_SAVE_ON_EXIT_ENABLED)
        saveDraft(payload, exitType.current);
    };
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [poll, exitType.current, draftSubmission?.id]);

  if (isActive || isPublicStatusLoading || formStatusLoading) {
    return (
      <div data-testid="loading-view-component">
        <Loading />
      </div>
    );
  }

  const manuallySaveDraft = ({ submission }) => {
    if (DRAFT_ENABLED) {
      const { data } = submission;
      const draftData = cloneDeep(data);
      setDraftData(draftData);
      draftRef.current = draftData;
      let payload = getDraftReqFormat(form._id, draftData);
      saveDraft(payload, exitType.current);
    }
  };

  const deleteDraft = () => {
    dispatch(
      setDraftDelete({
        modalOpen: true,
        draftId: draftSubmission.id,
        draftName: draftSubmission.DraftName,
      })
    );
  };

  const onYes = (e) => {
    e.currentTarget.disabled = true;
    deleteDraftbyId(draftDelete.draftId)
      .then(() => {
        toast.success(t("Draft Deleted Successfully"));
        dispatch(push(`${redirectUrl}draft`));
      })
      .catch((error) => {
        toast.error(error.message);
      })
      .finally(() => {
        dispatch(
          setDraftDelete({
            modalOpen: false,
            draftId: null,
            draftName: "",
          })
        );
      });
  };

  const onNo = () => {
    dispatch(
      setDraftDelete({
        modalOpen: false,
        draftId: null,
        draftName: "",
      })
    );
  };

  if (isFormSubmitted && !isAuthenticated) {
    //This code has relevance only for form Submission Edit by Anonymous Users
    return (
      <div className="text-center pt-5">
        <h1>{t("Thank you for your response.")}</h1>
        <p>{t("saved successfully")}</p>
      </div>
    );
  }

  return (
    <div className="container overflow-y-auto form-view-wrapper">
      {
        <>
          <span className="pr-2  mr-2 d-flex justify-content-end align-items-center">
            {poll && showNotification && DRAFT_FEEDBACK_ENABLED && (
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
      }
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
            <div className="d-flex align-items-center">
              {DRAFT_DELETE_CTA_ENABLED && processData?.status === "active" ? (
                <button
                  className="btn btn-danger mr-2 h-100"
                  onClick={() => deleteDraft()}
                >
                  {t("Discard Draft")}
                </button>
              ) : null}
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
          <Confirm
            modalOpen={draftDelete.modalOpen}
            message={`${t(
              "Are you sure you wish to delete the draft"
            )} "${textTruncate(14, 12, draftDelete.draftName)}" 
            ${t("with ID")} "${draftDelete.draftId}"`}
            onNo={() => onNo()}
            onYes={(e) => {
              exitType.current = "SUBMIT";
              onYes(e);
            }}
          />
          {processData?.status === "active" ? (
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
                    let payload = getDraftReqFormat(form._id, updatedDraftData);
                    saveDraft(payload, exitType.current);
                  }
                }}
              />
              <Form
                form={form}
                submission={submission.submission}
                url={url}
                options={{
                  ...options,
                  language: lang,
                  i18n: getFormTranslations(),
                }}
                hideComponents={hideComponents}
                onChange={(formData) => {
                  setDraftData(formData.data);
                  draftRef.current = formData.data;
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
                  enrichForm(formRef, manuallySaveDraft, draftId);
                }}
              />
            </>
          ) : (
            <span>
              <div
                className="container"
                style={{
                  maxWidth: "900px",
                  margin: "auto",
                  height: "50vh",
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <h3>{t("Form not published")}</h3>
                <p>{t("You can't submit this form until it is published")}</p>
              </div>
            </span>
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
    dispatch(resetSubmissions("submission"));
    const origin = `${window.location.origin}${redirectUrl}`;
    const data = getProcessReq(form, submission._id, origin);
    let draft_id = state.draft.submission?.id;
    const applicationId = state.draft.draftSubmission?.applicationId;
    let isDraftCreated = draft_id ? true : false;
    const applicationCreateAPI = selectApplicationCreateAPI(
      isAuth,
      isDraftCreated,
      DRAFT_ENABLED
    );
    dispatch(
      applicationCreateAPI(data, draft_id ? draft_id : null, (err, res) => {
        dispatch(setFormSubmissionLoading(false));
        if (!err) {
          if (formSubmitCallback?.current) {
            formSubmitCallback?.current(null, res);
            dispatch(setFormSubmitted(true));
          } else {
            FORM_ALERTS_ENABLED &&
              toast.success(
                <Translation>{(t) => t("Submission Saved")}</Translation>
              );
            if (isAuth)
              dispatch(
                push(
                  `${PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
                    ":tenantId",
                    tenantKey
                  )}`
                )
              );
            else {
              dispatch(setFormSubmitted(true));
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
    submission: selectRoot("draft", state),
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
          dispatch(push(`${redirectUrl}draft`));
          break;
        case CUSTOM_EVENT_TYPE.CANCEL_SUBMISSION:
          dispatch(push(`${redirectUrl}draft`));
          break;
        default:
          return;
      }
    },
    onConfirm: () => dispatch(onSubmissionErrorConfirm()),
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(View);
