const MULTI_LANGUAGE_VARIABLE =
  (window._env_ && window._env_.REACT_APP_MULTI_LANGUAGE_ENABLED) ||
  false;

export const MULTI_LANGUAGE_ENABLED =
  MULTI_LANGUAGE_VARIABLE === "true" ||
  MULTI_LANGUAGE_VARIABLE === true
    ? true
    : false;

export const LANGUAGE =
  (window._env_ && window._env_.REACT_APP_LANGUAGE) ||
  "bg";