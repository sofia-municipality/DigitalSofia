import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import Reference from "../../../../../../components/sm/pages/RequestService/LocalTaxesAndFees/Reference";
import { useGetTaxReference } from "../../../../../../apiManager/apiHooks";
import StoreService from "../../../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";
import { mockTaxes } from "../../../../../_mocks_/functions/mockTaxes";

const store = StoreService.configureStore();

jest.mock("react-responsive", () => ({
  ...jest.requireActual("react-responsive"),
  useMediaQuery: () => ({}),
}));

jest.mock("../../../../../../apiManager/apiHooks", () => ({
  useGetTaxReference: jest.fn(),
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

it("should render the Local Taxes and Fees Reference page without breaking when no taxes", async () => {
  useGetTaxReference.mockReturnValue({});

  renderWithRouterMatch(Reference, {
    path: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE,
    route: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "localTaxes.reference.taxInfo.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByText("localTaxes.reference.taxInfo.subtitle")
  ).toBeInTheDocument();
  expect(
    screen.getByText("localTaxes.reference.noTaxData")
  ).toBeInTheDocument();
});

it("should render the Local Taxes and Fees Reference page with all components", async () => {
  useGetTaxReference.mockReturnValue(mockTaxes());

  renderWithRouterMatch(Reference, {
    path: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE,
    route: PAGE_ROUTES.LOCAL_TAXES_AND_FEES_REFERENCE,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "localTaxes.reference.taxInfo.title",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByText("localTaxes.reference.taxInfo.subtitle")
  ).toBeInTheDocument();
  expect(
    screen.queryByText("localTaxes.reference.noTaxData")
  ).not.toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: /localTaxes\.reference\.category\.vehicle\.title/i,
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: /localTaxes\.reference\.category\.houseHoldWaste\.title/i,
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", {
      name: /localTaxes\.reference\.category\.realEstate\.title/i,
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("region", { name: "10002H2222" })
  ).toBeInTheDocument();
  expect(screen.getByText("test property address 1")).toBeInTheDocument();

  expect(
    screen.getByRole("region", { name: "10002H2223" })
  ).toBeInTheDocument();
  expect(screen.getByText("test property address 2")).toBeInTheDocument();

  expect(
    screen.getByRole("region", { name: "10002H2224" })
  ).toBeInTheDocument();
  expect(screen.getByText("AA0000A")).toBeInTheDocument();
});
