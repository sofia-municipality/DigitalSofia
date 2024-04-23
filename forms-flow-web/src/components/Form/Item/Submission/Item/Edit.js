import React, { useEffect, useState, useRef, useMemo } from "react";
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

import Loading from "../../../../../containers/Loading";

import { getFormTranslations } from "../../../../../utils";
import {
  setFormSubmissionError,
  setFormSubmissionLoading,
} from "../../../../../actions/formActions";
import { getUserRolePermission } from "../../../../../helper/user";
import {
  CLIENT,
  CUSTOM_SUBMISSION_URL,
  CUSTOM_SUBMISSION_ENABLE,
  FORM_ALERTS_ENABLED,
} from "../../../../../constants/constants";
import {
  CLIENT_EDIT_STATUS,
  UPDATE_EVENT_STATUS,
  getProcessDataReq,
} from "../../../../../constants/applicationConstants";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import * as formEmbeddedConstants from "../../../../../constants/formEmbeddedConstants";
import { useParams } from "react-router-dom";
import { updateApplicationEvent } from "../../../../../apiManager/services/applicationServices";
import LoadingOverlay from "react-loading-overlay";
import { toast } from "react-toastify";
import { Translation, useTranslation } from "react-i18next";
import {
  updateCustomSubmission,
  updateSubmissionData,
} from "../../../../../apiManager/services/FormServices";
import { usePageTitleRef, useEnrichForm } from "../../../../../customHooks";
import SmCta, { SmCtaTypes } from "../../../../sm/components/buttons/SmCta";
import SubmissionError from "../../../../sm/components/Modal/SubmissionErrorModal";
import { useUpdateApplicationStatus } from "../../../../../apiManager/apiHooks";
import AutofillDataModal from "../../../AutofillDataModal";
import FormSuccessModal from "../../../FormSuccessModal";
import FormSignDocumentModal from "../../../FormSignDocumentModal";
import FormErrorModal from "../../../FormErrorModal";
import {
  useGetBaseUrl,
  useFormRestrictionsCheck,
} from "../../../../../customHooks";

