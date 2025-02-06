import React from "react";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";
import { usePageTitleRef } from "../../../../customHooks";
import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import { useGetPageBlocks } from "../../../../apiManager/apiHooks";
import Loading from "../../../../containers/Loading";
import PageContainer from "../../components/PageContainer";
import ContactsAccordion from "../../components/ContactsAccordion";
import styles from "./contacts.module.scss";

const Contacts = () => {
  const { t } = useTranslation();
  const headingRef = usePageTitleRef();
  const contactsPageBlocks = PAGE_BLOCKS[PAGE_NAMES.CONTACTS_PAGE];

  const userLanguage = useSelector((state) => state.user.lang);
  const pageBlocks =
    useGetPageBlocks(PAGE_NAMES.CONTACTS_PAGE, userLanguage) || {};

  const contactsBlock = pageBlocks[contactsPageBlocks.CONTACTS_BLOCK];
  return contactsBlock ? (
    <PageContainer>
      <div className={`${styles.contactsContainer}`}>
        <div className={`container-fluid ${styles.content} pr-md-5 pl-md-5`}>
          <div className="row mt-3 mt-md-5 pl-2 pr-2 pl-lg-0 pr-lg-0">
            <div className="col-12">
              <h1 className={styles.pageTitle} tabIndex="-1" ref={headingRef}>
                {t("contacts.page.title")}
              </h1>
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-12">
              <p className={styles.description}>
                {t("contacts.page.description")}
              </p>
            </div>
          </div>
          <div className="row">
            <div className="col-12">
              <p className={styles.contactViaEmail}>
                {t("contacts.page.contactViaEmail")}
                <a
                  href="mailto:address@sofia.bg"
                  className="ml-1 linkSystemBlue"
                >
                  address@sofia.bg
                </a>
              </p>
            </div>
          </div>

          <div className="row">
            <div className="col-12">
              <div className={styles.contactsContent}>
                <div className={styles.contactsContentTitle}>
                  {t("contacts.page.contacts.title")}
                </div>
                {contactsBlock?.items?.map((items, index) => (
                  <ContactsAccordion
                    id={index}
                    key={index}
                    data={items}
                    forceOpenClose={"open"}
                    className={styles.contactsAccordion}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </PageContainer>
  ) : (
    <Loading />
  );
};

export default Contacts;
