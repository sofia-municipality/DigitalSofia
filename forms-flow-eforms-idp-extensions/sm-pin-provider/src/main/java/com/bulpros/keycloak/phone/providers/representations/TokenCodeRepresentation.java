package com.bulpros.keycloak.phone.providers.representations;

import com.bulpros.keycloak.phone.providers.model.CodeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenCodeRepresentation {

    private String id;
    private String personIdentifier;
    private String code;
    private String type;
    private Date createdAt;
    private Date expiresAt;
    private CodeStatusEnum status;

    public static TokenCodeRepresentation forPersonIdentifier(String personIdentifier) throws NoSuchAlgorithmException {

        TokenCodeRepresentation tokenCode = new TokenCodeRepresentation();

        tokenCode.id = KeycloakModelUtils.generateId();
        tokenCode.personIdentifier = personIdentifier;
        tokenCode.code = generateTokenCode();
        tokenCode.status = CodeStatusEnum.WAITING;

        return tokenCode;
    }

    private static String generateTokenCode() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        Integer code = secureRandom.nextInt(999_999_999);
        return toHexString(getSHA(code.toString()));
    }

    private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
