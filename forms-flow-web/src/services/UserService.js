/* istanbul ignore file */
import {
  setUserRole,
  setUserToken,
  setUserDetails,
  setUserAuth,
} from "../actions/bpmActions";
import { BPM_API_URL_WITH_VERSION } from "../apiManager/endpoints/config";
import { AppConfig } from "../config";
import {
  WEB_BASE_URL,
  WEB_BASE_CUSTOM_URL,
  CUSTOM_SUBMISSION_URL,
} from "../apiManager/endpoints/config";

// import {_kc} from "../constants/tenantConstant";
import { setLanguage } from "../actions/languageSetAction";
import Keycloak from "keycloak-js";
import { updateUserlang } from "../apiManager/services/userservices";
import { getTenantKeycloakJson } from "../apiManager/services/tenantServices";
import { getFormioRoleIds } from "../apiManager/services/userservices";
import { LANGUAGE } from "../constants/constants";

let KeycloakData;

const appendParentOriginIntoUrl = (url, customParams = {}) => {
  return (url += Object.entries(customParams).reduce((acc, [key, value]) => {
    acc += `&${key}=${value}`;
    return acc;
  }, ""));
};

const setKeycloakJson = (tenantKey = null) => {
  const kcJson = getTenantKeycloakJson(tenantKey);
  KeycloakData = new Keycloak(kcJson);
  const originalCreateLoginUrl = KeycloakData.createLoginUrl;

  KeycloakData.createLoginUrl = function (options) {
    const url = originalCreateLoginUrl.call(this, options);
    const urlWithParentOrigin = appendParentOriginIntoUrl(
      url,
      options?.customParams
    );

    return urlWithParentOrigin;
  };

  return kcJson.clientId;
};

/**
 * Initializes Keycloak instance and calls the provided callback function if successfully authenticated.
 *
 * @param onAuthenticatedCallback
 */
// const KeycloakData = new Keycloak(tenantDetail);

const initKeycloak = async ({
  store,
  tenantKey,
  externalToken,
  externalRefreshToken,
}) => {
  const { dispatch } = store;
  const { user } = store.getState();
  const clientId = setKeycloakJson(tenantKey);
  const isWebView = localStorage.getItem("hideNav");

  let authenticated;
  try {
    authenticated = await KeycloakData.init({
      onLoad: "check-sso",
      promiseType: "native",
      silentCheckSsoRedirectUri:
        window.location.origin + "/silent-check-sso.html",
      pkceMethod: "S256",
      checkLoginIframe: false,
      token: externalToken || user.bearerToken,
      refreshToken: externalRefreshToken || user.refreshToken,
    });
  } catch (err) {
    console.log("Error initializing keycloak");
    console.log(err);
    authenticated = false;
  }

  dispatch(setUserAuth(authenticated));
  if (authenticated) {
    if (KeycloakData.resourceAccess[clientId]) {
      const UserRoles = KeycloakData.resourceAccess[clientId].roles;
      dispatch(setUserRole(UserRoles));
      localStorage.setItem("USER_ROLES", JSON.stringify(UserRoles));
      dispatch(
        setUserToken({
          token: KeycloakData.token,
          refreshToken: KeycloakData.refreshToken,
          tokenParsed: KeycloakData.tokenParsed,
        })
      );

      const keycloakLang = KeycloakData.tokenParsed.locale;
      if (!localStorage.getItem("lang")) {
        dispatch(setLanguage(keycloakLang || LANGUAGE));
      } else if (keycloakLang && keycloakLang !== user.lang) {
        dispatch(updateUserlang(user.lang));
      }

      //Set Cammunda/Formio Base URL
      setApiBaseUrlToLocalStorage();
      // get formio roles
      dispatch(
        getFormioRoleIds((err) => {
          if (err) {
            console.error(err);
            // doLogout();
          } else {
            KeycloakData.loadUserInfo()
              .then((res) => dispatch(setUserDetails(res)))
              .catch((err) => {
                console.log(err);
                dispatch(setUserDetails(KeycloakData.tokenParsed));
              });
            // onAuthenticatedCallback();
          }
        })
      );
      // refreshToken(store);
      isWebView ? setRefreshTokenInterval(store) : logoutOnTokenExpiration();
    } else {
      KeycloakData?.logout();
    }
  } else {
    console.warn("not authenticated!");
    if (user.bearerToken && user.refreshToken) {
      userLogout(externalToken && externalRefreshToken);
    }
  }
};

