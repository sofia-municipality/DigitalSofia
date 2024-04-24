import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import moment from "moment";
import CachedIcon from "@mui/icons-material/Cached";

import { useGetPaidTaxesData } from "../../../../../../apiManager/apiHooks";
import { TAX_CATEGORIES } from "../../../../../../constants/constants";

import TaxesAccordion from "../../../../components/Taxes/TaxesAccordion";
import Loading from "../../../../../../containers/Loading";
import PageContainer from "../../../../components/PageContainer";
import CustomBreadcrumbs from "../../../../components/Breadcrumbs/CustomBreadcrumbs";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import { TaxesContainerType } from "../../../../components/Taxes/utils";
import SmCta, {
  SmCtaTypes,
  SmCtaSizes,
} from "../../../../../../components/sm/components/buttons/SmCta";

import styles from "./localTaxesAndFeesDetails.module.scss";

const LocalTaxesAndFeesDetails = () => {
  const { t } = useTranslation();
  const { paymentId } = useParams();
  const { data = {}, isLoading, fetch: getPaidTaxData } = useGetPaidTaxesData();
  const { payments = {}, paymentDate } = data;
  const taxRecords = Object.entries(payments);
  const date = moment(paymentDate).format("DD.MM.YYYY");

  const onClick = () => {
    getPaidTaxData({ id: paymentId, refetch: true });
  };

  useEffect(() => {
    getPaidTaxData({ id: paymentId });
  }, [paymentId, getPaidTaxData]);

  return (
    <PageContainer>
      <div className={styles.localTaxesWrapper}>
        <div className={styles.localTaxesContainer}>
          {!isLoading ? (
            <>
              <CustomBreadcrumbs
                className={styles.breadcrumbs}
                link={PAGE_ROUTES.MY_SERVICES}
                linkText={t("myServices.backLinkText")}
                title={t("myServices.localTaxesAndFees.title")}
              />
              <div className={styles.content}>
                <div className="row no-gutters">
                  {taxRecords?.length ? (
                    <div className={`col-12 ${styles.contentHeader}`}>
                      <span className={styles.requestDate}>{`${t(
                        "myServices.localTaxesAndFees.details.date"
                      )} ${date}`}</span>

                      <SmCta
                        size={SmCtaSizes.MEDIUM}
                        type={SmCtaTypes.OUTLINE}
                        className={styles.refreshCta}
                        onClick={onClick}
                      >
                        <span className="sm-cta-outline-underline">
                          {t("refreshData.cta")}
                        </span>
                        <CachedIcon
                          className={styles.refreshCtaIcon}
                          width="20"
                          height="15"
                        />
                      </SmCta>
                    </div>
                  ) : null}
                  <div className={`col-12 ${styles.accordionWrapper}`}>
                    {taxRecords?.length ? (
                      taxRecords.map(([key, value], index) => {
                        if (Object.values(TAX_CATEGORIES).includes(key)) {
                          return (
                            <TaxesAccordion
                              id={index}
                              key={key}
                              data={value}
                              type={key}
                              forceOpenClose={"open"}
                              showTotal={false}
                              containerType={TaxesContainerType.STATUS}
                            />
                          );
                        }
                      })
                    ) : (
                      <div className={styles.noTaxDataFound}>
                        <span className={styles.noTaxDataFoundText}>
                          {t("localTaxes.details.noTaxData")}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
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

export default LocalTaxesAndFeesDetails;
