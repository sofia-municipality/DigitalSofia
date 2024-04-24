import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";
import configureStore from "redux-mock-store";

import MyServices from "../../../../components/sm/pages/MyServices";
import { PAGE_ROUTES } from "../../../../constants/navigation";

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
  // Expect to have page title and link to request service
  expect(screen.getByText("myServices.title")).toBeInTheDocument();
  expect(screen.getByRole("link", { name: "myServices.link" })).toHaveAttribute(
    "href",
    PAGE_ROUTES.REQUEST_SERVICE
  );

  // Expect to two section cards
  expect(screen.getByText("myServices.card.1.title")).toBeInTheDocument();
  expect(screen.getByText("myServices.card.2.title")).toBeInTheDocument();
};

it("should render the My Services page without breaking", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
    },
  });
  renderWithRouterMatch(MyServices, {
    path: PAGE_ROUTES.MY_SERVICES,
    route: PAGE_ROUTES.MY_SERVICES,
  });

  expectAllElementsToBeInDocument();
});

it("should render the My Servicespage without breaking if not authenticated", async () => {
  store = mockStore({
    user: {
      isAuthenticated: false,
    },
  });
  renderWithRouterMatch(MyServices, {
    path: PAGE_ROUTES.MY_SERVICES,
    route: PAGE_ROUTES.MY_SERVICES,
  });

  expectAllElementsToBeInDocument();
});
