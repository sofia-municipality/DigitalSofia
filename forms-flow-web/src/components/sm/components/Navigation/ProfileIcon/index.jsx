import React, { useEffect, useState, useRef } from "react";
import { Nav, Button } from "react-bootstrap";
import { CircularProgressbarWithChildren } from "react-circular-progressbar";
import "react-circular-progressbar/dist/styles.css";
import NavLink from "../NavLink";

import { useProfileMenuConfig } from "../hooks";
import {
  useDevice,
  useLogout,
  useTokenExpireTimer,
} from "../../../../../customHooks";
import Timer from "../../Timer";

import styles from "./profileIcon.module.scss";
import { useTranslation } from "react-i18next";

const idCSS = "progress-bar-gradient";

const GradientSVG = () => (
  <svg style={{ height: 0, display: "block" }}>
    <defs>
      <linearGradient id={idCSS} gradientTransform="rotate(90)">
        <stop stopColor="#F1471D" />
        <stop offset="0.1462" stopColor="#F1491D" />
        <stop offset="0.1989" stopColor="#F2501C" />
        <stop offset="0.2364" stopColor="#F35B19" />
        <stop offset="0.2668" stopColor="#F46C17" />
        <stop offset="0.2928" stopColor="#F68213" />
        <stop offset="0.3158" stopColor="#F89E0E" />
        <stop offset="0.3361" stopColor="#FABD09" />
        <stop offset="0.3473" stopColor="#FCD205" />
        <stop offset="0.7338" stopColor="#5E984B" />
        <stop offset="0.7428" stopColor="#5D9656" />
        <stop offset="0.7776" stopColor="#5B917A" />
        <stop offset="0.8143" stopColor="#5A8C98" />
        <stop offset="0.8529" stopColor="#5989AF" />
        <stop offset="0.8943" stopColor="#5886BF" />
        <stop offset="0.9402" stopColor="#5784C9" />
        <stop offset="1" stopColor="#5784CC" />
      </linearGradient>
    </defs>
  </svg>
);

const ProfileIcon = ({
  isExpanded,
  setPosition = () => {},
  showOverlay = () => {},
  displayTimer = false,
}) => {
  const { t } = useTranslation();
  const { isMobile } = useDevice();
  const strokeWidth = isMobile ? 4 : 2;
  const { seconds, minutes, hours, timeLeft, initialTime } =
    useTokenExpireTimer();

  const ref = useRef();
  useEffect(() => {
    setPosition({ x: ref.current?.offsetLeft, y: ref.current?.offsetTop });
  }, [setPosition, ref.current?.offsetLeft, ref.current?.offsetTop]);

  return (
    <div className={styles.profileWrapper}>
      {displayTimer ? (
        <div className={styles.sessionTimer}>
          <Timer seconds={seconds} minutes={minutes} hours={hours} />
        </div>
      ) : null}
      <Button
        ref={ref}
        aria-label={t("screen.reader.profile.menu.section")}
        aria-expanded={isExpanded ? "true" : "false"}
        className={styles.profileIconWrapper}
        onMouseEnter={() => !isMobile && showOverlay()}
        onFocus={() => !isMobile && showOverlay()}
      >
        <GradientSVG />
        <CircularProgressbarWithChildren
          maxValue={initialTime}
          value={timeLeft}
          counterClockwise
          styles={{
            path: {
              stroke: `url(#${idCSS})`,
              height: "100%",
              strokeWidth,
            },
            trail: {
              stroke: "#FAFAFA",
              strokeWidth,
            },
          }}
        >
          <img className={styles.profileIcon} src="/person.svg" alt="" />
        </CircularProgressbarWithChildren>
      </Button>
    </div>
  );
};

const ProfileOverlay = ({ showOverlay, position, hideOverlay }) => {
  const { t } = useTranslation();
  const logout = useLogout();
  const menuConfig = useProfileMenuConfig();

  return showOverlay ? (
    <div
      className={styles.overlay}
      onMouseLeave={hideOverlay}
      style={{
        top: position.y ? position.y - 20 : 0,
        left: position.x ? position.x - 200 : 0,
      }}
    >
      <div className="container-fluid">
        <div className={`row ${styles.overlayMenu}`}>
          {menuConfig.map(({ id, title, Icon, iconColorClass, href }, index) =>
            id === "logoutCta" ? (
              <div className="col-12" key={index}>
                <Button
                  className={styles.logoutCta}
                  onClick={logout}
                  onBlur={hideOverlay}
                >
                  <span>
                    <Icon className={`${styles.icon} ${iconColorClass}`} />
                  </span>
                  <span>{t(title)}</span>
                </Button>
              </div>
            ) : (
              <div className="col-12" key={index}>
                <Nav.Link
                  as={NavLink}
                  to={href}
                  className={`${styles.navLink}`}
                  onClick={hideOverlay}
                >
                  <span>
                    <Icon className={`${styles.icon} ${iconColorClass}`} />
                  </span>
                  <span>{t(title)}</span>
                </Nav.Link>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  ) : null;
};

const ProfileIconWrapper = () => {
  const [showOverlay, setShowOverlay] = useState(false);
  const [position, setPosition] = useState({});
  const { isMobile } = useDevice();
  const hideOverlay = () => {
    setShowOverlay(false);
  };

  return !isMobile ? (
    <>
      <ProfileIcon
        setPosition={setPosition}
        showOverlay={() => setShowOverlay(true)}
        displayTimer={showOverlay}
        isExpanded={showOverlay}
      />
      <ProfileOverlay
        showOverlay={showOverlay}
        position={position}
        hideOverlay={hideOverlay}
      />
    </>
  ) : (
    <ProfileIcon displayTimer isExpanded={false} />
  );
};

export default ProfileIconWrapper;
