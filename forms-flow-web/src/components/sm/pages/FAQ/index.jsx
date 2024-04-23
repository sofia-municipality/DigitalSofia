import React, { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

import { usePageTitleRef, useOpenCloseAllItems } from "../../../../customHooks";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../components/buttons/SmCta";
import PageContainer from "../../components/PageContainer";
import styles from "./faq.module.scss";
import SmAccordion from "../../components/Accordion";
import { fetchFAQ } from "../../../../apiManager/services/faqServices";
import Pagination from "../../components/Pagination";

const perPage = 10;

const FAQ = () => {
  const { t } = useTranslation();
  const headingRef = usePageTitleRef();
  const userLanguage = useSelector((state) => state.user.lang);
  const [page, setPage] = useState(1);
  const [totalItems, setTotalItems] = useState(1);
  const [faqs, setFaqs] = useState([]);
  const [loading, setLoading] = useState(true);
  const {
    onOpenCloseAllClick,
    onExpand,
    openCloseAll,
    shouldOpenCloseAll,
    forceCloseAll,
  } = useOpenCloseAllItems(faqs.length);

  useEffect(() => {
    setLoading(true);
    fetchFAQ(page, perPage, userLanguage).then((data) => {
      setFaqs(data.data.faqs);
      setTotalItems(data.data.total);
      setLoading(false);
    });
  }, [page, userLanguage]);

  const setPaginationPage = (index) => {
    forceCloseAll();
    setPage(index);
    document.getElementById("app").scrollTo(0, 0);
  };

  let skeleton = [];
  for (let i = 1; i <= perPage; i++) {
    skeleton.push(
      <div className="row mb-4 pb-4 border-bottom" key={i}>
        <div className="col-6 bg-light p-3"></div>
        <div className="col-5"></div>
        <div className="col-1 bg-light p-3"></div>
      </div>
    );
  }
  return (
    <PageContainer>
      <div className={`${styles.faqContainer}`}>
        <div className={`container-fluid ${styles.content} pr-md-5 pl-md-5`}>
          <div className="row d-flex align-items-end mt-3 mt-md-5 pl-2 pr-2 pl-lg-0 pr-lg-0">
            <div className="col-lg-9">
              <h1 className={styles.pageTitle} tabIndex="-1" ref={headingRef}>
                <span>{t("faqs.page.title")}</span>
                {totalItems > perPage ? (
                  <span style={{ fontSize: 0 }}>
                    {`. ${t(
                      "screen.reader.pagination.navigation.current"
                    )} ${page}`}
                  </span>
                ) : null}
              </h1>
            </div>
            <div className="col-lg-3">
              <SmCta
                className={styles.openAllLink}
                type={SmCtaTypes.OUTLINE}
                size={SmCtaSizes.SMALL}
                accessibilityProps={{
                  "aria-expanded":
                    shouldOpenCloseAll !== "open" ? "false" : "true",
                }}
                onClick={onOpenCloseAllClick}
              >
                <span className="sm-cta-outline-underline">
                  {t(
                    shouldOpenCloseAll !== "open"
                      ? "faqs.openAll.cta.text"
                      : "faqs.closeAll.cta.text"
                  )}
                </span>
              </SmCta>
            </div>
          </div>
          <div className={`row ${styles.faqList}`}>
            <div className="col-12 bg-white">
              {loading ? (
                <div>{skeleton}</div>
              ) : (
                <SmAccordion
                  cards={faqs}
                  openCloseAll={openCloseAll}
                  onExpand={onExpand}
                ></SmAccordion>
              )}
            </div>
          </div>
        </div>
        {totalItems > perPage ? (
          <div className="container-fluid">
            <div className="row">
              <div className="col-12 mt-4">
                <Pagination
                  limit={perPage}
                  totalItems={totalItems}
                  selectedPage={page}
                  changePage={setPaginationPage}
                ></Pagination>
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </PageContainer>
  );
};
export default FAQ;
