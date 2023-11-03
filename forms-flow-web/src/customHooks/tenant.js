import { useSelector } from "react-redux";

import { MULTITENANCY_ENABLED, TENANT_ID } from "../constants/constants";

export const useGetTenantLink = (href, isExternalLink = false) => {
  const tenantId = useSelector((state) => state.tenants?.tenantId);

  return tenantId && MULTITENANCY_ENABLED && !isExternalLink && !TENANT_ID
    ? href.replace(":tenantId", tenantId)
    : href;
};

const getBaseUrl = (tenantId) =>
  !TENANT_ID && MULTITENANCY_ENABLED ? `/tenant/${tenantId}/` : "/";

export const useGetBaseUrl = () => {
  const tenantId = useSelector((state) => state.tenants?.tenantId);
  return getBaseUrl(tenantId);
};

useGetBaseUrl.getBaseUrl = getBaseUrl;
