import { PAGE_NAMES } from "../../constants/pages";

import homePageFallback from "./pageBlocks/homePage";
import contactsPageFallback from "./pageBlocks/contactsPage";
import termsPageFallback from "./pageBlocks/termsPage";
import cookiePolicyFallback from "./pageBlocks/cookiePolicyPage";
import personalDataFallback from "./pageBlocks/personalDataPage";

const pageBlocksFallbacks = {
  [PAGE_NAMES.HOME_PAGE]: homePageFallback,
  [PAGE_NAMES.CONTACTS_PAGE]: contactsPageFallback,
  [PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE]: termsPageFallback,
  [PAGE_NAMES.COOKIE_POLICY]: cookiePolicyFallback,
  [PAGE_NAMES.PERSONAL_DATA]: personalDataFallback,
};

export const getPageBlocksFallback = (page, language) =>
  pageBlocksFallbacks[page][language] || {};
