import React from "react";
import { useSelector } from "react-redux";
import { useLocation } from "react-router-dom";
import { matchPath } from "react-router";

import styles from "./login-button.module.scss";
import { useLogin } from "../../../../../customHooks";
import { PAGE_ROUTES } from "../../../../../constants/navigation";

import SmCta from "../SmCta";

const LoginButton = ({ children, className = null }) => {
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const tenantId = useSelector((state) => state.tenants?.tenantId);
  const { pathname } = useLocation();
  const isHome = matchPath(pathname, { path: PAGE_ROUTES.HOME, exact: true });
  const { origin } = window.location;
  const fallbackUrl = `${origin}${PAGE_ROUTES.REQUEST_SERVICE}`.replace(
    ":tenantId",
    tenantId
  );

  const redirectUrl = isHome ? fallbackUrl : undefined;
  const login = useLogin();

  return !isAuth ? (
    <SmCta
      className={`${styles.loginCta} ${className}`}
      onClick={() => login(redirectUrl)}
    >
      {children}
    </SmCta>
  ) : null;
};

export default LoginButton;
