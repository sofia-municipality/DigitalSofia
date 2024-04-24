import React, { useContext, useEffect } from "react";
import Modal from "react-bootstrap/Modal";

import { signDocument } from "../../../../../apiManager/services/documentSignServices";
import {
  getNexuJS,
  formatPayloadForKEP,
  getDocumentToSign,
  signDocumentNexu,
} from "../../../../../apiManager/services/documentSignServices";
import { APPLICATION_STATUS_FORCED_ERROR } from "../../../../../constants/formEmbeddedConstants.js";

import {
  SignDocumentContext,
  SignDocumentContextProvider,
  STEPS,
} from "./context";
import "./signDocumentModal.scss";

import Pending from "./steps/Pending";
import Success from "./steps/Success";
import Error from "./steps/Error";
import Sent from "./steps/Sent";
import Loading from "./steps/Loading";
import Rejected from "./steps/Rejected";
import NexuInstructions from "./steps/NexuInstructions";
import ApplicationStatusError from "./steps/ApplicationStatusError";

const renderCurrentStep = ({ currentStep, ...rest }) => {
  switch (currentStep) {
    case STEPS.SENT:
      return <Sent {...rest} />;
    case STEPS.PENDING:
      return <Pending {...rest} />;
    case STEPS.SUCCESS:
      return <Success {...rest} />;
    case STEPS.REJECTED:
      return <Rejected {...rest} />;
    case STEPS.ERROR:
      return <Error {...rest} />;
    case STEPS.NEXU_INSTRUCTIONS:
      return <NexuInstructions {...rest} />;
    case STEPS.APPLICATION_STATUS_ERROR:
      return <ApplicationStatusError {...rest} />;
    case STEPS.LOADING:
      return <Loading />;
    default:
      return <></>;
  }
};

const SignDocumentModal = React.memo(
  ({
    modalOpen = false,
    params = {},
    hideStatusModals = false,
    onInit = () => {},
    onSubmit = () => {},
    closeModal = () => {},
    onSuccess = () => {},
    onSuccessClose = () => {},
    onReject = () => {},
    onRejectClose = () => {},
    onFormSubmissionError = () => {},
    onFormSubmissionSuccess = () => {},
  }) => {
    const { signDocumentContext, setSignDocumentContext } =
      useContext(SignDocumentContext);
    const { currentStep, applicationStatusForcedError } = signDocumentContext;
    const {
      type = "evrotrust",
      referenceNumber,
      signitureType,
      rejectModalTitle,
      rejectModalDescription,
      shouldSubmitOnPendingStatus,
      redirectUrl,
    } = params;

    const onClose = () => {
      setSignDocumentContext({});
      closeModal();
    };

    useEffect(() => {
      if (type === "evrotrust") {
        setSignDocumentContext({
          currentStep: STEPS.LOADING,
        });
        signDocument(params)
          .then((res) => {
            if (hideStatusModals && onSubmit) {
              onSubmit()
                .then(() => {
                  onFormSubmissionSuccess && onFormSubmissionSuccess();
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
                      : setSignDocumentContext({
                          currentStep: STEPS.ERROR,
                        });
                  }
                });
            } else {
              setSignDocumentContext({
                currentStep: STEPS.SENT,
                transactions: res?.response?.transactions,
              });
              onInit();
            }
          })
          .catch((err) => {
            console.log(err);
            setSignDocumentContext({ currentStep: STEPS.ERROR });
          });
      } else if (type === "kep") {
        getNexuJS()
          .then((nexu) => {
            if (nexu?.status === 200) {
              var se = document.createElement("script");
              se.type = "text/javascript";
              se.text = nexu.data;
              document.getElementsByTagName("head")[0].appendChild(se);
              window.nexu_get_certificates(
                async (certificate) => {
                  if (certificate.response !== null) {
                    try {
                      const certificateChain = certificate.response;
                      let payload = formatPayloadForKEP(
                        certificateChain,
                        params.params
                      );
                      try {
                        let data = await getDocumentToSign(payload);
                        if (data.status === 200) {
                          window.nexu_sign_with_token_infos(
                            certificateChain.tokenId.id,
                            certificateChain.keyId,
                            data.data.dataToSign,
                            "SHA256",
                            async (signatureData) => {
                              payload.signatureValue =
                                signatureData.response.signatureValue;
                              try {
                                setSignDocumentContext({
                                  currentStep: STEPS.LOADING,
                                });
                                const res = await signDocumentNexu(payload);
                                if (res.status === 200) {
                                  const file =
                                    "data:application/pdf;base64," +
                                    res.data.bytes;
                                  if (params.noAutosubmit) {
                                    setSignDocumentContext({});
                                  }
                                  onSuccess(file)
                                    .then(() => {
                                      setSignDocumentContext({
                                        currentStep: STEPS.SUCCESS,
                                      });
                                    })
                                    .catch((err) => {
                                      console.log(err);
                                      const isApplicationStatusForcedError =
                                        APPLICATION_STATUS_FORCED_ERROR.includes(
                                          err
                                        );

                                      if (isApplicationStatusForcedError) {
                                        setSignDocumentContext({
                                          currentStep:
                                            STEPS.APPLICATION_STATUS_ERROR,
                                          applicationStatusForcedError: err,
                                        });
                                      } else {
                                        onFormSubmissionError
                                          ? onFormSubmissionError(err)
                                          : setSignDocumentContext({
                                              currentStep: STEPS.ERROR,
                                            });
                                      }
                                    });
                                }
                              } catch (err) {
                                console.log(err);
                                setSignDocumentContext({
                                  currentStep: STEPS.ERROR,
                                });
                              }
                            },
                            (err) => {
                              console.log(err);
                              setSignDocumentContext({
                                currentStep: STEPS.ERROR,
                              });
                            }
                          );
                        }
                      } catch (err) {
                        console.log(err);
                        setSignDocumentContext({ currentStep: STEPS.ERROR });
                      }
                    } catch (err) {
                      console.log(err);
                      setSignDocumentContext({ currentStep: STEPS.ERROR });
                    }
                  } else {
                    throw new Error("Problem getting certificates");
                  }
                },
                (err) => {
                  console.log(err);
                  setSignDocumentContext({
                    currentStep: STEPS.ERROR,
                  });
                }
              );
            } else {
              throw new Error("Problem loading nexu");
            }
          })
          .catch((err) => {
            console.log(err);
            setSignDocumentContext({ currentStep: STEPS.NEXU_INSTRUCTIONS });
          });
      }

      //eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return currentStep ? (
      <Modal
        size="lg"
        aria-labelledby="modal-title"
        show={modalOpen}
        className={`signDocumentModal signDocumentModal-${currentStep}`}
      >
        {renderCurrentStep({
          currentStep,
          onClose,
          onSuccess,
          referenceNumber,
          onSuccessClose,
          signitureType,
          onReject,
          onRejectClose,
          rejectModalTitle,
          rejectModalDescription,
          shouldSubmitOnPendingStatus,
          onFormSubmissionError,
          redirectUrl,
          applicationStatusForcedError,
        })}
      </Modal>
    ) : null;
  }
);

const SignDocumentWrapper = React.memo((props) => {
  return (
    <SignDocumentContextProvider>
      <SignDocumentModal {...props} />
    </SignDocumentContextProvider>
  );
});

export default SignDocumentWrapper;
