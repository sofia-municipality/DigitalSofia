import { useEffect, useState } from "react";
import { useDispatch, useStore, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { useTimer } from "react-timer-hook";
import querystring from "querystring";

import { TENANT_ID } from "../constants/constants";
import { setTenantFromId } from "../apiManager/services/tenantServices";
import UserService from "../services/UserService";

export const useLogin = () => {
  const store = useStore();

  return (redirectUri) => UserService.userLogin({ store, redirectUri });
};

export const useLogout = () => {
  return () => UserService.userLogout();
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
      } = params;

      if (hideNav) {
        localStorage.setItem("hideNav", hideNav);
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
