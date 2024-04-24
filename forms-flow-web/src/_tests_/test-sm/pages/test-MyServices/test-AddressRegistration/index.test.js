import React from "react";
import { render as rtlRender, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { Provider } from "react-redux";
import { Router, Route } from "react-router";
import { createMemoryHistory } from "history";

import AddressRegistration from "../../../../../components/sm/pages/MyServices/AddressRegistration";
import { useGetDraftsAndSubmissions } from "../../../../../apiManager/apiHooks";
import { APPLICATION_STATUS } from "../../../../../constants/formEmbeddedConstants";
import StoreService from "../../../../../services/StoreService";
import { PAGE_ROUTES } from "../../../../../constants/navigation";
import { mockServices } from "../../../../_mocks_/functions/mockServices";

const store = StoreService.configureStore();

jest.mock("react-responsive", () => ({
  ...jest.requireActual("react-responsive"),
  useMediaQuery: () => ({}),
}));

jest.mock("../../../../../apiManager/apiHooks", () => ({
  useGetDraftsAndSubmissions: jest.fn(),
  useWithdrawApplication: () => ({
    fetch: () => ({}),
  }),
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

it("should render the My Services Address Registration page without breaking when no services", async () => {
  useGetDraftsAndSubmissions.mockReturnValue({
    fetch: () => ({}),
    data: { items: [], total: 0 },
    isLoading: false,
  });

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText("myServices.noServices")).toBeInTheDocument();
});

it("should render the My Services Address Registration page without breaking", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(mockServices("draftInProgress"));

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.queryByText("myServices.noServices")).not.toBeInTheDocument();
});

it("should render the Draft in progress status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.DRAFT_IN_PROGRESS)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.draftInProgress.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.draft.accordion.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.draft.accordion.content")
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "1");

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("link", {
      name: "myServices.continue.cta",
    })
  ).toHaveAttribute(
    "href",
    "/form/650156a2b7a5d5df1fdf371d/draft/3080/edit?behalf=myBehalf"
  );
});

it("should render the Form submitted status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.FORM_SUBMITTED)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.formSubmitted.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.formSubmitted.accordion.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.formSubmitted.accordion.content.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.formSubmitted.accordion.content.description"
    )
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "5");
});

it("should render the Waiting for third party signiture status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.WAITING_FOR_THIRD_PARTY_SIGNUTURE)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.waitingForThirdPartySigniture.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.waitingForThirdPartySigniture.accordion.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.waitingForThirdPartySigniture.accordion.content.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.waitingForThirdPartySigniture.accordion.content.description"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.waitingForThirdPartySigniture.accordion.content.subdescription"
    )
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "2");

  expect(screen.getByText(/test\.ownerFirstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.ownerLastName/i)).toBeInTheDocument();
  expect(
    screen.getByText("myServices.waitingForSigniture.cta")
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "myServices.status.waitingForThirdPartySigniture.accordion.cancelInvitation.cta",
    })
  ).toBeInTheDocument();

  expect(
    screen.getByRole("button", {
      name: "myServices.status.waitingForThirdPartySigniture.accordion.resendInvitation.cta",
    })
  ).toBeInTheDocument();
});

it("should render the Expired third party signiture status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.EXPIRED_INVITATION)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.expiredInvitation.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.expiredThirdPartySigniture.accordion.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.expiredThirdPartySigniture.accordion.content.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.expiredThirdPartySigniture.accordion.content"
    )
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "3");

  expect(screen.getByText(/test\.ownerFirstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.ownerLastName/i)).toBeInTheDocument();
  expect(screen.getByText(/23\.10\.2023/i)).toBeInTheDocument();

  expect(
    screen.getByRole("link", {
      name: "myServices.continue.cta",
    })
  ).toHaveAttribute(
    "href",
    "/form/651d58812860fe831d4010c5/submission/65291a5d2860fe831d434aa8/edit?behalf=myBehalf&invitee=owner"
  );

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();
});

it("should render the Rejected third party signiture status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.CANCELLED_THIRD_PARTY_SIGNUTURE)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.cancelledThirdPartySigniture.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.cancelledThirdPartySigniture.accordion.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.cancelledThirdPartySigniture.accordion.content.title"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.cancelledThirdPartySigniture.accordion.content.description"
    )
  ).toBeInTheDocument();
  expect(
    screen.getByText(
      "myServices.status.cancelledThirdPartySigniture.accordion.content.subdescription"
    )
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "3");

  expect(screen.getByText(/test\.ownerFirstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.ownerLastName/i)).toBeInTheDocument();
  expect(screen.getByText(/01\.11\.2023/i)).toBeInTheDocument();
  expect(
    screen.getByText(
      /myServices\.status\.cancelledThirdPartySigniture\.signiture\.rejected\.date\.label/i
    )
  ).toBeInTheDocument();

  expect(
    screen.getByRole("link", {
      name: "myServices.checkData.cta",
    })
  ).toHaveAttribute(
    "href",
    "/form/6530dca38ceb5b25403a78d0/submission/65421cb22fe2d7d686a48c09/edit?behalf=&invitee=owner"
  );

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();
});

it("should render the Waiting for signiture status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.SIGNITURE_NEEDED)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.signitureNeeded.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.signitureNeeded.accordion.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.signitureNeeded.accordion.content")
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "4");

  expect(
    screen.getByRole("link", {
      name: "myServices.continue.cta",
    })
  ).toHaveAttribute("href", "/user-task/3ef84c82-78a9-11ee-bcb4-96b0a6073479");

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();
});

it("should render the Withdrawn third party signiture status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.WITHDRAWN_INVITATION)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.draftFilled.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.draft.accordion.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.draft.accordion.content")
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "1");

  expect(
    screen.getByRole("link", {
      name: "myServices.continue.cta",
    })
  ).toHaveAttribute(
    "href",
    "/form/6530dca38ceb5b25403a78d0/submission/65421cb22fe2d7d686a48c09/edit?behalf=myBehalf"
  );

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();
});

it("should render the Sign document pending status with all components", async () => {
  useGetDraftsAndSubmissions.mockReturnValue(
    mockServices(APPLICATION_STATUS.SIGN_DOCUMENT_PENDING)
  );

  renderWithRouterMatch(AddressRegistration, {
    path: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
    route: PAGE_ROUTES.MY_SERVICES_ADDRESS_REGISTRATION,
  });

  expect(
    screen.getByRole("heading", {
      level: 1,
      name: "addressRegistratrion.title",
    })
  ).toBeInTheDocument();

  expect(screen.getByText("myServices.backLinkText")).toBeInTheDocument();
  expect(screen.getByText(/test\.firstName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.middleName/i)).toBeInTheDocument();
  expect(screen.getByText(/test\.lastName/i)).toBeInTheDocument();

  expect(screen.getByText("**.3-ТА, № 001, София, Средец")).toBeInTheDocument();
  expect(screen.getByText("test.referenceNumber")).toBeInTheDocument();

  expect(
    screen.getByText("myServices.status.waitingForSigniture.label")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.waitingForSigniture.accordion.title")
  ).toBeInTheDocument();
  expect(
    screen.getByText("myServices.status.waitingForSigniture.accordion.content")
  ).toBeInTheDocument();

  expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "4");

  expect(
    screen.getByRole("button", {
      name: "myServices.delete.cta",
    })
  ).toBeInTheDocument();
});
