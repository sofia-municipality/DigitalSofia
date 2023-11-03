//import Keycloak from "keycloak-js";
import { Translation } from "react-i18next";
//application details
export const APPLICATION_NAME =
  (window._env_ && window._env_.REACT_APP_APPLICATION_NAME) ||
  process.env.REACT_APP_APPLICATION_NAME ||
  "formsflow.ai";
//language details
export const LANGUAGE =
  (window._env_ && window._env_.REACT_APP_LANGUAGE) ||
  process.env.REACT_APP_LANGUAGE ||
  "bg";
//custom url
export const WEB_BASE_CUSTOM_URL =
  (window._env_ && window._env_.REACT_APP_WEB_BASE_CUSTOM_URL) ||
  process.env.REACT_APP_WEB_BASE_CUSTOM_URL ||
  "";
export const KEYCLOAK_ENABLE_CLIENT_AUTH_VARIABLE =
  (window._env_ && window._env_.REACT_APP_KEYCLOAK_ENABLE_CLIENT_AUTH) ||
  process.env.REACT_APP_KEYCLOAK_ENABLE_CLIENT_AUTH ||
  false;
export const KEYCLOAK_ENABLE_CLIENT_AUTH =
  KEYCLOAK_ENABLE_CLIENT_AUTH_VARIABLE === "true" ||
  KEYCLOAK_ENABLE_CLIENT_AUTH_VARIABLE === true
    ? true
    : false;
export const CUSTOM_SUBMISSION_URL =
  (window._env_ && window._env_.REACT_APP_CUSTOM_SUBMISSION_URL) ||
  process.env.REACT_APP_CUSTOM_SUBMISSION_URL ||
  "";
const CUSTOM_SUBMISSION_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_CUSTOM_SUBMISSION_ENABLED) ||
  process.env.REACT_APP_CUSTOM_SUBMISSION_ENABLED ||
  "";
export const CUSTOM_SUBMISSION_ENABLE =
  CUSTOM_SUBMISSION_ENABLED_VARIABLE === "true" ||
  CUSTOM_SUBMISSION_ENABLED_VARIABLE === true
    ? true
    : false;
//keycloak
export const Keycloak_Client =
  (window._env_ && window._env_.REACT_APP_KEYCLOAK_CLIENT) ||
  process.env.REACT_APP_KEYCLOAK_CLIENT ||
  "forms-flow-web";

const MULTITENANCY_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_MULTI_TENANCY_ENABLED) ||
  process.env.REACT_APP_MULTI_TENANCY_ENABLED ||
  false;
export const PUBLIC_WORKFLOW_ENABLED =
  (window._env_ && window._env_.REACT_APP_PUBLIC_WORKFLOW_ENABLED) === "true" ||
  process.env.REACT_APP_PUBLIC_WORKFLOW_ENABLED === "true"
    ? true
    : false;

export const MULTITENANCY_ENABLED =
  MULTITENANCY_ENABLED_VARIABLE === "true" ||
  MULTITENANCY_ENABLED_VARIABLE === true
    ? true
    : false;

export const TENANT_ID =
  (window._env_ && window._env_.REACT_APP_TENANT_ID) ||
  process.env.REACT_APP_TENANT_ID ||
  false;

export const BASE_ROUTE =
  !TENANT_ID && MULTITENANCY_ENABLED ? "/tenant/:tenantId/" : "/";

export const Keycloak_Tenant_Client = "forms-flow-web";

export const KEYCLOAK_REALM =
  (window._env_ && window._env_.REACT_APP_KEYCLOAK_URL_REALM) ||
  process.env.REACT_APP_KEYCLOAK_URL_REALM ||
  "forms-flow-ai";
export const KEYCLOAK_URL =
  (window._env_ && window._env_.REACT_APP_KEYCLOAK_URL) ||
  process.env.REACT_APP_KEYCLOAK_URL;
export const KEYCLOAK_AUTH_URL = `${KEYCLOAK_URL}/auth`;

export const CLIENT = "formsflow-client";
export const STAFF_DESIGNER = "formsflow-designer";
export const STAFF_REVIEWER = "formsflow-reviewer";
export const PAGE_ADMIN = "formsflow-page-admin";
export const ANONYMOUS_USER = "anonymous";

