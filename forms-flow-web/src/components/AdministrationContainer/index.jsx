import React from "react";
import Breadcrumbs from "../sm/components/Breadcrumbs";

const AdministrationContainer = ({
  title,
  children,
  withBreadcrumbs = true,
}) => {
  return (
    <div className="container" id="main">
      <div className="container-fluid">
        <div className="flex-item-left">
          <div style={{ display: "flex" }}>
            <h3 className="task-head" style={{ marginTop: "3px" }}>
              <i className="fa fa-cogs" aria-hidden="true" />
            </h3>
            <h3 className="task-head">
              {" "}
              <span className="forms-text" style={{ marginLeft: "1px" }}>
                {title}
              </span>
            </h3>
          </div>
        </div>
      </div>
      <div className="container-fluid mt-3">
        {withBreadcrumbs && <Breadcrumbs />}
      </div>
      <div className="container-fluid">{children}</div>
    </div>
  );
};

export default AdministrationContainer;
