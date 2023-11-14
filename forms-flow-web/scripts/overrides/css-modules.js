const loaderUtils = require("loader-utils");
const path = require("path");

function overrideCssModulesHashGeneration(config) {
  const sassModulesRule = config.module.rules[1].oneOf.find(
    (e) => String(e.test) === String(/\.module\.(scss|sass)$/)
  );

  const cssModulesRule = config.module.rules[1].oneOf.find(
    (e) => String(e.test) === String(/\.module\.css$/)
  );

  overrideGetLocalIdentName(sassModulesRule);
  overrideGetLocalIdentName(cssModulesRule);
}

function overrideGetLocalIdentName(rule) {
  rule.use.forEach((rule) => {
    if (
      rule.options &&
      rule.options.modules &&
      Object.keys(rule.options.modules).includes("getLocalIdent")
    ) {
      rule.options.modules.getLocalIdent = getLocalIdent;
    }
  });
}

function getLocalIdent(context, localIdentName, localName, options) {
  // Use the filename or folder name, based on some uses the index.js / index.module.(css|scss|sass) project style
  const fileNameOrFolder = context.resourcePath.match(
    /index\.module\.(css|scss|sass)$/
  )
    ? "[folder]"
    : "[name]";

  // Create a hash based on a the file location and class name. Will be unique across a project, and close to globally unique.
  const hash = loaderUtils.getHashDigest(
    path.posix.relative(context.rootContext, context.resourcePath) + localName,
    "md5",
    "base62",
    5
  );

  // Use loaderUtils to find the file or folder name
  const className = loaderUtils.interpolateName(
    context,
    fileNameOrFolder + "_" + localName + "__" + hash,
    options
  );
  // Remove the .module that appears in every classname when based on the file and replace all "." with "_".
  return className.replace(".module_", "_").replace(/\./g, "_");
}

module.exports = { overrideCssModulesHashGeneration };