export const OPERATIONS = {
  insert: {
    action: "insert",
    buttonType: "primary button_font",
    icon: "pencil",
    permissionsResolver: function permissionsResolver() {
      return true;
    },
    title: <Translation>{(t) => t("Submit New")}</Translation>,
  },
  submission: {
    action: "submission",
    buttonType: "outline-primary button_font",
    icon: "list-alt",
    permissionsResolver: function permissionsResolver() {
      return true;
    },
    title: <Translation>{(t) => t("View Submissions")}</Translation>,
  },
  edit: {
    action: "edit",
    buttonType: "secondary button_font",
    icon: "edit",
    permissionsResolver: function permissionsResolver() {
      return true;
    },

    title: <Translation>{(t) => t("Edit Form")}</Translation>,
  },
  viewForm: {
    action: "viewForm",
    buttonType: "outline-primary button_font",
    icon: "pencil-square-o",
    permissionsResolver: function permissionsResolver() {
      return true;
    },

    title: <Translation>{(t) => t("View/Edit Form")}</Translation>,
  },
  delete: {
    action: "delete",
    buttonType: " delete_button",
    icon: "trash",
    permissionsResolver: function permissionsResolver() {
      return true;
    },
  },
  view: {
    action: "viewSubmission",
    buttonType: "primary",
    icon: "list",
    permissionsResolver: function permissionsResolver() {
      return true;
    },

    title: <Translation>{(t) => t("View")}</Translation>,
  },
  editSubmission: {
    action: "edit",
    buttonType: "secondary",
    icon: "edit",
    permissionsResolver: function permissionsResolver() {
      return true;
    },

    title: <Translation>{(t) => t("Edit")}</Translation>,
  },
  deleteSubmission: {
    action: "delete",
    buttonType: "danger",
    icon: "trash",
    permissionsResolver: function permissionsResolver() {
      return true;
    },

    title: <Translation>{(t) => t("Delete")}</Translation>,
  },
};

export const PageSizes = [5, 10, 25, 50, 100, "all"];

// draft config
const DRAFT_POLLING_RATE_FROM_ENV =
  (window._env_ && window._env_.REACT_APP_DRAFT_POLLING_RATE) ||
  process.env.REACT_APP_DRAFT_POLLING_RATE;
export const DRAFT_POLLING_RATE = DRAFT_POLLING_RATE_FROM_ENV
  ? Number(DRAFT_POLLING_RATE_FROM_ENV)
  : null;
const DRAFT_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_DRAFT_ENABLED) ||
  process.env.REACT_APP_DRAFT_ENABLED ||
  false;
export const DRAFT_ENABLED =
  DRAFT_ENABLED_VARIABLE === "true" || DRAFT_ENABLED_VARIABLE === true
    ? true
    : false;

const DRAFT_FEEDBACK_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_DRAFT_FEEDBACK_ENABLED) ||
  process.env.REACT_APP_DRAFT_FEEDBACK_ENABLED ||
  false;

export const DRAFT_FEEDBACK_ENABLED =
  DRAFT_FEEDBACK_ENABLED_VARIABLE === "true" ||
  DRAFT_FEEDBACK_ENABLED_VARIABLE === true
    ? true
    : false;

const DRAFT_SAVE_ON_EXIT_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_DRAFT_SAVE_ON_EXIT_ENABLED) ||
  process.env.REACT_APP_DRAFT_SAVE_ON_EXIT_ENABLED ||
  false;

export const DRAFT_SAVE_ON_EXIT_ENABLED =
  DRAFT_SAVE_ON_EXIT_ENABLED_VARIABLE === "true" ||
  DRAFT_SAVE_ON_EXIT_ENABLED_VARIABLE === true
    ? true
    : false;

const DRAFT_CREATE_ON_INIT_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_DRAFT_CREATE_ON_INIT_ENABLED) ||
  process.env.REACT_APP_DRAFT_CREATE_ON_INIT_ENABLED ||
  false;

export const DRAFT_CREATE_ON_INIT_ENABLED =
  DRAFT_CREATE_ON_INIT_ENABLED_VARIABLE === "true" ||
  DRAFT_CREATE_ON_INIT_ENABLED_VARIABLE === true
    ? true
    : false;

const FORM_ALERTS_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_FORM_ALERTS_ENABLED) ||
  process.env.REACT_APP_FORM_ALERTS_ENABLED ||
  false;

export const FORM_ALERTS_ENABLED =
  FORM_ALERTS_ENABLED_VARIABLE === "true" ||
  FORM_ALERTS_ENABLED_VARIABLE === true
    ? true
    : false;

const MOBILE_SECTIONS_VARIABLE =
  (window._env_ && window._env_.REACT_APP_MOBILE_SECTIONS_ENABLED) ||
  process.env.REACT_APP_MOBILE_SECTIONS_ENABLED ||
  false;

export const MOBILE_SECTIONS_ENABLED =
  MOBILE_SECTIONS_VARIABLE === "true" || MOBILE_SECTIONS_VARIABLE === true
    ? true
    : false;

// address form config
export const CURRENT_ADDRESS_FORM_PATH =
  (window._env_ && window._env_.REACT_APP_CURRENT_ADDRESS_FORM_PATH) ||
  process.env.REACT_APP_CURRENT_ADDRESS_FORM_PATH ||
  "changeofcurrentaddress";

export const PERMANENT_ADDRESS_FORM_PATH =
  (window._env_ && window._env_.REACT_APP_PERMANENT_ADDRESS_FORM_PATH) ||
  process.env.REACT_APP_PERMANENT_ADDRESS_FORM_PATH ||
  "changeofpernamentaddress";

