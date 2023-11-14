import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import PropTypes from "prop-types";
import { Provider } from "react-redux";
import { ConnectedRouter } from "connected-react-router";
import { TranslationsService } from "@formsflow/service";

import "../assets/styles/layouts.scss";
import "../assets/styles/user-styles.css";
import BaseRouting from "./BaseRouting";
import { Helmet } from "react-helmet";
import { KEYCLOAK_URL, MULTITENANCY_ENABLED } from "../constants/constants";
import Loading from '../containers/Loading';

const LanguageProvider = ({ publish, children }) => {
  const tenantId = useSelector((state) => state.tenants?.tenantId);
  const [areTranslationsReady, setAreTranslationsReady] = useState();


  useEffect(() => {
    TranslationsService.loadTranslations(tenantId, MULTITENANCY_ENABLED, () => {
      publish("FF_TRANSLATIONS_READY", true);
      setAreTranslationsReady(true);
    });
  }, [tenantId]);

  return areTranslationsReady ? <>{children}</> : <Loading />;
};

const App = React.memo((props) => {
  const { store, history, publish, subscribe, getKcInstance } = props;
  return (
    <div>
      <Helmet>
        {KEYCLOAK_URL ? <link rel="preconnect" href={KEYCLOAK_URL} /> : null}
      </Helmet>
      <Provider store={store}>
        <ConnectedRouter history={history}>
          <LanguageProvider publish={publish}>
            <BaseRouting
              store={store}
              publish={publish}
              subscribe={subscribe}
              getKcInstance={getKcInstance}
            />
          </LanguageProvider>
        </ConnectedRouter>
      </Provider>
    </div>
  );
});

App.propTypes = {
  history: PropTypes.any.isRequired,
  store: PropTypes.any.isRequired,
};

export default App;
