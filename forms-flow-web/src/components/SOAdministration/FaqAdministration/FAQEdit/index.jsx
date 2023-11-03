import React, { useState, useCallback } from "react";

import { Button, Row, Col } from "react-bootstrap";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  useGetFAQ,
  useUpdateFAQ,
  useDeleteFAQ,
} from "../../../../apiManager/apiHooks";
import AdministrationContainer from "../../AdministrationContainer";
import FAQForm from "../FAQForm";
import { useNavigateTo } from "../../../../customHooks";
import { PAGE_ROUTES } from "../../../../constants/navigation";
import { LANGUAGE } from "../../../../constants/constants";
import LanguageTabs from "../../LanguageTabs";

import styles from "./faqEdit.module.scss";

const EditFAQ = () => {
  const { t } = useTranslation();
  const { faqId } = useParams();
  const [language, setLanguage] = useState(LANGUAGE);
  const [faq, loading] = useGetFAQ(faqId, language);
  const navigateToFaqList = useNavigateTo(PAGE_ROUTES.FAQ_ADMINISTRATION);
  const updateFaq = useUpdateFAQ();
  const deleteFaq = useDeleteFAQ();

  const onSubmit = (payload) => {
    updateFaq(faqId, payload, language);
  };

  const onDelete = async () => {
    const res = await deleteFaq(faqId);
    if (res) navigateToFaqList();
  };

  const onLanguageChange = useCallback((lang) => {
    setLanguage(lang);
  }, []);

  return (
    <AdministrationContainer
      title={t("so.administration.faqAdministration.edit")}
    >
      <Row>
        <Col>
          <Button
            className={styles.deleteCta}
            variant="danger"
            onClick={onDelete}
          >
            {t("Delete")}
          </Button>
        </Col>
      </Row>

      {faq ? (
        <>
          <LanguageTabs onChange={onLanguageChange} />
          {!loading ? (
            <FAQForm
              title={faq?.title}
              content={faq?.content}
              isFavoured={faq?.isFavoured}
              onSubmit={onSubmit}
            />
          ) : null}
        </>
      ) : null}
    </AdministrationContainer>
  );
};

export default EditFAQ;
