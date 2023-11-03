import { Translation } from "react-i18next";
import { TASK_LIST_DEFAULT_ASSIGNEE_ENABLED } from "./constants";

export const TASK_FILTER_LIST_DEFAULT_PARAM = {
  sortBy: "created",
  sortOrder: "desc",
  label: <Translation>{(t) => t("Created")}</Translation>,
};

const DEFAULT_WORKFLOW_PROCESS_KEY = "Defaultflow";
export const DEFAULT_WORKFLOW = {
  label: "Default Flow",
  value: DEFAULT_WORKFLOW_PROCESS_KEY,
};

export const getTaskSearchFilterDefaultParam = (preferred_username) => {
  return TASK_LIST_DEFAULT_ASSIGNEE_ENABLED
    ? {
        assigneeLike: `%${preferred_username}%`,
      }
    : {};
};
