import ACTION_CONSTANTS from "./actionConstants";

export const setPageBlocks = (data) => (dispatch) => {
  dispatch({
    type: ACTION_CONSTANTS.SET_PAGE_BLOCKS,
    payload: data,
  });
};
