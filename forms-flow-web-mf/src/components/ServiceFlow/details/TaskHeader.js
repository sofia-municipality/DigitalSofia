import React, { useEffect, useState } from "react";
import { Row, Col } from "react-bootstrap";
import {
  getISODateTime,
  getFormattedDateAndTime,
  getProcessDataObjectFromList,
} from "../../../apiManager/services/formatterService";
import { useDispatch, useSelector } from "react-redux";
import DatePicker from "react-datepicker";
import moment from "moment";
import "react-datepicker/dist/react-datepicker.css";
import "./../ServiceFlow.scss";
import AddGroupModal from "./AddGroupModal";
import {
  claimBPMTask,
  // fetchFilterList,
  fetchServiceTaskList,
  getBPMTaskDetail,
  unClaimBPMTask,
  updateAssigneeBPMTask,
  updateBPMTask,
} from "../../../apiManager/services/bpmTaskServices";
import { TASK_PAGE_TASK_HEADER_ACTIONS_ENABLED, TASK_PAGE_NEW_DESIGN_ENABLED, MULTITENANCY_ENABLED } from '../../../constants/constants';
import { setBPMTaskDetailUpdating } from "../../../actions/bpmTaskActions";
//import UserSelection from "./UserSelection";
import UserSelectionDebounce from "./UserSelectionDebounce";
import SocketIOService from "../../../services/SocketIOService";
import { useTranslation } from "react-i18next";
import CloseIcon from "@mui/icons-material/Close";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../Buttons/SmCta";
import { useDevice } from "../../../customHooks";
import { jsonTryParse } from "../../../utils";

