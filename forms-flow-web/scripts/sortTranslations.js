const fs = require("fs");
const path = require("path");
const translations = require("../src/resourceBundles");

const TRANSLATIONS_PATH = "./src/resourceBundles";

const sortTranslations = async (translations, languageCode) => {
  const newTransltions = Object.keys(translations)
    .sort((a, b) => a.localeCompare(b))
    .reduce((acc, key) => {
      acc[key] = translations[key];
      return acc;
    }, {});

  fs.writeFileSync(
    path.resolve(`${TRANSLATIONS_PATH}/${languageCode}.json`),
    JSON.stringify(newTransltions, null, 2)
  );

  console.log(`Sorted ${languageCode} translations`);
};

sortTranslations(translations.bg, "bg");
sortTranslations(translations.en, "en");
