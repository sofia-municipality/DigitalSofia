import {
  useRef,
  useEffect,
  useCallback,
  useState,
  useLayoutEffect,
} from "react";
import { useDispatch } from "react-redux";
import { useHistory } from "react-router-dom";
import { push } from "connected-react-router";

import { useGetTenantLink } from "./tenant";

export const usePrevious = (value) => {
  const ref = useRef();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
};

export const useNavigateTo = (url) => {
  const dispatch = useDispatch();
  let link = useGetTenantLink(url, false);

  return (urlParams, state) => {
    if (urlParams) {
      Object.entries(urlParams).forEach(([key, value]) => {
        link = link.replace(key, value);
      });
    }

    dispatch(push(link, state));
  };
};

export const usePageTitleRef = () => {
  const { action } = useHistory();
  const [elementRef, setElementRef] = useState();
  const setRef = useCallback((node) => {
    setElementRef(node);
  }, []);

  useEffect(() => {
    if (elementRef && action === "PUSH") {
      elementRef.focus();
    }
  }, [elementRef, action]);

  return setRef;
};

export const useHandleNavResize = () => {
  const [navRef, setNavRef] = useState();

  useLayoutEffect(() => {
    const onResize = () => {
      if (navRef) {
        const toggler = navRef.getElementsByClassName("navbar-toggler")?.[0];
        const isCollapsed = toggler?.classList?.contains("collapsed");
        if (isCollapsed) {
          const navHeight = navRef.offsetHeight;
          const container =
            document.getElementsByClassName("main-container")?.[0];
          if (container) {
            if (!container.style) {
              container.style = {};
            }
            container.style.marginTop = `calc(${navHeight}px - 20px)`;
          }
        }
      }
    };

    if (navRef) {
      window.addEventListener("resize", onResize);
      onResize();
      return () => {
        window.removeEventListener("resize", onResize);
      };
    }
  }, [navRef]);

  return setNavRef;
};

export const useSyncEnvVars = () => {
  useEffect(() => {
    if (
      process?.env?.NODE_ENV === "development" &&
      !Object.keys(window?._env_ || {}).length
    ) {
      window._env_ = process.env;
    }
  }, []);
};

export const useOpenCloseAllItems = (itemsLength) => {
  const [openCloseAll, setOpenCloseAll] = useState(0);
  const [shouldOpenCloseAll, setShouldOpenCloseAll] = useState();
  const [expandedItemsCount, setExpandedItemsCount] = useState(0);

  const forceOpenAll = useCallback(() => {
    setShouldOpenCloseAll("open");
    setOpenCloseAll(1);
    setExpandedItemsCount(itemsLength);
  }, [itemsLength]);

  const forceCloseAll = useCallback(() => {
    setShouldOpenCloseAll("close");
    setOpenCloseAll(0);
    setExpandedItemsCount(0);
  }, []);

  const onOpenCloseAllClick = () => {
    if (shouldOpenCloseAll !== "open") {
      setShouldOpenCloseAll("open");
      setExpandedItemsCount(itemsLength);
      if (openCloseAll < 0) {
        setOpenCloseAll(1);
      } else {
        setOpenCloseAll((state) => state + 1);
      }
    } else {
      setShouldOpenCloseAll("close");
      setExpandedItemsCount(0);
      if (openCloseAll > 1) {
        setOpenCloseAll(0);
      } else {
        setOpenCloseAll((state) => state - 1);
      }
    }
  };

  const onExpand = (isExpanded) => {
    const nexExpandedItemsCount = isExpanded
      ? expandedItemsCount + 1
      : expandedItemsCount - 1;

    if (nexExpandedItemsCount === itemsLength) {
      setShouldOpenCloseAll("open");
    } else {
      setShouldOpenCloseAll("close");
    }

    setExpandedItemsCount(nexExpandedItemsCount);
  };

  return {
    openCloseAll,
    shouldOpenCloseAll,
    forceOpenAll,
    forceCloseAll,
    onOpenCloseAllClick,
    onExpand,
  };
};
