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

const setKeycloakJson = (tenantKey = null) => {
  const kcJson = getTenantKeycloakJson(tenantKey);
  KeycloakData = new Keycloak(kcJson);
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
  const authenticated = await KeycloakData.init({
    onLoad: "check-sso",
    promiseType: "native",
    silentCheckSsoRedirectUri:
      window.location.origin + "/silent-check-sso.html",
    pkceMethod: "S256",
    checkLoginIframe: false,
    token: user.bearerToken || externalToken,
    refreshToken: user.refreshToken || externalRefreshToken,
  });

  dispatch(setUserAuth(authenticated));
  if (authenticated) {
    if (KeycloakData.resourceAccess[clientId]) {
      const UserRoles = KeycloakData.resourceAccess[clientId].roles;
      dispatch(setUserRole(UserRoles));
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
            KeycloakData.loadUserInfo().then((res) =>
              dispatch(setUserDetails(res))
            );
            // onAuthenticatedCallback();
          }
        })
      );
      // refreshToken(store);
      logoutOnTokenExpiration();
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

const userLogin = ({ store, redirectUri }) => {
  const { user } = store.getState();
  if (!user.isAuthenticated) {
    const locale = user.lang || "bg";
    const options = { locale };
    if (redirectUri) {
      options.redirectUri = redirectUri;
    }
    KeycloakData?.login(options);
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

// let refreshInterval;
// const refreshToken = (store) => {
//   const refreshTime = getTokenExpireTime(KeycloakData);
//   refreshInterval = setInterval(() => {
//     KeycloakData &&
//       KeycloakData.updateToken(5)
//         .then((refreshed) => {
//           if (refreshed) {
//             clearInterval(refreshInterval);
//             store.dispatch(
//               setUserToken({
//                 token: KeycloakData.token,
//                 refreshToken: KeycloakData.refreshToken,
//                 tokenParsed: KeycloakData.tokenParsed,
//               })
//             );
//             refreshToken(store);
//           }
//         })
//         .catch((error) => {
//           console.log(error);
//           userLogout();
//         });
//   }, refreshTime);
// };

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
const userLogout = (isWithExternalTokens) => {
  const language = localStorage.getItem("lang");
  const hideNav = localStorage.getItem("hideNav");
  localStorage.clear();
  localStorage.setItem("lang", language || LANGUAGE);
  hideNav && localStorage.setItem("hideNav", hideNav);
  sessionStorage.clear();
  const options = {};
  if (isWithExternalTokens) {
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
