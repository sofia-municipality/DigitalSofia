import ACTION_CONSTANTS from "./actionConstants";

export const setRoleIds = (data) => (dispatch) => {
  dispatch({
    type: ACTION_CONSTANTS.ROLE_IDS,
    payload: data,
  });
};

export const setAccessForForm = (data) => (dispatch) => {
  dispatch({
    type: ACTION_CONSTANTS.ACCESS_ADDING,
    payload: data,
  });
};

export const openCloseForbiddenModal = (data) => (dispatch) => {
  dispatch({
    type: ACTION_CONSTANTS.OPEN_CLOSE_FORBIDDEN_MODAL,
    payload: data,
  });
};
