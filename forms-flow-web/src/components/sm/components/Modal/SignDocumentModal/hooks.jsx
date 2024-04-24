import { useContext } from "react";

import { useGetDocumentSignStatus } from "../../../../../apiManager/apiHooks";
import { SIGN_DOCUMENT_STATUSES } from "../../../../../constants/constants";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import { useNavigateTo } from "../../../../../customHooks";
import { APPLICATION_STATUS_FORCED_ERROR } from "../../../../../constants/formEmbeddedConstants";

import { SignDocumentContext, STEPS } from "./context";

export const useCheckDocumentStatus = ({
  signitureType,
  onSuccess,
  onReject,
  shouldSubmitOnPendingStatus,
  onFormSubmissionError,
  redirectUrl,
}) => {
  const redirect = useNavigateTo(
    redirectUrl || PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION
  );
  const { fetch: getDocumentStatus, isLoading } = useGetDocumentSignStatus();

  const { signDocumentContext, setSignDocumentContext } =
    useContext(SignDocumentContext);

  const { currentStep, transactions = [] } = signDocumentContext;
  const checkDocumentStatus = async () => {
    try {
      const res = await getDocumentStatus(transactions[0]?.transactionID);
      const { status } = res || {};
      switch (status) {
        case SIGN_DOCUMENT_STATUSES.SIGNED: {
          setSignDocumentContext({ currentStep: STEPS.LOADING });
          onSuccess()
            .then(() => {
              setSignDocumentContext({ currentStep: STEPS.SUCCESS });
            })
            .catch((err) => {
              console.log(err);
              const isApplicationStatusForcedError =
                APPLICATION_STATUS_FORCED_ERROR.includes(err);

              if (isApplicationStatusForcedError) {
                setSignDocumentContext({
                  currentStep: STEPS.APPLICATION_STATUS_ERROR,
                  applicationStatusForcedError: err,
                });
              } else {
                onFormSubmissionError
                  ? onFormSubmissionError(err)
                  : setSignDocumentContext({ currentStep: STEPS.ERROR });
              }
            });

          break;
        }
        case SIGN_DOCUMENT_STATUSES.PENDING: {
          if (currentStep === STEPS.PENDING) {
            if (shouldSubmitOnPendingStatus) {
              setSignDocumentContext({ currentStep: STEPS.LOADING });
              onSuccess(null, true)
                .then(() => {
                  redirect();
                })
                .catch((err) => {
                  console.log(err);
                  onFormSubmissionError
                    ? onFormSubmissionError()
                    : setSignDocumentContext({ currentStep: STEPS.ERROR });
                });
            } else {
              redirect();
            }
          } else {
            setSignDocumentContext({ currentStep: STEPS.PENDING });
          }
          break;
        }
        case SIGN_DOCUMENT_STATUSES.REJECTED: {
          setSignDocumentContext({ currentStep: STEPS.LOADING });
          if (signitureType === "consent") {
            onReject()
              .then(() => {
                setSignDocumentContext({ currentStep: STEPS.REJECTED });
              })
              .catch((err) => {
                console.log(err);
                onFormSubmissionError
                  ? onFormSubmissionError()
                  : setSignDocumentContext({ currentStep: STEPS.ERROR });
              });
          } else {
            setSignDocumentContext({ currentStep: STEPS.ERROR });
          }
          break;
        }
        default: {
          setSignDocumentContext({ currentStep: STEPS.ERROR });
          break;
        }
      }
    } catch (_) {
      setSignDocumentContext({ currentStep: STEPS.ERROR });
    }
  };

  return {
    checkDocumentStatus,
    isLoading,
  };
};
