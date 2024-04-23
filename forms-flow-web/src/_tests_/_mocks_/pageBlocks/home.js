import { PAGE_NAMES, PAGE_BLOCKS } from "../../../constants/pages";

const homePageBlogs = PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE];

export default {
  [homePageBlogs.MASTHEAD_BLOCK]: {
    title: "masthead.section.title",
    description: "masthead.section.description",
    features: [
      "masthead.section.bullets.1",
      "masthead.section.bullets.2",
      "masthead.section.bullets.3",
    ],
  },
  [homePageBlogs.ADDRESS_BLOCK]: {
    image: "/address_map.png",
    title: "addressRegistration.section.title",
    subtitle: "addressRegistration.section.subtitle",
    description: "addressRegistration.section.description",
    ctaText: "addressRegistration.section.cta",
  },
  [homePageBlogs.TAXES_BLOCK]: {
    image: "/local-taxes-masthead.png",
    title: "localTaxes.section.title",
    subtitle: "localTaxes.section.subtitle",
    description: "localTaxes.section.description",
    ctaText: "localTaxes.section.cta",
  },
  [homePageBlogs.HIY_BLOCK]: {
    title: "howItWorks.section.title",
    subtitle: "howItWorks.section.subtitle",
    items: [
      {
        image: "/how-it-works-1.svg",
        description: "howItWorks.section.item.1",
      },
      {
        image: "/how-it-works-2.svg",
        description: "howItWorks.section.item.2",
      },
      {
        image: "/how-it-works-3.svg",
        description: "howItWorks.section.item.3",
      },
      {
        image: "/how-it-works-4.svg",
        description: "howItWorks.section.item.4",
      },
      {
        image: "/how-it-works-5.svg",
        description: "howItWorks.section.item.5",
      },
    ],
  },
  [homePageBlogs.MOBILE_APP_BLOCK]: {
    image: "/digital-sofia.png",
    title: "mobileApp.section.title",
    subtitle: "mobileApp.section.subtitle",
    description: "mobileApp.section.description",
    ctaItems: [
      {
        ctaId: "googleplay",
        ctaHref: "/",
      },
      {
        ctaId: "ios",
        ctaHref: "/",
      },
    ],
  },
  [homePageBlogs.CONTACTS_BLOCK]: {
    title: "contacts.section.title",
    items: [
      {
        image: "/contacts-working-hours.svg",
        title: "contacts.section.item.1.title",
        description: "contacts.section.item.1.description",
      },
      {
        image: "/contacts-questions.svg",
        title: "contacts.section.item.2.title",
        description: "contacts.section.item.2.description",
      },
      {
        image: "/contacts-contacts.svg",
        title: "contacts.section.item.3.title",
        description: "contacts.section.item.3.description",
      },
    ],
  },
  [homePageBlogs.FAQ_BLOCK]: {
    title: "faq.section.title",
  },
};
