import React, { createContext, useState } from "react";

const SmContext = createContext({});

const SmContextProvider = (props) => {
  const { children } = props;

  const [context, setContext] = useState({
    smallNav: false,
    isNavExpanded: false,
  });

  const setSmContext = (state = {}) => {
    setContext({
      ...context,
      ...state,
    });
  };

  return (
    <SmContext.Provider value={{ smContext: context, setSmContext }}>
      {children}
    </SmContext.Provider>
  );
};

export { SmContext, SmContextProvider };
