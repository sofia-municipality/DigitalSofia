/*******************************************************************************
 * Copyright (c) 2022 Digitall Nature Bulgaria
 *
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

function isValidDate(year, month, day) {
  const date = new Date(year, month, day);
  return (
    date.getDate() === day &&
    date.getMonth() === month &&
    date.getFullYear() === year
  );
}

function validatePersonalIdentifier(value, type = "egn") {
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
    if (!isValidDate(year, month - 1, day)) {
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
}

function setError(elementId, errorElementId, validity, customMessage) {
  const element = document.getElementById(elementId);
  if (element) {
    let errorMsg;
    if (customMessage) {
      errorMsg = element.getAttribute(`data-error-${customMessage}`);
    } else if (validity.valueMissing) {
      errorMsg = element.getAttribute("data-error-required");
    } else {
      errorMsg = element.getAttribute("data-error-invalid");
    }

    const errorEl = document.getElementById(errorElementId);
    if (errorEl) {
      errorEl.innerHTML = errorMsg;
      element.setAttribute("aria-invalid", true);
    }
  }
}

function removeError(element, errorElementId) {
  const errorEl = document.getElementById(errorElementId);
  if (errorEl) {
    errorEl.innerHTML = "";
    element.setAttribute("aria-invalid", false);
  }
}

function onProviderSelect() {
  const sections = [
    "eAuth-section",
    "digitalSofia-section",
    "personIdentifier-section",
  ];
  const select = document.getElementById("sm-provider-select");
  const value = select.value;
  const activeElementId = `${value}-section`;
  const activeSection = document.getElementById(activeElementId);
  activeSection.style = "display: block";
  const disabledSectionIds = sections.filter((e) => e !== activeElementId);
  disabledSectionIds.forEach((e) => {
    const disabledSection = document.getElementById(e);
    disabledSection.style = "display: none";
  });
}

function onDigitalSofiaNextClick() {
  const sections = ["eAuth-section", "digitalSofia-section"];
  const select = document.getElementById("sm-provider-select");
  select.style = "display: none";
  const activeSection = document.getElementById("personIdentifier-section");
  activeSection.style = "display: block";
  const activeSectionTitle = document.getElementById(
    "digitalSofia-login-title"
  );
  activeSectionTitle.style = "display: block";
  const backButtonContainer = document.getElementById(
    "sm-back-button-container"
  );
  backButtonContainer.style = "display: flex";
  const disabledSectionTitle = document.getElementById("eAuth-login-title");
  disabledSectionTitle.style = "display: none";
  sections.forEach((e) => {
    const disabledSection = document.getElementById(e);
    disabledSection.style = "display: none";
  });

  // check and remove message if it exists
  const message = document.getElementById("pf-c-alert");
  if (message) {
    message.style = "display: none";
  }

  // add listeners to input element
  const personIdentifierElement = document.getElementById("personIdentifier");

  if (personIdentifierElement) {
    personIdentifierElement.addEventListener("input", function (e) {
      if (!personIdentifierElement.validity.valid) {
        setError(
          "personIdentifier",
          "input-error-personIdentifier",
          personIdentifierElement.validity
        );
      } else {
        removeError(personIdentifierElement, "input-error-personIdentifier");
      }
    });

    personIdentifierElement.addEventListener("invalid", function (e) {
      e.preventDefault();
      setError(
        "personIdentifier",
        "input-error-personIdentifier",
        e.target.value
      );
    });
  }
}

let totalSeconds;
function renderTimer(seconds) {
  const timer = document.getElementById("timer");
  if (seconds > 0 && timer) {
    const timerProgressPath = document.getElementById("timer-progress-path");
    if (timerProgressPath) {
      const totalPx = 289.027;
      const persentage = (100 * seconds) / totalSeconds;
      const px = (persentage / 100) * totalPx;
      const dashoffset = px - totalPx;
      timerProgressPath.style = `stroke: url('#progress-bar-gradient'); height: 100%; stroke-width: 2; stroke-dasharray: 289.027px, 289.027px; stroke-dashoffset: ${dashoffset}px;`;
    }

    const miliseconds = seconds * 1000;
    const time = new Date(miliseconds).toISOString().slice(11, 19);
    timer.innerHTML = time;

    setTimeout(function () {
      renderTimer(seconds - 1);
    }, 1000);
  } else {
    timer.style = "display: none";
    const timerProgress = document.getElementById("timer-progress");
    if (timerProgress) {
      timerProgress.style = "display: none";
    }

    const tryAgainCta = document.getElementById("try-again-cta-wrapper");
    tryAgainCta.style = "display: flex";
  }
}

async function onPersonIdentifierSubmit(form, e) {
  e.preventDefault();

  const personIdentifierElement = document.getElementById("personIdentifier");
  const submitCta = document.getElementById("personIdentifier-submit-cta");
  const submitCtaWrapper = document.getElementById(
    "personIdentifier-submit-cta-wrapper"
  );
  const realmName = submitCtaWrapper.getAttribute("data-realmName");
  const errorSection = document.getElementById("error-section");
  const errorSectionMobileAppLinks = document.getElementById(
    "error-section-mobileApp-links"
  );
  const backButtonContainer = document.getElementById(
    "sm-back-button-container"
  );
  backButtonContainer.style = "display: none";

  if (errorSection) {
    errorSection.style = "display: none";
  }

  if (errorSectionMobileAppLinks) {
    errorSectionMobileAppLinks.style = "display: none";
  }

  if (!personIdentifierElement.validity.valid) {
    setError(
      "personIdentifier",
      "input-error-personIdentifier",
      personIdentifierElement.validity
    );
  } else {
    //validate if valid egn
    const isValid = validatePersonalIdentifier(personIdentifierElement.value);
    if (!isValid) {
      setError(
        "personIdentifier",
        "input-error-personIdentifier",
        null,
        "invalid-egn"
      );
      return;
    }
    // send push notification and get expiration in seconds
    submitCtaWrapper.classList.add("loading-animation");
    submitCta.setAttribute("disabled", true);

    try {
      const res = await axios.get(
        window.location.origin +
          `/auth/realms/${realmName}/message/authentication-code/generate-code`,
        { params: { personIdentifier: personIdentifierElement.value } }
      );
      const personIdentifierSection = document.getElementById(
        "personIdentifier-section"
      );
      personIdentifierSection.style = "display: none";

      const loadingSection = document.getElementById("loading-section");
      loadingSection.style = "display: block";

      totalSeconds = res.data.expiresIn;
      renderTimer(totalSeconds);
      form.submit();
    } catch (err) {
      console.log(err);
      if (errorSection) {
        errorSection.style = "display: block";
      }

      if (errorSectionMobileAppLinks) {
        errorSectionMobileAppLinks.style = "display: block";
      }

      if (submitCta) {
        submitCta.removeAttribute("disabled");
      }

      if (submitCtaWrapper) {
        submitCtaWrapper.classList.remove("loading-animation");
      }
    }
  }
}

function onRetrySubmit() {
  window.location.href = window.location.href;
}

function goBack() {
  const select = document.getElementById("sm-provider-select");
  select.style = "display: block";
  const backButtonContainer = document.getElementById(
    "sm-back-button-container"
  );
  backButtonContainer.style = "display: none";
  const activeSectionTitle = document.getElementById(
    "digitalSofia-login-title"
  );
  activeSectionTitle.style = "display: none";
  const disabledSectionTitle = document.getElementById("eAuth-login-title");
  disabledSectionTitle.style = "display: block";
  onProviderSelect();
}
