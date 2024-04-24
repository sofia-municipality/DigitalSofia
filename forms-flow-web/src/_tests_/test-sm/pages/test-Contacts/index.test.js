import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import ContactsPage from "../../../../components/sm/pages/Contacts";
import StoreService from "../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../constants/navigation";

import mockPageBlocks from "../../../_mocks_/pageBlocks/contacts";

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

it("should render the Contacts page without breaking", async () => {
  renderWithRouterMatch(ContactsPage, {
    path: PAGE_ROUTES.CONTACTS,
    route: PAGE_ROUTES.CONTACTS,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "contacts.page.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("contacts.page.description")).toBeInTheDocument();
  expect(screen.getByText("contacts.page.contacts.title")).toBeInTheDocument();
  expect(screen.getByText("contacts.item.title")).toBeInTheDocument();
  expect(screen.getByText("contacts.item.phone")).toBeInTheDocument();

  expect(
    screen.getByRole("link", {
      name: /contacts\.item\.title/i,
    })
  ).toBeInTheDocument();
});
