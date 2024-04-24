import { Translation } from "react-i18next";
import { TASK_LIST_DISABLE_FILTER_ENABLED } from "./constants";

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

export const getTaskSearchFilterDefaultParam = () => {
  const userDetailsString = localStorage.getItem("UserDetails") || "{}";
  const userDetails =
    userDetailsString == "undefined" ? "{}" : userDetailsString;
  let user = JSON.parse(userDetails);

  return TASK_LIST_DISABLE_FILTER_ENABLED
    ? {
        assigneeLike: `%${user.preferred_username}%`,
      }
    : {};
};
