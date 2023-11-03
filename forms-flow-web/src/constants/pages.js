export const PAGE_NAMES = {
  HOME_PAGE: "home",
  TERMS_AND_CONDITIONS_PAGE: "terms",
  CONTACTS_PAGE: "contacts",
  COOKIE_POLICY: "cookiePolicy",
  COOKIE_POLICY_MOBILE: "cookiePolicyMobile",
  PERSONAL_DATA: "personalData",
};

export const PAGE_BLOCKS = {
  [PAGE_NAMES.HOME_PAGE]: {
    MASTHEAD_BLOCK: "hero-block",
    ADDRESS_BLOCK: "map-action-block",
    TAXES_BLOCK: "local-taxes-action-block",
    HIY_BLOCK: "how-it-works-block",
    MOBILE_APP_BLOCK: "mobile-app-block",
    CONTACTS_BLOCK: "contacts-block",
    FAQ_BLOCK: "faq-block",
  },
  [PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE]: {
    TERMS_BLOCK: "terms-block",
  },
  [PAGE_NAMES.COOKIE_POLICY]: {
    COOKIE_POLICY_BLOCK: "cookie-policy-block",
  },
  [PAGE_NAMES.COOKIE_POLICY_MOBILE]: {
    COOKIE_POLICY_MOBILE_BLOCK: "cookie-policy-mobile-block",
  },
  [PAGE_NAMES.CONTACTS_PAGE]: {
    CONTACTS_BLOCK: "contacts-regions-block",
  },
  [PAGE_NAMES.PERSONAL_DATA]: {
    PERSONAL_DATA_BLOCK: "personal-data-block",
  },
};

export const PAGE_BLOCK_ATTRIBUTES = {
  [PAGE_NAMES.HOME_PAGE]: {
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].MASTHEAD_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "description",
        label: "form.label.description",
        type: "textarea",
      },
      {
        fieldId: "features",
        label: "form.label.items",
        type: "array",
        content: [
          {
            fieldId: "features_0",
            type: "textarea",
          },
          {
            fieldId: "features_1",
            type: "textarea",
          },
          {
            fieldId: "features_2",
            type: "textarea",
          },
        ],
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].ADDRESS_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "subtitle",
        label: "form.label.subtitle",
        type: "textarea",
      },
      {
        fieldId: "description",
        label: "form.label.description",
        type: "textarea",
      },
      {
        fieldId: "ctaText",
        label: "form.label.ctaText",
        type: "text",
      },
      {
        fieldId: "image",
        label: "form.label.image",
        type: "file",
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].TAXES_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "subtitle",
        label: "form.label.subtitle",
        type: "textarea",
      },
      {
        fieldId: "description",
        label: "form.label.description",
        type: "textarea",
      },
      {
        fieldId: "ctaText",
        label: "form.label.ctaText",
        type: "text",
      },
      {
        fieldId: "image",
        label: "form.label.image",
        type: "file",
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].HIY_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "subtitle",
        label: "form.label.subtitle",
        type: "textarea",
      },
      {
        fieldId: "items",
        label: "form.label.items",
        type: "array_objects",
        fromApi: true,
        content: [
          {
            type: "object",
            content: [
              {
                fieldId: "description",
                type: "text",
              },
              {
                fieldId: "image",
                type: "file",
              },
            ],
          },
        ],
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].MOBILE_APP_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "subtitle",
        label: "form.label.subtitle",
        type: "textarea",
      },
      {
        fieldId: "description",
        label: "form.label.description",
        type: "textarea",
      },
      {
        fieldId: "image",
        label: "form.label.image",
        type: "file",
      },
      {
        fieldId: "ctaItems",
        label: "form.label.buttons",
        type: "array_objects",
        content: [
          {
            fieldId: "ctaItems_0",
            type: "object",
            content: [
              {
                fieldId: "ctaItems_0.ctaId",
                props: {
                  readOnly: true,
                  defaultValue: "googleplay",
                },
                type: "text",
              },
              {
                fieldId: "ctaItems_0.ctaHref",
                type: "text",
              },
            ],
          },
          {
            fieldId: "ctaItems_1",
            type: "object",
            content: [
              {
                fieldId: "ctaItems_1.ctaId",
                type: "text",
                props: {
                  readOnly: true,
                  defaultValue: "ios",
                },
              },
              {
                fieldId: "ctaItems_1.ctaHref",
                type: "text",
              },
            ],
          },
        ],
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].CONTACTS_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
      {
        fieldId: "items",
        label: "form.label.items",
        type: "array_objects",
        fromApi: true,
        content: [
          {
            type: "object",
            content: [
              {
                fieldId: "title",
                type: "text",
              },
              {
                fieldId: "description",
                type: "textarea",
              },
              {
                fieldId: "image",
                type: "file",
              },
            ],
          },
        ],
      },
    ],
    [PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE].FAQ_BLOCK]: [
      {
        fieldId: "title",
        label: "form.label.title",
        type: "textarea",
      },
    ],
  },
  [PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE]: {
    [PAGE_BLOCKS[PAGE_NAMES.TERMS_AND_CONDITIONS_PAGE].TERMS_BLOCK]: [
      {
        fieldId: "text",
        type: "richtext",
      },
    ],
  },
  [PAGE_NAMES.COOKIE_POLICY]: {
    [PAGE_BLOCKS[PAGE_NAMES.COOKIE_POLICY].COOKIE_POLICY_BLOCK]: [
      {
        fieldId: "text",
        type: "richtext",
      },
    ],
  },
  [PAGE_NAMES.COOKIE_POLICY_MOBILE]: {
    [PAGE_BLOCKS[PAGE_NAMES.COOKIE_POLICY_MOBILE].COOKIE_POLICY_MOBILE_BLOCK]: [
      {
        fieldId: "text",
        type: "richtext",
      },
    ],
  },
  [PAGE_NAMES.PERSONAL_DATA]: {
    [PAGE_BLOCKS[PAGE_NAMES.PERSONAL_DATA].PERSONAL_DATA_BLOCK]: [
      {
        fieldId: "text",
        type: "richtext",
      },
    ],
  },
  [PAGE_NAMES.CONTACTS_PAGE]: {
    [PAGE_BLOCKS[PAGE_NAMES.CONTACTS_PAGE].CONTACTS_BLOCK]: [
      {
        fieldId: "items",
        label: "form.label.items",
        type: "array_objects",
        fromApi: true,
        content: [
          {
            type: "object",
            content: [
              {
                fieldId: "title",
                label: "form.label.region",
                type: "text",
              },
              {
                fieldId: "phone",
                label: "form.label.phone",
                type: "text",
              },
              {
                fieldId: "link",
                label: "form.label.link",
                type: "text",
              },
            ],
          },
        ],
      },
    ],
  },
};
