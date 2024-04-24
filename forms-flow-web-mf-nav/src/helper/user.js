import {
  STAFF_REVIEWER,
  STAFF_DESIGNER,
  ADMIN_ROLE,
  ANALYTICS_VIEWER,
  PAGE_ADMIN
} from "../constants/constants";

const getUserRoleName = (userRoles) => {
  let role = "";
  if (userRoles.includes(ADMIN_ROLE)) {
    role = "ADMIN";
  } else if (userRoles.includes(STAFF_REVIEWER)) {
    role = "REVIEWER";
  } else if (userRoles.includes(STAFF_DESIGNER)) {
    role = "DESIGNER";
  } else if (userRoles.includes(PAGE_ADMIN)) {
    role = "Marketing Admin";
  } else if (userRoles.includes(ANALYTICS_VIEWER)) {
    role = "Analytics Admin";
  } else {
    role = "CLIENT";
  }
  return role;
};

const getUserRolePermission = (userRoles, role) => {
  return userRoles && userRoles.includes(role);
};

export { getUserRoleName, getUserRolePermission };
