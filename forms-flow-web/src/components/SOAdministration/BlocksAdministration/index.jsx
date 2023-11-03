import React from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import { Nav } from "react-bootstrap";

import { PAGE_BLOCKS, PAGE_NAMES } from "../../../constants/pages";
import { PAGE_ROUTES } from "../../../constants/navigation";
import NavLink from "../../sm/components/Navigation/NavLink";
import AdministrationContainer from "../AdministrationContainer";
import { useGetPageBlockForEdit } from "../../../apiManager/apiHooks";
import Loading from "../../../containers/Loading";

import styles from "./blocksAdministration.module.scss";

const BlocksAdministration = () => {
  const { t } = useTranslation();
  const { page } = useParams();
  const blocksConfig = PAGE_BLOCKS[page] || {};
  const [blocks, isLoading] = useGetPageBlockForEdit(page);
  const title = Object.entries(PAGE_NAMES).find(
    // eslint-disable-next-line no-unused-vars
    ([_, value]) => value === page
  )?.[0];

  if (isLoading) {
    return <Loading />;
  }

  return (
    <AdministrationContainer title={t(title || "Blocks Administration")}>
      {blocks?.length && !isLoading ? (
        <div className={styles.content}>
          <div className={styles.blocksWrapper}>
            {Object.entries(blocksConfig).map(([key, value], index) => {
              const block = blocks.find((b) => b["machine_name"] === value);
              return block ? (
                <Nav.Link
                  key={index}
                  as={NavLink}
                  to={PAGE_ROUTES.BLOCKS_ADMINISTRATION_EDIT.replace(
                    ":page",
                    page
                  ).replace(":block", block?.id)}
                  className={styles.block}
                >
                  <span>{t(key)}</span>
                  <EditOutlinedIcon />
                </Nav.Link>
              ) : null;
            })}
          </div>
        </div>
      ) : (
        <div className={styles.noBlocksFound}>
          <span className={styles.noBlocksText}>
            {t("blocks.seed.missing.error")}
          </span>
        </div>
      )}
    </AdministrationContainer>
  );
};

export default BlocksAdministration;
