import React, { useLayoutEffect, useEffect, useState } from "react";
import { Route, Switch, Redirect, useLocation } from "react-router-dom";
import { matchPath } from "react-router";
import { useSelector, useDispatch } from "react-redux";
import querystring from "querystring";
import i18n from "i18next";
import { Helmet } from "react-helmet";

import PublicRoute from "./PublicRoute";
import PrivateRoute from "./PrivateRoute";
import {
  BASE_ROUTE,
  CHECK_ASSURANCE_LEVEL_ENABLED,
} from "../constants/constants";
import {
  PAGE_ROUTES,
  SM_ROUTES,
  ROUTES_WITHOUT_NAV,
  ROUTES_WITH_ASSURANCE_PROTECTION,
  ROUTES_ASSURANCE_PROTECTION_SERVICE_NAMES,
} from "../constants/navigation";
import { setLanguage } from "../actions/languageSetAction";
import { openCloseForbiddenModal } from "../actions/roleActions";
import { updateUserlang } from "../apiManager/services/userservices";
import { useLogout, useCheckUserAssuranceLevel } from "../customHooks";

import SmNavigation from "./sm/components/Navigation";
import SmFooter from "./sm/components/Footer";
import ForbiddenModal, {
  ForbiddenModalTypes,
} from "./sm/components/Modal/ForbiddenModal";

/*import SideBar from "../containers/SideBar";*/
import NavBar from "../containers/NavBar";
import Footer from "../components/Footer";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import NotFound from "./NotFound";
import Home from "./sm/pages/Home";
import AddressRegistration from "./sm/pages/RequestService/AddressRegistration";
import SmFAQ from "./sm/pages/FAQ";
import Contacts from "./sm/pages/Contacts";
import TermsAndConditions from "./sm/pages/TermsAndConditions";
import CookiePolicy from "./sm/pages/CookiePolicy";
import PersonalData from "./sm/pages/PersonalData";
import RequestService from "./sm/pages/RequestService";
import LocalTaxesAndFees from "./sm/pages/RequestService/LocalTaxesAndFees";
import MyServices from "./sm/pages/MyServices";
import Loading from "../containers/Loading";

const BaseRouting = React.memo(({ store }) => {
  const dispatch = useDispatch();
  const logout = useLogout();
  const [showContent, setShowContent] = useState(false);
  const [isWithLangParam, setIsWithLangParam] = useState(false);
  const { search } = useLocation();
  const params = querystring.parse(search.replace("?", "")) || {};
  const { hideNav, lang, showRequestServiceLink } = params;

  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const userLanguage = useSelector((state) => state.user.lang);

  useLayoutEffect(() => {
    if (userLanguage) {
      document.documentElement.setAttribute("lang", userLanguage);
    }
  }, [userLanguage]);

  useEffect(() => {
    if (hideNav) {
      localStorage.setItem("hideNav", true);
    }
  }, [hideNav]);

  useEffect(() => {
    if (showRequestServiceLink) {
      sessionStorage.setItem("showRequestServiceLink", true);
    }
  }, [showRequestServiceLink]);

  useEffect(() => {
    if (lang && lang !== i18n.language) {
      setIsWithLangParam(true);
      dispatch(setLanguage(lang));
      if (isAuth) {
        dispatch(updateUserlang(lang));
      }

      i18n.changeLanguage(lang);
    }
  }, [lang, dispatch, isAuth]);

  useEffect(() => {
    if (isWithLangParam && lang === userLanguage) {
      setShowContent(true);
      setIsWithLangParam(false);
    } else if (!isWithLangParam) {
      setShowContent(true);
    }
  }, [isWithLangParam, lang, userLanguage]);

  useEffect(() => {
    const handleInvalidToken = (e) => {
      if (e.key === "logout-event" && !e.oldValue && e.newValue) {
        logout();
      }

      if (e.key === "authToken" && !e.oldValue && e.newValue) {
        window.location.reload();
      }
    };
    window.addEventListener("storage", handleInvalidToken);
    return function cleanup() {
      window.removeEventListener("storage", handleInvalidToken);
    };
  }, [logout]);

  return showContent ? (
    <PageWrapper isAuth={isAuth} hideNav={hideNav}>
      <ToastContainer />
      <Switch>
        <Route path={BASE_ROUTE}>
          <BaseRoute store={store} />
        </Route>
      </Switch>
    </PageWrapper>
  ) : (
    <Loading />
  );
});

