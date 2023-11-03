import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import TaskOutlinedIcon from "@mui/icons-material/TaskOutlined";
import PersonOutlineOutlinedIcon from "@mui/icons-material/PersonOutlineOutlined";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";
import { PAGE_ROUTES } from "../../../../constants/navigation";

export const useProfileMenuConfig = () => [
  {
    title: "profile.menu.createService",
    href: `${PAGE_ROUTES.REQUEST_SERVICE}`,
    Icon: EditOutlinedIcon,
    iconColorClass: "text-sm-orange",
  },
  {
    title: "profile.menu.myRequests",
    href: `${PAGE_ROUTES.MY_SERVICES}`,
    Icon: TaskOutlinedIcon,
    iconColorClass: "text-sm-blue",
  },
  {
    title: "profile.menu.profile",
    href: PAGE_ROUTES.PROFILE,
    Icon: PersonOutlineOutlinedIcon,
    iconColorClass: "text-sm-indigo-4",
  },
  {
    id: "logoutCta",
    title: "profile.menu.logout",
    Icon: LogoutOutlinedIcon,
    iconColorClass: "text-sm-indigo-4",
  },
];
