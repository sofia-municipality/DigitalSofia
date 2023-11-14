import React, { createContext, useState } from "react";

export const STEPS = {
  SENT: "sent",
  PENDING: "pending",
  SUCCESS: "success",
  ERROR: "error",
  LOADING: "loading",
  REJECTED: "rejected",
  NEXU_INSTRUCTIONS: "nexu_instructions",
};

const SignDocumentContext = createContext({});

const SignDocumentContextProvider = (props) => {
  const { children } = props;

  const [context, setContext] = useState({});

  const setSignDocumentContext = (state = {}) => {
    setContext({
      ...context,
      ...state,
    });
  };

  return (
    <SignDocumentContext.Provider
      value={{ signDocumentContext: context, setSignDocumentContext }}
    >
      {children}
    </SignDocumentContext.Provider>
  );
};

export { SignDocumentContext, SignDocumentContextProvider };
