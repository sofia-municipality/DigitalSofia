import React from "react";

import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import CMSPage from "../../components/CMSPage";

const CookiePolicy = () => {
  const termsPageBlocks = PAGE_BLOCKS[PAGE_NAMES.PERSONAL_DATA];

  return (
    <CMSPage
      pageName={PAGE_NAMES.PERSONAL_DATA}
      pageBlock={termsPageBlocks.PERSONAL_DATA_BLOCK}
    />
  );
};

export default CookiePolicy;
