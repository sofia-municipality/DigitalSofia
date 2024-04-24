import React from "react";
import { useTranslation } from "react-i18next";

import { PAGE_ROUTES } from "../../constants/navigation";
import AdminCards from "./AdminCards";
import AdministrationContainer from "../AdministrationContainer";

const SOAdminstrationPage = () => {
  const { t } = useTranslation();
  const cards = [
    {
      link: PAGE_ROUTES.PAGE_ADMINISTRATION,
      text: t("so.administration.pages"),
    },
    {
      link: PAGE_ROUTES.FAQ_ADMINISTRATION,
      text: t("so.administration.faq"),
    },
    {
      link: PAGE_ROUTES.TRANSLATION_ADMINISTRATION,
      text: t("so.administration.translations"),
    },
  ];

  return (
    <AdministrationContainer title={t("so.administration")}>
      <AdminCards cards={cards} />
    </AdministrationContainer>
  );
};

export default SOAdminstrationPage;