const Edit = React.memo((props) => {
  const isWebView = localStorage.getItem("hideNav");
  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const lang = useSelector((state) => state.user.lang);
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const { formId, submissionId } = useParams();
  const {
    hideComponents,
    onSubmit,
    options,
    errors,
    onFormSubmit,
    onCustomEvent,
    form: { form, isActive: isFormActive },
    submission: { submission, isActive: isSubActive, url },
    showFormTitle = true,
    parentFormUrl,
    isUserTask,
  } = props;

  useFormRestrictionsCheck(form?.path, tenantKey);

  const updateApplicationStatus = useUpdateApplicationStatus();
  const [updatedSubmissionData, setUpdatedSubmissionData] = useState({});

  const applicationStatus = useSelector(
    (state) => state.applications.applicationDetail?.applicationStatus || ""
  );
  const userRoles = useSelector((state) => {
    return selectRoot("user", state).roles;
  });
  const applicationDetail = useSelector(
    (state) => state.applications.applicationDetail
  );
  const applicationDetailRef = useRef();

  const isFormSubmissionLoading = useSelector(
    (state) => state.formDelete.isFormSubmissionLoading
  );

  const customSubmission = useSelector(
    (state) => state.customSubmission?.submission || {}
  );
  const redirectUrl = useGetBaseUrl();
  const formRef = useRef();
  const enrichForm = useEnrichForm();
  const formSubmitCallback = useRef();
  const setFormSubmitCallback = (func) => (formSubmitCallback.current = func);
  const formSuccessModalParams = useRef();

  const headingRef = usePageTitleRef();

  useEffect(() => {
    if (applicationDetail) {
      applicationDetailRef.current = applicationDetail;
    }
  }, [applicationDetail]);

  useEffect(() => {
    // Check if the application is in "Resubmit" or "Awaiting Acknowledgement" status (old approach and it’s kept to have backward compatibility)
    // In the new approach, we will use the "isResubmit" key
    if (applicationStatus && !onFormSubmit) {
      if (
        getUserRolePermission(userRoles, CLIENT) &&
        !CLIENT_EDIT_STATUS.includes(applicationStatus) &&
        !applicationDetail.isResubmit
      ) {
        // Redirect the user to the submission view page if not allowed to edit
        dispatch(push(`/form/${formId}/submission/${submissionId}`));
      }
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    applicationStatus,
    userRoles,
    dispatch,
    submissionId,
    formId,
    onFormSubmit,
  ]);
  const updatedSubmission = useMemo(() => {
    if (CUSTOM_SUBMISSION_URL && CUSTOM_SUBMISSION_ENABLE) {
      return customSubmission;
    } else {
      return submission;
    }
  }, [customSubmission, submission]);

  if (
    isFormActive ||
    (isSubActive && !isFormSubmissionLoading && !formSubmitCallback?.current)
  ) {
    return <Loading />;
  }

  return (
    <div className="container">
      <div className="d-flex align-items-center justify-content-center w-100">
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
            } ${showFormTitle ? "" : "justify-content-end"}`}
          >
            {form.title && showFormTitle ? (
              <h3 tabIndex="-1" ref={isUserTask ? headingRef : null}>
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
          {formEmbeddedConstants.REACT_APP_SHOW_AUTO_FULFILLMENT_CHECKBOX ? (
            <AutofillDataModal formRef={formRef} />
          ) : null}
          <FormSuccessModal
            formRef={formRef}
            onInit={(params) => (formSuccessModalParams.current = params)}
          />
          <FormErrorModal />
          <FormSignDocumentModal
            formRef={formRef}
            setFormSubmitCallback={setFormSubmitCallback}
            onInit={(params) => {
              if (params.updateStatusOnInit) {
                updateApplicationStatus({
                  applicationId: formRef.current?.data?.applicationId,
                  formUrl: parentFormUrl || url,
                  applicationStatus:
                    formEmbeddedConstants.APPLICATION_STATUS
                      .SIGN_DOCUMENT_PENDING,
                });
              }
            }}
          />
          <Form
            form={form}
            submission={
              isFormSubmissionLoading
                ? updatedSubmissionData
                : updatedSubmission
            }
            url={url}
            hideComponents={hideComponents}
            onSubmit={(submission) => {
              setUpdatedSubmissionData(submission);
              onSubmit(
                submission,
                applicationDetailRef.current,
                onFormSubmit,
                form._id,
                redirectUrl,
                formSubmitCallback,
                formSuccessModalParams?.current?.processMessage,
                tenantKey
              );
            }}
            options={{
              ...options,
              i18n: getFormTranslations(),
              language: lang,
            }}
            onCustomEvent={onCustomEvent}
            formReady={(form) => {
              formRef.current = form;
              enrichForm(formRef);
            }}
          />
        </div>
      </LoadingOverlay>
    </div>
  );
});

Edit.defaultProps = {
  onCustomEvent: () => {},
};

const mapStateToProps = (state) => {
  return {
    user: state.user.userDetail,
    form: selectRoot("form", state),
    submission: selectRoot("submission", state),
    isAuthenticated: state.user.isAuthenticated,
    errors: [selectError("form", state), selectError("submission", state)],
    options: {
      noAlerts: !FORM_ALERTS_ENABLED,
      i18n: {
        en: {
          error: (
            <Translation>
              {(t) => t("Please fix the errors before submitting again.")}
            </Translation>
          ),
        },
      },
    },
    submissionError: selectRoot("formDelete", state).formSubmissionError,
  };
};

const onSubmissionErrorConfirm = () => {
  return (dispatch) => {
    const ErrorDetails = { modalOpen: false, message: "" };
    dispatch(setFormSubmissionError(ErrorDetails));
  };
};

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    onSubmit: (
      submission,
      applicationDetail,
      onFormSubmit,
      formId,
      redirectUrl,
      formSubmitCallback,
      processMessage,
      tenantKey
    ) => {
      !formSubmitCallback?.current && dispatch(setFormSubmissionLoading(true));
      const callBack = (err) => {
        if (!err) {
          if (
            UPDATE_EVENT_STATUS.includes(applicationDetail.applicationStatus) ||
            applicationDetail.isResubmit
          ) {
            const { data, url } = getProcessDataReq(
              applicationDetail,
              submission.data,
              processMessage
            );
            dispatch(
              updateApplicationEvent(url, data, (err) => {
                dispatch(setFormSubmissionLoading(false));
                if (!err) {
                  dispatch(resetSubmissions("submission"));
                  if (onFormSubmit) {
                    onFormSubmit(null, formSubmitCallback?.current);
                  } else {
                    FORM_ALERTS_ENABLED &&
                      toast.success(
                        <Translation>
                          {(t) => t("Submission Saved")}
                        </Translation>
                      );
                    dispatch(
                      push(
                        `${PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
                          ":tenantId",
                          tenantKey
                        )}`
                      )
                    );
                  }
                } else {
                  formSubmitCallback.current && formSubmitCallback.current(err);
                  dispatch(
                    setFormSubmissionError({
                      modalOpen: true,
                    })
                  );
                }
              })
            );
          } else {
            dispatch(resetSubmissions("submission"));
            dispatch(setFormSubmissionLoading(false));
            if (onFormSubmit) {
              onFormSubmit(null, formSubmitCallback?.current);
            } else {
              FORM_ALERTS_ENABLED &&
                toast.success(
                  <Translation>{(t) => t("Submission Saved")}</Translation>
                );
              dispatch(
                push(
                  `${PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION.replace(
                    ":tenantId",
                    tenantKey
                  )}`
                )
              );
            }
          }
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
        updateCustomSubmission(
          submission,
          onFormSubmit ? formId : ownProps.match.params.formId,
          callBack
        );
      } else if (ownProps.embeddedInTask) {
        updateSubmissionData(
          submission?._id,
          formId,
          submission?.data,
          callBack
        );
      } else {
        dispatch(
          saveSubmission(
            "submission",
            submission,
            onFormSubmit ? formId : ownProps.match.params.formId,
            callBack
          )
        );
      }
    },
    onConfirm: () => dispatch(onSubmissionErrorConfirm()),
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(Edit);
