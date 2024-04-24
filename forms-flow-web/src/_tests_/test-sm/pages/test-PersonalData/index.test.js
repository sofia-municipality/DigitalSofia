import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import PersonalData from "../../../../components/sm/pages/PersonalData";
import StoreService from "../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../constants/navigation";

import mockPageBlocks from "../../../_mocks_/pageBlocks/personalData";

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
          <Route path={path} component={ui} />
        </Router>
      </Provider>
    ),
  };
}

it("should render the Personal data page without breaking", async () => {
  renderWithRouterMatch(PersonalData, {
    path: PAGE_ROUTES.PERSONAL_DATA,
    route: PAGE_ROUTES.PERSONAL_DATA,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "personal.data.title",
    })
  ).toBeInTheDocument();
});
