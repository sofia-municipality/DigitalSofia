import React from "react";
import { useTranslation } from "react-i18next";

import styles from "./loading.module.scss";

export const Loading = () => {
  const { t } = useTranslation();
  const notificationTranslated = t("screen.reader.loading.alert");
  const notification =
    notificationTranslated === "screen.reader.loading.alert"
      ? "Зареждане"
      : notificationTranslated;

  return (
    <div
      className={styles.loader}
      role="alert"
      aria-busy="true"
      style={{ fontSize: 0 }}
    >
      {notification}
    </div>
  );
};

const LoadingContainer = React.memo(({ className, ...rest }) => (
  <div
    className={`${styles.wrapper} ${className}`}
    data-testid="loading-component"
  >
    <Loading {...rest} />
  </div>
));

export default LoadingContainer;
