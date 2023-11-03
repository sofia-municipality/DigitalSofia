import { useState, useEffect, useCallback } from "react";

export const mockApiCall = (data, timeout = 1000) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(data);
    }, timeout);
  });
};

export const useApiCall = (apiCall, params, onMount = true) => {
  const [isLoading, setIsLoading] = useState();
  const [data, setData] = useState();
  const [error, setError] = useState();

  const resetError = () => setError(null);

  const fetch = useCallback(
    async (fetchParams) => {
      try {
        setError(null);
        setIsLoading(true);
        const res = await apiCall(fetchParams || params);
        setData(res);
        setIsLoading(false);
        return res;
      } catch (err) {
        setError(err);
        setIsLoading(false);
        throw err;
      }
    },
    [apiCall, params]
  );

  useEffect(() => {
    if (onMount) fetch(params);
  }, [onMount, fetch, params]);

  return { fetch, data, isLoading, error, resetError };
};
