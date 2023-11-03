import React, { useState } from "react";
import { useTranslation } from "react-i18next";

import TranslationFormModal from "../TranslationFormModal";
import TranslationDeleteModal from "../TranslationDeleteModal";

const Operations = ({ data, onEdit, onDelete }) => {
  const { t } = useTranslation();
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const onSubmit = (data) => {
    setIsEditModalOpen(false);
    onEdit(data);
  };

  return (
    <>
      <div className="d-flex align-items-center flex-column flex-md-row">
        <button
          className="btn btn-outline-primary mr-md-3"
          onClick={() => setIsEditModalOpen(true)}
        >
          <i className="fa fa-pencil-square-o mr-1" />
          {t("so.translations.edit.cta")}
        </button>
        <i
          role="button"
          tabIndex={0}
          className="fa fa-trash fa-lg delete_button mt-4 mt-md-0"
          onClick={() => setIsDeleteModalOpen(true)}
          onKeyDown={(e) => e.key === "Enter" && setIsDeleteModalOpen(true)}
        />
      </div>
      {isEditModalOpen ? (
        <TranslationFormModal
          title="so.translations.edit.modal.title"
          isOpen={isEditModalOpen}
          onClose={() => setIsEditModalOpen(false)}
          onSubmit={onSubmit}
          data={data}
        />
      ) : null}
      {isDeleteModalOpen ? (
        <TranslationDeleteModal
          isOpen={isDeleteModalOpen}
          onClose={() => setIsDeleteModalOpen(false)}
          onDelete={() => {
            setIsDeleteModalOpen(false);
            onDelete(data.id);
          }}
        />
      ) : null}
    </>
  );
};

export default Operations;
