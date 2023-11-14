import React, { useEffect, useRef } from "react";
import { ListGroup, Row } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { fetchServiceTaskList } from "../../../apiManager/services/bpmTaskServices";
import {
  setBPMTaskListActivePage,
  setBPMTaskLoader,
} from "../../../actions/bpmTaskActions";
import Loading from "../../../containers/Loading";
import { useTranslation } from "react-i18next";
import moment from "moment";
import {
  getProcessDataObjectFromList,
  getFormattedDateAndTime,
} from "../../../apiManager/services/formatterService";
import TaskFilterComponent from "./search/TaskFilterComponent";
import Pagination from "react-js-pagination";
import { push } from "connected-react-router";
import { MAX_RESULTS } from "../constants/taskConstants";
import { getFirstResultIndex } from "../../../apiManager/services/taskSearchParamsFormatterService";
import TaskVariable from "./TaskVariable";
import { MULTITENANCY_ENABLED, TASK_PAGE_NEW_DESIGN_ENABLED, TASK_LIST_DISABLE_FILTER_ENABLED } from "../../../constants/constants";
import { APPLICATION_STATUS_LABEL, DEFAULT_APPLICATION_STATUS_LABEL } from "../../../constants/formEmbeddedConstants";

const getRequestorNames = (data = {}) => {
  const firstName = data.requestorFirstName || data.firstName;
  const middleName = data.requestorMiddleName || data.middleName;
  const lastName = data.requestorFamilyName || data.lastName;

  if(firstName || middleName || lastName) {
    return firstName + " " + 
    middleName + " " + 
    lastName;
  }

  return 'None';
};

