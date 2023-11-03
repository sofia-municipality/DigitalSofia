import React, { useEffect, Suspense, lazy, useMemo } from "react";
import { Route, Switch, Redirect } from "react-router-dom";
import { useSelector } from "react-redux";
import { BASE_ROUTE, DRAFT_ENABLED } from "../constants/constants";
import UserService from "../services/UserService";
import {
  CLIENT,
  STAFF_REVIEWER,
  STAFF_DESIGNER,
  PAGE_ADMIN,
} from "../constants/constants";
import { PAGE_ROUTES } from "../constants/navigation";
import { useGetBaseUrl } from "../customHooks";

import Loading from "../containers/Loading";
import NotFound from "./NotFound";

const Form = lazy(() => import("./Form"));
const ServiceFlow = lazy(() => import("./ServiceFlow"));
const DashboardPage = lazy(() => import("./Dashboard"));
const InsightsPage = lazy(() => import("./Insights"));
const Application = lazy(() => import("./Application"));
const Admin = lazy(() => import("./Admin"));
const Modeler = lazy(() => import("./Modeler")); //BPMN Modeler
const Drafts = lazy(() => import("./Draft"));

const SOAdministration = lazy(() => import("./SOAdministration"));
const PageAdministration = lazy(() =>
  import("./SOAdministration/PageAdministration")
);
const BlocksAdministration = lazy(() =>
  import("./SOAdministration/BlocksAdministration")
);
const BlockEdit = lazy(() =>
  import("./SOAdministration/BlocksAdministration/BlockEdit")
);
const FaqAdministration = lazy(() =>
  import("./SOAdministration/FaqAdministration")
);
const FaqEdit = lazy(() =>
  import("./SOAdministration/FaqAdministration/FAQEdit")
);
const FaqAdd = lazy(() =>
  import("./SOAdministration/FaqAdministration/FAQAdd")
);
const TranslationAdministration = lazy(() =>
  import("./SOAdministration/TranslationAdministration")
);
const MyServicesAddressRegistration = lazy(() =>
  import("./sm/pages/MyServices/AddressRegistration")
);
const LocalTaxesAndFeesReference = lazy(() =>
  import("./sm/pages/RequestService/LocalTaxesAndFees/Reference")
);
const LocalTaxesAndFeesPayment = lazy(() =>
  import("./sm/pages/RequestService/LocalTaxesAndFees/Payment")
);
const UserTask = lazy(() => import("./sm/pages/UserTask"));
const Profile = lazy(() => import("./sm/pages/Profile"));

const PrivateRoute = React.memo((props) => {
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const userRoles = useSelector((state) => state.user.roles || []);
  const redirecUrl = useGetBaseUrl();
  useEffect(() => {
    if (!isAuth) {
      UserService.userLogin({ store: props.store });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuth]);

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

  const ClientReviewerRoute = useMemo(
    () =>
      ({ component: Component, ...rest }) =>
        (
          <Route
            {...rest}
            render={(props) =>
              userRoles.includes(STAFF_REVIEWER) ||
              userRoles.includes(CLIENT) ? (
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
              DRAFT_ENABLED &&
              (userRoles.includes(STAFF_REVIEWER) ||
                userRoles.includes(CLIENT)) ? (
                <Component {...props} />
              ) : (
                <Redirect exact to="/404" />
              )
            }
          />
        ),
    [userRoles]
  );

  return (
    <>
      {isAuth ? (
        <Suspense
          fallback={
            <div style={{ minHeight: "100svh" }}>
              <Loading />
            </div>
          }
        >
          <Switch>
            <Route path={`${BASE_ROUTE}form`} component={Form} />
            <DraftRoute path={`${BASE_ROUTE}draft`} component={Drafts} />
            <DesignerRoute path={`${BASE_ROUTE}admin`} component={Admin} />
            <DesignerRoute path={`${BASE_ROUTE}formflow`} component={Form} />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.SO_ADMINISTRATION}
              component={SOAdministration}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.PAGE_ADMINISTRATION}
              component={PageAdministration}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.BLOCKS_ADMINISTRATION}
              component={BlocksAdministration}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.BLOCKS_ADMINISTRATION_EDIT}
              component={BlockEdit}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.FAQ_ADMINISTRATION}
              component={FaqAdministration}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.FAQ_ADD}
              component={FaqAdd}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.FAQ_EDIT}
              component={FaqEdit}
            />
            <PageAdminRoute
              exact
              path={PAGE_ROUTES.TRANSLATION_ADMINISTRATION}
              component={TranslationAdministration}
            />
            <DesignerRoute
              path={`${BASE_ROUTE}processes`}
              component={Modeler}
            />
            <ClientReviewerRoute
              exact
              path={PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION}
              component={MyServicesAddressRegistration}
            />
            <ClientReviewerRoute
              exact
              path={PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE}
              component={LocalTaxesAndFeesReference}
            />
            <ClientReviewerRoute
              exact
              path={PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT}
              component={LocalTaxesAndFeesPayment}
            />
            <ClientReviewerRoute
              exact
              path={PAGE_ROUTES.USER_TASK}
              component={UserTask}
            />
            <ClientReviewerRoute
              exact
              path={PAGE_ROUTES.PROFILE}
              component={Profile}
            />
            <ClientReviewerRoute
              path={`${BASE_ROUTE}application`}
              component={Application}
            />
            <ReviewerRoute
              path={`${BASE_ROUTE}metrics`}
              component={DashboardPage}
            />
            <ReviewerRoute path={`${BASE_ROUTE}task`} component={ServiceFlow} />
            <ReviewerRoute
              path={`${BASE_ROUTE}insights`}
              component={InsightsPage}
            />
            <Route exact path={BASE_ROUTE}>
              <Redirect
                to={
                  userRoles.includes(STAFF_REVIEWER)
                    ? `${redirecUrl}task`
                    : `${redirecUrl}form`
                }
              />
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
