window["_env_"] = {
  // To define project level configuration  possible values development,test, production
  NODE_ENV: "development",
  //Environment Variables for forms-flow-web

  /*URL of forms-flow-forms
   Form-IO API-URL*/
  REACT_APP_API_SERVER_URL: "http://localhost:3001",
  // Form-IO API-PROJECT-URL
  REACT_APP_API_PROJECT_URL: "http://localhost:3001",
  // Keycloak-client-name for web
  REACT_APP_KEYCLOAK_CLIENT: "forms-flow-web",
  // Keycloak-Realm-name
  REACT_APP_KEYCLOAK_URL_REALM: "forms-flow-ai",
  // Keycloak URL
  REACT_APP_KEYCLOAK_URL: "<Keycloak URL>",

  ////Environment Variables for forms-flow-bpm////

  //Insight Api End point
  //REACT_APP_INSIGHT_API_BASE: "Insight Api base end-point",
  //REACT_APP_INSIGHTS_API_KEY: "<API_KEY from REDASH>",
  //web Api End point
  REACT_APP_WEB_BASE_URL: "Web Api base end-point",
  //bpm base api
  REACT_APP_BPM_URL: "Camunda base API",
  REACT_APP_WEBSOCKET_ENCRYPT_KEY: "Web Socket encrypt key for Socket IO",
  //application name
  REACT_APP_APPLICATION_NAME: "formsflow.ai",
  //custom url
  REACT_APP_WEB_BASE_CUSTOM_URL: "Custom URL",
  REACT_APP_CUSTOM_SUBMISSION_URL: "Custom Submission URL",
  REACT_APP_CUSTOM_SUBMISSION_ENABLED: "false",
  REACT_APP_USER_ACCESS_PERMISSIONS: {
    accessAllowApplications: false,
    accessAllowSubmissions: false,
  },
  REACT_APP_MULTI_TENANCY_ENABLED: "false",
  REACT_APP_TENANT_ID: "sofia",
  REACT_APP_DRAFT_ENABLED: "false",
  REACT_APP_DRAFT_POLLING_RATE: 15000,
  REACT_APP_DRAFT_FEEDBACK_ENABLED: false,
  REACT_APP_DRAFT_SAVE_ON_EXIT_ENABLED: false,
  REACT_APP_DRAFT_CREATE_ON_INIT_ENABLED: false,
  REACT_APP_CHECK_APPLICATION_PERMISSIONS_ENABLED: true,
  REACT_APP_FORM_ALERTS_ENABLED: false,
  REACT_APP_MOBILE_SECTIONS_ENABLED: false,
  REACT_APP_DRAFT_DELETE_CTA_ENABLED: false,
  REACT_APP_TASK_LIST_DISABLE_FILTER: false,
  REACT_APP_TRANSLATE_BPMN_MODELER: true,
  REACT_APP_TRANSLATE_FORM_BUILDER: true,
  REACT_APP_MY_SERVICES_LOCAL_TAXES_ENABLED: true,
  REACT_APP_SM_NEW_DESIGN_ENABLED: true,
  REACT_APP_CHECK_ASSURANCE_LEVEL_ENABLED: true,
  REACT_APP_EPAYMENT_REDIRECT_URL: "https://pay-test.egov.bg/Account/EAuth",
  REACT_APP_EPAYMENT_ACCESS_CODE_LOGIN_URL:
    "https://pay-test.egov.bg/Home/AccessByCode",
  REACT_APP_CURRENT_ADDRESS_FORM_PATH: "changeofcurrentaddress",
  REACT_APP_PERMANENT_ADDRESS_FORM_PATH: "changeofpernamentaddress",
  REACT_APP_FORM_PREFILLED_DATA_INPUT_NAME: "behalf",
  REACT_APP_EXPORT_PDF_ENABLED: "false",
  REACT_APP_PUBLIC_WORKFLOW_ENABLED: "false",
  REACT_APP_DOCUMENT_SERVICE_URL: "Custom URL",
  REACT_APP_OPENTELEMETRY_SERVICE: "Opentelemetry service",
  REACT_APP_SHOW_AUTO_FULFILLMENT_CHECKBOX: false,
};
