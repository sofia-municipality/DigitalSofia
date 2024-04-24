import React, { createContext, useState } from "react";

const TaxAccordionContext = createContext({});

const TaxAccordionContextProvider = (props) => {
  const { children, defaultState } = props;

  const [context, setContext] = useState(defaultState);

  const setTaxAccordionContext = (state = {}) => {
    setContext({
      ...context,
      ...state,
    });
  };

  return (
    <TaxAccordionContext.Provider
      value={{ taxAccordionContext: context, setTaxAccordionContext }}
    >
      {children}
    </TaxAccordionContext.Provider>
  );
};

export { TaxAccordionContext, TaxAccordionContextProvider };
