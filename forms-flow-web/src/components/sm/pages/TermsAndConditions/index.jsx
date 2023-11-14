import React from "react";

import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import CMSPage from "../../components/CMSPage";

const TermsAndConditions = () => {
  const termsPageBlocks = PAGE_BLOCKS[PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE];

  return (
    <CMSPage
      pageName={PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE}
      pageBlock={termsPageBlocks.TERMS_BLOCK}
    />
  );
};

export default TermsAndConditions;
