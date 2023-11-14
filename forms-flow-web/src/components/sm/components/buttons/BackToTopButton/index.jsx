import React from "react";
import KeyboardArrowUpIcon from "@mui/icons-material/KeyboardArrowUp";
import { Button } from "react-bootstrap";

import { NavLinksSections } from "../../../../../constants/navigation";
import styles from "./backToTop.module.scss";

const BackToTopButton = () => {
  return (
    <Button
      aria-label="Back to the top of the page"
      className={styles.backToTopButton}
      onClick={() => {
        const element = document.getElementById("app");
        if (element) {
          element.scrollTo({ top: 0, left: 0, behavior: "smooth" });
        }
      }}
      onKeyDown={(e) => {
        if (e.key === "Enter") {
          const element = document.getElementById(NavLinksSections.SKIP_LINK);
          if (element) {
            element.focus();
          }
        }
      }}
    >
      <KeyboardArrowUpIcon />
    </Button>
  );
};

export default BackToTopButton;
