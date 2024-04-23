export const mockTaxes = () => {
  const data = {
    taxSubject: {
      taxsubjectid: 0,
      idn: "00000000",
      name: "firstName middleName lastName",
      permanentaddress: null,
    },
    obligations: {
      total: 285,
      data: {
        real_estate: {
          total: 10,
          data: {
            "10002H2222": {
              total: 10,
              data: [
                {
                  debtInstalmentId: 0,
                  rnu: "test-rnu",
                  municipalityId: 2025,
                  municipalityName: "ЛОЗЕНЕЦ",
                  partidaNo: "10002H2222",
                  registerNo: null,
                  propertyAddress: "test property address 1",
                  taxPeriodYear: "2020",
                  kindDebtRegId: "2",
                  kindDebtRegName: "д.недв.имот",
                  termPayDate: "2020-11-02",
                  instNo: 2,
                  residual: 8,
                  interest: 2,
                  payOrder: 1,
                },
              ],
            },
          },
        },
        household_waste: {
          total: 9,
          data: {
            "10002H2223": {
              total: 9,
              data: [
                {
                  debtInstalmentId: 0,
                  rnu: "test-rnu",
                  municipalityId: 2025,
                  municipalityName: "ЛОЗЕНЕЦ",
                  partidaNo: "10002H2223",
                  registerNo: null,
                  propertyAddress: "test property address 2",
                  taxPeriodYear: "2020",
                  kindDebtRegId: "5",
                  kindDebtRegName: "т. бит.отп.",
                  termPayDate: "2020-09-30",
                  instNo: 3,
                  residual: 7,
                  interest: 2,
                  payOrder: 1,
                },
              ],
            },
          },
        },
        vehicle: {
          total: 266,
          data: {
            "10002H2224": {
              total: 32,
              data: [
                {
                  debtInstalmentId: 0,
                  rnu: "test-rnu",
                  municipalityId: 2045,
                  municipalityName: "СЛАТИНА",
                  partidaNo: "10002H2224",
                  registerNo: "AA0000A",
                  propertyAddress: " ",
                  taxPeriodYear: "2023",
                  kindDebtRegId: "4",
                  kindDebtRegName: "д. МПС",
                  termPayDate: "2023-06-30",
                  instNo: 1,
                  residual: 16,
                  interest: 0,
                  payOrder: 1,
                },
                {
                  debtInstalmentId: 0,
                  rnu: "test-rnu",
                  municipalityId: 2045,
                  municipalityName: "СЛАТИНА",
                  partidaNo: "7221T104247",
                  registerNo: "AA0000A",
                  propertyAddress: " ",
                  taxPeriodYear: "2023",
                  kindDebtRegId: "4",
                  kindDebtRegName: "д. МПС",
                  termPayDate: "2023-10-31",
                  instNo: 2,
                  residual: 16,
                  interest: 0,
                  payOrder: 1,
                },
              ],
            },
          },
        },
      },
    },
    hasMore: false,
  };

  return { data, isLoading: false };
};
