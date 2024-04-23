import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import HomePage from "../../../../components/sm/pages/Home";
import StoreService from "../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../constants/navigation";
import { SmContextProvider } from "../../../../components/sm/context";

import mockPageBlocks from "../../../_mocks_/pageBlocks/home";
import initIntersectionObserver from "../../../_mocks_/functions/intersectionObserver";

initIntersectionObserver();
const store = StoreService.configureStore();

jest.mock("react-responsive", () => ({
  ...jest.requireActual("react-responsive"),
  useMediaQuery: () => ({}),
}));

jest.mock("../../../../apiManager/apiHooks", () => ({
  useGetPageBlocks: () => mockPageBlocks,
  useFetchFAQ: () => [{ faqs: [] }, false],
}));

function renderWithRouterMatch(
  ui,
  {
    path = "/",
    route = "/",
    history = createMemoryHistory({ initialEntries: [route] }),
  } = {}
) {
  return {
    ...rtlRender(
      <Provider store={store}>
        <Router history={history}>
          <SmContextProvider>
            <Route path={path} component={ui} />
          </SmContextProvider>
        </Router>
      </Provider>
    ),
  };
}

it("should render the Home page without breaking", async () => {
  renderWithRouterMatch(HomePage, {
    path: PAGE_ROUTES.HOME,
    route: PAGE_ROUTES.HOME,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "masthead.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "addressRegistration.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "localTaxes.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "howItWorks.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "mobileApp.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "contacts.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: "faq.section.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "Back to the top of the page",
    })
  ).toBeInTheDocument();
});

describe("MastHead section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: "masthead.section.title",
      })
    ).toBeInTheDocument();

    expect(
      screen.getByText("masthead.section.description")
    ).toBeInTheDocument();

    expect(screen.getByText("masthead.section.bullets.1")).toBeInTheDocument();
    expect(screen.getByText("masthead.section.bullets.2")).toBeInTheDocument();
    expect(screen.getByText("masthead.section.bullets.3")).toBeInTheDocument();
  });
});

describe("MastHead CTAs section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("link", {
        name: "masthead.cta.addressRegistration",
      })
    ).toHaveAttribute("href", PAGE_ROUTES.ADDRESS_REGISTRATION);

    expect(
      screen.getByRole("link", {
        name: "masthead.cta.localTaxes",
      })
    ).toHaveAttribute("href", PAGE_ROUTES.LOCAL_TAXES_AND_FEES);
  });
});

describe("Address registration section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "addressRegistration.section.title",
      })
    ).toBeInTheDocument();

    expect(
      screen.getByText("addressRegistration.section.subtitle")
    ).toBeInTheDocument();

    expect(
      screen.getByText("addressRegistration.section.description")
    ).toBeInTheDocument();

    expect(
      screen.getByRole("link", {
        name: "addressRegistration.section.cta",
      })
    ).toHaveAttribute("href", PAGE_ROUTES.ADDRESS_REGISTRATION);

    expect(screen.getByTestId("address-section-image")).toHaveAttribute(
      "src",
      "/address_map.png"
    );
  });
});

describe("Local taxes section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "localTaxes.section.title",
      })
    ).toBeInTheDocument();

    expect(screen.getByText("localTaxes.section.subtitle")).toBeInTheDocument();

    expect(
      screen.getByText("localTaxes.section.description")
    ).toBeInTheDocument();

    expect(
      screen.getByRole("link", {
        name: "localTaxes.section.cta",
      })
    ).toHaveAttribute("href", PAGE_ROUTES.LOCAL_TAXES_AND_FEES);

    expect(screen.getByTestId("localTaxes-section-image")).toHaveAttribute(
      "src",
      "/local-taxes-masthead.png"
    );
  });
});

describe("How it works section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "howItWorks.section.title",
      })
    ).toBeInTheDocument();

    expect(screen.getByText("howItWorks.section.subtitle")).toBeInTheDocument();
    expect(screen.getByText("howItWorks.section.item.1")).toBeInTheDocument();
    expect(screen.getByText("howItWorks.section.item.2")).toBeInTheDocument();
    expect(screen.getByText("howItWorks.section.item.3")).toBeInTheDocument();
    expect(screen.getByText("howItWorks.section.item.4")).toBeInTheDocument();
    expect(screen.getByText("howItWorks.section.item.5")).toBeInTheDocument();
  });
});

describe("Mobile app section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "mobileApp.section.title",
      })
    ).toBeInTheDocument();

    expect(screen.getByText("mobileApp.section.subtitle")).toBeInTheDocument();
    expect(
      screen.getByText("mobileApp.section.description")
    ).toBeInTheDocument();

    expect(screen.getByAltText("Google Play icon")).toBeInTheDocument();
    expect(screen.getByAltText("Apple store icon")).toBeInTheDocument();
  });
});

describe("Faq section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "faq.section.title",
      })
    ).toBeInTheDocument();

    expect(screen.getByText("mobileApp.section.subtitle")).toBeInTheDocument();

    expect(
      screen.getByRole("link", {
        name: "faqs.seeAll.cta.text",
      })
    ).toHaveAttribute("href", PAGE_ROUTES.FAQ);

    expect(
      screen.getByRole("button", {
        name: "faqs.openAll.cta.text",
      })
    ).toBeInTheDocument();
  });
});

describe("Contacts section", () => {
  it("should render all components", () => {
    renderWithRouterMatch(HomePage, {
      path: PAGE_ROUTES.HOME,
      route: PAGE_ROUTES.HOME,
    });

    expect(
      screen.getByRole("heading", {
        level: 2,
        name: "contacts.section.title",
      })
    ).toBeInTheDocument();

    expect(
      screen.getByText("contacts.section.item.1.title")
    ).toBeInTheDocument();
    expect(
      screen.getByText("contacts.section.item.1.description")
    ).toBeInTheDocument();

    expect(
      screen.getByText("contacts.section.item.2.title")
    ).toBeInTheDocument();
    expect(
      screen.getByText("contacts.section.item.2.description")
    ).toBeInTheDocument();

    expect(
      screen.getByText("contacts.section.item.3.title")
    ).toBeInTheDocument();
    expect(
      screen.getByText("contacts.section.item.3.description")
    ).toBeInTheDocument();
    expect(
      screen.getByText("contacts.section.item.3.description")
    ).toHaveAttribute("href", PAGE_ROUTES.CONTACTS);
  });
});
