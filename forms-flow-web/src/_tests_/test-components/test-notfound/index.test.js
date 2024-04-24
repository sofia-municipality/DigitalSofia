import React from "react";
import { render, screen } from "@testing-library/react";
import NotFound from "../../../components/NotFound/index";

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useHistory: () => ({ action: "" }),
  useLocation: () => ({ pathName: "/" }),
}));

test("render Error message", () => {
  render(<NotFound />);
  expect(screen.getByText("Page Not Found"));
  expect(screen.getByText("404"));
});
