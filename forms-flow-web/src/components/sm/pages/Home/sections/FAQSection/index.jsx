import React from "react";
import { useSelector } from "react-redux";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import { useTranslation } from "react-i18next";

import { useOpenCloseAllItems } from "../../../../../../customHooks";
import NavLink from "../../../../components/Navigation/NavLink";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import Accordion from "../../../../components/Accordion";
import { useDevice } from "../../../../../../customHooks";
import { useFetchFAQ } from "../../../../../../apiManager/apiHooks";
import styles from "./faqSection.module.scss";
import Loading from "../../../../../../containers/Loading";

const FAQSection = ({
  id,
  title,
  allLink = PAGE_ROUTES.FAQ,
  openAllText = "faqs.openAll.cta.text",
  closeAllText = "faqs.closeAll.cta.text",
  allLinkText = "faqs.seeAll.cta.text",
}) => {
  const { t } = useTranslation();
  const userLanguage = useSelector((state) => state.user.lang);
  const { isPhone } = useDevice();
  const [{ faqs = [] } = {}, isLoading] = useFetchFAQ(
    1,
    999999,
    userLanguage,
    true
  );

  const { onOpenCloseAllClick, onExpand, openCloseAll, shouldOpenCloseAll } =
    useOpenCloseAllItems(faqs.length);

  const onOpenCloseAllEnterPressed = (e) => {
    if (e.key === "Enter") {
      onOpenCloseAllClick();
    }
  };

  return (
    <section
      id={id}
      className={`${styles.section} ${styles.faqSection}`}
      tabIndex="-1"
      aria-labelledby="faq-section-title"
    >
      <div className={`${styles.sectionContent} ${styles.faqWrapper}`}>
        <div className={`row no-gutters ${styles.faqContent}`}>
          {!isLoading ? (
            <>
              <div className="col-12">
                <div
                  className={`row no-gutters justify-content-between ${styles.faqHeader}`}
                >
                  <div className="col-8 col-md-auto">
                    <h2
                      className={styles.sectionMainTitle}
                      id="faq-section-title"
                    >
                      {title}
                    </h2>
                  </div>
                  <div className="col-12 col-md-auto align-self-end">
                    <span
                      role="button"
                      tabIndex="0"
                      className={`${styles.openAllLink} ${
                        isPhone ? styles.openAllLinkMobile : ""
                      }`}
                      onClick={onOpenCloseAllClick}
                      onKeyDown={onOpenCloseAllEnterPressed}
                      aria-expanded={
                        shouldOpenCloseAll !== "open" ? "false" : "true"
                      }
                    >
                      {t(
                        shouldOpenCloseAll !== "open"
                          ? openAllText
                          : closeAllText
                      )}
                    </span>
                  </div>
                </div>
              </div>
              <div className="col-12">
                <Accordion
                  className={styles.faqList}
                  cards={faqs}
                  openCloseAll={openCloseAll}
                  onExpand={onExpand}
                />
              </div>
              <div className={`col-12 ${styles.viewAllLinkWrapper}`}>
                <NavLink to={allLink} className={styles.viewAllLink}>
                  <span>{t(allLinkText)}</span>
                  <ArrowForwardIcon className={styles.viewAllLinkIcon} />
                </NavLink>
              </div>
            </>
          ) : (
            <Loading className="w-100" />
          )}
        </div>
      </div>
    </section>
  );
};

export default FAQSection;
