import React from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { setDraftDelete } from "../../actions/draftActions";
import { useGetBaseUrl } from "../../customHooks";

const DraftOperations = ({ row }) => {
  const { t } = useTranslation();
  const dispatch = useDispatch();
  const redirectUrl = useGetBaseUrl();
  const url = `${redirectUrl}form/${row.formId}/draft/${row.id}/edit`;
  const buttonText = t("Edit");
  const icon = "fa fa-edit";
  const deleteIcon = "fa fa-trash fa-lg delete_button";
  const deleteDraft = () => {
    dispatch(
      setDraftDelete({
        modalOpen: true,
        draftId: row.id,
        draftName: row.DraftName,
      })
    );
  };

  return (
    <>
      <div>
        <Link to={url} style={{ textDecoration: "none" }}>
          <span style={{ color: "blue", cursor: "pointer" }}>
            <span>
              <i className={icon} />
              &nbsp;
            </span>
            {buttonText}
          </span>
        </Link>
        <span style={{ marginLeft: "2rem" }}>
          <span>
            {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
            <i className={deleteIcon} onClick={() => deleteDraft()} />
            &nbsp;
          </span>
        </span>
      </div>
    </>
  );
};

export default DraftOperations;
