import React from "react";
import { useSelector } from "react-redux";

import PageContainer from "../../components/PageContainer";
import MastHeadSection from "./sections/MastHeadSection";
import MastHeadCtaSection from "./sections/MastHeadCtaSection";
import AddressSection from "./sections/AddressSection";
import TaxSection from "./sections/TaxSection";
import HowItWorksSection from "./sections/HowItWorksSection";
import MobileAppSection from "./sections/MobileAppSection";
import FAQSection from "./sections/FAQSection";
import ContactsSection from "./sections/ContactsSection";
import BackToTopSection from "./sections/BackToTopSection";

import { NavLinksSections } from "../../../../constants/navigation";
import { MOBILE_SECTIONS_ENABLED } from "../../../../constants/constants";
import { PAGE_NAMES, PAGE_BLOCKS } from "../../../../constants/pages";
import { useGetPageBlocks } from "../../../../apiManager/apiHooks";
import Loading from "../../../../containers/Loading";

const HomePage = () => {
  const homePageBlocks = PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE];
  const userLanguage = useSelector((state) => state.user.lang);
  const pageBlocks = useGetPageBlocks(PAGE_NAMES.HOME_PAGE, userLanguage) || {};

  return Object.keys(pageBlocks).length ? (
    <PageContainer>
      <MastHeadSection {...(pageBlocks[homePageBlocks.MASTHEAD_BLOCK] || {})} />
      <MastHeadCtaSection />
      <AddressSection
        {...(pageBlocks[homePageBlocks.ADDRESS_BLOCK] || {})}
        id={NavLinksSections.ADDRESS_SECTION}
      />
      <TaxSection {...(pageBlocks[homePageBlocks.TAXES_BLOCK] || {})} />
      {MOBILE_SECTIONS_ENABLED ? (
        <>
          <HowItWorksSection
            {...(pageBlocks[homePageBlocks.HIY_BLOCK] || {})}
            id={NavLinksSections.HOW_IT_WORKS_SECTION}
          />
          <MobileAppSection
            {...(pageBlocks[homePageBlocks.MOBILE_APP_BLOCK] || {})}
          />
        </>
      ) : null}

      <FAQSection
        {...(pageBlocks[homePageBlocks.FAQ_BLOCK] || {})}
        id={NavLinksSections.FAQ_SECTION}
      />
      <ContactsSection
        {...(pageBlocks[homePageBlocks.CONTACTS_BLOCK] || {})}
        id={NavLinksSections.CONTACTS_SECTION}
      />
      <BackToTopSection />
    </PageContainer>
  ) : (
    <Loading />
  );
};

export default HomePage;