const ServiceFlowTaskList = React.memo(() => {
  const { t } = useTranslation();
  const taskList = useSelector((state) => state.bpmTasks.tasksList);
  const tasksCount = useSelector((state) => state.bpmTasks.tasksCount);
  const bpmTaskId = useSelector((state) => state.bpmTasks.taskId);
  const isTaskListLoading = useSelector(
    (state) => state.bpmTasks.isTaskListLoading
  );
  const reqData = useSelector((state) => state.bpmTasks.listReqParams);
  const dispatch = useDispatch();
  const processList = useSelector((state) => state.bpmTasks.processList);
  const selectedFilter = useSelector((state) => state.bpmTasks.selectedFilter);
  const activePage = useSelector((state) => state.bpmTasks.activePage);
  const tasksPerPage = MAX_RESULTS;
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const redirectUrl = useRef(
    MULTITENANCY_ENABLED ? `/tenant/${tenantKey}/` : "/"
  );

  useEffect(() => {
    if (selectedFilter) {
      dispatch(setBPMTaskLoader(true));
      dispatch(setBPMTaskListActivePage(1));
      dispatch(fetchServiceTaskList(selectedFilter.id, 0, reqData));
    }
  }, [dispatch, selectedFilter, reqData]);

  const getTaskDetails = (taskId) => {
    if (taskId !== bpmTaskId) {
      dispatch(push(`${redirectUrl.current}task/${taskId}`));
    }
  };

  const handlePageChange = (pageNumber) => {
    dispatch(setBPMTaskListActivePage(pageNumber));
    dispatch(setBPMTaskLoader(true));
    let firstResultIndex = getFirstResultIndex(pageNumber);
    dispatch(
      fetchServiceTaskList(selectedFilter.id, firstResultIndex, reqData)
    );
  };

  const renderTaskList = () => {
    if ((tasksCount || taskList.length) && selectedFilter) {
      return (
        <>
          {taskList.map((task, index) => (
            <div
              className={`clickable shadow border  ${
                task?.id === bpmTaskId && "selected"
              }`}
              key={index}
              onClick={() => getTaskDetails(task.id)}
            >
              {!TASK_PAGE_NEW_DESIGN_ENABLED ? (
                <>
                   <Row>
                    <div className="col-12">
                      <h5 className="font-weight-bold">{task.name}</h5>
                    </div>
                  </Row>
                  <div className="font-size-16 d-flex justify-content-between">
                    <div className="pr-0" style={{ maxWidth: "65%" }}>
                      <span data-toggle="tooltip" title="Form Name">
                        {
                          getProcessDataObjectFromList(
                            processList,
                            task.processDefinitionId
                          )?.name
                        }
                      </span>
                    </div>
                    <div
                      data-toggle="tooltip"
                      title={t("Task assignee")}
                      className="pr-0 text-right d-inline-block text-truncate"
                      style={{maxWidth:"150"}}
                    >
                      <span> {task.assignee}</span>
                    </div>
                  </div>
                  <div
                    className="d-flex justify-content-between text-muted"
                    style={{ marginBottom: "-8px", fontSize: "14px" }}
                  >
                    <div style={{ maxWidth: "70%" }}>
                      <span
                        className="tooltiptext"
                        title={task.due ? getFormattedDateAndTime(task.due) : ""}
                      >
                        {" "}
                        {task.due
                          ? `${t("Due")} ${moment(task.due).fromNow()}, `
                          : ""}{" "}
                      </span>
                      <span
                        className="tooltiptext"
                        title={
                          task.followUp
                            ? getFormattedDateAndTime(task.followUp)
                            : ""
                        }
                      >
                        {" "}
                        {task.followUp
                          ? `${t("Follow-up")} ${moment(task.followUp).fromNow()}, `
                          : ""}{" "}
                      </span>
                      <span
                        className="tooltiptext"
                        title={
                          task.created ? getFormattedDateAndTime(task.created) : ""
                        }
                      >
                        {" "}
                        {t("Created")} {moment(task.created).fromNow()}
                      </span>
                    </div>
                    <div className="pr-0 text-right tooltips" title={t("Priority")}>
                      {task.priority}
                    </div>
                  </div>

                  {task._embedded?.variable && (
                    <TaskVariable variables={task._embedded?.variable || []} />
                  )}
                </>
              ) : (
                <Row>
                   <div className="col-4">
                    <Row>
                      <div className="col-6">
                        <h6>Входящ номер: </h6>
                        <h6>УРИ: </h6>
                        <h6>Заявител: </h6>
                        <h6>Дата на получаване: </h6>
                        <h6>Статус: </h6>
                      </div>
                      <div className="col-6">
                        <h6 className="font-weight-bold">
                          {
                            task.submissionData?.exitNumber || 
                            task.submissionData?.reference_number ||
                            "None"
                          }
                        </h6>
                        <h6>
                          {
                            task.submissionData?.exitNumber || "None"
                          }
                        </h6>
                        <h6>{getRequestorNames(task.submissionData)}</h6>
                        <h6>{moment(task.created).format("DD.MM.YYYY")}</h6>
                        <h6>{t(
                            APPLICATION_STATUS_LABEL[task.submissionData?.applicationStatus] || 
                            DEFAULT_APPLICATION_STATUS_LABEL
                          )}
                        </h6>
                      </div>
                    </Row>
                   </div>
                   <div className="col-8 d-flex flex-column justify-content-center align-items-center">
                      <h6 className="font-size-16 text-right align-self-end">{task.submissionData?.documentTitle || task.name}</h6>
                      <div
                        data-toggle="tooltip"
                        title={t("Task assignee")}
                        className="pr-0 text-right d-inline-block text-truncate mt-4 align-self-end"
                        style={{maxWidth:"150"}}
                      >
                        <span> {task.assignee}</span>
                      </div>
                   </div>
                </Row>
              )}
            </div>
          ))}

          <Row style={{justifyContent: "flex-end"}} className={TASK_PAGE_NEW_DESIGN_ENABLED ? "mr-1" : ""}>
            <div className="pagination-wrapper">
              <Pagination
                activePage={activePage}
                itemsCountPerPage={tasksPerPage}
                totalItemsCount={tasksCount}
                pageRangeDisplayed={3}
                onChange={handlePageChange}
                prevPageText="<"
                nextPageText=">"
              />
            </div>
          </Row>

        </>
      );
    } else {
      return (
        <Row className="not-selected mt-2 ml-1">
          <i className="fa fa-info-circle mr-2 mt-1" />
          {t("No task matching filters found.")}
        </Row>
      );
    }
  };

  return (
    <>
      <ListGroup className="service-task-list">
        {!TASK_LIST_DISABLE_FILTER_ENABLED ? (
            <TaskFilterComponent
              totalTasks={isTaskListLoading ? 0 : tasksCount}
            />
          ) : null
        }
        {isTaskListLoading ? <Loading /> : renderTaskList()}
      </ListGroup>
    </>
  );
});

export default ServiceFlowTaskList;
