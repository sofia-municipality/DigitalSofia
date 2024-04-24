import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";
import configureStore from "redux-mock-store";

import Profile from "../../../../components/sm/pages/Profile";
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

it("should render the Profile page without breaking", async () => {
  store = mockStore({
    user: {
      isAuthenticated: true,
      roles: ["formsflow-client"],
      selectLanguages: [{ name: "en", value: "English" }],
      userDetail: {},
    },
  });
  renderWithRouterMatch(Profile, {
    path: PAGE_ROUTES.PROFILE,
    route: PAGE_ROUTES.PROFILE,
  });

  expect(screen.getByText("profile.page.title")).toBeInTheDocument();
  expect(
    screen.getByRole("button", { name: "profile.page.delete.cta" })
  ).toBeInTheDocument();
});
