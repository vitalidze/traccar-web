/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.model;

import org.traccar.web.shared.model.PasswordHashMethod;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

class PasswordUtils {
    private static final Random RANDOM = new SecureRandom();
    private static final int SALT_SIZE = 24;

    private PasswordUtils() {
    }

    static String generateRandomString() {
        return generateRandomString(8);
    }

    static String generateRandomString(int length) {

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int c = RANDOM.nextInt(62);
            if (c <= 9) {
                sb.append(String.valueOf(c));
            } else if (c < 36) {
                sb.append((char) ('a' + c - 10));
            } else {
                sb.append((char) ('A' + c - 36));
            }
        }
        return sb.toString();
    }

    static String generateRandomUserSalt() {
        byte[] salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);
        return DatatypeConverter.printHexBinary(salt);
    }

    static String hash(PasswordHashMethod method, String password, String appSalt, String userSalt) {
        try {
            switch (method) {
                case SHA512:
                    return sha512(password, appSalt);
                case MD5:
                    return md5(password, appSalt);
                case PBKDF2WithHmacSha1:
                    return pbkdf2WithHmacSha1(password, userSalt);
                default:
                    return password;
            }
        } catch (GeneralSecurityException gse) {
            throw new RuntimeException(gse);
        }
    }

    private static String sha512(String password, String salt) throws GeneralSecurityException {
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        sha512.reset();
        if (salt != null && !salt.isEmpty()) {
            sha512.update(salt.getBytes());
        }
        byte[] data = sha512.digest(password.getBytes());
        return DatatypeConverter.printHexBinary(data).toLowerCase();
    }

    private static String md5(String s, String salt) throws GeneralSecurityException {
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        if (salt != null && !salt.isEmpty()) {
            md5.update(salt.getBytes());
        }
        byte[] data = md5.digest(s.getBytes());
        return DatatypeConverter.printHexBinary(data).toLowerCase();
    }

    private static String pbkdf2WithHmacSha1(String s, String salt) throws GeneralSecurityException {
        final int ITERATIONS = 1000;
        final int HASH_SIZE = 24;

        PBEKeySpec spec = new PBEKeySpec(s.toCharArray(), DatatypeConverter.parseHexBinary(salt), ITERATIONS, HASH_SIZE * Byte.SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return DatatypeConverter.printHexBinary(factory.generateSecret(spec).getEncoded());
    }
}