const TaskHeader = React.memo(() => {
  const task = useSelector((state) => state.bpmTasks.taskDetail);
  const taskId = useSelector((state) => state.bpmTasks.taskId);
  const processList = useSelector((state) => state.bpmTasks.processList);
  const username = useSelector(
    (state) => state.user?.userDetail?.preferred_username || ""
  );
  const taskGroups = useSelector((state) => state.bpmTasks.taskGroups);
  const isCandidateUser = !!taskGroups?.find(e => e.userId === username);
  const selectedFilter = useSelector((state) => state.bpmTasks.selectedFilter);
  const reqData = useSelector((state) => state.bpmTasks.listReqParams);
  const firstResult = useSelector((state) => state.bpmTasks.firstResult);
  const submissionData = useSelector((state) => state.submission?.submission?.data);
  const acstreData = jsonTryParse(submissionData?.acstreData);
  const caseDataSource = jsonTryParse(submissionData?.caseDataSource);
  const [followUpDate, setFollowUpDate] = useState(null);
  const [dueDate, setDueDate] = useState(null);
  const [showModal, setModal] = useState(false);
  const [isEditAssignee, setIsEditAssignee] = useState(false);
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isMobile } = useDevice();

  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const redirectUrl = MULTITENANCY_ENABLED ? `/tenant/${tenantKey}/` : "/";

  useEffect(() => {
    const followUp = task?.followUp ? new Date(task?.followUp) : null;
    setFollowUpDate(followUp);
  }, [task?.followUp]);

  useEffect(() => {
    const due = task?.due ? new Date(task?.due) : null;
    setDueDate(due);
  }, [task?.due]);

  const onClaim = () => {
    dispatch(setBPMTaskDetailUpdating(true));
    dispatch(
      // eslint-disable-next-line no-unused-vars
      claimBPMTask(taskId, username, (err, response) => {
        if (!err) {
          if (!SocketIOService.isConnected()) {
            if (selectedFilter) {
              dispatch(getBPMTaskDetail(taskId));
              dispatch(
                fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
              );
            } else {
              dispatch(setBPMTaskDetailUpdating(false));
            }
          }
          if(selectedFilter){
            dispatch(
              fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
            );
          }
           
        } else {
          dispatch(setBPMTaskDetailUpdating(false));
        }
      })
    );
  };
  const onChangeClaim = (userId) => {
    setIsEditAssignee(false);
    if (userId && userId !== task.assignee) {
      dispatch(setBPMTaskDetailUpdating(true));
      dispatch(
        // eslint-disable-next-line no-unused-vars
        updateAssigneeBPMTask(taskId, userId, (err, response) => {
          if (!err) {
            if (!SocketIOService.isConnected()) {
              if (selectedFilter) {
                dispatch(getBPMTaskDetail(taskId));
              }
            }
            if(selectedFilter){
              dispatch(
                fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
              );
            }
           
          } else {
            dispatch(setBPMTaskDetailUpdating(false));
          }
        })
      );
    }
  };

  const onUnClaimTask = () => {
    dispatch(setBPMTaskDetailUpdating(true));
    dispatch(
      // eslint-disable-next-line no-unused-vars
      unClaimBPMTask(taskId, (err, response) => {
        if (!err) {
          if (!SocketIOService.isConnected()) {
            if (selectedFilter) {
              dispatch(getBPMTaskDetail(taskId));
            }
          }
          if(selectedFilter){
            dispatch(
              fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
            );
          }
          
        } else {
          dispatch(setBPMTaskDetailUpdating(false));
        }
      })
    );
  };

  const onFollowUpDateUpdate = (followUpDate) => {
    setFollowUpDate(followUpDate);
    dispatch(setBPMTaskDetailUpdating(true));
    const updatedTask = {
      ...task,
      ...{ followUp: followUpDate ? getISODateTime(followUpDate) : null },
    };
    dispatch(
      // eslint-disable-next-line no-unused-vars
      updateBPMTask(taskId, updatedTask, (err, response) => {
        if (!err) {
          if (!SocketIOService.isConnected()) {
            dispatch(getBPMTaskDetail(taskId));
            dispatch(
              fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
            );
          }
        } else {
          dispatch(setBPMTaskDetailUpdating(false));
        }
      })
    );
  };

  const onDueDateUpdate = (dueDate) => {
    setDueDate(dueDate);
    dispatch(setBPMTaskDetailUpdating(true));
    const updatedTask = {
      ...task,
      ...{ due: dueDate ? getISODateTime(dueDate) : null },
    };
    dispatch(
      // eslint-disable-next-line no-unused-vars
      updateBPMTask(taskId, updatedTask, (err, response) => {
        if (!err) {
          if (!SocketIOService.isConnected()) {
            dispatch(getBPMTaskDetail(taskId));
            dispatch(
              fetchServiceTaskList(selectedFilter.id, firstResult, reqData)
            );
          }
        } else {
          dispatch(setBPMTaskDetailUpdating(false));
        }
      })
    );
  };

  // eslint-disable-next-line no-unused-vars
  const FollowUpDateInput = React.forwardRef(({ value, onClick }, ref) => {
    return (
      <div onClick={onClick} ref={ref}>
        <i className="fa fa-calendar mr-1" />{" "}
        {followUpDate ? (
          <span className="mr-4">{moment(followUpDate).fromNow()}</span>
        ) : (
          t("Set follow-up Date")
        )}
      </div>
    );
  });

  // eslint-disable-next-line no-unused-vars
  const DueDateInput = React.forwardRef(({ value, onClick }, ref) => {
    return (
      <div onClick={onClick} ref={ref}>
        <i className="fa fa-bell mr-1" />{" "}
        {dueDate ? (
          <span className="mr-4">{moment(dueDate).fromNow()}</span>
        ) : (
          t("Set Due date")
        )}
      </div>
    );
  });

  const getGroups = (groups) => {
    return groups?.filter((group) => group.groupId)?.map((group) => group.groupId).join(", ");
  };

  return (
    <>
      <AddGroupModal
        modalOpen={showModal}
        onClose={() => setModal(false)}
        groups={taskGroups}
      />
      <Row className="d-flex justify-content-between">
        {isMobile && TASK_PAGE_NEW_DESIGN_ENABLED ? (
          <Col lg="1" className="d-flex justify-content-end">
            <SmCta
              className="align-self-start mr-3"
              size={SmCtaSizes.SMALL}
              type={SmCtaTypes.OUTLINE}
              href={`${redirectUrl}task`}
              isLink
              accessibilityProps={{
                "aria-label": t("screen.reader.modal.close.cta"),
              }}
            >
              <CloseIcon/>
            </SmCta>
          </Col>
        ) : null
        }
        <Col lg={TASK_PAGE_NEW_DESIGN_ENABLED ? "5" : undefined} className="d-flex flex-column" style={{gap: "10px"}}>
          <Row className="ml-0 task-header">{task?.name}</Row>
          <Row className="ml-0 task-name">
            <span className="application-id" data-title={t("Process Name")}>
              {" "}
              {
                getProcessDataObjectFromList(processList, task?.processDefinitionId)
                  ?.name
              }
            </span>
          </Row>
          <Row className="ml-0">
            <span data-title={t("Application ID")} className="application-id">
              {t("Application ID")}# {task?.applicationId}
            </span>
          </Row>
        </Col>
        {TASK_PAGE_NEW_DESIGN_ENABLED && (
          <>
            { submissionData ? (
              <Col lg="6" className="d-flex flex-column justify-content-between mt-3" style={{gap: "10px"}}>
                <Row className="ml-0 align-self-lg-end">
                    <span className="application-id text-lg-right" data-title={t("Входящ номер")}>
                        <span className="mr-2 font-weight-bold">{`${t("Входящ номер")}:`}</span>
                        <span>{acstreData?.externalId || "None"}</span>
                    </span>
                </Row>
                <Row className="ml-0 align-self-lg-end"> 
                    <span className="application-id text-lg-right" data-title={t("Референтен номер")}>
                        <span className="mr-2 font-weight-bold">{`${t("Референтен номер")}:`}</span>
                        <span>{submissionData?.businessKey || "None"}</span>
                    </span>
                </Row>
                <Row className="ml-0 align-self-lg-end">
                    <span className="application-id text-lg-right" data-title={t("Име на услуга")}>
                        {caseDataSource?.data?.serviceId ? (
                            <span className="">{caseDataSource?.data?.serviceId} {'- '}</span>
                          ) : null
                        }
                        <span>
                          {
                            caseDataSource?.data?.serviceName || 
                            submissionData?.documentTitle ||
                            "None"
                          }
                        </span>
                    </span>
                </Row>
              </Col> 
            ) : null}
            {!isMobile ? (
              <Col lg="1" className="d-flex justify-content-end">
                <SmCta
                  className="align-self-start mr-3"
                  size={SmCtaSizes.SMALL}
                  type={SmCtaTypes.OUTLINE}
                  href={`${redirectUrl}task`}
                  isLink
                  accessibilityProps={{
                    "aria-label": t("screen.reader.modal.close.cta"),
                  }}
                >
                  <CloseIcon/>
                </SmCta>
              </Col>
            ) : null}
          </>
        )}
      </Row>
      
      {TASK_PAGE_TASK_HEADER_ACTIONS_ENABLED || isCandidateUser ?
        (
          <Row className="actionable mb-4" style={!TASK_PAGE_TASK_HEADER_ACTIONS_ENABLED ? {cursor: "default"} : {}}>
            {TASK_PAGE_TASK_HEADER_ACTIONS_ENABLED ? (
              <>
                <Col
                  sm={followUpDate ? 2 : "auto"}
                  data-title={
                    followUpDate
                      ? getFormattedDateAndTime(followUpDate)
                      : t("Set FollowUp Date")
                  }
                  className="date-container"
                >
                  <DatePicker
                    selected={followUpDate}
                    onChange={onFollowUpDateUpdate}
                    showTimeSelect
                    isClearable
                    popperPlacement="bottom-start"
                    popperModifiers={{
                      offset: {
                        enabled: true,
                        offset: "5px, 10px",
                      },
                      preventOverflow: {
                        enabled: true,
                        escapeWithReference: false,
                        boundariesElement: "viewport",
                      },
                    }}
                    customInput={<FollowUpDateInput />}
                  />
                </Col>
                <Col
                  sm={dueDate ? 2 : "auto"}
                  data-title={
                    dueDate ? getFormattedDateAndTime(dueDate) : t("Set Due date")
                  }
                  className="date-container"
                >
                  <DatePicker
                    selected={dueDate}
                    onChange={onDueDateUpdate}
                    showTimeSelect
                    isClearable
                    shouldCloseOnSelect
                    popperPlacement="bottom-start"
                    popperModifiers={{
                      offset: {
                        enabled: true,
                        offset: "5px, 10px",
                      },
                      preventOverflow: {
                        enabled: true,
                        escapeWithReference: false,
                        boundariesElement: "viewport",
                      },
                    }}
                    customInput={<DueDateInput />}
                  />
                </Col>
                <Col
                  className="center-position"
                  sm={4}
                  onClick={() => setModal(true)}
                  data-title={t("groups")}
                >
                  <i className="fa fa-group mr-1" />
                  {taskGroups.length === 0 ? (
                    <span>{t("Add groups")}</span>
                  ) : (
                    <span className="group-align">{getGroups(taskGroups)}</span>
                  )}
                </Col>
              </>
            ) : null}
            
            <Col className="d-flex text-right align-items-end justify-content-end mr-3">
              {isEditAssignee ? (
                task?.assignee ? (
                  <span style={{minWidth: "300px"}}>
                    <UserSelectionDebounce
                      onClose={() => setIsEditAssignee(false)}
                      currentUser={task.assignee}
                      onChangeClaim={onChangeClaim}
                    />
                  </span>
                ) : (
                  <div style={{cursor: "pointer"}}>
                    <span data-testid="clam-btn" onClick={onClaim}>
                      {" "}
                      {t("Claim")}
                    </span>
                  </div>
                )
              ) : (
                <div style={{cursor: "pointer"}}>
                  <i className="fa fa-user mr-1" />
                  {task?.assignee ? (
                    <span>
                      <span
                        className="change-tooltip"
                        onClick={isCandidateUser ? onUnClaimTask : () => setIsEditAssignee(true)}
                        data-title={t("Click to Change Assignee")}
                      >
                        {task.assignee}
                      </span>
                      <i
                        className="fa fa-times ml-1"
                        onClick={onUnClaimTask}
                        data-title={t("Reset Assignee")}
                      />
                    </span>
                  ) : (
                    <span data-testid="clam-btn" onClick={onClaim}>
                      {t("Claim")}
                    </span>
                  )}
                </div>
              )}
            </Col>
          </Row>
        ) : <div className="mb-5"/>
      }
    </>
  );
});

export default TaskHeader;
