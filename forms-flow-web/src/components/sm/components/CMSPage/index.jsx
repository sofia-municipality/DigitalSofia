import React, { useEffect } from "react";
import { useSelector } from "react-redux";
import { useHistory } from "react-router-dom";
import DOMPurify from "dompurify";

import { useGetPageBlocks } from "../../../../apiManager/apiHooks";
import Loading from "../../../../containers/Loading";
import PageContainer from "../PageContainer";

import styles from "./cmsPage.module.scss";

const CMSPage = ({ pageName, pageBlock }) => {
  const { action } = useHistory();
  const userLanguage = useSelector((state) => state.user.lang);
  const pageBlocks = useGetPageBlocks(pageName, userLanguage) || {};

  const block = pageBlocks[pageBlock];
  useEffect(() => {
    if (block && action === "PUSH") {
      const heading = document.querySelectorAll("h1, h2, h3, h4, h5, h6")[0];
      if (heading) {
        heading.setAttribute("tabIndex", "-1");
        heading.focus();
      }
    }
  }, [block, action]);

  return block ? (
    <PageContainer>
      <div className={`${styles.cmsContainer}`}>
        <div className={`container-fluid ${styles.content} pr-md-5 pl-md-5`}>
          <div className="row mt-3 mt-md-5">
            <div className="col-12">
              <div className={styles.cmsContent}>
                <div
                  dangerouslySetInnerHTML={{
                    __html: DOMPurify.sanitize(block?.text),
                  }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </PageContainer>
  ) : (
    <Loading />
  );
};

export default CMSPage;
