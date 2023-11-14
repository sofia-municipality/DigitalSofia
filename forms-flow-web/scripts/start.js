const rewire = require("rewire");
const defaults = rewire("react-scripts/scripts/start.js");
const webpackConfig = require("react-scripts/config/webpack.config");

const { overrideConfig } = require("./overrides");

//In order to override the webpack configuration without ejecting the create-react-app
defaults.__set__("configFactory", (webpackEnv) => {
  let config = webpackConfig(webpackEnv);

  //Customize the webpack configuration here, for reference I have updated webpack externals field
  overrideConfig(config);

  return config;
});
