const { overrideCssModulesHashGeneration } = require("./css-modules");
function overrideConfig(config) {
  overrideCssModulesHashGeneration(config);
}

module.exports = { overrideConfig };
