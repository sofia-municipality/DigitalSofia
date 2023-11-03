import React from "react";
import { Link } from "react-router-dom";
import { useGetTenantLink } from "../../../../../customHooks";

const BaseCta = ({
  children,
  className = null,
  isLink = false,
  href = "/",
  hardRedirect = false,
  isExternalLink = false,
  onClick = () => {},
  disabled = false,
  target,
  accessibilityProps = {},
  refObj,
}) => {
  return isLink ? (
    <BaseLink
      isExternalLink={isExternalLink}
      hardRedirect={hardRedirect}
      href={href}
      className={className}
      onClick={onClick}
      disabled={disabled}
      target={target}
      accessibilityProps={accessibilityProps}
      refObj={refObj}
    >
      {children}
    </BaseLink>
  ) : (
    <button
      className={className}
      onClick={onClick}
      disabled={disabled}
      {...accessibilityProps}
      ref={refObj}
    >
      {children}
    </button>
  );
};

const BaseLink = ({
  hardRedirect,
  href,
  className,
  onClick,
  isExternalLink,
  children,
  disabled,
  target,
  accessibilityProps = {},
  refObj,
}) => {
  const link = useGetTenantLink(href, isExternalLink);
  return hardRedirect ? (
    <a
      href={link}
      className={className}
      onClick={(e) => {
        if (disabled) {
          e.preventDefault();
        } else {
          onClick(e);
        }
      }}
      disabled={disabled}
      target={target}
      ref={refObj}
      {...accessibilityProps}
    >
      {children}
    </a>
  ) : (
    <Link
      to={link}
      className={className}
      onClick={(e) => {
        if (disabled) {
          e.preventDefault();
        } else {
          onClick(e);
        }
      }}
      disabled={disabled}
      ref={refObj}
      {...accessibilityProps}
    >
      {children}
    </Link>
  );
};

export default BaseCta;
