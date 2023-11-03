import moment from "moment";

export const validatePersonalIdentifier = (value, type = "egn") => {
  if (value.length != 10) {
    return false;
  }
  // Check if EGN/PID is numeric
  for (const element of value) {
    if (Number.isNaN(element)) {
      return false;
    }
  }
  if (type === "egn") {
    // Check day, month and year
    let year = parseInt(value.substr(0, 2));
    let month = parseInt(value.substr(2, 2));
    const day = parseInt(value.substr(4, 2));
    // Check month
    if (month < 1 || (month > 12 && month < 21) || (month > 32 && month < 41)) {
      return false;
    }
    // Build full year
    if (month >= 1 && month <= 12) {
      year += 1900;
    } else if (month >= 21 && month <= 32) {
      month -= 20;
      year += 1800;
    } else if (month >= 41 && month <= 52) {
      month -= 40;
      year += 2000;
    }
    // Check day
    var m = moment(year + "-" + month + "-" + day, "YYYY-MM-DD");
    if (!m.isValid()) {
      return false;
      // false
    }
  }

  let contr;
  let divisor;
  if (type === "egn") {
    contr = "020408051009070306";
    divisor = 11;
  } else {
    contr = "211917131109070301";
    divisor = 10;
  }
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    const digit = value.substr(i, 1);
    const weight = contr.substr(i * 2, 2);
    sum += Number(digit) * Number(weight);
  }
  const last = sum % divisor == 10 ? 0 : sum % divisor;
  return last == parseInt(value.substr(9, 1));
};
