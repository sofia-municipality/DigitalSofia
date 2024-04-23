import { useDispatch } from "react-redux";
import { useGetTenantLink } from "./tenant";
import { push } from "connected-react-router";

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