import CloseIcon from "@mui/icons-material/Close";
import CheckOutlinedIcon from "@mui/icons-material/CheckOutlined";
import MoreHorizIcon from "@mui/icons-material/MoreHoriz";

const TaxRecordGroupStatus = {
  PAID: "Paid",
  CANCELED: "Canceled",
  NEW: "New",
};

export const getTaxRecordGroupStatusIcon = (status) => {
  switch (status) {
    case TaxRecordGroupStatus.PAID:
      return {
        Icon: CheckOutlinedIcon,
        className: "bg-sm-green",
        border: true,
        accessibleName: "myServices.localTaxesGroup.status.paid",
      };
    case TaxRecordGroupStatus.CANCELED:
      return {
        Icon: CloseIcon,
        className: "bg-sm-red",
        border: true,
        accessibleName: "myServices.localTaxesGroup.status.canceled",
      };
    case TaxRecordGroupStatus.NEW:
      return {
        Icon: MoreHorizIcon,
        className: "bg-sm-blue",
        border: true,
        accessibleName: "myServices.localTaxesGroup.status.new",
      };
    default:
      return {};
  }
};
