import { PAGE_NAMES, PAGE_BLOCKS } from "../../../constants/pages";

const personalDataPageBlocks = PAGE_BLOCKS[PAGE_NAMES.PERSONAL_DATA];

export default {
  bg: {
    [personalDataPageBlocks.PERSONAL_DATA_BLOCK]: {
      text: `<h1><strong>Лични данни</strong></h1><p><br></p><p>За повече информация моля посетете:&nbsp;<a href="https://www.sofia.bg/en/general-provisions" rel="noopener noreferrer" target="_blank" style="color: rgb(128, 28, 175);">https://www.sofia.bg/en/general-provisions</a></p>`,
    },
  },
  en: {
    [personalDataPageBlocks.PERSONAL_DATA_BLOCK]: {
      text: `<h1><strong>Privacy policy</strong></h1><p><br></p><p>For more information please visit: <a href="https://www.sofia.bg/en/general-provisions" rel="noopener noreferrer" target="_blank">https://www.sofia.bg/en/general-provisions</a></p>`,
    },
  },
};
