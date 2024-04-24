import { PAGE_NAMES, PAGE_BLOCKS } from "../../../constants/pages";

const cookiePolicyPageBlocks = PAGE_BLOCKS[PAGE_NAMES.COOKIE_POLICY];

export default {
  [cookiePolicyPageBlocks.COOKIE_POLICY_BLOCK]: {
    text: `<div><h1>cookie.policy.title</h1></div>`,
  },
};
