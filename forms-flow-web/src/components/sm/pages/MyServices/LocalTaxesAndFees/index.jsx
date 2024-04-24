import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import PageContainer from "../../../components/PageContainer";

import CustomBreadcrumbs from "../../../components/Breadcrumbs/CustomBreadcrumbs";
import { useGetPaidTaxesGroups } from "../../../../../apiManager/apiHooks";
import TaxesSection from "./components/TaxSection";
import styles from "./myServices.localTaxesAndFees.module.scss";
import Loading from "../../../../../containers/Loading";
import Pagination from "../../../components/Pagination";

const limit = 10;

const LocalTaxesAndFees = () => {
  const { t } = useTranslation();
  const [currentPage, setCurrentPage] = useState(1);
  const {
    data,
    isLoading,
    fetch: fetchPaidTaxesGroups,
  } = useGetPaidTaxesGroups();
  const { obligations = {}, total: totalItems } = data;

  useEffect(() => {
    fetchPaidTaxesGroups({ pageNo: currentPage, limit });
  }, [currentPage, fetchPaidTaxesGroups]);

  const setPaginationPage = (index) => {
    setCurrentPage(index);
    document.getElementById("app").scrollTo(0, 0);
  };

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
              {Object.keys(obligations).length ? (
                <>
                  <div className={styles.taxSectionsWrapper}>
                    {Object.entries(obligations).map(([key, value], index) => (
                      <TaxesSection key={index} title={key} items={value} />
                    ))}
                  </div>
                  {totalItems > limit ? (
                    <div className="container-fluid">
                      <div className="row">
                        <div className="col-12 mt-4">
                          <Pagination
                            limit={limit}
                            totalItems={totalItems}
                            selectedPage={currentPage}
                            changePage={setPaginationPage}
                          ></Pagination>
                        </div>
                      </div>
                    </div>
                  ) : null}
                </>
              ) : (
                <div className={styles.noServicesFound}>
                  <span className={styles.noServicesText}>
                    {t("myServices.localTaxes.noData")}
                  </span>
                </div>
              )}
            </>
          ) : (
            <Loading />
          )}
        </div>
      </div>
    </PageContainer>
  );
};
export default LocalTaxesAndFees;
