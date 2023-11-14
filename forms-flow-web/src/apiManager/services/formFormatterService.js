import { addHiddenApplicationComponent } from "../../constants/applicationComponent";
import { addTenantkey } from "../../helper/helper";

export const manipulatingFormData = (
  form,
  MULTITENANCY_ENABLED,
  tenantKey,
  formAccess,
  submissionAccess,
  submissionAccessResource
) => {
  const newFormData = addHiddenApplicationComponent(form);
  newFormData.access = formAccess;
  if (submissionAccessResource?.length && form?.type === "resource") {
    newFormData.submissionAccess = submissionAccessResource;
  } else {
    newFormData.submissionAccess = submissionAccess;
  }

  if (MULTITENANCY_ENABLED && tenantKey) {
    if (newFormData.path) {
      newFormData.path = addTenantkey(newFormData.path, tenantKey);
    }
    if (newFormData.name) {
      newFormData.name = addTenantkey(newFormData.name, tenantKey);
    }
  }
  return newFormData;
};
