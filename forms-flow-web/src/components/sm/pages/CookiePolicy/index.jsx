import React from "react";

import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import CMSPage from "../../components/CMSPage";

const CookiePolicy = () => {
  const termsPageBlocks = PAGE_BLOCKS[PAGE_NAMES.COOKIE_POLICY];

  return (
    <CMSPage
      pageName={PAGE_NAMES.COOKIE_POLICY}
      pageBlock={termsPageBlocks.COOKIE_POLICY_BLOCK}
    />
  );
};

export default CookiePolicy;
