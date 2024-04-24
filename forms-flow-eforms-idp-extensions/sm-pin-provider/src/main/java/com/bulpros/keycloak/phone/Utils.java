package com.bulpros.keycloak.phone;

import com.bulpros.common.OptionalUtils;
import com.bulpros.keycloak.phone.providers.constants.SpiConstants;
import com.bulpros.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import com.bulpros.keycloak.phone.providers.spi.PinProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);
    private static int[] egnWeights = { 2, 4, 8, 5, 10, 9, 7, 3, 6 };
    private static int[] piWeights = { 21, 19, 17, 13, 11, 9, 7, 3, 1 };

    public static Optional<UserModel> findUserByPhone(KeycloakSession session, RealmModel realm, String phoneNumber) {

        var userProvider = session.users();
        Set<String> numbers = new HashSet<>();
        numbers.add(phoneNumber);

        return numbers.stream()
                .flatMap(number -> userProvider.searchForUserByUserAttributeStream(realm, "phoneNumber", number))
                .max((u1, u2) -> {
                    var result = comparatorAttributesAnyMatch(u1, u2, "phoneNumberVerified", "true"::equals);
                    if (result == 0) {
                        result = comparatorAttributesAnyMatch(u1, u2, "phoneNumber", number -> number.startsWith("+"));
                    }
                    return result;
                });

    }

    public static Optional<UserModel> findUserByPersonIdentifier(KeycloakSession session, RealmModel realm,
            String personIdentifier) {
        var userProvider = session.users();
        return userProvider.searchForUserByUserAttributeStream(realm, SpiConstants.PERSON_IDENTIFIER, personIdentifier)
                .findFirst();
    }

    public static boolean deleteUser(KeycloakSession session, RealmModel realm, UserModel user) {
        var userProvider = session.users();
        return userProvider.removeUser(realm, user);
    }

    public static void createNewUser(KeycloakSession session, RealmModel realm, String personIdentifier) {
        var userProvider = session.users();
        UserModel newUser = userProvider.addUser(realm, personIdentifier);
        newUser.setSingleAttribute(SpiConstants.VERIFIED, Boolean.FALSE.toString());
        newUser.setSingleAttribute(SpiConstants.PERSON_IDENTIFIER, personIdentifier);
        newUser.setSingleAttribute(SpiConstants.LOG_LEVEL, Integer.toString(0));
        newUser.setEnabled(true);
    }

    public static boolean findUserByEmailAndCheckForAttributeByName(KeycloakSession session, RealmModel realm,
            String email, String attributeName) {
        var userProvider = session.users();
        UserModel user = userProvider.getUserByEmail(realm, email);
        if (!user.getAttributes().isEmpty() && user.getAttributes().get(attributeName) != null) {
            return true;
        }
        return false;
    }

    private static int comparatorAttributesAnyMatch(UserModel user1, UserModel user2, String attribute,
            Predicate<? super String> predicate) {
        return Boolean.compare(//
                user1.getAttributeStream(attribute).anyMatch(predicate), //
                user2.getAttributeStream(attribute).anyMatch(predicate) //
        );
    }

    private static Optional<String> localeToCountry(String locale) {
        return OptionalUtils.ofBlank(locale).flatMap(l -> {
            Pattern countryRegx = Pattern.compile("[^a-z]*\\-?([A-Z]{2,3})");
            return Optional.of(countryRegx.matcher(l))
                    .flatMap(m -> m.find() ? OptionalUtils.ofBlank(m.group(1)) : Optional.empty());
        });
    }

    private static String defaultRegion(KeycloakSession session) {
        var defaultRegion = session.getProvider(PinProvider.class).defaultPhoneRegion();
        return defaultRegion.orElseGet(
                () -> localeToCountry(session.getContext().getRealm().getDefaultLocale()).orElse(null));
    }

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

    /**
     * Parses a phone number with google's libphonenumber and then outputs it's international canonical form
     */
    public static String canonicalizePhoneNumber(KeycloakSession session, @NotNull String phoneNumber)
            throws PhoneNumberInvalidException {
        var provider = session.getProvider(PinProvider.class);

        var phoneNumberUtil = PhoneNumberUtil.getInstance();
        var resultPhoneNumber = phoneNumber.trim();
        var defaultRegion = defaultRegion(session);
        logger.info(String.format("default region '%s' will be used", defaultRegion));
        try {
            var parsedNumber = phoneNumberUtil.parse(resultPhoneNumber, defaultRegion);
            if (provider.validPhoneNumber() && !phoneNumberUtil.isValidNumber(parsedNumber)) {
                logger.info(
                        String.format("Phone number [%s] Valid fail with google's libphonenumber", resultPhoneNumber));
                throw new PhoneNumberInvalidException(PhoneNumberInvalidException.ErrorType.VALID_FAIL,
                        String.format("Phone number [%s] Valid fail with google's libphonenumber", resultPhoneNumber));
            }

            var canonicalizeFormat = provider.canonicalizePhoneNumber();
            try {
                resultPhoneNumber = canonicalizeFormat.map(PhoneNumberFormat::valueOf)
                        .map(format -> phoneNumberUtil.format(parsedNumber, format)).orElse(resultPhoneNumber);
            } catch (RuntimeException e) {
                logger.warn(String.format(
                        "canonicalize format param error! '%s' is not in supported list: %s, E164 Will be used.",
                        Arrays.toString(PhoneNumberFormat.values()), canonicalizeFormat.orElse("")), e);
                resultPhoneNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberFormat.E164);
            }

            var phoneNumberRegex = provider.phoneNumberRegex();
            if (!phoneNumberRegex.map(resultPhoneNumber::matches).orElse(true)) {
                logger.info(String.format("Phone number [%s] not match regex '%s'", resultPhoneNumber,
                        phoneNumberRegex.orElse("")));
                throw new PhoneNumberInvalidException(PhoneNumberInvalidException.ErrorType.NOT_SUPPORTED,
                        String.format("Phone number [%s] not match regex '%s'", resultPhoneNumber,
                                phoneNumberRegex.orElse("")));
            }
            return resultPhoneNumber;
        } catch (NumberParseException e) {
            logger.info(e);
            throw new PhoneNumberInvalidException(e);
        }
    }

    public static boolean isDuplicatePhoneAllowed(KeycloakSession session) {
        return session.getProvider(PinProvider.class).isDuplicatePhoneAllowed();
    }

    public static int getOtpExpires(KeycloakSession session) {
        return session.getProvider(PinProvider.class).otpExpires();
    }
}
