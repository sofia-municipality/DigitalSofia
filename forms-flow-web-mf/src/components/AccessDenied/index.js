import React from "react";
import "./accessDenied.scss";

const AccessDenied = React.memo(() => {
  return (
    <section>
      <div className="circles">
        <p>
          <small>Нямате достъп до модула за обработка.</small>
          <small>Моля, свържете се с Администратор на системата.</small>
        </p>
        <span className="circle big" />
        <span className="circle med" />
        <span className="circle small" />
      </div>
    </section>
  );
});

export default AccessDenied;
