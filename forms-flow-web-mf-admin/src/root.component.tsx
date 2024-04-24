import { useEffect, useState } from 'react';
import Admin from ".";
import { BrowserRouter, Route, Switch, useParams } from "react-router-dom";
import { TranslationsService } from "@formsflow/service";

import Loading from "./components/loading";
import { BASE_ROUTE, MULTITENANCY_ENABLED } from "./constants";

const LanguageProvider = ({ publish, children }) => {
  const { tenantId }  = useParams();
  const [areTranslationsReady, setAreTranslationsReady] = useState(false);

  useEffect(() => {
    if (!TranslationsService.areTranslationsLoaded()) {
      TranslationsService.loadTranslations(tenantId, MULTITENANCY_ENABLED, () => {
        publish("FF_TRANSLATIONS_READY", true);
        setAreTranslationsReady(true);
      });
    } else {
      setAreTranslationsReady(true);
    }
    
  }, [tenantId]);

  return areTranslationsReady ? <>{children}</> : <Loading />;
};

export default function Root(props) {
  return (
    <BrowserRouter>
      <LanguageProvider publish={props?.publish}>
          <Switch>
            <Route path={BASE_ROUTE} render={() => <Admin props={props} />}></Route>
          </Switch>
      </LanguageProvider>
    </BrowserRouter>
    
  );
}
