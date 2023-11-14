export const getLocalDateAndTime = (date) => {
  return date ? new Date(date.replace(" ", "T") + "Z").toLocaleString() : "-";
};