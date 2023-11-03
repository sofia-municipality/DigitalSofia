module.exports = {
  overrideWebpackConfig: ({
    webpackConfig,
  }) => {
    webpackConfig.externals = [...webpackConfig.externals, "react-i18next"];
    return webpackConfig;
  },
};