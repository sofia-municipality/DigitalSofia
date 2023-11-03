import React from "react";

import TaxesContainer, {
  TaxesContainerType,
} from "../components/TaxesContainer";

const ReferencePage = () => {
  return (
    <TaxesContainer
      type={TaxesContainerType.PAYMENT}
      title={"localTaxes.payment.taxInfo.title"}
    />
  );
};

export default ReferencePage;
