import React, { useState, useEffect, useCallback } from "react";
import { useSelector } from "react-redux";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";
import BootstrapTable from "react-bootstrap-table-next";
import ToolkitProvider from "react-bootstrap-table2-toolkit";
import filterFactory from "react-bootstrap-table2-filter";
import { Translation } from "react-i18next";
import LoadingOverlay from "react-loading-overlay";
import paginationFactory from "react-bootstrap-table2-paginator";

import { useDevice } from "../../../customHooks";
import {
  useFetchAdminTranslations,
  useCreateTranslation,
  useUpdateTranslation,
  useDeleteTranslation,
} from "../../../apiManager/apiHooks";
import AdministrationContainer from "../AdministrationContainer";
import { getoptions } from "./tableConfig";
import Search from "./components/Search";
import Operations from "./components/Operations";
import TranslationFormModal from "./components/TranslationFormModal";
import FileService from "../../../services/FileService";
import TranslationUploadCta from "./components/TranslationUploadCta";

import styles from "./translationAdministration.module.scss";

const defaultLimit = 5;

const useGetColumns = (onEdit, onDelete) => {
  return [
    {
      dataField: "language",
      classes: styles.column,
      headerClasses: styles.column,
      text: (
        <Translation>
          {(t) => t("so.translations.table.column.language")}
        </Translation>
      ),
    },
    {
      dataField: "key",
      classes: styles.column,
      headerClasses: styles.column,
      text: (
        <Translation>
          {(t) => t("so.translations.table.column.key")}
        </Translation>
      ),
    },
    {
      dataField: "translation",
      classes: styles.column,
      headerClasses: styles.column,
      text: (
        <Translation>
          {(t) => t("so.translations.table.column.translation")}
        </Translation>
      ),
    },
    {
      dataField: "id",
      text: (
        <Translation>
          {(t) => t("so.translations.table.column.operations")}
        </Translation>
      ),
      headerClasses: styles.column,
      formatter: (cell, row) => {
        return <Operations data={row} onEdit={onEdit} onDelete={onDelete} />;
      },
    },
  ];
};

