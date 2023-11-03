/* istanbul ignore file */
import {
  httpGETRequest,
  httpGETRequestWithoutToken,
  httpPUTRequest,
} from "../httpRequestHandler";
import { Translation } from "react-i18next";
import { toast } from "react-toastify";

import API from "../endpoints";

export const getPageBlocks = (page, language) => {
  return httpGETRequestWithoutToken(API.GET_PAGE_BLOCKS, { page }, language)
    .then((res) => {
      const pageBlocks = res.data.page_blocks || [];
      return pageBlocks;
    })
    .catch((error) => {
      throw error;
    });
};

export const getBlock = (id, language) => {
  const url = API.GET_BLOCK.replace("<block_id>", id);
  return httpGETRequest(url, null, null, true, null, language)
    .then((res) => {
      return res.data;
    })
    .catch((error) => {
      throw error;
    });
};

export const updateBlock = (blockId, payload, language) => {
  const url = API.GET_BLOCK.replace("<block_id>", blockId);
  return httpPUTRequest(url, payload, null, true, null, language)
    .then((res) => {
      toast.success(
        <Translation>{(t) => t("Successfully Updated")}</Translation>
      );
      return res.data;
    })
    .catch((error) => {
      toast.error(
        <Translation>{(t) => t("Something went wrong")}</Translation>
      );
      throw error;
    });
};
