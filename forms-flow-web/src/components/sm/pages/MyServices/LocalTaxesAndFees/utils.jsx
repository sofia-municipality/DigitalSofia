import CloseIcon from "@mui/icons-material/Close";
import CheckOutlinedIcon from "@mui/icons-material/CheckOutlined";
import MoreHorizIcon from "@mui/icons-material/MoreHoriz";

export const TaxRecordGroupStatus = {
  PENDING: "Pending",
  PAID: "Paid",
  CANCELED: "Canceled",
  NEW: "New",
  EXPIRED: "Expired",
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
    case TaxRecordGroupStatus.EXPIRED:
    case TaxRecordGroupStatus.CANCELED:
      return {
        Icon: CloseIcon,
        className: "bg-sm-red",
        border: true,
        accessibleName: "myServices.localTaxesGroup.status.canceled",
      };
    case TaxRecordGroupStatus.PENDING:
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
