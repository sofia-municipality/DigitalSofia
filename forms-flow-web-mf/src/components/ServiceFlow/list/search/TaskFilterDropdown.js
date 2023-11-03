import { taskFilters } from "../../constants/taskConstants";
import { TASK_LIST_DEFAULT_ASSIGNEE_ENABLED } from "../../../../constants/constants";
import React from "react";

const TaskFilterDropdown = React.memo(({ onFilterSelect }) => {
  return (
    <div className="filter-items">
      {taskFilters.filter(filter => {
        if(TASK_LIST_DEFAULT_ASSIGNEE_ENABLED) {
          return filter.key !== "assignee";
        }
        return true;
      }).map((filter, index) => (
        <div
          key={index}
          className="clickable p-0 mb-2"
          onClick={() => onFilterSelect(filter)}
        >
          {filter.label}
        </div>
      ))}
    </div>
  );
});

export default TaskFilterDropdown;
