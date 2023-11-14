import { BASE_ROUTE } from "./constants";

export const NavLinksSections = {
  ADDRESS_SECTION: "addressSection",
  CONTACTS_SECTION: "contactsSection",
  FAQ_SECTION: "faqSection",
  HOW_IT_WORKS_SECTION: "howItWorksSection",
  MAIN_CONTENT: "mainContent",
  SKIP_LINK: "skipLink",
};

export const ROUTES_WITHOUT_NAV = {
  FORM: `${BASE_ROUTE}form/:formId`,
  FORM_EDIT: `${BASE_ROUTE}form/:formId/edit`,
  DRAFT_EDIT: `${BASE_ROUTE}form/:formId/draft/:draftId/edit`,
  SUBMISSION: `${BASE_ROUTE}form/:formId/submission/:submissionId`,
  SUBMISSION_EDIT: `${BASE_ROUTE}form/:formId/submission/:submissionId/edit`,
  LOCAL_TAXES_AND_FEES_REFERENCE: `${BASE_ROUTE}local-taxes-fees/reference`,
  LOCAL_TAXES_AND_FEES_PAYMENT: `${BASE_ROUTE}local-taxes-fees/payment`,
  USER_TASK: `${BASE_ROUTE}user-task/:taskId`,
};

export const SM_ROUTES = {
  HOME: BASE_ROUTE,
  REQUEST_SERVICE: `${BASE_ROUTE}request-service`,
  MY_SERVICES: `${BASE_ROUTE}my-services`,
  FAQ: `${BASE_ROUTE}sm-faq`,
  ADDRESS_REGISTRATION: `${BASE_ROUTE}sm-address-registration`,
  MY_SERVICES_ADDRESS_REGISTRATION: `${BASE_ROUTE}my-services/address-registration`,
  CONTACTS: `${BASE_ROUTE}contacts`,
  TERMS_AND_CONDITIONS: `${BASE_ROUTE}terms-and-conditions`,
  COOKIE_POLICY: `${BASE_ROUTE}cookie-policy`,
  PERSONAL_DATA: `${BASE_ROUTE}personal-data`,
  LOCAL_TAXES_AND_FEES: `${BASE_ROUTE}local-taxes-fees`,
  PROFILE: `${BASE_ROUTE}profile`,
  NOT_FOUND: "/404",
  ...ROUTES_WITHOUT_NAV,
};

export const ADMIN_ROUTES = {
  SO_ADMINISTRATION: `${BASE_ROUTE}so-administration`,
  PAGE_ADMINISTRATION: `${BASE_ROUTE}so-administration/pages`,
  FAQ_ADMINISTRATION: `${BASE_ROUTE}so-administration/faq`,
  FAQ_EDIT: `${BASE_ROUTE}so-administration/faq/:faqId`,
  FAQ_ADD: `${BASE_ROUTE}so-administration/faq/add`,
  BLOCKS_ADMINISTRATION: `${BASE_ROUTE}so-administration/pages/:page`,
  BLOCKS_ADMINISTRATION_EDIT: `${BASE_ROUTE}so-administration/pages/:page/:block`,
  TRANSLATION_ADMINISTRATION: `${BASE_ROUTE}so-administration/translations`,
};

export const PAGE_ROUTES = { ...SM_ROUTES, ...ADMIN_ROUTES };

export const ROUTES_WITH_NAV_ANIMATION = [PAGE_ROUTES.HOME];

export const PAGE_TITLES = {
  [PAGE_ROUTES.REQUEST_SERVICE]:
    "Digital Sofia - Заяви услуга - Digital Sofia - Request Service",
  [PAGE_ROUTES.MY_SERVICES]:
    "Digital Sofia - Моите услуги - Digital Sofia - My services",
  [PAGE_ROUTES.FAQ]:
    "Digital Sofia - Често задавани въпроси - Digital Sofia - Frequently Asked Questions",
  [PAGE_ROUTES.ADDRESS_REGISTRATION]:
    "Digital Sofia - Заявяне на услуга - Адресна регистрация - Digital Sofia - Request Service - Address Registration",
  [PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION]:
    "Digital Sofia - Моите услуги - Адресна регистрация - Digital Sofia - My services - Address Registration",
  [PAGE_ROUTES.CONTACTS]: "Digital Sofia - Контакти - Digital Sofia - Contacts",
  [PAGE_ROUTES.TERMS_AND_CONDITIONS]:
    "Digital Sofia - Общи условия - Digital Sofia - Terms and conditions",
  [PAGE_ROUTES.COOKIE_POLICY]:
    "Digital Sofia - Бисквитки - Digital Sofia - Cookie policy",
  [PAGE_ROUTES.PERSONAL_DATA]:
    "Digital Sofia - Лични данни - Digital Sofia - Personal data",
  [PAGE_ROUTES.LOCAL_TAXES_AND_FEES]:
    "Digital Sofia - Заяви услуга - Местни данъци и такси - Digital Sofia - Request Service - Local taxes and fees",
  [PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE]:
    "Digital Sofia - Заяви услуга - Местни данъци и такси справка - Digital Sofia - Request Service - Local taxes and fees reference",
  [PAGE_ROUTES.PROFILE]: "Digital Sofia - Профил - Digital Sofia - Profile",
  [PAGE_ROUTES.FORM]:
    "Digital Sofia - Попълване на форма - Digital Sofia - Form fill",
  [PAGE_ROUTES.DRAFT_EDIT]:
    "Digital Sofia - Промяна на чернова - Digital Sofia - Draft edit",
  [PAGE_ROUTES.SUBMISSION_EDIT]:
    "Digital Sofia - Промяна данни на форма - Digital Sofia - Form submission edit",
  DEFAULT: "Digital Sofia",
};
