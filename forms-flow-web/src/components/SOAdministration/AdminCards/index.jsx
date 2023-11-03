import React from "react";
import { Nav } from "react-bootstrap";
import NavLink from "../../sm/components/Navigation/NavLink";

import styles from "./adminCards.module.scss";

const AdminCards = ({ cards }) => {
  return (
    <div className={`row ${styles.cardsContainer}`}>
      {cards.map((card, index) => (
        <div key={index} className="col-12 col-md-6">
          <Nav.Link as={NavLink} to={card.link} className={styles.card}>
            {card.text}
          </Nav.Link>
        </div>
      ))}
    </div>
  );
};

export default AdminCards;
