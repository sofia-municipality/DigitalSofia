/* eslint-disable no-unused-vars */
import React, { useEffect, Suspense, lazy, useMemo } from "react";
import { Route, Switch, Redirect, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import {
  BASE_ROUTE,
  DRAFT_ENABLED,
  MULTITENANCY_ENABLED,
  KEYCLOAK_AUTH_URL,
  Keycloak_Client,
  KEYCLOAK_REALM,
} from "../constants/constants";
import { KeycloakService, StorageService } from "@formsflow/service";
import {
  setUserAuth,
  setUserRole,
  setUserToken,
  setUserDetails,
} from "../actions/bpmActions";
import { setLanguage } from "../actions/languageSetAction";
import {
  STAFF_REVIEWER,
  STAFF_DESIGNER,
  ANALYTICS_VIEWER,
  PAGE_ADMIN,
  ADMIN,
  ENABLE_APPLICATIONS_MODULE,
  ENABLE_DASHBOARDS_MODULE,
  ENABLE_FORMS_MODULE,
  ENABLE_PROCESSES_MODULE,
  ENABLE_TASKS_MODULE,
  ENABLE_TRANSLATIONS_ADMINISTRATION_MODULE,
  TASK_PAGE_NEW_DESIGN_ENABLED,
  MULTI_LANGUAGE_ENABLED,
  LANGUAGE
} from "../constants/constants";

import Loading from "../containers/Loading";
import NotFound from "./NotFound";
import { setTenantFromId } from "../apiManager/services/tenantServices";

// Lazy imports is having issues with micro-front-end build

import Form from "./Form";
import ServiceFlow from "./ServiceFlow";
import DashboardPage from "./Dashboard";
import InsightsPage from "./Insights";
import Application from "./Application";
import Modeler from "./Modeler";
import Drafts from "./Draft";
import TranslationAdministration from "./TranslationAdministration";
import ServiceFlowTaskDetails from "./ServiceFlow/details/ServiceTaskDetails";

import {
  BPM_API_URL_WITH_VERSION,
  WEB_BASE_URL,
  WEB_BASE_CUSTOM_URL,
  CUSTOM_SUBMISSION_URL,
} from "../apiManager/endpoints/config";
import { AppConfig } from "../config";
import { getFormioRoleIds } from "../apiManager/services/userservices";
import { toast } from "react-toastify";

export const kcServiceInstance = (tenantId = null) => {
  return KeycloakService.getInstance(
    KEYCLOAK_AUTH_URL,
    KEYCLOAK_REALM,
    tenantId ? `${tenantId}-${Keycloak_Client}` : Keycloak_Client
  );
};

const setApiBaseUrlToLocalStorage = () => {
  localStorage.setItem("bpmApiUrl", BPM_API_URL_WITH_VERSION);
  localStorage.setItem("formioApiUrl", AppConfig.projectUrl);
  localStorage.setItem("formsflow.ai.url", window.location.origin);
  localStorage.setItem("formsflow.ai.api.url", WEB_BASE_URL);
  localStorage.setItem("customApiUrl", WEB_BASE_CUSTOM_URL);
  localStorage.setItem("customSubmissionUrl", CUSTOM_SUBMISSION_URL);
};

const PrivateRoute = React.memo((props) => {
  const { publish, subscribe, getKcInstance } = props;
  const dispatch = useDispatch();
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const userRoles = useSelector((state) => state.user.roles || []);
  const { tenantId } = useParams();
  const redirecUrl = MULTITENANCY_ENABLED ? `/tenant/${tenantId}/` : `/`;

  const [kcInstance, setKcInstance] = React.useState(getKcInstance());

  const authenticate = (instance, store) => {
    store.dispatch(
      setUserRole(JSON.parse(StorageService.get(StorageService.User.USER_ROLE)))
    );
    dispatch(setUserAuth(instance.isAuthenticated()));
    store.dispatch(setUserToken(instance.getToken()));
    MULTI_LANGUAGE_ENABLED && 
      store.dispatch(setLanguage(instance.getUserData()?.locale || LANGUAGE));
    //Set Cammunda/Formio Base URL
    setApiBaseUrlToLocalStorage();
    // get formio roles
    store.dispatch(
      getFormioRoleIds((err) => {
        if (err) {
          console.error(err);
          // doLogout();
        } else {
          store.dispatch(
            setUserDetails(
              JSON.parse(StorageService.get(StorageService.User.USER_DETAILS))
            )
          );

          // onAuthenticatedCallback();
        }
      })
    );
  };

  useEffect(() => {
    let instance = tenantId ? kcServiceInstance(tenantId) : kcServiceInstance();
    if (tenantId && props.store) {
      let currentTenant = sessionStorage.getItem("tenantKey");
      if (currentTenant && currentTenant !== tenantId) {
        sessionStorage.clear();
        localStorage.clear();
      }
      sessionStorage.setItem("tenantKey", tenantId);
      dispatch(setTenantFromId(tenantId));
    }
    if (props.store) {
      if (kcInstance) {
        authenticate(kcInstance, props.store);
      } else {
        instance.initKeycloak((authenticated) => {
          if(!authenticated)
          {
           toast.error("Unauthorized Access.",{autoClose: 3000}); 
           setTimeout(function() {
            instance.userLogout();
          }, 3000);
          }
          else{
            authenticate(instance, props.store);
            publish("FF_AUTH", instance);
          }
        });
      }
    }
  }, [props.store, tenantId, dispatch]);

  // useMemo prevents unneccessary rerendering caused by the route update.

  const DesignerRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(STAFF_DESIGNER) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const PageAdminRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(PAGE_ADMIN) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const AdminRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(ADMIN) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const AnalyticsViewerRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(ANALYTICS_VIEWER) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const ReviewerRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(STAFF_REVIEWER) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const DraftRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              DRAFT_ENABLED && userRoles.includes(ADMIN) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  const redirectUrlToHomePageBasedOnRole = () => {
    if (userRoles?.includes(STAFF_REVIEWER)) {
      return `${redirecUrl}task`;
    } else if (userRoles?.includes(STAFF_DESIGNER)) {
      return `${redirecUrl}form`;
    } else if (userRoles?.includes(PAGE_ADMIN)) {
      return `${redirecUrl}translations`;
    } else if (userRoles?.includes(ANALYTICS_VIEWER)) {
      return `${redirecUrl}metrics`;
    } else {
      return `${redirecUrl}application`;
    }
  };

  return (
    <>
      {isAuth ? (
        <Suspense fallback={<Loading />}>
          <Switch>
            {ENABLE_FORMS_MODULE && (
              <DesignerRoute path={`${BASE_ROUTE}form`} component={Form} />
            )}
            {ENABLE_FORMS_MODULE && (
              <DesignerRoute path={`${BASE_ROUTE}formflow`} component={Form} />
            )}
            {ENABLE_APPLICATIONS_MODULE && (
              <DraftRoute path={`${BASE_ROUTE}draft`} component={Drafts} />
            )}
            {ENABLE_APPLICATIONS_MODULE && (
              <AdminRoute
                path={`${BASE_ROUTE}application`}
                component={Application}
              />
            )}

            {ENABLE_PROCESSES_MODULE && (
              <DesignerRoute
                path={`${BASE_ROUTE}processes`}
                component={Modeler}
              />
            )}

            {ENABLE_DASHBOARDS_MODULE && (
              <AnalyticsViewerRoute
                path={`${BASE_ROUTE}metrics`}
                component={DashboardPage}
              />
            )}
            {ENABLE_DASHBOARDS_MODULE && (
              <AnalyticsViewerRoute
                path={`${BASE_ROUTE}insights`}
                component={InsightsPage}
              />
            )}
            {ENABLE_TASKS_MODULE && (
              <ReviewerRoute
                exact={TASK_PAGE_NEW_DESIGN_ENABLED}
                path={`${BASE_ROUTE}task`}
                component={ServiceFlow}
              />
            )}

            {TASK_PAGE_NEW_DESIGN_ENABLED && (
              <ReviewerRoute
                path={`${BASE_ROUTE}task/:taskId`}
                component={ServiceFlowTaskDetails}
              />
            )}

            {ENABLE_TRANSLATIONS_ADMINISTRATION_MODULE && (
              <PageAdminRoute
                exact
                path={`${BASE_ROUTE}translations`}
                component={TranslationAdministration}
              />
            )}

            <Route exact path={BASE_ROUTE}>
             {userRoles.length && <Redirect
                to={
                  redirectUrlToHomePageBasedOnRole()
                }
              />}
            </Route>
            <Route path="/404" exact={true} component={NotFound} />
            <Redirect from="*" to="/404" />
          </Switch>
        </Suspense>
      ) : (
        <Loading />
      )}
    </>
  );
});

export default PrivateRoute;
