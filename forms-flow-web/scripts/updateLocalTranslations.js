const axios = require("axios");
const readline = require("readline");
const fs = require("fs");
const path = require("path");
const https = require("https");

const httpHandler = axios.create({
  httpsAgent: new https.Agent({
    rejectUnauthorized: false,
  }),
});

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

const TRANSLATIONS_PATH = "./src/resourceBundles";
let API_URL = "http://localhost:3001";
let SUBMISSION_PATH = "/form/64366811e463c6d8ef79b06c/submission";
const LOGIN_PATH = "/user/login/submission";

const login = async (email, password) => {
  return await httpHandler
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

const getSubmissions = async (jwt) => {
  const res = await httpHandler.get(
    `${API_URL}${SUBMISSION_PATH}?limit=99999&skip=0`,
    {
      headers: {
        "x-jwt-token": jwt,
      },
    }
  );

  return res.data;
};

const updateTranslations = async (translations, language, languageCode) => {
  const newTransltions = translations
    .filter((e) => e?.data?.language === language)
    .map((e) => e.data)
    .sort((a, b) => a.key.localeCompare(b.key))
    .reduce((acc, e) => {
      acc[e.key] = e.translation;
      return acc;
    }, {});

  fs.writeFileSync(
    path.resolve(`${TRANSLATIONS_PATH}/${languageCode}.json`),
    JSON.stringify(newTransltions, null, 2)
  );

  console.log(`Updated ${languageCode} translations`);
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
      await updateTranslations(currentSubmissons, bgLanguage, "bg");

      await updateTranslations(currentSubmissons, enLanguage, "en");

      rl.close();
    })
    .catch((err) => {
      console.log(err);
      rl.close();
    });
};

execute();
