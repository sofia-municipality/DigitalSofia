import { httpPOSTRequest, httpGETRequest } from "../httpRequestHandler";
import API from "../endpoints";
import { SIGN_DOCUMENT_PROVIDERS } from "../../constants/constants";

export const signDocument = async ({ params, type }) => {
  const url =
    type === SIGN_DOCUMENT_PROVIDERS.EVROTRUST
      ? API.EVROTRUST_SIGN
      : API.EVROTRUST_SIGN;

  const res = await httpPOSTRequest(url, params);
  return res.data;
};

export const getDocumentStatus = async (transactionId) => {
  const url = API.EVROTRUST_STATUS.replace("<transactionId>", transactionId);

  const res = await httpGETRequest(url);
  return res.data;
};

export const getNexuJS = async () => {
  const result = await httpGETRequest(
    "http://localhost:9795/nexu.js",
    null,
    null,
    false,
    {}
  );
  return result;
};

export const formatPayloadForKEP = (
  certificate,
  file,
  signatureValue = null
) => {
  const { content, fileName, formioId, ...rest } = file;
  const payloadForSign = {
    signingCertificate: certificate.certificate,
    certificateChain: certificate.certificateChain,
    encryptionAlgorithm: certificate.encryptionAlgorithm,
    documentToSign: content,
    documentName: fileName,
    formioId,
    signingDate: new Date(),
    ...(rest || {}),
  };
  if (signatureValue) {
    payloadForSign.signatureValue = signatureValue;
  }
  return payloadForSign;
};

export const getDocumentToSign = async (payloadForSign) => {
  const res = await httpPOSTRequest(API.KEP_DATA, payloadForSign);
  return res;
};

export const signDocumentNexu = async (payloadForSign) => {
  const res = await httpPOSTRequest(API.KEP_SIGN, payloadForSign);
  return res;
};
