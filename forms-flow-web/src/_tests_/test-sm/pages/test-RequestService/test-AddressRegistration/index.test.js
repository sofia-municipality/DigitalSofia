import React from "react";
import { render as rtlRender, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";
import configureStore from "redux-mock-store";

import { PAGE_ROUTES } from "../../../../../constants/navigation";
import AddressRegistration from "../../../../../components/sm/pages/RequestService/AddressRegistration";

let store;
let mockStore = configureStore([]);

jest.mock("react-responsive", () => ({
  ...jest.requireActual("react-responsive"),
  useMediaQuery: () => ({}),
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
          <Route path={path} component={ui} />
        </Router>
      </Provider>
    ),
  };
}

const expectAllElementsToBeInDocument = () => {
  // Expect to have page title and back link
  expect(screen.getByText("addressRegistratrion.title")).toBeInTheDocument();
  expect(
    screen.getByRole("link", { name: "addressRegistratrion.backLinkText" })
  ).toBeInTheDocument();

  // Expect to three section cards
  expect(
    screen.getByText("addressRegistratrion.card.1.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("addressRegistratrion.card.2.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("addressRegistratrion.card.3.title")
  ).toBeInTheDocument();

  // Expect to have buttons
  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.primaryAddress.myBehalf",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.currentAddress.myBehalf",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.primaryAddress.child",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.currentAddress.child",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.primaryAddress.otherPerson",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "screen.reader.addressRegistration.cta.currentAddress.otherPerson",
    })
  ).toBeInTheDocument();
};

it("should render the Address Registration page without breaking", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
    },
  });
  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.ADDRESS_REGISTRATION,
  });

  expectAllElementsToBeInDocument();
});

it("should render the Address Registration page without breaking if not authenticated", async () => {
  store = mockStore({
    user: {
      isAuthenticated: false,
    },
  });
  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.ADDRESS_REGISTRATION,
  });

  expectAllElementsToBeInDocument();
});

it("should trigger modal forbidden for foreigners if personal identifier is not containing pnobg", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
    },
  });
  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.ADDRESS_REGISTRATION,
  });

  const button = screen.getByRole("button", {
    name: "screen.reader.addressRegistration.cta.primaryAddress.myBehalf",
  });

  fireEvent.click(button);
  expect(
    screen.getByText("foreigners.forbidden.modal.title")
  ).toBeInTheDocument();
});

it("should not trigger modal forbidden for foreigners if personal identifier is containing pnobg", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
      userDetail: { personIdentifier: "PNOBG-0000000000" },
    },
  });
  store.dispatch = jest.fn();

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.ADDRESS_REGISTRATION,
  });

  const button = screen.getByRole("button", {
    name: "screen.reader.addressRegistration.cta.primaryAddress.myBehalf",
  });

  fireEvent.click(button);
  expect(
    screen.queryByText("foreigners.forbidden.modal.title")
  ).not.toBeInTheDocument();
});
