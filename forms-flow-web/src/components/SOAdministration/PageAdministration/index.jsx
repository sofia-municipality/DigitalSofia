import React from "react";
import { useTranslation } from "react-i18next";

import { PAGE_ROUTES } from "../../../constants/navigation";
import { PAGE_NAMES } from "../../../constants/pages";
import AdminCards from "../AdminCards";
import AdministrationContainer from "../../AdministrationContainer";

const PageAdministration = () => {
  const { t } = useTranslation();
  const cards = Object.entries(PAGE_NAMES).map(([key, value]) => ({
    link: PAGE_ROUTES.BLOCKS_ADMINISTRATION.replace(":page", value),
    text: t(key),
  }));
  return (
    <AdministrationContainer title={t("so.administration.pageAdministration")}>
      <AdminCards cards={cards} />
    </AdministrationContainer>
  );
};

export default PageAdministration;
