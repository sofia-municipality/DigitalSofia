import React, { useCallback, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getForm, getSubmission, Formio } from "react-formio";

import { setBPMTaskDetailLoader } from "../../../../actions/bpmTaskActions";
import { onBPMTaskFormSubmit } from "../../../../apiManager/services/bpmTaskServices";
import {
  useCheckApplicationPermissions,
  useGetBPMUserTaskDetail,
} from "../../../../apiManager/apiHooks";

import Loading from "../../../../containers/Loading";
import {
  getFormIdSubmissionIdFromURL,
  getFormUrlWithFormIdSubmissionId,
} from "../../../../apiManager/services/formatterService";
import FormEdit from "../../../Form/Item/Submission/Item/Edit";
import FormView from "../../../Form/Item/Submission/Item/View";
import LoadingOverlay from "react-loading-overlay";

import SubmissionError from "../../components/Modal/SubmissionErrorModal";
import { CUSTOM_EVENT_TYPE } from "../../../ServiceFlow/constants/customEventTypes";
import { getTaskSubmitFormReq } from "../../../../apiManager/services/bpmServices";
import { useParams } from "react-router-dom";
import { push } from "connected-react-router";
import {
  resetFormData,
  setFormSubmissionLoading,
  setFormSubmissionError,
} from "../../../../actions/formActions";
import {
  CUSTOM_SUBMISSION_URL,
  CUSTOM_SUBMISSION_ENABLE,
  CHECK_APPLICATION_PERMISSIONS_ENABLED,
} from "../../../../constants/constants";
import { getCustomSubmission } from "../../../../apiManager/services/FormServices";
import { getFormioRoleIds } from "../../../../apiManager/services/userservices";
import { useGetBaseUrl } from "../../../../customHooks";

const UserTask = React.memo(() => {
  const { taskId } = useParams();
  const {
    fetch: checkApplicationPermissions,
    isLoading: isCheckPermissionInProgress,
  } = useCheckApplicationPermissions();

  const {
    isLoading: isTaskLoading,
    data: task,
    error,
  } = useGetBPMUserTaskDetail(taskId);

  const dispatch = useDispatch();
  const currentUser = useSelector(
    (state) => state.user?.userDetail?.preferred_username || ""
  );
  const submissionErrorParams = useSelector(
    (state) => state.formDelete?.formSubmissionError
  );
  const [canEdit, setCanEdit] = useState(true);
  const redirectUrl = useGetBaseUrl();

  useEffect(() => {
    if (error) {
      dispatch(push("/404"));
    }
  }, [dispatch, error]);

  useEffect(() => {
    return () => {
      Formio.clearCache();
    };
  }, []);

  const getFormSubmissionData = useCallback(
    (formUrl) => {
      const { formId, submissionId } = getFormIdSubmissionIdFromURL(formUrl);
      Formio.clearCache();
      dispatch(resetFormData("form"));
      function fetchForm() {
        dispatch(
          getForm("form", formId, (err) => {
            if (!err) {
              if (CUSTOM_SUBMISSION_URL && CUSTOM_SUBMISSION_ENABLE) {
                dispatch(getCustomSubmission(submissionId, formId));
              } else {
                dispatch(getSubmission("submission", submissionId, formId));
              }
              dispatch(setFormSubmissionLoading(false));
            } else {
              if (err === "Bad Token" || err === "Token Expired") {
                dispatch(resetFormData("form"));
                dispatch(
                  getFormioRoleIds((err) => {
                    if (!err) {
                      fetchForm();
                    } else {
                      dispatch(setFormSubmissionLoading(false));
                    }
                  })
                );
              } else {
                dispatch(setFormSubmissionLoading(false));
              }
            }
          })
        );
      }

      const checkPermissions = CHECK_APPLICATION_PERMISSIONS_ENABLED
        ? checkApplicationPermissions
        : ({ callback }) => {
            callback();
          };

      checkPermissions({
        formioFormId: formId,
        taskId,
        callback: (err) => {
          if (!err) {
            setCanEdit(true);
          } else {
            setCanEdit(false);
          }

          fetchForm();
        },
      });
    },
    [dispatch, taskId, checkApplicationPermissions]
  );

  useEffect(() => {
    if (task?.taskFormUrl || task?.formUrl) {
      getFormSubmissionData(task?.taskFormUrl || task?.formUrl);
    }
  }, [task?.taskFormUrl, task?.formUrl, dispatch, getFormSubmissionData]);

  const onCustomEventCallBack = (customEvent) => {
    switch (customEvent.type) {
      case CUSTOM_EVENT_TYPE.ACTION_COMPLETE:
        onFormSubmitCallback(customEvent.actionType);
        break;
      default:
        return;
    }
  };

  const onFormSubmitCallback = (actionType = "", formSubmitCallback) => {
    if (taskId) {
      !formSubmitCallback && dispatch(setBPMTaskDetailLoader(true));
      const { formId, submissionId } = getFormIdSubmissionIdFromURL(
        task?.formUrl
      );
      const formUrl = getFormUrlWithFormIdSubmissionId(formId, submissionId);
      const origin = `${window.location.origin}${redirectUrl}`;
      const webFormUrl = `${origin}form/${formId}/submission/${submissionId}`;
      dispatch(
        onBPMTaskFormSubmit(
          taskId,
          getTaskSubmitFormReq(
            formUrl,
            task?.applicationId,
            actionType,
            webFormUrl
          ),
          (err) => {
            if (!err) {
              if (formSubmitCallback) {
                formSubmitCallback();
              } else {
                dispatch(push("/"));
              }
            } else {
              dispatch(setFormSubmissionLoading(false));
              if (formSubmitCallback) {
                formSubmitCallback(err);
              } else {
                dispatch(setBPMTaskDetailLoader(false));
              }
              dispatch(
                setFormSubmissionError({
                  modalOpen: true,
                })
              );
            }
          }
        )
      );
    }
  };

  if (isTaskLoading || isCheckPermissionInProgress) {
    return (
      <div className="service-task-details">
        <Loading />
      </div>
    );
  } else {
    /*TODO split render*/
    return (
      <div className="service-task-details">
        <LoadingOverlay
          active={
            task?.assignee?.toLowerCase() !== currentUser?.toLowerCase() ||
            !canEdit
          }
          styles={{
            overlay: (base) => ({
              ...base,
              background: "rgba(0, 0, 0, 0.2)",
              cursor: "not-allowed !important",
              minHeight: "100svh",
            }),
          }}
        >
          {task?.assignee?.toLowerCase() === currentUser?.toLowerCase() &&
          canEdit ? (
            <>
              <SubmissionError
                modalOpen={submissionErrorParams.modalOpen}
                message={submissionErrorParams.message}
                onConfirm={() => {
                  const ErrorDetails = { modalOpen: false, message: "" };
                  dispatch(setFormSubmissionError(ErrorDetails));
                }}
              />
              <FormEdit
                onFormSubmit={onFormSubmitCallback}
                onCustomEvent={onCustomEventCallBack}
                parentFormUrl={task?.formUrl}
                isUserTask={true}
              />
            </>
          ) : (
            <FormView showPrintButton={false} />
          )}
        </LoadingOverlay>
      </div>
    );
  }
});

export default UserTask;
