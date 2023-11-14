import React from "react";
import NavLink from "../../../../components/Navigation/NavLink";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";

import styles from "./contactsSection.module.scss";

const ContactsSection = ({ id, title, items = [] }) => (
  <section
    tabIndex="-1"
    id={id}
    className={`container-fluid ${styles.section} ${styles.contactsSection}`}
    aria-labelledby="contacts-section-title"
  >
    <div className={`row flex-column ${styles.sectionContent}`}>
      <div className={`col-12 ${styles.contactsContent}`}>
        <h2 className={styles.sectionMainTitle} id="contacts-section-title">
          {title}
        </h2>
      </div>
      <div className={`col-12 ${styles.contactsContainer}`}>
        <div className="container-fluid">
          <div className="row flex-column flex-md-row">
            {items.map((element, index) => (
              <div
                key={index}
                className={`col col-md-4 d-flex ${styles.contantWrapper}`}
              >
                <img src={element.image} width="50px" height="50px" alt="" />
                <div className="container-fluid">
                  <div className="row flex-column">
                    <div className="col-12">
                      <span className={styles.contentTitle}>
                        {element.title}
                      </span>
                    </div>
                    <div className="col-12">
                      {index === items.length - 1 ? (
                        <p className={styles.contentDescription}>
                          <NavLink
                            to={PAGE_ROUTES.CONTACTS}
                            className={styles.contactsLink}
                          >
                            {element.description}
                          </NavLink>
                        </p>
                      ) : (
                        <p className={styles.contentDescription}>
                          {element.description}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  </section>
);

export default ContactsSection;
