import ACTION_CONSTANTS from "../actions/actionConstants";

const initialState = {};

const pageBlocks = (state = initialState, action) => {
  switch (action.type) {
    case ACTION_CONSTANTS.SET_PAGE_BLOCKS: {
      const { page, language, data } = action.payload;
      if (!state[page]) state[page] = {};

      state[page][language] = data;
      return { ...state };
    }

    default:
      return state;
  }
};

export default pageBlocks;
