import React, { useEffect, useMemo, useState } from "react";
import { Navbar, Container, Nav, NavDropdown } from "react-bootstrap";
import { Link, useLocation } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import UserService from "../services/UserService";
import {
  getUserRoleName,
  getUserRolePermission,
  // TODO : modify insigth permission conditions
  // getUserInsightsPermission,
} from "../helper/user";
import createURLPathMatchExp from "../helper/regExp/pathMatch";
import { useTranslation } from "react-i18next";
import "./styles.scss";
import {
  // STAFF_REVIEWER,
  APPLICATION_NAME,
  STAFF_DESIGNER,
  MULTITENANCY_ENABLED,
  TENANT_ID,
  PAGE_ADMIN,
  ANALYTICS_VIEWER,
  ADMIN,
} from "../constants/constants";
import { PAGE_ROUTES } from "../constants/navigation";
import { push } from "connected-react-router";
import i18n from "i18next";

import { setLanguage } from "../actions/languageSetAction";
import { updateUserlang } from "../apiManager/services/userservices";
import {
  useGetBaseUrl,
  useHandleNavResize,
  useGetUserEvrotrustName,
} from "../customHooks";

const NavBar = React.memo(() => {
  const isAuthenticated = useSelector((state) => state.user.isAuthenticated);
  const location = useLocation();
  const { pathname } = location;
  const user = useSelector((state) => state.user.userDetail);
  const lang = useSelector((state) => state.user.lang);
  const userRoles = useSelector((state) => state.user.roles);
  const showApplications = useSelector((state) => state.user.showApplications);
  const applicationTitle = useSelector(
    (state) => state.tenants?.tenantData?.details?.applicationTitle
  );
  const tenantKey = useSelector((state) => state.tenants?.tenantId);
  const formTenant = useSelector((state) => state.form?.form?.tenantKey);
  const baseUrl = useGetBaseUrl();
  const setNavRef = useHandleNavResize();

  /**
   * For anonymous forms the only way to identify the tenant is through the
   * form data with current implementation. To redirect to the correact tenant
   * we will use form as the data source for the tenantKey
   */

  const [loginUrl, setLoginUrl] = useState(baseUrl);

  const selectLanguages = useSelector((state) => state.user.selectLanguages);
  const dispatch = useDispatch();
  const logoPath = "/logo.svg";
  const getAppName = useMemo(
    () => () => {
      if (!MULTITENANCY_ENABLED) {
        return APPLICATION_NAME;
      }
      // TODO: Need a propper fallback component prefered a skeleton.
      return applicationTitle || "";
    },
    //eslint-disable-next-line react-hooks/exhaustive-deps
    [MULTITENANCY_ENABLED, applicationTitle]
  );
  const appName = getAppName();
  const { t } = useTranslation();
  const evrotrustFullName = useGetUserEvrotrustName();

  useEffect(() => {
    if (!isAuthenticated && formTenant && MULTITENANCY_ENABLED && !TENANT_ID) {
      setLoginUrl(`/tenant/${formTenant}/`);
    }
  }, [isAuthenticated, formTenant]);

  useEffect(() => {
    i18n.changeLanguage(lang);
  }, [lang]);

  const handleOnclick = (selectedLang) => {
    dispatch(setLanguage(selectedLang));
    dispatch(updateUserlang(selectedLang));
  };
  const logout = () => {
    dispatch(push(baseUrl));
    UserService.userLogout();
  };
  // const goToTask = () => {
  //   dispatch(push(`${baseUrl}task`));
  // };

  return (
    <header>
      <Navbar
        ref={setNavRef}
        expand="lg"
        bg="white"
        className="topheading-border-bottom"
        fixed="top"
      >
        <Container fluid>
          <Navbar.Brand className="d-flex">
            <Link to={`${baseUrl}`}>
              <img
                className="img-fluid"
                src={logoPath}
                width="50"
                height="55"
                alt="Logo"
              />
            </Link>
            <div className="custom-app-name">{appName}</div>
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="responsive-navbar-nav " />
          {isAuthenticated ? (
            <Navbar.Collapse id="responsive-navbar-nav" className="navbar-nav">
              <Nav id="main-menu-nav" className="active align-items-lg-center">
                {getUserRolePermission(userRoles, STAFF_DESIGNER) ? (
                  <Nav.Link
                    as={Link}
                    to={`${baseUrl}form`}
                    className={`main-nav nav-item ${
                      pathname.match(createURLPathMatchExp("form", baseUrl))
                        ? "active-tab"
                        : ""
                    }`}
                  >
                    <i className="fa fa-wpforms fa-fw fa-lg mr-2" />
                    {t("Forms")}
                  </Nav.Link>
                ) : null}

                {getUserRolePermission(userRoles, ADMIN) ? (
                  <Nav.Link
                    as={Link}
                    to={`${baseUrl}admin`}
                    className={`main-nav nav-item ${
                      pathname.match(createURLPathMatchExp("admin", baseUrl))
                        ? "active-tab"
                        : ""
                    }`}
                  >
                    <i className="fa fa-user-circle-o fa-lg mr-2" />
                    {t("Admin")}
                  </Nav.Link>
                ) : null}

                {getUserRolePermission(userRoles, STAFF_DESIGNER) ? (
                  <Nav.Link
                    as={Link}
                    to={`${baseUrl}processes`}
                    className={`main-nav nav-item ${
                      pathname.match(
                        createURLPathMatchExp("processes", baseUrl)
                      )
                        ? "active-tab"
                        : ""
                    }`}
                  >
                    <i className="fa fa-cogs fa-lg fa-fw mr-2" />
                    {t("Processes")}
                  </Nav.Link>
                ) : null}

                {getUserRolePermission(userRoles, PAGE_ADMIN) ? (
                  <Nav.Link
                    as={Link}
                    to={`${PAGE_ROUTES.SO_ADMINISTRATION.replace(
                      ":tenantId",
                      tenantKey
                    )}`}
                    className={`main-nav nav-item ${
                      pathname.match(PAGE_ROUTES.SO_ADMINISTRATION)
                        ? "active-tab"
                        : ""
                    }`}
                  >
                    <i className="fa fa-cogs fa-lg fa-fw mr-2" />
                    {t("so.administration")}
                  </Nav.Link>
                ) : null}

                {showApplications ? (
                  getUserRolePermission(userRoles, ADMIN) ? (
                    <Nav.Link
                      as={Link}
                      to={`${baseUrl}application`}
                      className={`main-nav nav-item ${
                        pathname.match(
                          createURLPathMatchExp("application", baseUrl)
                        )
                          ? "active-tab"
                          : pathname.match(
                              createURLPathMatchExp("draft", baseUrl)
                            )
                          ? "active-tab"
                          : ""
                      }`}
                    >
                      {" "}
                      <i className="fa fa-list-alt fa-fw fa-lg mr-2" />
                      {t("Applications")}
                    </Nav.Link>
                  ) : null
                ) : null}
                {/* {getUserRolePermission(userRoles, STAFF_REVIEWER) ? (
                  <Nav.Link
                    as={Link}
                    to={`${baseUrl}task`}
                    className={`main-nav nav-item taskDropdown ${
                      pathname.match(createURLPathMatchExp("task", baseUrl))
                        ? "active-tab"
                        : ""
                    }`}
                    onClick={goToTask}
                  >
                    {" "}
                    <i className="fa fa-list fa-lg fa-fw mr-2" />
                    {t("Tasks")}
                  </Nav.Link>
                ) : null} */}

                {getUserRolePermission(userRoles, ANALYTICS_VIEWER) ? (
                  <Nav.Link
                    as={Link}
                    to={`${baseUrl}metrics`}
                    data-testid="Dashboards"
                    className={`main-nav nav-item ${
                      pathname.match(
                        createURLPathMatchExp("metrics", baseUrl)
                      ) ||
                      pathname.match(createURLPathMatchExp("insights", baseUrl))
                        ? "active-tab"
                        : ""
                    }`}
                  >
                    {" "}
                    <i className="fa fa-tachometer fa-lg fa-fw mr-2" />
                    {t("Dashboards")}
                  </Nav.Link>
                ) : null}
              </Nav>

              <Nav className="px-lg-0 px-3">
                {selectLanguages.length === 1 ? (
                  selectLanguages.map((e, i) => {
                    return (
                      <>
                        <i className="fa fa-globe fa-lg mr-1 mt-1" />
                        <h4 key={i}>{e.name}</h4>
                      </>
                    );
                  })
                ) : (
                  <NavDropdown
                    title={
                      <>
                        <i className="fa fa-globe fa-lg mr-2" />
                        {lang ? lang : "LANGUAGE"}
                      </>
                    }
                    id="basic-nav-dropdown"
                  >
                    {selectLanguages.map((e, index) => (
                      <NavDropdown.Item
                        key={index}
                        onClick={() => {
                          handleOnclick(e.name);
                        }}
                      >
                        {e.value}{" "}
                      </NavDropdown.Item>
                    ))}
                  </NavDropdown>
                )}
              </Nav>

              <Nav className="px-lg-0 px-3">
                <NavDropdown
                  title={
                    // here

                    <>
                      <i className="fa fa-user fa-lg mr-1" />
                      {evrotrustFullName ||
                        user?.name ||
                        user?.preferred_username ||
                        ""}
                    </>
                  }
                >
                  <NavDropdown.Item>
                    {" "}
                    {/* here */}
                    {evrotrustFullName ||
                      user?.name ||
                      user?.preferred_username}
                    <br />
                    <i className="fa fa-users fa-lg fa-fw" />
                    <b>{t(getUserRoleName(userRoles))}</b>
                  </NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item onClick={logout}>
                    <i className="fa fa-sign-out fa-fw" /> {t("Logout")}{" "}
                  </NavDropdown.Item>
                </NavDropdown>
              </Nav>
            </Navbar.Collapse>
          ) : (
            <Link to={loginUrl} className="btn btn-primary">
              Login
            </Link>
          )}
        </Container>
      </Navbar>
    </header>
  );
});

export default NavBar;
