/*
 * Copyright 2015 Antonio Fernandes (antoniopaisfernandes@gmail.com)
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
package org.traccar.web.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum PasswordHashMethod implements IsSerializable {
    PLAIN("plain") {
        @Override
        public String doHash(String s) {
            return s;
        }
    },
    SHA512("sha512") {
        @Override
        public String doHash(String s) {
            try {
                final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
                sha512.update(s.getBytes());
                byte data[] = sha512.digest();
                StringBuffer hexData = new StringBuffer();
                for (int byteIndex = 0; byteIndex < data.length; byteIndex++)
                    hexData.append(Integer.toString((data[byteIndex] & 0xff) + 0x100, 16).substring(1));
                return hexData.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    },
    MD5("md5") {
        @Override
        public String doHash(String s) {
            try {
                final MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(s.getBytes());
                byte[] array = md5.digest();
                StringBuffer hexData = new StringBuffer();
                for (int i = 0; i < array.length; ++i) {
                    hexData.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
                }
                return hexData.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    };

    final String method;

    PasswordHashMethod(String name) {
        this.method = name;
    }

    public String getName() {
        return method;
    }

    public abstract String doHash(String s);
}
