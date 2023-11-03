import React, { useState } from "react";
import { useTranslation } from "react-i18next";

const Search = ({
  searchInputs = [],
  handleSearch,
  onSearchClear,
  className,
}) => {
  const { t } = useTranslation();
  const searchInputsArray = Array.isArray(searchInputs)
    ? searchInputs
    : [searchInputs];
  const [searchData, setSearchData] = useState({});

  const onClear = () => {
    setSearchData({});
    handleSearch({});
    onSearchClear();
  };

  return (
    <div className={`d-flex ${className}`}>
      {searchInputsArray.map(({ id, placeholder }, index) => (
        <input
          key={index}
          type="search"
          onChange={(e) => {
            setSearchData((prevState) => ({
              ...prevState,
              [id]: e.target.value,
            }));
          }}
          onKeyDown={(e) => e.key === "Enter" && handleSearch(searchData)}
          autoComplete="off"
          className="form-control mr-2"
          value={searchData[id] || ""}
          placeholder={t(placeholder || "Search...")}
        />
      ))}
      {!!Object.keys(searchData).length && (
        <button
          type="button"
          className="btn btn-outline-primary mr-2"
          onClick={() => onClear()}
        >
          <i className="fa fa-times"></i>
        </button>
      )}
      <button
        type="button"
        className="btn btn-outline-primary"
        name="search-button"
        title={t("so.translations.search.cta.title")}
        onClick={() => handleSearch(searchData)}
      >
        <i className="fa fa-search"></i>
      </button>
    </div>
  );
};

export default Search;
