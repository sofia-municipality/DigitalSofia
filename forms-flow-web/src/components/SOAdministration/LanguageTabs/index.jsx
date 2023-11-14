import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import Tabs from "../../sm/components/Tabs";

import styles from "./languageTabs.module.scss";

const LanguageTabs = ({ onChange }) => {
  const [tabsConfig, setTabsConfig] = useState();

  const selectLanguages = useSelector((state) => state.user.selectLanguages);

  useEffect(() => {
    if (selectLanguages) {
      const conf = selectLanguages.map((lang) => ({
        id: lang.name,
        title: lang.label,
        renderContent: () => null,
      }));

      setTabsConfig(conf);
    }
  }, [selectLanguages]);

  return tabsConfig ? (
    <Tabs
      sections={tabsConfig}
      lineClassName={styles.tabsLine}
      isLink={false}
      onChange={onChange}
    />
  ) : null;
};

export default LanguageTabs;
