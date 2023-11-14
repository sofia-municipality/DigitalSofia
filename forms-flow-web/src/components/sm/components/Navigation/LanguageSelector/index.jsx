import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Button } from "react-bootstrap";
import i18n from "i18next";

import styles from "./languageSelector.module.scss";
import { setLanguage } from "../../../../../actions/languageSetAction";
import { updateUserlang } from "../../../../../apiManager/services/userservices";
import { useTranslation } from "react-i18next";

const LanguageSelector = ({ smallNav }) => {
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const userLanguage = useSelector((state) => state.user.lang);
  const [currentLanguage, setCurrentLanguage] = useState(i18n.language);
  const supportedLanguage = useSelector((state) => state.user.selectLanguages);

  useEffect(() => {
    setTimeout(() => {
      i18n.changeLanguage(userLanguage, () => {
        setCurrentLanguage(userLanguage);
      });
    }, 100);
  }, [userLanguage]);

  const handleOnclick = (selectedLang) => {
    if (currentLanguage !== selectedLang) {
      dispatch(setLanguage(selectedLang));
      if (isAuth) {
        dispatch(updateUserlang(selectedLang));
      }
    }
  };

  return (
    <section
      className={`container-fluid d-flex justify-content-end ${
        styles.language
      } ${smallNav ? styles.small : null}`}
      aria-label={t("screen.reader.choose.language.section")}
    >
      <div className="pr-0">
        {supportedLanguage.map((lang, index) => (
          <Button
            key={index}
            className={currentLanguage === lang.name ? styles.active : ""}
            variant="link"
            aria-hidden={smallNav}
            tabIndex={smallNav ? "-1" : "0"}
            onClick={() => handleOnclick(lang.name)}
            aria-label={lang.label}
          >
            {lang.value}
          </Button>
        ))}
      </div>
    </section>
  );
};

export default LanguageSelector;
