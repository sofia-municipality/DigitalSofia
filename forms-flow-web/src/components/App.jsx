import React from "react";
import PropTypes from "prop-types";
import { Provider } from "react-redux";
import { matchPath } from "react-router";
import { useLocation } from "react-router-dom";
import { ConnectedRouter } from "connected-react-router";
import { Switch } from "react-router-dom";

import "../assets/styles/layouts.scss";
import "../assets/styles/user-styles.css";
import BaseRouting from "./BaseRouting";
import { Helmet } from "react-helmet";
import { KEYCLOAK_URL } from "../constants/constants";
import { PAGE_TITLES } from "../constants/navigation";
import { SmContextProvider } from "./sm/context";
import {
  useInitKeycloak,
  useSetInitialLanguageTranslations,
} from "../customHooks";
import Loading from "../containers/Loading";

const App = React.memo((props) => {
  const { store, history } = props;

  return (
    <div>
      <Helmet>
        {KEYCLOAK_URL ? <link rel="preconnect" href={KEYCLOAK_URL} /> : null}
      </Helmet>
      <Provider store={store}>
        <ConnectedRouter history={history}>
          <SmContextProvider>
            <AppContent store={store} />
          </SmContextProvider>
        </ConnectedRouter>
      </Provider>
    </div>
  );
});

const LanguageProvider = ({ children }) => {
  const areTranslationsReady = useSetInitialLanguageTranslations();
  return areTranslationsReady ? <>{children}</> : <Loading />;
};

const AppContent = ({ store }) => {
  const { pathname } = useLocation();
  const [isInitiated] = useInitKeycloak();

  const routeKey = Object.keys(PAGE_TITLES).find((route) =>
    matchPath(pathname, { path: route, exact: true })
  );

  const pageTitle = PAGE_TITLES[routeKey] || PAGE_TITLES.DEFAULT;
  return (
    <>
      <Helmet>{pageTitle ? <title>{pageTitle}</title> : null}</Helmet>
      {isInitiated ? (
        <LanguageProvider>
          <Switch>
            <BaseRouting store={store} />
          </Switch>
        </LanguageProvider>
      ) : (
        <Loading />
      )}
    </>
  );
};

App.propTypes = {
  history: PropTypes.any.isRequired,
  store: PropTypes.any.isRequired,
};

export default App;
