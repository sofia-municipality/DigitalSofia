import { PAGE_NAMES, PAGE_BLOCKS } from "../../../constants/pages";

const homePageBlogs = PAGE_BLOCKS[PAGE_NAMES.HOME_PAGE];

export default {
  bg: {
    [homePageBlogs.MASTHEAD_BLOCK]: {
      title: "Новите е-услуги вече са налични за цяла София!",
      description:
        "Адресната регистрация в София вече е само на няколко клика разстояние! Услугата е налична за всички административни райони на Столична община и е интегрирана със системата за плащане на Министерство на електронното управление.",
      features: [
        "Ново и сигурно електронно удостоверение",
        "По-лесен достъп",
        "Изцяло онлайн, без чакане на гише",
      ],
    },
    [homePageBlogs.ADDRESS_BLOCK]: {
      image: "/address_map.png",
      title: "Адресна регистрация",
      subtitle:
        "Подайте заявление за смяна на адресна регистрация в рамките на минути!",
      description:
        "Преди да започнете да попълвате онлайн заявлението, моля, изберете вида на адресната регистрация, както и Вашия статут спрямо собствеността на имота, на чийто адрес ще се регистрирате.",
      ctaText: "Промяна на адрес",
    },
    [homePageBlogs.TAXES_BLOCK]: {
      image: "/local-taxes-masthead.png",
      title: "Местни данъци и такси",
      subtitle: "Прегледайте и платете местните данъци и такси бързо и сигурно",
      description:
        "Преди да започнете да попълвате онлайн заявлението, моля, изберете вида на адресната регистрация, както и Вашия статут спрямо собствеността на имота, на чийто адрес ще се регистрирате.",
      ctaText: "Справка и плашане",
    },
    [homePageBlogs.HIY_BLOCK]: {
      title: "Как работи услугата?",
      subtitle: "Изцяло онлайн, както никога досега!",
      items: [
        {
          image: "/how-it-works-1.svg",
          description: "Изтеглете приложение за електронна идентификация",
        },
        {
          image: "/how-it-works-2.svg",
          description: "Влезте в Digital Sofia и изберете услуга",
        },
        {
          image: "/how-it-works-3.svg",
          description: "Попълнете заявление за избраната услуга",
        },
        {
          image: "/how-it-works-4.svg",
          description: "Изчакайте обработка на заявлението",
        },
        {
          image: "/how-it-works-5.svg",
          description: "Получавате ново удостоверение до 3 работни дни",
        },
      ],
    },
    [homePageBlogs.MOBILE_APP_BLOCK]: {
      image: "/digital-sofia.png",
      title: "Свалете мобилното приложение Digital Sofia",
      subtitle:
        "Новият Ви личен и надежден електронен подпис за изцяло онлайн дигитални услуги",
      description:
        "Преди да започнете да попълвате онлайн заявлението, моля, уверете се, че сте изтеглили мобилното приложение Digital Sofia и успешно сте се регистирали на него.",
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
      title: "За контакти",
      items: [
        {
          image: "/contacts-working-hours.svg",
          title: "работно време",
          description: "понеделник - петък 09:00 - 17:00",
        },
        {
          image: "/contacts-questions.svg",
          title: "технически въпроси",
          description: "address@sofia.bg",
        },
        {
          image: "/contacts-contacts.svg",
          title: "За контакти",
          description: "по райони",
        },
      ],
    },
    [homePageBlogs.FAQ_BLOCK]: {
      title: "Често задавани въпроси",
      itemsCount: "3",
    },
  },
  en: {
    [homePageBlogs.MASTHEAD_BLOCK]: {
      title: "New e-services available in Sofia!",
      description:
        "Address registration in Sofia is now just a few clicks away! The service is available for all administrative regions of the Metropolitan Municipality and is integrated with the payment system of the Ministry of e-Government.",
      features: [
        "A new and secure electronic certificate",
        "Easier access",
        "Completely online, no waiting at the counter",
      ],
    },
    [homePageBlogs.ADDRESS_BLOCK]: {
      image: "/address_map.png",
      title: "Address registration",
      subtitle: "Apply for change of address registration within minutes!",
      description:
        "Before you start filling out the online application, please select the type of address registration, as well as your status in relation to the ownership of the property at whose address you will be registering.",
      ctaText: "Change of address",
    },
    [homePageBlogs.TAXES_BLOCK]: {
      image: "/local-taxes-masthead.png",
      title: "Local taxes and fees",
      subtitle: "View and pay local taxes and fees quickly and securely",
      description:
        "Before you start filling out the online application, please select the type of address registration, as well as your status in relation to the ownership of the property at whose address you will be registering.",
      ctaText: "Reference and intimidation",
    },
    [homePageBlogs.HIY_BLOCK]: {
      title: "How does the service work?",
      subtitle: "Completely online like never before!",
      items: [
        {
          image: "/how-it-works-1.svg",
          description: "Download an electronic identification application",
        },
        {
          image: "/how-it-works-2.svg",
          description: "Log in to Digital Sofia and select a service",
        },
        {
          image: "/how-it-works-3.svg",
          description: "Fill out an application for the selected service",
        },
        {
          image: "/how-it-works-4.svg",
          description: "Wait for the application to be processed",
        },
        {
          image: "/how-it-works-5.svg",
          description:
            "You will receive a new certificate within 3 working days",
        },
      ],
    },
    [homePageBlogs.MOBILE_APP_BLOCK]: {
      image: "/digital-sofia.png",
      title: "Download the Digital Sofia mobile app",
      subtitle:
        "Your new personal and trusted electronic signature for fully online digital services",
      description:
        "Before starting to complete the online application, please ensure that you have downloaded the Digital Sofia mobile application and successfully registered with it.",
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
      title: "Contacts",
      items: [
        {
          image: "/contacts-working-hours.svg",
          title: "work time",
          description: "Monday - Friday 09:00 - 17:00",
        },
        {
          image: "/contacts-questions.svg",
          title: "technical questions",
          description: "address@sofia.bg",
        },
        {
          image: "/contacts-contacts.svg",
          title: "Contacts",
          description: "by regions",
        },
      ],
    },
    [homePageBlogs.FAQ_BLOCK]: {
      title: "Frequently Asked Questions",
      itemsCount: "3",
    },
  },
};
