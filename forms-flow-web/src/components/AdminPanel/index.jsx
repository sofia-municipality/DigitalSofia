import React from "react";
import { useTranslation } from "react-i18next";
import AdministrationContainer from "../AdministrationContainer";

const AdminPanelPage = () => {
  const { t } = useTranslation();

  return (
    <AdministrationContainer
      title={t("admin.panel.page.title")}
    ></AdministrationContainer>
  );
};

export default AdminPanelPage;
