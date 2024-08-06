package cc.jq1024.middleware.sdk.types.utils;

import java.util.Random;

/**
 * @author li--jiaqiang
 * @date 2024−08−06
 */
public class RandomUtils {


    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}