const BaseRoute = ({ store }) => {
  return (
    <Switch>
      <Route exact index path={PAGE_ROUTES.HOME} component={Home} />
      <Route
        exact
        path={PAGE_ROUTES.ADDRESS_REGISTRATION}
        component={AddressRegistration}
      />
      <Route path={PAGE_ROUTES.FAQ} component={SmFAQ} />
      <Route path={PAGE_ROUTES.CONTACTS} component={Contacts} />
      <Route path={PAGE_ROUTES.PERSONAL_DATA} component={PersonalData} />
      <Route
        path={PAGE_ROUTES.TERMS_AND_CONDITIONS}
        component={TermsAndConditions}
      />
      <Route path={PAGE_ROUTES.COOKIE_POLICY} component={CookiePolicy} />
      <Route path={PAGE_ROUTES.REQUEST_SERVICE} component={RequestService} />
      <Route
        exact
        path={PAGE_ROUTES.LOCAL_TAXES_AND_FEES}
        component={LocalTaxesAndFees}
      />
      <Route exact path={PAGE_ROUTES.MY_SERVICES} component={MyServices} />
      <Route path="/public">
        <PublicRoute store={store} />
      </Route>
      <Route path={BASE_ROUTE}>
        <PrivateRoute store={store} />
      </Route>
      <Route path="/404" exact={true} component={NotFound} />
      <Redirect from="*" to="/404" />
    </Switch>
  );
};

const PageWrapper = (props) => {
  const dispatch = useDispatch();
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const checkUserAssuranceLevel = useCheckUserAssuranceLevel();
  const [isUserPermitted, setIsUserPermitted] = useState(true);
  const { pathname } = useLocation();
  const smRoutes = Object.values(SM_ROUTES);
  const isSmRoute = smRoutes.some((route) =>
    matchPath(pathname, { path: route, exact: true })
  );

  useEffect(() => {
    if (CHECK_ASSURANCE_LEVEL_ENABLED && isAuth) {
      const assuranceProtectedRoute = ROUTES_WITH_ASSURANCE_PROTECTION.find(
        (route) => matchPath(pathname, { path: route, exact: true })
      );

      const { isPassed, requiredAssuranceLevel } = checkUserAssuranceLevel(
        ROUTES_ASSURANCE_PROTECTION_SERVICE_NAMES[assuranceProtectedRoute]
      );

      if (assuranceProtectedRoute && !isPassed) {
        setIsUserPermitted(false);
        dispatch(
          openCloseForbiddenModal({
            isOpen: true,
            type: ForbiddenModalTypes.ASSURANCE_LEVEL,
            requiredAssuranceLevel,
          })
        );
      }
    }
  }, [checkUserAssuranceLevel, dispatch, pathname, isAuth]);

  return (
    <>
      <ForbiddenModal />
      {isUserPermitted ? (
        isSmRoute ? (
          <SmRoutesWrapper {...props} />
        ) : (
          <FormsFlowRoutesWrapper {...props} />
        )
      ) : null}
    </>
  );
};

const SmRoutesWrapper = ({ hideNav, children }) => {
  const adminOverridenRoutes = Object.values(ROUTES_WITHOUT_NAV);
  const isAdminOverridenRoute = adminOverridenRoutes.some((route) =>
    matchPath(window.location.pathname, { path: route, exact: true })
  );

  const isHideNavParamReceived =
    hideNav || localStorage.getItem("hideNav") === "true";

  return isAdminOverridenRoute || isHideNavParamReceived ? (
    <>
      {isHideNavParamReceived ? (
        <Helmet>
          <meta
            name="viewport"
            content="width=device-width,initial-scale=1.0, user-scalable=no, maximum-scale=1"
          />
        </Helmet>
      ) : null}
      {children}
    </>
  ) : (
    <>
      <SmNavigation />
      {children}
      <SmFooter />
    </>
  );
};

const FormsFlowRoutesWrapper = ({ isAuth, children }) => (
  <>
    {isAuth ? <NavBar /> : null}
    <div className="wrapper">
      {/*{isAuth?<SideBar store={store} />:null}*/}
      <div className="container-fluid content main-container">
        {children}
        {isAuth ? <Footer /> : null}
      </div>
    </div>
  </>
);

export default BaseRouting;
