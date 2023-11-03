import React from "react";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";
import { usePageTitleRef } from "../../../../customHooks";
import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import { useGetPageBlocks } from "../../../../apiManager/apiHooks";
import Loading from "../../../../containers/Loading";
import PageContainer from "../../components/PageContainer";
import SmCta, { SmCtaTypes } from "../../components/buttons/SmCta";

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
            <div className="col-12">
              <p className={styles.description}>
                {t("contacts.page.description")}
              </p>
            </div>
            <div className="col-12">
              <div className={styles.contactsContent}>
                <div className={styles.contactsContentTitle}>
                  {t("contacts.page.contacts.title")}
                </div>
                <div>
                  {contactsBlock?.items?.map((item, index) => (
                    <ContactItem key={index} {...item} />
                  ))}
                </div>
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

const ContactItem = ({ title, phone, link }) => (
  <SmCta
    className="p-0 w-100"
    href={link}
    type={SmCtaTypes.TRANSPARENT}
    target="_blank"
    isLink
    hardRedirect
  >
    <div className={`row no-gutters w-100 ${styles.contactsItem}`}>
      <div className={`col-md-4 ${styles.contactsItemRegion}`}>{title}</div>
      <div className={`col-md-8 ${styles.phone}`}>{phone}</div>
    </div>
  </SmCta>
);

export default Contacts;
