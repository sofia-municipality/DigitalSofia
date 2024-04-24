import React from "react";
import { render } from "@testing-library/react";
import Loading from "../../containers/Loading";

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useLocation: () => ({ pathName: "/" }),
}));

test("Render Loading Component", () => {
  render(<Loading />);
});
