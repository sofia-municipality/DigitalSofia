const rewire = require("rewire");
const defaults = rewire("react-scripts/scripts/build.js");
const config = defaults.__get__("config");
const { overrideConfig } = require("./overrides");

overrideConfig(config);
