import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";

import Loading from "../../containers/Loading";
import { getForm, getSubmission } from "react-formio";
// import { Translation } from "react-i18next";
import { getDraftById } from "../../apiManager/services/draftService";
import Edit from "./Edit";
import { push } from "connected-react-router";
import { useGetBaseUrl } from "../../customHooks";

const EditDraft = React.memo(() => {
  const { draftId } = useParams();

  const isDraftDetailLoading = useSelector(
    (state) => state.draft.isDraftDetailLoading
  );

  const dispatch = useDispatch();
  const redirectUrl = useGetBaseUrl();

  useEffect(() => {
    dispatch(
      getDraftById(draftId, (err, res) => {
        if (!err) {
          if (res.id && res.formId) {
            dispatch(getForm("form", res.formId));
            dispatch(getSubmission("submission", res.submissionId, res.formId));
          }
        } else {
          dispatch(push(`${redirectUrl}404`));
        }
      })
    );
    return () => {
      //   dispatch(setApplicationDetailLoader(true));
      //   dispatch(setApplicationDetailStatusCode(""));
    };
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draftId, dispatch]);

  if (isDraftDetailLoading) {
    return <Loading />;
  }
  return <Edit page="draft-edit" />;
});

export default EditDraft;
