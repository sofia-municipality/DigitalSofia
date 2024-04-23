import { useEffect, useState, useCallback } from "react";
import { useDispatch, useStore, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { useTimer } from "react-timer-hook";
import querystring from "querystring";

import {
  TENANT_ID,
  ASSURANCE_LEVEL,
  SERVICES_ASSURANCE_LEVEL,
  ASSURANCE_LEVEL_MAPPING,
} from "../constants/constants";
import { setTenantFromId } from "../apiManager/services/tenantServices";
import UserService from "../services/UserService";

export const useLogin = () => {
  const store = useStore();

  return (options, forceLogin) =>
    UserService.userLogin({ store, options, forceLogin });
};

export const useLogout = () => {
  return (shouldClearUrlParams) => UserService.userLogout(shouldClearUrlParams);
};

export const useInitKeycloak = () => {
  const dispatch = useDispatch();
  const store = useStore();
  const { tenantId: tenantIdInUrl } = useParams();
  const tenantId = TENANT_ID || tenantIdInUrl;
  const [isInitiated, setIsInitiated] = useState(false);

  useEffect(() => {
    const init = async () => {
      const { search } = window.location;
      const params = querystring.parse(search.replace("?", "")) || {};
      const {
        token: externalToken,
        refreshToken: externalRefreshToken,
        hideNav,
        showRequestServiceLink,
      } = params;

      if (hideNav) {
        localStorage.setItem("hideNav", hideNav);
      }

      if (showRequestServiceLink) {
        sessionStorage.setItem(
          "showRequestServiceLink",
          showRequestServiceLink
        );
      }

      if (externalToken) {
        localStorage.setItem("authToken", externalToken);
      }

      if (externalRefreshToken) {
        localStorage.setItem("refreshToken", externalRefreshToken);
      }

      setIsInitiated(false);
      if (tenantId && store) {
        let currentTenant = sessionStorage.getItem("tenantKey");
        if (currentTenant && currentTenant !== tenantId) {
          sessionStorage.clear();
          localStorage.clear();
        }
        sessionStorage.setItem("tenantKey", tenantId);
        dispatch(setTenantFromId(tenantId));
        await UserService.initKeycloak({
          store: store,
          tenantKey: tenantId,
          externalToken,
          externalRefreshToken,
        });
      } else {
        if (store) {
          await UserService.initKeycloak({
            store: store,
            externalToken,
            externalRefreshToken,
          });
        }
      }

      setIsInitiated(true);
    };

    init();
  }, [store, tenantId, dispatch]);

  return [isInitiated, setIsInitiated];
};

export const useTokenExpireTimer = () => {
  const [initialDate, setInitialDate] = useState(null);
  const { exp } = useSelector((state) => state.user.tokenParsed) || {};
  const expirationDate = new Date(0);
  expirationDate.setUTCSeconds(exp);
  const { seconds, minutes, hours } = useTimer({
    expiryTimestamp: expirationDate,
  });

  useEffect(() => {
    if (!initialDate) {
      setInitialDate(new Date());
    }
    // eslint-disable-next-line
  }, []);

  return {
    seconds,
    minutes,
    hours,
    // eslint-disable-next-line
    timeLeft: ((((hours * 60) + minutes) * 60) + seconds) * 1000,
    initialTime: expirationDate.getTime() - initialDate?.getTime(),
  };
};

export const useGetUserAssuranceLevel = () => {
  const userDetails = useSelector((state) => state?.user?.userDetail);
  const tokenAssuranceLevel = userDetails?.assurance_level;
  const authProvider = userDetails?.auth_provider;

  if (authProvider === "digitalSofia") {
    return ASSURANCE_LEVEL.HIGH;
  }

  return tokenAssuranceLevel;
};

export const useCheckUserAssuranceLevel = () => {
  const userAssuranceLevel = useGetUserAssuranceLevel();
  return useCallback(
    (serviceName) => {
      const requiredAssuranceLevel = SERVICES_ASSURANCE_LEVEL[serviceName];
      let isPassed = false;

      if (Object.values(ASSURANCE_LEVEL).includes(userAssuranceLevel)) {
        isPassed =
          ASSURANCE_LEVEL_MAPPING[userAssuranceLevel] >=
          ASSURANCE_LEVEL_MAPPING[requiredAssuranceLevel];
      }

      return { isPassed, requiredAssuranceLevel };
    },
    [userAssuranceLevel]
  );
};