const userLogin = ({ store, options = {}, forceLogin = false }) => {
  const { user } = store.getState();
  if (!user.isAuthenticated || forceLogin) {
    if (!options?.locale) {
      options.locale = user.lang || "bg";
    }

    try {
      KeycloakData?.login(options);
    } catch (err) {
      console.log("Error on login");
      console.log(err);
    }
  }
};

// const getTokenExpireTime = (keycloak) => {
//   const { exp, iat } = keycloak.tokenParsed;
//   if (exp && iat) {
//     const toeknExpiretime =
//       new Date(exp).getMilliseconds() - new Date(iat).getMilliseconds();
//     return toeknExpiretime * 1000;
//   } else {
//     return 60000;
//   }
// };

let refreshInterval;
const setRefreshTokenInterval = (store) => {
  const refreshTime = getTokenExpireTime(KeycloakData);
  refreshInterval = setInterval(() => {
    KeycloakData &&
      KeycloakData.updateToken(5)
        .then((refreshed) => {
          if (refreshed) {
            clearInterval(refreshInterval);
            store.dispatch(
              setUserToken({
                token: KeycloakData.token,
                refreshToken: KeycloakData.refreshToken,
                tokenParsed: KeycloakData.tokenParsed,
              })
            );
            setRefreshTokenInterval(store);
          }
        })
        .catch((error) => {
          console.log(error);
        });
  }, refreshTime);
};

const getTokenExpireTime = (keycloak) => {
  const { exp, iat } = keycloak.tokenParsed;
  if (exp && iat) {
    return (exp - iat) * 1000;
  } else {
    return 60000;
  }
};

let logoutInterval;
const logoutOnTokenExpiration = () => {
  const refreshTime = getTokenExpireTime(KeycloakData);
  if (refreshTime) {
    logoutInterval = setInterval(() => {
      userLogout();
      clearInterval(logoutInterval);
    }, refreshTime);
  }
};

/**
 * Logout function
 */
const userLogout = (shouldClearUrlParams) => {
  const language = localStorage.getItem("lang");
  const hideNav = localStorage.getItem("hideNav");
  const logoutEvent = localStorage.getItem("logout-event");
  localStorage.clear();
  localStorage.setItem("lang", language || LANGUAGE);
  hideNav && localStorage.setItem("hideNav", hideNav);
  if (!logoutEvent) {
    localStorage.setItem("logout-event", `logout-${Date.now()}`);
  }

  const showRequestServiceLink = sessionStorage.getItem(
    "showRequestServiceLink"
  );

  sessionStorage.clear();
  showRequestServiceLink &&
    sessionStorage.setItem("showRequestServiceLink", showRequestServiceLink);
  const options = {};
  if (shouldClearUrlParams) {
    options.redirectUri = `${window.location.origin}${window.location.pathname}`;
  }
  // clearInterval(refreshInterval);
  KeycloakData?.logout(options);
};

const setApiBaseUrlToLocalStorage = () => {
  localStorage.setItem("bpmApiUrl", BPM_API_URL_WITH_VERSION);
  localStorage.setItem("formioApiUrl", AppConfig.projectUrl);
  localStorage.setItem("formsflow.ai.url", window.location.origin);
  localStorage.setItem("formsflow.ai.api.url", WEB_BASE_URL);
  localStorage.setItem("customApiUrl", WEB_BASE_CUSTOM_URL);
  localStorage.setItem("customSubmissionUrl", CUSTOM_SUBMISSION_URL);
};

const getFormioToken = () => localStorage.getItem("formioToken");

//const getUserEmail = () => KeycloakData.tokenParsed.email;

/*const updateToken = (successCallback) => {
  return KeycloakData.updateToken(5).then(successCallback).catch(doLogin);
};*/

// const KeycloakData= _kc;

// const doLogin = KeycloakData.login;
// const doLogout = KeycloakData.logout;
const getToken = () => KeycloakData?.token;

const UserService = {
  initKeycloak,
  userLogout,
  getToken,
  getFormioToken,
  setKeycloakJson,
  userLogin,
};

export default UserService;
