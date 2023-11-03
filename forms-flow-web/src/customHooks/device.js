import { useMediaQuery } from "react-responsive";

import bp from "../assets/styles/breakpoints.module.scss";

const OS = {
  WINDOWS: "Windows",
  MAC: "Mac OS",
  LINUX: "Linux",
  IOS: "iOS",
  ANDROID: "Android",
  IPAD: "iPad",
  UNKNOWN: "Unknown",
};

const detectOS = () => {
  const platform = navigator.platform;
  if (platform.indexOf("Win") !== -1) return OS.WINDOWS;
  if (platform.indexOf("Mac") !== -1) return OS.MAC;
  if (platform.indexOf("Linux") !== -1) return OS.LINUX;
  if (platform.indexOf("iPhone") !== -1) return OS.IOS;
  if (platform.indexOf("Android") !== -1) return OS.ANDROID;
  if (platform.indexOf("iPad") !== -1) return OS.IPAD;
  return OS.UNKNOWN;
};

export const useDevice = () => {
  const isMobile = useMediaQuery({
    query: `(max-width: calc(${bp.large} - 1px))`,
  });
  const isPhone = useMediaQuery({
    query: `(max-width: calc(${bp.medium} - 1px))`,
  });

  const isLarge = useMediaQuery({
    query: `(max-width: calc(${bp.large} - 1px))`,
  });

  const isExtraLarge = useMediaQuery({
    query: `(max-width: calc(${bp["extra-large"]} - 1px))`,
  });

  const isPortraitMode = useMediaQuery({
    query: `(orientation: portrait)`,
  });

  return {
    isMobile,
    isPhone,
    isPortraitMode: !isPortraitMode,
    isLarge: isLarge && !isPhone,
    isExtraLarge: isExtraLarge && !isLarge,
    currentOS: detectOS(),
  };
};

useDevice.SYSTEMS = OS;
