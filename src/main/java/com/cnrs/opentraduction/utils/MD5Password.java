package com.cnrs.opentraduction.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5Password {

    public static String encodedPassword(String key) {

        try {
            var hash = MessageDigest.getInstance("MD5").digest(key.getBytes());
            StringBuilder hashString = new StringBuilder();
            for (int i = 0; i < hash.length; ++i) {
                var hex = Integer.toHexString(hash[i]);
                if (hex.length() == 1) {
                    hashString.append('0');
                    hashString.append(hex.charAt(hex.length() - 1));
                } else {
                    hashString.append(hex.substring(hex.length() - 2));
                }
            }
            return hashString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM : " + e);
        }

    }
}
