import { PAGE_NAMES, PAGE_BLOCKS } from "../../../constants/pages";

const contactsPageBlogs = PAGE_BLOCKS[PAGE_NAMES.CONTACTS_PAGE];

export default {
  [contactsPageBlogs.CONTACTS_BLOCK]: {
    items: [
      {
        link: "https://bankya.bg/",
        phone: "contacts.item.phone",
        title: "contacts.item.title",
      },
    ],
  },
};