const TranslationAdministration = () => {
  const { t } = useTranslation();
  const { isMobile } = useDevice();
  const {
    data = {},
    isLoading,
    fetch: getTranslations,
  } = useFetchAdminTranslations();
  const { fetch: addTranslation } = useCreateTranslation();
  const { fetch: updateTranslation } = useUpdateTranslation();
  const { fetch: deleteTranslation } = useDeleteTranslation();

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [shouldRefresh, setShouldRefresh] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [limit, setLimit] = useState(defaultLimit);
  const [searchData, setSearchData] = useState({});
  const { data: translationData = [], total } = data;
  const mappedTranslationsData = translationData?.map((item) => ({
    id: item._id,
    ...item.data,
  }));
  const selectLanguages = useSelector((state) => state.user.selectLanguages);

  const onTranslationUpdate = useCallback(
    (data) => {
      updateTranslation(data)
        .then(() => {
          setShouldRefresh(true);
          toast.success(t("so.translations.edit.success"));
        })
        .catch((err) => {
          toast.error(err.message || t("so.translations.uknown.error"));
        });
    },
    [updateTranslation, t]
  );

  const onTranslationDelete = useCallback(
    (data) => {
      deleteTranslation(data)
        .then(() => {
          setShouldRefresh(true);
          toast.success(t("so.translations.delete.success"));
        })
        .catch((err) => {
          toast.error(err.message || t("so.translations.uknown.error"));
        });
    },
    [deleteTranslation, t]
  );

  const columns = useGetColumns(onTranslationUpdate, onTranslationDelete);

  useEffect(() => {
    getTranslations({ limit, currentPage, searchData });
  }, [currentPage, getTranslations, limit, searchData]);

  useEffect(() => {
    if (shouldRefresh) {
      getTranslations({ limit, currentPage, searchData }).then(() => {
        setShouldRefresh(false);
      });
    }
  }, [currentPage, getTranslations, limit, searchData, shouldRefresh]);

  const handleSearch = (data) => {
    setCurrentPage(1);
    setSearchData((prevState) => ({
      ...prevState,
      ...data,
    }));
  };

  const onSearchClear = () => {
    setCurrentPage(1);
    setSearchData((prevState) => ({
      language: prevState.language,
    }));
  };

  const handlePageChange = (_, data) => {
    if (limit !== data.sizePerPage) {
      setCurrentPage(1);
      setLimit(data.sizePerPage);
    } else {
      setCurrentPage(data.page);
    }
  };

  const exportData = () => {
    const dataToExport = translationData.map((e) =>
      Object.assign({}, { id: e._id }, e.data)
    );
    FileService.downloadFile(
      dataToExport,
      () => {
        toast.success(t("so.translations.export.success"));
      },
      "translations"
    );
  };

  const noDataFound = () => {
    return (
      <span>
        <div
          className="container"
          style={{
            maxWidth: "900px",
            margin: "auto",
            height: "50vh",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <h3>{t("so.translations.no.records.found")}</h3>
          <p>{t("so.translations.no.records.found.hint")}</p>
        </div>
      </span>
    );
  };

  return (
    <AdministrationContainer
      title={t("so.administration.translationAdministration")}
    >
      <section>
        <div className="row my-2 justify-content-between">
          <div className="col-md-8">
            <div className="input-group">
              <select
                className="form-control select mr-2 mt-2"
                title={t("so.translations.language.select.title")}
                style={{ maxWidth: "150px", minWidth: "150px" }}
                onChange={(e) => {
                  const language =
                    e.target.value === "all" ? null : e.target.value;
                  handleSearch({ language });
                }}
                aria-label={t("so.translations.language.select.title")}
              >
                <option key={0} value="all">
                  {t("so.translations.language.select.all.option")}
                </option>
                {selectLanguages.map(({ label }, index) => (
                  <option key={index + 1} value={label}>
                    {label}
                  </option>
                ))}
              </select>
              <Search
                className="mt-2"
                searchInputs={[
                  {
                    id: "key",
                    placeholder: t("so.translations.search.by.key.label"),
                  },
                  {
                    id: "translation",
                    placeholder: t(
                      "so.translations.search.by.translation.label"
                    ),
                  },
                ]}
                handleSearch={handleSearch}
                onSearchClear={onSearchClear}
              />
            </div>
          </div>
          <div className="col-md-4 d-flex justify-content-md-end align-items-end mt-2">
            <button
              className="btn btn-primary btn-md"
              onClick={() => setIsAddModalOpen(true)}
            >
              {t("so.translations.add.cta")}
            </button>
            <button
              className="btn btn-outline-primary btn-md ml-2"
              onClick={exportData}
            >
              {t("so.translations.export.cta")}
            </button>
            <TranslationUploadCta
              className="ml-2"
              total={total}
              onClose={() => setShouldRefresh(true)}
            />
          </div>
        </div>
        {isAddModalOpen ? (
          <TranslationFormModal
            title="so.translations.add.modal.title"
            type="add"
            isOpen={isAddModalOpen}
            onClose={() => setIsAddModalOpen(false)}
            onSubmit={(data) => {
              addTranslation(data)
                .then(() => {
                  setIsAddModalOpen(false);
                  setShouldRefresh(true);
                  toast.success(t("so.translations.add.success"));
                })
                .catch((err) => {
                  toast.error(err.message || t("so.translations.uknown.error"));
                });
            }}
          />
        ) : null}
        <ToolkitProvider
          bootstrap4
          keyField="id"
          data={mappedTranslationsData}
          columns={columns}
          search
        >
          {(props) => {
            return (
              <LoadingOverlay
                active={isLoading}
                spinner
                text={t("so.translations.loading")}
                className={styles.tableWrapper}
              >
                <BootstrapTable
                  remote={{
                    pagination: true,
                    filter: true,
                  }}
                  pagination={paginationFactory(
                    getoptions(currentPage, limit, total, isMobile)
                  )}
                  onTableChange={handlePageChange}
                  Loading={isLoading}
                  filter={filterFactory()}
                  filterPosition={"top"}
                  noDataIndication={() =>
                    !translationData.length && !isLoading ? noDataFound() : ""
                  }
                  {...props.baseProps}
                />
              </LoadingOverlay>
            );
          }}
        </ToolkitProvider>
      </section>
    </AdministrationContainer>
  );
};

export default TranslationAdministration;
