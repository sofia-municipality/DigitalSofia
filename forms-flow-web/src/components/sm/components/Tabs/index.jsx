import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { Button } from "react-bootstrap";

import NavLink from "../../components/Navigation/NavLink";
import { usePrevious } from "../../../../customHooks";

import styles from "./tabs.module.scss";

const Tabs = React.memo(
  ({
    sections,
    className = "",
    lineClassName,
    onChange = () => {},
    isLink = true,
  }) => {
    const [activeSection, setActiveSection] = useState(sections[0]?.id);
    const previousActiveSection = usePrevious(activeSection);
    const { hash } = useLocation();

    useEffect(() => {
      if (hash && isLink) {
        const id = hash.replace("#", "");
        const section = sections.find((el) => el.id === id);
        if (section) {
          setActiveSection(section.id);
        }
      }
    }, [sections, hash, isLink]);

    const getAnimationDirectionClassName = (id) => {
      const isActive = id === activeSection;
      const isPreviousActive = id === previousActiveSection;
      if (isActive || isPreviousActive) {
        const activeElement = sections.find((e) => e.id === activeSection);
        const activeIndex = sections.indexOf(activeElement);
        const previuosActiveElement = sections.find(
          (e) => e.id === previousActiveSection
        );

        const previousActiveIndex = sections.indexOf(previuosActiveElement);

        if (isActive) {
          return activeIndex > previousActiveIndex
            ? styles.floatRight
            : styles.floatLeft;
        }

        return activeIndex > previousActiveIndex
          ? styles.floatLeft
          : styles.floatRight;
      }

      return "";
    };

    return (
      <div className={className}>
        <div className="row no-gutters">
          {sections.map(({ id, title, Icon, iconColorClass }) => (
            <div className="col-6" key={id}>
              <TabCta
                link={`#${id}`}
                className={`${styles.menuItem} ${
                  activeSection === id ? styles.active : ""
                }`}
                isLink={isLink}
                onChange={() => {
                  !isLink && setActiveSection(id);
                  onChange(id);
                }}
              >
                {Icon ? (
                  <Icon className={`${styles.icon} ${iconColorClass}`} />
                ) : null}
                <span>{title}</span>
              </TabCta>
              <div
                className={`${styles.inactiveLine} ${
                  activeSection !== id ? styles.show : ""
                } ${getAnimationDirectionClassName(id)}`}
              />
            </div>
          ))}
        </div>
        <div className="row no-gutters">
          <div className={`col-12 ${styles.line} ${lineClassName || ""}`}></div>
        </div>
        {sections.find((e) => e.id === activeSection).renderContent()}
      </div>
    );
  }
);

const TabCta = ({ isLink, link, className, onChange = () => {}, children }) =>
  isLink ? (
    <NavLink to={link} className={className} onClick={onChange}>
      {children}
    </NavLink>
  ) : (
    <Button className={className} onClick={onChange}>
      {children}
    </Button>
  );

export default Tabs;
