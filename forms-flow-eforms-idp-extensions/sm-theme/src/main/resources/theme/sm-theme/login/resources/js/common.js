/*******************************************************************************
 * Copyright (c) 2022 Digitall Nature Bulgaria
 *
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
(function() {
  if ('URLSearchParams' in window) {
      let parameters = new URLSearchParams(location.search);
      let locale = parameters.get('ui_locales');
      if (locale != null) {
          console.log(locale);
          let [language, country] = locale.split('-');
          document.cookie = "KEYCLOAK_LOCALE=" + language + "; path=/;";
      }
  }
})();