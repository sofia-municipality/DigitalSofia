import React, { useState, useContext, useEffect } from "react";
import { useSelector } from "react-redux";
import { Navbar, Nav, Button } from "react-bootstrap";
import { Link, useLocation } from "react-router-dom";
import { matchPath } from "react-router";

import { SmContext } from "../../context";
import { useDevice, useLogout } from "../../../../customHooks";
import ProfileIcon from "./ProfileIcon";
import LanguageSelector from "./LanguageSelector";
import LoginButton from "../buttons/LoginButton";
import { useProfileMenuConfig } from "./hooks";
import NavLink from "./NavLink";

import {
  NavLinksSections,
  PAGE_ROUTES,
  ROUTES_WITH_NAV_ANIMATION,
} from "../../../../constants/navigation";

import styles from "./navigation.module.scss";
import { useTranslation } from "react-i18next";

const navLinks = [
  {
    name: "navigation.menu.services",
    link: `${PAGE_ROUTES.HOME}#${NavLinksSections.ADDRESS_SECTION}`,
    colorClass: "bg-sm-red",
    borderClass: "bg-sm-circle-border-red",
  },
  {
    name: "navigation.menu.howItWorks",
    link: `${PAGE_ROUTES.HOME}#${NavLinksSections.HOW_IT_WORKS_SECTION}`,
    colorClass: "bg-sm-yellow",
    borderClass: "bg-sm-circle-border-yellow",
  },
  {
    name: "navigation.menu.faq",
    shortName: "navigation.menu.faq.short",
    link: `${PAGE_ROUTES.HOME}#${NavLinksSections.FAQ_SECTION}`,
    colorClass: "bg-sm-green",
    borderClass: "bg-sm-circle-border-green",
  },
  {
    name: "navigation.menu.contacts",
    link: `${PAGE_ROUTES.HOME}#${NavLinksSections.CONTACTS_SECTION}`,
    colorClass: "bg-sm-blue",
    borderClass: "bg-sm-circle-border-blue",
  },
];

const ProfileMenu = ({ onNavLinkClick }) => {
  const loggedInMenuItems = useProfileMenuConfig();
  const logout = useLogout();
  const { t } = useTranslation();

  return (
    <div className={styles.profileMenu}>
      {loggedInMenuItems.map(
        ({ id, title, Icon, iconColorClass, href }, index) =>
          id === "logoutCta" ? (
            <Button
              key={index}
              className={`${styles.logoutCta} ${styles.navLink}`}
              onClick={logout}
            >
              <span>
                <Icon className={`${styles.mobileNavIcon} ${iconColorClass}`} />
              </span>
              <span>{t(title)}</span>
            </Button>
          ) : (
            <Nav.Link
              as={NavLink}
              to={href}
              className={styles.navLink}
              key={index}
              onClick={onNavLinkClick}
            >
              <span>
                <Icon
                  className={`${styles.mobileNavIcon} ${iconColorClass}
                    `}
                />
              </span>
              <span>{t(title)}</span>
            </Nav.Link>
          )
      )}
    </div>
  );
};

const NavigationLink = ({
  link,
  name,
  shortName,
  color,
  borderClass,
  colorClass,
  onClick,
}) => {
  const { isMobile, isExtraLarge } = useDevice();
  const { t } = useTranslation();

  return (
    <Nav.Link
      as={NavLink}
      to={link}
      className={styles.navLink}
      onClick={onClick}
    >
      {isMobile ? (
        <div className={styles.mobileNavIcon}>
          <span className={`${styles.circle} ${borderClass}`}></span>
        </div>
      ) : null}
      {isExtraLarge && shortName ? t(shortName) : t(name)}
      {!isMobile ? (
        <div className={styles.circleWrapper} aria-hidden="true">
          <span
            className={`${styles.line} ${colorClass}`}
            style={{ background: color }}
          ></span>
          <span className={`${styles.animatedCircle} ${borderClass}`}></span>
        </div>
      ) : null}
    </Nav.Link>
  );
};

