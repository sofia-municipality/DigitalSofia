import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import RequestService from "../../../../components/sm/pages/RequestService";
import StoreService from "../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../constants/navigation";

const store = StoreService.configureStore();

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

it("should render the Request Service page without breaking", async () => {
  renderWithRouterMatch(RequestService, {
    path: PAGE_ROUTES.REQUEST_SERVICE,
    route: PAGE_ROUTES.REQUEST_SERVICE,
  });
  // Expect to have page title and two cards
  expect(screen.getByText("requestService.title")).toBeInTheDocument();
  expect(
    screen.getByText("requestService.addressRegistration.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("requestService.localTaxes.title")
  ).toBeInTheDocument();

  // Expect to have links to taxes and address registration
  expect(
    screen.getByRole("link", { name: "requestService.localTaxes.cta" })
  ).toHaveAttribute("href", PAGE_ROUTES.LOCAL_TAXES_AND_FEES);

  expect(
    screen.getByRole("link", { name: "requestService.addressRegistration.cta" })
  ).toHaveAttribute("href", PAGE_ROUTES.ADDRESS_REGISTRATION);

  //expect to have link to my-services
  expect(
    screen.getByRole("link", { name: "requestService.link" })
  ).toHaveAttribute("href", PAGE_ROUTES.MY_SERVICES);
});