export const FORM_PREFILLED_DATA_INPUT_NAME =
  (window._env_ && window._env_.REACT_APP_FORM_PREFILLED_DATA_INPUT_NAME) ||
  process.env.REACT_APP_FORM_PREFILLED_DATA_INPUT_NAME ||
  "behalf";

export const FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES = {
  MY_BEHALF: "myBehalf",
  CHILD: "child",
  OTHER_PERSON: "otherPerson",
};

export const SIGN_DOCUMENT_PROVIDERS = {
  EVROTRUST: "evrotrust",
};

export const SIGN_DOCUMENT_STATUSES = {
  UNSIGNED: "unsigned",
  PENDING: "signing",
  SIGNED: "signed",
  REJECTED: "rejected",
  EXPIRED: "expired",
  FAILED: "failed",
  WITHDRAWN: "withdrawn",
};

export const TAX_CATEGORIES = {
  REAL_ESTATE: "real_estate",
  HOUSEHOLD_WASTE: "household_waste",
  VEHICLE: "vehicle",
};

export const TAX_CATEGORIES_IDENTIFIER_PROP = {
  [TAX_CATEGORIES.REAL_ESTATE]: "propertyAddress",
  [TAX_CATEGORIES.HOUSEHOLD_WASTE]: "propertyAddress",
  [TAX_CATEGORIES.VEHICLE]: "registerNo",
};

export const REACT_APP_ENV =
  (window._env_ && window._env_.REACT_APP_ENV) || process.env.REACT_APP_ENV;

const CHECK_APPLICATION_PERMISSIONS_VARIABLE =
  (window._env_ &&
    window._env_.REACT_APP_CHECK_APPLICATION_PERMISSIONS_ENABLED) ||
  process.env.REACT_APP_CHECK_APPLICATION_PERMISSIONS_ENABLED ||
  false;

export const CHECK_APPLICATION_PERMISSIONS_VARIABLE_ENABLED =
  CHECK_APPLICATION_PERMISSIONS_VARIABLE === "true" ||
  CHECK_APPLICATION_PERMISSIONS_VARIABLE === true
    ? true
    : false;

export const CHECK_APPLICATION_PERMISSIONS_ENABLED =
  CHECK_APPLICATION_PERMISSIONS_VARIABLE_ENABLED &&
  !(
    (REACT_APP_ENV === "dev" || REACT_APP_ENV === "dhchodev") &&
    localStorage.getItem("CHECK_APPLICATION_PERMISSIONS_ENABLED") === "true"
  );

const DRAFT_DELETE_CTA_ENABLED_VARIABLE =
  (window._env_ && window._env_.REACT_APP_DRAFT_DELETE_CTA_ENABLED) ||
  process.env.REACT_APP_DRAFT_DELETE_CTA_ENABLED ||
  false;

export const DRAFT_DELETE_CTA_ENABLED =
  DRAFT_DELETE_CTA_ENABLED_VARIABLE === "true" ||
  DRAFT_DELETE_CTA_ENABLED_VARIABLE === true
    ? true
    : false;

const TASK_LIST_DISABLE_FILTER_VARIABLE =
  (window._env_ && window._env_.REACT_APP_TASK_LIST_DISABLE_FILTER) ||
  process.env.REACT_APP_TASK_LIST_DISABLE_FILTER ||
  false;

export const TASK_LIST_DISABLE_FILTER_ENABLED =
  TASK_LIST_DISABLE_FILTER_VARIABLE === "true" ||
  TASK_LIST_DISABLE_FILTER_VARIABLE === true
    ? true
    : false;
export const ENABLE_FORMS_MODULE =
  window._env_?.REACT_APP_ENABLE_FORMS_MODULE === "false" ||
  window._env_?.REACT_APP_ENABLE_FORMS_MODULE === false
    ? false
    : true;

export const ENABLE_TASKS_MODULE =
  window._env_?.REACT_APP_ENABLE_TASKS_MODULE === "false" ||
  window._env_?.REACT_APP_ENABLE_TASKS_MODULE === false
    ? false
    : true;

export const ENABLE_DASHBOARDS_MODULE =
  window._env_?.REACT_APP_ENABLE_DASHBOARDS_MODULE === "false" ||
  window._env_?.REACT_APP_ENABLE_DASHBOARDS_MODULE === false
    ? false
    : true;

export const ENABLE_PROCESSES_MODULE =
  window._env_?.REACT_APP_ENABLE_PROCESSES_MODULE === "false" ||
  window._env_?.REACT_APP_ENABLE_PROCESSES_MODULE === false
    ? false
    : true;

export const ENABLE_APPLICATIONS_MODULE =
  window._env_?.REACT_APP_ENABLE_APPLICATIONS_MODULE === "false" ||
  window._env_?.REACT_APP_ENABLE_APPLICATIONS_MODULE === false
    ? false
    : true;
