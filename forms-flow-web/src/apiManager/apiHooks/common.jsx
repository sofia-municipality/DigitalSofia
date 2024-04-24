import { useState, useEffect, useCallback } from "react";

export const useApi = (apiCall) => {
  const [loading, setIsLoading] = useState(false);
  const [data, setData] = useState();
  const [error, setError] = useState();

  useEffect(() => {
    const makeApiCall = async () => {
      try {
        setError(null);
        setIsLoading(true);
        const res = await apiCall();
        if (res) setData(res);
      } catch (err) {
        setError(err);
      } finally {
        setIsLoading(false);
      }
    };

    makeApiCall();
  }, [apiCall]);

  return [data, loading, error];
};

export const useMockApiCall = (mockData, timeout = 1000) => {
  const [isLoading, setIsLoading] = useState(true);
  const [data, setData] = useState();

  setTimeout(() => {
    setData(mockData);
    setIsLoading(false);
  }, timeout);

  return { data, isLoading };
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
