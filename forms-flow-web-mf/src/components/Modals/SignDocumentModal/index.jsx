import React, { useContext, useEffect } from "react";
import Modal from "react-bootstrap/Modal";

import {
  getNexuJS,
  formatPayloadForKEP,
  getDocumentToSign,
  signDocumentNexu,
} from "../../../apiManager/services/documentSignServices";

import {
  SignDocumentContext,
  SignDocumentContextProvider,
  STEPS,
} from "./context";
import "./signDocumentModal.scss";

import Success from "./steps/Success";
import Error from "./steps/Error";
import Loading from "./steps/Loading";
import NexuInstructions from "./steps/NexuInstructions.jsx";

const renderCurrentStep = ({ currentStep, ...rest }) => {
  switch (currentStep) {
    case STEPS.SUCCESS:
      return <Success {...rest} />;
    case STEPS.ERROR:
      return <Error {...rest} />;
    case STEPS.NEXU_INSTRUCTIONS:
      return <NexuInstructions {...rest} />;
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
    closeModal = () => {},
    onSuccess = () => {},
    onSuccessClose = () => {},
  }) => {
    const { signDocumentContext, setSignDocumentContext } =
      useContext(SignDocumentContext);
    const { currentStep } = signDocumentContext;
    const {
      type = "kep",
      referenceNumber,
      signitureType,
    } = params;

    const onClose = () => {
      setSignDocumentContext({});
      closeModal();
    };

    useEffect(() => {
      if (type === "kep") {
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
                                      setSignDocumentContext({
                                        currentStep: STEPS.ERROR,
                                      });
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
    }, []);

    return currentStep ? (
      <Modal
        size={currentStep === STEPS.SUCCESS ? "md" : "lg" }
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
