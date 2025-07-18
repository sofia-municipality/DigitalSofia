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
import { TASK_LIST_DISABLE_FILTER_ENABLED } from "../../../constants/constants";
import { getFirstResultIndex } from "../../../apiManager/services/taskSearchParamsFormatterService";
import TaskVariable from "./TaskVariable";
import { useGetBaseUrl } from "../../../customHooks";

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
  const baseUrl = useGetBaseUrl();
  const redirectUrl = useRef(baseUrl);

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
            //eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
            <div
              className={`clickable shadow border  ${
                task?.id === bpmTaskId && "selected"
              }`}
              key={index}
              onClick={() => getTaskDetails(task.id)}
            >
              <Row>
                <div className="col-12">
                  <h5 className="font-weight-bold">{t(task.name)}</h5>
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
                  style={{ maxWidth: "150" }}
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
            </div>
          ))}

          <Row style={{ justifyContent: "flex-end" }}>
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
        ) : null}
        {isTaskListLoading ? <Loading /> : renderTaskList()}
      </ListGroup>
    </>
  );
});

export default ServiceFlowTaskList;
