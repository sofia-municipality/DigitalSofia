import React from "react";

import { TaxesContainerType } from "../../../../components/Taxes/utils";
import TaxesContainer from "../components/TaxesContainer";

const ReferencePage = () => {
  return (
    <TaxesContainer
      type={TaxesContainerType.PAYMENT}
      title={"localTaxes.payment.taxInfo.title"}
    />
  );
};

export default ReferencePage;
