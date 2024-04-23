package com.bulpros.keycloak.util;

import org.jboss.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);
    private static int[] egnWeights = { 2, 4, 8, 5, 10, 9, 7, 3, 6 };
    private static int[] piWeights = { 21, 19, 17, 13, 11, 9, 7, 3, 1 };

    public static boolean isPersonIdentifierNotValid(String personIdentifier) {
        if (personIdentifier.length() != 10)
            return true;
        try {
            Long.parseLong(personIdentifier);
        } catch (NumberFormatException e) {
            return true;
        }
        String year = personIdentifier.substring(0, 2);
        String month = personIdentifier.substring(2, 4);
        String day = personIdentifier.substring(4, 6);
        int monthInt = Integer.parseInt(month);
        if (monthInt > 12) {
            monthInt = monthInt - 40;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH);
            String dateInString = day + "-" + String.valueOf(monthInt) + "-" + year;
            Date date = formatter.parse(dateInString);
        } catch (Exception e) {
            return true;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum = sum + (Integer.parseInt(personIdentifier.substring(i, i + 1))) * egnWeights[i];
        }
        int reminder = sum % 11;
        if (reminder < 10) {
            if (reminder != Integer.parseInt(personIdentifier.substring(9, 10))) {
                return true;
            }
        } else {
            if (reminder != 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForeignPersonIdentifierNotValid(String foreignPersonIdentifier) {
        if (foreignPersonIdentifier.length() != 10)
            return true;
        try {
            Long.parseLong(foreignPersonIdentifier);
        } catch (NumberFormatException e) {
            return true;
        }
        String year = foreignPersonIdentifier.substring(0, 2);
        String month = foreignPersonIdentifier.substring(2, 4);
        String day = foreignPersonIdentifier.substring(4, 6);
        int monthInt = Integer.parseInt(month);
        if (monthInt > 12) {
            monthInt = monthInt - 40;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy", Locale.ENGLISH);
            String dateInString = day + "-" + String.valueOf(monthInt) + "-" + year;
            Date date = formatter.parse(dateInString);
        } catch (Exception e) {
            return true;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum = sum + (Integer.parseInt(foreignPersonIdentifier.substring(i, i + 1))) * piWeights[i];
        }
        int reminder = sum % 10;
        if (reminder < 10) {
            if (reminder != Integer.parseInt(foreignPersonIdentifier.substring(9, 10))) {
                return true;
            }
        } else {
            if (reminder != 0) {
                return true;
            }
        }
        return false;
    }
}
