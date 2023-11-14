import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import querystring from "querystring";
import PageContainer from "../../../components/PageContainer";
import SmCta, { SmCtaTypes } from "../../../components/buttons/SmCta";
import CustomBreadcrumbs from "../../../components/Breadcrumbs/CustomBreadcrumbs";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import {
  PERMANENT_ADDRESS_FORM_PATH,
  CURRENT_ADDRESS_FORM_PATH,
  FORM_PREFILLED_DATA_INPUT_NAME,
  FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES,
  BASE_ROUTE,
  MULTITENANCY_ENABLED,
} from "../../../../../constants/constants";
import { fetchBPMFormList } from "../../../../../apiManager/services/bpmFormServices";
import { useNavigateTo, useLogin } from "../../../../../customHooks";

import ForeignerForbiddenModal from "./components/ForeignerForbiddenModal";
import styles from "./addressRegistration.module.scss";
import Loading from "../../../../../containers/Loading";

const sections = [
  {
    title: "addressRegistratrion.card.1.title",
    subtitle: "addressRegistratrion.card.1.subtitle",
    [FORM_PREFILLED_DATA_INPUT_NAME]:
      FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.MY_BEHALF,
  },
  {
    title: "addressRegistratrion.card.2.title",
    subtitle: "addressRegistratrion.card.2.subtitle",
    [FORM_PREFILLED_DATA_INPUT_NAME]:
      FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.CHILD,
  },
  {
    title: "addressRegistratrion.card.3.title",
    subtitle: "addressRegistratrion.card.3.subtitle",
    [FORM_PREFILLED_DATA_INPUT_NAME]:
      FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.OTHER_PERSON,
  },
];

const AddressRegistration = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const [isCheckFinished, setIsCheckFinished] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isForeignerForbiddenModalOpen, setIsForeignerForbiddenModalOpen] =
    useState(false);
  const userPersonIdenfitier = useSelector(
    (state) => state.user?.userDetail?.personIdentifier || ""
  );
  const isAuth = useSelector((state) => state.user.isAuthenticated);
  const doLogin = useLogin();
  const tenantId = useSelector((state) => state.tenants?.tenantId);
  const baseUrl = `${BASE_ROUTE}form/:formId`.replace(":tenantId", tenantId);
  const navigateToForm = useNavigateTo(
    `${baseUrl}?${FORM_PREFILLED_DATA_INPUT_NAME}=:formPreffiledInputValue`
  );

  const { search } = useLocation();

  const getFormByPath = (formPath, inputValue, callback = () => {}) => {
    setIsLoading(true);
    dispatch(
      fetchBPMFormList(
        1,
        99999999,
        null,
        null,
        null,
        null,
        formPath,
        (_, data) => {
          const formId = data?.forms?.[0]?.formId;
          const urlParams = {
            ":formId": formId,
            ":formPreffiledInputValue": inputValue,
          };
          navigateToForm(urlParams);
          callback();
          setIsLoading(false);
        }
      )
    );
  };

  useEffect(() => {
    if (search) {
      setIsCheckFinished(false);
      const params = querystring.parse(search.replace("?", ""));
      const formPath = params.formPath;
      const inputValue = params[FORM_PREFILLED_DATA_INPUT_NAME];

      const wasFormLoginInitiated = localStorage.getItem(
        "isFormLoginInitiated"
      );

      if (formPath && inputValue && wasFormLoginInitiated && isAuth) {
        if (
          inputValue === FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.MY_BEHALF &&
          userPersonIdenfitier.split("-")?.[0] !== "PNOBG"
        ) {
          setIsForeignerForbiddenModalOpen(true);
          setIsCheckFinished(true);
        } else {
          getFormByPath(formPath, inputValue, () => {
            localStorage.removeItem("isFormLoginInitiated");
          });
        }
      } else {
        setIsCheckFinished(true);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onCtaClick = (formPath, inputValue) => {
    let fullFormPath = formPath;
    if (MULTITENANCY_ENABLED && tenantId) {
      fullFormPath = `${tenantId}-${fullFormPath}`;
    }
    if (isAuth) {
      if (
        inputValue === FORM_PREFILLED_DATA_ALLOWED_INPUT_VALUES.MY_BEHALF &&
        userPersonIdenfitier.split("-")?.[0] !== "PNOBG"
      ) {
        setIsForeignerForbiddenModalOpen(true);
      } else {
        getFormByPath(fullFormPath, inputValue);
      }
    } else {
      localStorage.setItem("isFormLoginInitiated", true);
      doLogin(
        // eslint-disable-next-line max-len
        `${window.location.href}?formPath=${fullFormPath}&${FORM_PREFILLED_DATA_INPUT_NAME}=${inputValue}`
      );
    }
  };

  return (
    <PageContainer>
      <div className={styles.addressWrapper}>
        <div className={styles.addressContainer}>
          {isCheckFinished && !isLoading ? (
            <>
              <CustomBreadcrumbs
                link={PAGE_ROUTES.REQUEST_SERVICE}
                linkText={t("addressRegistratrion.backLinkText")}
                title={t("addressRegistratrion.title")}
              />
              <ForeignerForbiddenModal
                modalOpen={isForeignerForbiddenModalOpen}
              />
              <div className={`row no-gutters ${styles.cardWrapper}`}>
                {sections.map((section, index) => (
                  <div className={`col ${styles.card}`} key={index}>
                    <div>
                      <h2 className={styles.title}>{t(section.title)}</h2>
                      <h3 className={styles.subtitle}>{t(section.subtitle)}</h3>
                    </div>
                    <div>
                      <div>{t("addressRegistratrion.changeOf")}</div>
                      <SmCta
                        className={styles.cta}
                        type={SmCtaTypes.SECONDARY}
                        accessibilityProps={{
                          "aria-label": t(
                            // eslint-disable-next-line
                          `screen.reader.addressRegistration.cta.primaryAddress.${section[FORM_PREFILLED_DATA_INPUT_NAME]}`
                          ),
                        }}
                        onClick={() =>
                          onCtaClick(
                            PERMANENT_ADDRESS_FORM_PATH,
                            section[FORM_PREFILLED_DATA_INPUT_NAME]
                          )
                        }
                      >
                        {t("addressRegistratrion.primaryAddress")}
                      </SmCta>
                      <SmCta
                        className={styles.cta}
                        type={SmCtaTypes.SECONDARY}
                        accessibilityProps={{
                          "aria-label": t(
                            // eslint-disable-next-line
                            `screen.reader.addressRegistration.cta.currentAddress.${section[FORM_PREFILLED_DATA_INPUT_NAME]}`
                          ),
                        }}
                        onClick={() =>
                          onCtaClick(
                            CURRENT_ADDRESS_FORM_PATH,
                            section[FORM_PREFILLED_DATA_INPUT_NAME]
                          )
                        }
                      >
                        {t("addressRegistratrion.currentAddress")}
                      </SmCta>
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <Loading />
          )}
        </div>
      </div>
    </PageContainer>
  );
};
export default AddressRegistration;
