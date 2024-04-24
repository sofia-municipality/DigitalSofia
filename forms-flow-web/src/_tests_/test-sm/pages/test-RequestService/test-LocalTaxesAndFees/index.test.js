import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";
import configureStore from "redux-mock-store";

import { PAGE_ROUTES } from "../../../../../constants/navigation";
import LocalTaxesAndFees from "../../../../../components/sm/pages/RequestService/LocalTaxesAndFees";

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
  expect(screen.getByText("localTaxesAndFees.title")).toBeInTheDocument();
  expect(
    screen.getByRole("link", { name: "addressRegistratrion.backLinkText" })
  ).toBeInTheDocument();

  // Expect to two section cards
  expect(
    screen.getByText("localTaxesAndFees.card.1.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("localTaxesAndFees.card.2.title")
  ).toBeInTheDocument();

  // Expect to have links
  expect(
    screen.getByRole("link", {
      name: /localTaxesAndFees\.card\.1\.title/i,
    })
  ).toHaveAttribute("href", PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE);

  expect(
    screen.getByRole("link", {
      name: /localTaxesAndFees\.card\.2\.title/i,
    })
  ).toHaveAttribute("href", PAGE_ROUTES.LOCAL_TAXES_AND_FEES_PAYMENT);
};

it("should render the Local Taxes and Fees page without breaking", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
    },
  });
  renderWithRouterMatch(LocalTaxesAndFees, {
    path: PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
    route: PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
  });

  expectAllElementsToBeInDocument();
});

it("should render the Local Taxes and Fees page without breaking if not authenticated", async () => {
  store = mockStore({
    user: {
      isAuthenticated: false,
    },
  });
  renderWithRouterMatch(LocalTaxesAndFees, {
    path: PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
    route: PAGE_ROUTES.LOCAL_TAXES_AND_FEES,
  });

  expectAllElementsToBeInDocument();
});
