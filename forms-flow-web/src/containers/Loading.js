import React from "react";
import { useLocation } from "react-router-dom";
import { matchPath } from "react-router";

import SmLoading from "../components/sm/components/Loading";
import { SM_ROUTES } from "../constants/navigation";
import { SpinnerSVG } from "./SpinnerSVG";

const DefaultLoading = React.memo(() => (
  <div className="loader-container" data-testid="loading-component">
    <SpinnerSVG fill="#868e96" />
  </div>
));

const Loading = React.memo((props) => {
  const { pathname } = useLocation();
  const smRoutes = Object.values(SM_ROUTES);
  const isSmRoute = smRoutes.some((route) =>
    matchPath(pathname, { path: route, exact: true })
  );

  return isSmRoute ? <SmLoading {...props} /> : <DefaultLoading {...props} />;
});
export default Loading;
