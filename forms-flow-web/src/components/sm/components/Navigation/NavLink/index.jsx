import React from "react";
import { Link } from "react-router-dom";
import { useGetTenantLink } from "../../../../../customHooks";

const NavLink = ({ children, to = "/", isExternalLink = false, ...rest }) => {
  const link = useGetTenantLink(to, isExternalLink);

  return (
    <Link to={link} {...rest}>
      {children}
    </Link>
  );
};

export default NavLink;
