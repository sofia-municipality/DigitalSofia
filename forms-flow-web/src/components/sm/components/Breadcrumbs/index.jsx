import React from "react";
import { useLocation } from "react-router-dom";
import Breadcrumb from "react-bootstrap/Breadcrumb";
import NavLink from "../Navigation/NavLink";
import { BASE_ROUTE } from "../../../../constants/constants";

const SmBreadcrumbs = () => {
  const { pathname } = useLocation();
  const paths = pathname.split(BASE_ROUTE).filter((e) => e);
  paths.pop();

  return paths.length ? (
    <Breadcrumb>
      {paths.map((path, index) => {
        const nestedPaths = paths.slice(0, index + 1);
        const href = `${BASE_ROUTE}${nestedPaths.join("/")}`;
        return (
          <Breadcrumb.Item
            linkAs={NavLink}
            key={index}
            linkProps={{ to: href }}
          >
            {path}
          </Breadcrumb.Item>
        );
      })}
    </Breadcrumb>
  ) : null;
};

export default SmBreadcrumbs;
