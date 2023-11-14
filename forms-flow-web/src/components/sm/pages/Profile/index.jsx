import React, { useState } from "react";
import VisibilityOffOutlinedIcon from "@mui/icons-material/VisibilityOffOutlined";
import VisibilityOutlinedIcon from "@mui/icons-material/VisibilityOutlined";
import PersonOutlineOutlinedIcon from "@mui/icons-material/PersonOutlineOutlined";
import { Button } from "react-bootstrap";
import { useSelector } from "react-redux";

import { usePageTitleRef } from "../../../../customHooks";
import PageContainer from "../../components/PageContainer";
import styles from "./profilePage.module.scss";
import SmCta, { SmCtaTypes, SmCtaSizes } from "../../components/buttons/SmCta";
import { useTranslation } from "react-i18next";

const ProfilePage = () => {
  const { t } = useTranslation();
  const headingRef = usePageTitleRef();
  const user = useSelector((state) => state.user.userDetail);
  const [isIdentifierVisible, setIsIdentifierVisible] = useState(false);
  const Icon = isIdentifierVisible
    ? VisibilityOutlinedIcon
    : VisibilityOffOutlinedIcon;

  return (
    <PageContainer>
      <div className={styles.profilePage}>
        <div className={styles.pageContent}>
          <div className={styles.profilePageTitleWrapper}>
            <PersonOutlineOutlinedIcon />
            <h1
              className={styles.profilePageTitle}
              tabIndex="-1"
              ref={headingRef}
            >
              {t("profile.page.title")}
            </h1>
          </div>
          <div className={styles.name}>{user.fullName || user.name}</div>
          <div className={styles.info}>
            {user.personIdentifier ? (
              <div className="row no-gutters">
                <div className={`col-3 text-sm-red ${styles.profileText}`}>
                  {t("profile.page.personIdentifier")}
                </div>
                <div className={`col-8 text-sm-red ${styles.profileText}`}>
                  {isIdentifierVisible
                    ? user.personIdentifier?.replace("PNOBG-", "")
                    : "*".repeat(10)}
                </div>
                <div className="col-1">
                  <Button
                    className={styles.cta}
                    aria-label={t("screen.reader.profile.show.cta")}
                    aria-expanded={isIdentifierVisible ? "true" : "false"}
                    onClick={() => setIsIdentifierVisible(!isIdentifierVisible)}
                  >
                    <Icon className={styles.icon} />
                  </Button>
                </div>
              </div>
            ) : null}
            {user.phone_number ? (
              <div className="row no-gutters">
                <div className={`col-3 text-sm-blue-6 ${styles.profileText}`}>
                  {t("profile.page.phone")}
                </div>
                <div className={`col-8 ${styles.profileText}`}>
                  {user.phone_number}
                </div>
                <div className="col-1"></div>
              </div>
            ) : null}
            {user.email ? (
              <div className="row no-gutters">
                <div className={`col-3 text-sm-blue-6 ${styles.profileText}`}>
                  {t("profile.page.email")}
                </div>
                <div className={`col-8 ${styles.profileText}`}>
                  {user.email}
                </div>
                <div className="col-1"></div>
              </div>
            ) : null}
          </div>
          <div className={styles.deleteCtaWrapper}>
            <SmCta
              type={SmCtaTypes.SECONDARY}
              size={SmCtaSizes.MEDIUM}
              className={styles.deleteCta}
            >
              {t("profile.page.delete.cta")}
            </SmCta>
          </div>
        </div>
      </div>
    </PageContainer>
  );
};

export default ProfilePage;
