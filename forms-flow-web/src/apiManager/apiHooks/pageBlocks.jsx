import { useEffect, useState, useCallback } from "react";
import { useSelector, useDispatch } from "react-redux";

import {
  getPageBlocks,
  getBlock,
  updateBlock,
} from "../services/pageBlocksServices";
import { setPageBlocks } from "../../actions/pageBlocksActions";
import { getPageBlocksFallback } from "../fallbacks/pageBlocksFallbacks";
import { useApi } from "./common";

const mapPageBlocks = (blocks = []) =>
  blocks.reduce((acc, block) => {
    acc[block["machine_name"]] = block.attributes;
    return acc;
  }, {});

export const useGetPageBlocks = (
  page,
  language,
  withFallback = true,
  withCache = true
) => {
  const dispatch = useDispatch();
  const [pageBlocks, setCurrentPageBlocks] = useState();
  const pageBlocksInState = useSelector(
    (state) => state.pageBlocks[page]?.[language]
  );

  useEffect(() => {
    const fetchPageBlocks = async () => {
      try {
        if (!pageBlocksInState || !withCache) {
          const res = await getPageBlocks(page, language);
          const mappedPageBlocks = mapPageBlocks(res);
          if (mappedPageBlocks && Object.keys(mappedPageBlocks).length) {
            dispatch(setPageBlocks({ page, language, data: mappedPageBlocks }));
            setCurrentPageBlocks(mappedPageBlocks);
          } else {
            setCurrentPageBlocks(getPageBlocksFallback(page, language));
          }
        } else {
          setCurrentPageBlocks(pageBlocksInState);
        }
      } catch (err) {
        if (!withFallback) {
          throw err;
        } else {
          setCurrentPageBlocks(getPageBlocksFallback(page, language));
        }
      }
    };

    fetchPageBlocks();
  }, [page, language, pageBlocksInState, withFallback, withCache, dispatch]);

  return pageBlocks;
};

export const useGetPageBlockForEdit = (page) =>
  useApi(useCallback(() => getPageBlocks(page), [page]));

export const useGetBlock = (id, language, onInit = true) => {
  const [isLoading, setIsLoading] = useState();
  const [data, setData] = useState();
  const [error, setError] = useState();
  const fetch = useCallback(async () => {
    try {
      setError(null);
      setIsLoading(true);
      const res = await getBlock(id, language);
      setData(res);
      setIsLoading(false);
      return res;
    } catch (err) {
      setError(err);
      setIsLoading(false);
      throw err;
    }
  }, [id, language]);

  useEffect(() => {
    if (onInit) fetch(id, language);
  }, [onInit, fetch, id, language]);

  return { fetch, data, isLoading, error };
};

export const useUpdateBlock = () => (blockId, payload, language) =>
  updateBlock(blockId, payload, language);
