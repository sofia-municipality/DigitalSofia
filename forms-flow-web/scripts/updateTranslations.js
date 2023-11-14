const axios = require("axios");
const translations = require("../src/resourceBundles");
const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

const bgTranslations = translations.bg;
const enTranslations = translations.en;

let API_URL = "http://localhost:3001";
let SUBMISSION_PATH = "/form/64366811e463c6d8ef79b06c/submission";
const LOGIN_PATH = "/user/login/submission";

const login = async (email, password) => {
  return await axios
    .post(`${API_URL}${LOGIN_PATH}`, {
      data: {
        email,
        password,
        submit: true,
      },
      state: "submitted",
    })
    .then((response) => {
      const jwt = response.headers["x-jwt-token"];
      return jwt;
    })
    .catch((error) => {
      console.log(error);
    });
};

const saveTranslation = (key, value, jwt, language) => {
  return axios
    .post(
      `${API_URL}${SUBMISSION_PATH}`,
      {
        data: {
          language,
          key,
          identifier: `${language}-${key}`,
          status: "public",
          translation: value,
          submit: true,
        },
        state: "submitted",
      },
      {
        headers: {
          "x-jwt-token": jwt,
        },
      }
    )
    .then(() => {
      console.log(`${language}-${key} saved`);
    })
    .catch((error) => {
      console.log(error);
    });
};

const updateTranslation = (key, value, jwt, language, translationId) => {
  return axios
    .put(
      `${API_URL}${SUBMISSION_PATH}/${translationId}`,
      {
        data: {
          language,
          key,
          identifier: `${language}-${key}`,
          status: "public",
          translation: value,
          submit: true,
        },
        state: "submitted",
      },
      {
        headers: {
          "x-jwt-token": jwt,
        },
      }
    )
    .then(() => {
      console.log(`${language}-${key} updated`);
    })
    .catch((error) => {
      console.log(error);
    });
};

const getSubmissions = async (jwt) => {
  const res = await axios.get(
    `${API_URL}${SUBMISSION_PATH}?limit=99999&skip=0`,
    {
      headers: {
        "x-jwt-token": jwt,
      },
    }
  );

  return res.data;
};

const updateTranslations = async (
  jwt,
  oldTranslations,
  translations,
  language
) => {
  const calls = Object.entries(translations).map(async ([key, value]) => {
    const oldTranslation = oldTranslations.find(
      (item) => item.data.identifier === `${language}-${key}`
    );

    if (!oldTranslation) {
      return await saveTranslation(key, value, jwt, language);
    } else if (oldTranslation?.data?.translation !== value) {
      return await updateTranslation(
        key,
        value,
        jwt,
        language,
        oldTranslation._id
      );
    } else {
      // console.log(`${language}-${key} already exists`);
      return await Promise.resolve();
    }
  });

  await Promise.all(calls);
};

const execute = () => {
  rl.question("Email: ", function (email) {
    rl.question("Password: ", function (password) {
      rl.question("API_URL: ", function (apiUrl) {
        if (apiUrl) API_URL = apiUrl;
        rl.question("FORM_ID: ", function (formId) {
          if (formId) SUBMISSION_PATH = `/form/${formId}/submission`;
          start(email, password);
        });
      });
    });
  });
};

const start = (email, password) => {
  login(email, password)
    .then(async (jwt) => {
      const currentSubmissons = await getSubmissions(jwt);
      const bgLanguage = "Български";
      const enLanguage = "English";
      await updateTranslations(
        jwt,
        currentSubmissons,
        bgTranslations,
        bgLanguage
      );

      await updateTranslations(
        jwt,
        currentSubmissons,
        enTranslations,
        enLanguage
      );

      rl.close();
    })
    .catch((err) => {
      console.log(err);
      rl.close();
    });
};

execute();
