import { useSelector } from "react-redux";

import { MULTITENANCY_ENABLED } from "../constants/constants";

export const useGetTenantLink = (href, isExternalLink = false) => {
  const tenantId = useSelector((state) => state.tenants?.tenantId);

  return tenantId && MULTITENANCY_ENABLED && !isExternalLink
    ? href.replace(":tenantId", tenantId)
    : href;
};

const getBaseUrl = (tenantId) =>
   MULTITENANCY_ENABLED ? `/tenant/${tenantId}/` : "/";

export const useGetBaseUrl = () => {
  const tenantId = useSelector((state) => state.tenants?.tenantId);
  return getBaseUrl(tenantId);
};

useGetBaseUrl.getBaseUrl = getBaseUrl;