const Navigation = () => {
  const { t } = useTranslation();
  const { isMobile, isPortraitMode } = useDevice();
  const { smContext, setSmContext } = useContext(SmContext);
  const { smallNav: isSmallNavTriggered, isNavExpanded } = smContext;
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const [isExpanded, setIsExpanded] = useState(false);
  const { pathname } = useLocation();

  const withNavAnimation = ROUTES_WITH_NAV_ANIMATION.some((route) =>
    matchPath(pathname, { path: route, exact: true })
  );
  const smallNav = isSmallNavTriggered || !withNavAnimation;

  useEffect(() => {
    if (isExpanded !== isNavExpanded)
      setSmContext({ isNavExpanded: isExpanded });
  }, [setSmContext, isExpanded, isNavExpanded]);

  const onNavLinkClick = () => isExpanded && setIsExpanded(!isExpanded);

  return (
    <>
      <Link
        id={NavLinksSections.SKIP_LINK}
        to={`#${NavLinksSections.MAIN_CONTENT}`}
        className={`${styles["skip-to-main-content-link"]} ${
          smallNav ? styles.small : ""
        }`}
        tabIndex="0"
        onFocus={() => {
          document.querySelector("#app").scrollTo({
            top: 0,
            left: 0,
            behavior: "smooth",
          });
        }}
      >
        Skip to main content
      </Link>
      <Navbar
        sticky="top"
        bg="white"
        expand="lg"
        expanded={isExpanded}
        onToggle={() => setIsExpanded(!isExpanded)}
        className={`${styles.navContainer} ${smallNav ? styles.small : null}`}
      >
        {!isMobile ? <LanguageSelector smallNav={smallNav} /> : null}
        <div
          className={`container-fluid ${
            isMobile ? styles.mobileContainer : null
          } ${isPortraitMode && isExpanded ? styles.portrait : ""}`}
        >
          <div className={isMobile ? `container ${styles.navigation}` : null}>
            <div className={styles.logoWrapper}>
              <Navbar.Brand
                as={NavLink}
                to={`${PAGE_ROUTES.HOME}#${NavLinksSections.MAIN_CONTENT}`}
              >
                <img
                  src="/digital-sofia-logo.svg"
                  className={`d-inline-block align-top mr-0 ${styles.navLogo} ${
                    smallNav ? styles.small : null
                  }`}
                  alt="Digital Sofia logo"
                />
              </Navbar.Brand>
            </div>

            <div className={isMobile ? styles.mobileNavActions : ""}>
              {isMobile && isAuth ? <ProfileIcon /> : null}
              <div
                className={`${styles.collapseIcon} ${
                  isExpanded ? styles.collapseCloseIcon : null
                }`}
              >
                <Navbar.Toggle aria-controls="navbarScroll" />
              </div>
            </div>
          </div>

          <Navbar.Collapse id="navbarScroll">
            <div className={!isMobile ? "container" : styles.navContentWrapper}>
              {isMobile ? <LanguageSelector /> : null}
              <div
                className={`container-lg ${styles.navContent} ${
                  isPortraitMode && isExpanded ? styles.portrait : ""
                }`}
              >
                <Nav
                  className={`container-lg justify-content-lg-center
                flex-lg-nowrap ${styles.navLinksContainer}`}
                >
                  {navLinks.map((el, index) => (
                    <NavigationLink
                      key={index}
                      {...el}
                      onClick={onNavLinkClick}
                    />
                  ))}
                  {isMobile && isAuth ? (
                    <ProfileMenu onNavLinkClick={onNavLinkClick} />
                  ) : null}
                </Nav>
                <div className={styles.navLoginWrapper}>
                  {!isAuth ? (
                    <LoginButton
                      className={`${styles.navLoginCta} ${
                        smallNav ? styles.small : null
                      }`}
                    >
                      {t("login.ctaText")}
                    </LoginButton>
                  ) : !isMobile ? (
                    <ProfileIcon />
                  ) : null}
                </div>
              </div>
            </div>
          </Navbar.Collapse>
        </div>
      </Navbar>
    </>
  );
};
export default Navigation;
