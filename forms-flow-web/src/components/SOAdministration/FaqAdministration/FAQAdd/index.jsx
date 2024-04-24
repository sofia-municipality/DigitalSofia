import React from "react";

import { useTranslation } from "react-i18next";

import { useAddFAQ } from "../../../../apiManager/apiHooks";
import { useNavigateTo } from "../../../../customHooks";
import { PAGE_ROUTES } from "../../../../constants/navigation";
import { LANGUAGE } from "../../../../constants/constants";
import AdministrationContainer from "../../../AdministrationContainer";
import FAQForm from "../FAQForm";

const AddFAQ = () => {
  const { t } = useTranslation();
  const addFaq = useAddFAQ();
  const navigateToFaqList = useNavigateTo(PAGE_ROUTES.FAQ_ADMINISTRATION);

  const onSubmit = async (payload) => {
    const res = await addFaq(payload, LANGUAGE);
    if (res) navigateToFaqList();
  };

  return (
    <AdministrationContainer
      title={t("so.administration.faqAdministration.add")}
    >
      <FAQForm onSubmit={onSubmit} />
    </AdministrationContainer>
  );
};

export default AddFAQ;
