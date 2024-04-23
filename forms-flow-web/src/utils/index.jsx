import i18n from "i18next";
import moment from "moment";
import { saveAs } from "file-saver";

export const getFormTranslations = () =>
  Object.entries(i18n.options.resources).reduce((acc, [key, value]) => {
    acc[key] = value.translation;
    return acc;
  }, {});

export const validatePersonalIdentifier = (value, type = "egn") => {
  if (value.length != 10) {
    return false;
  }
  // Check if EGN/PID is numeric
  for (const element of value) {
    if (Number.isNaN(element)) {
      return false;
    }
  }
  if (type === "egn") {
    // Check day, month and year
    let year = parseInt(value.substr(0, 2));
    let month = parseInt(value.substr(2, 2));
    const day = parseInt(value.substr(4, 2));
    // Check month
    if (month < 1 || (month > 12 && month < 21) || (month > 32 && month < 41)) {
      return false;
    }
    // Build full year
    if (month >= 1 && month <= 12) {
      year += 1900;
    } else if (month >= 21 && month <= 32) {
      month -= 20;
      year += 1800;
    } else if (month >= 41 && month <= 52) {
      month -= 40;
      year += 2000;
    }
    // Check day
    var m = moment(year + "-" + month + "-" + day, "YYYY-MM-DD");
    if (!m.isValid()) {
      return false;
      // false
    }
  }

  let contr;
  let divisor;
  if (type === "egn") {
    contr = "020408051009070306";
    divisor = 11;
  } else {
    contr = "211917131109070301";
    divisor = 10;
  }
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    const digit = value.substr(i, 1);
    const weight = contr.substr(i * 2, 2);
    sum += Number(digit) * Number(weight);
  }
  const last = sum % divisor == 10 ? 0 : sum % divisor;
  return last == parseInt(value.substr(9, 1));
};

export const capitalizeFirstLetter = (string) => {
  return string.charAt(0).toUpperCase() + string.slice(1);
};

export const convertToDecimal = (value, fractionDigits = 2) =>
  Number(value.toFixed(fractionDigits));

export const MOBILE_APP_TYPES = {
  BOTH: "BOTH",
  ANDROID: "ANDROID",
  IOS: "IOS",
};

export const sendMobleAppMessage = (
  mobileAppInterface,
  method,
  data,
  os = MOBILE_APP_TYPES.BOTH
) => {
  const message = typeof data === "string" ? data : JSON.stringify(data);
  try {
    if (os !== MOBILE_APP_TYPES.IOS && window[mobileAppInterface]) {
      // Call Android interface
      window[mobileAppInterface]?.[method]?.(message);
    } else if (
      os !== MOBILE_APP_TYPES.ANDROID &&
      window.webkit &&
      window.webkit.messageHandlers
    ) {
      // Call iOS interface
      window.webkit.messageHandlers[mobileAppInterface]?.[method]?.(message);
    }
  } catch (err) {
    console.log(err);
  }
};

export const forceDownload = (url, fileName) => {
  const element = document.createElement("a");
  const clearUrl = url.replace(/^data:.+,/, "");

  element.setAttribute(
    "href",
    "data:application/octet-stream;base64," + encodeURIComponent(clearUrl)
  );
  element.setAttribute("download", fileName);
  document.body.appendChild(element);
  element.click();
  document.body.removeChild(element);
};

export const getIsSafari = () => {
  let isSafari = false;
  const browser = window.navigator.userAgent.toLowerCase();
  if (browser.includes("iphone")) {
    if (!browser.includes("fxios") && !browser.includes("crios")) {
      isSafari = true;
    }
  }

  return isSafari;
};

export const downloadBase64File = (
  base64Url,
  fileName,
  sendMobileAppEvent = false
) => {
  if (sendMobileAppEvent) {
    sendMobleAppMessage(
      "download_files_android",
      "sendFilename",
      fileName,
      MOBILE_APP_TYPES.ANDROID
    );
  }
  const isSafari = getIsSafari();
  if (isSafari) {
    forceDownload(base64Url, fileName);
  } else {
    saveAs(base64Url, fileName);
  }
};

export const downloadFile = async (
  url,
  fileName,
  sendMobileAppEvent = true
) => {
  const response = await fetch(url);
  const blob = await response.blob();
  var reader = new FileReader();
  reader.onload = function () {
    downloadBase64File(this.result, fileName, sendMobileAppEvent);
  };
  reader.readAsDataURL(blob);
};
