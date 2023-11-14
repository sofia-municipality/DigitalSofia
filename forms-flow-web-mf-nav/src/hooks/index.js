import { useState, useLayoutEffect } from "react";

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