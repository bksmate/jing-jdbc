package org.jing.jdbc.lang;

import org.jing.core.lang.ExceptionHandler;
import org.jing.core.lang.JingException;
import org.jing.core.util.StringUtil;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2021-01-07 <br>
 */
public class Security {
    public static String encryptX16(String content) {
        byte[] buffer = content.getBytes();
        StringBuilder stbr = new StringBuilder();
        for (byte b : buffer) {
            stbr.append(Integer.toHexString((int) b));
        }
        stbr.append("==");
        return stbr.toString();
    }

    public static String decryptX16(String content) throws JingException {
        ExceptionHandler.publishIfMatch(StringUtil.isEmpty(content) || content.length() % 2 != 0 || !content.endsWith("=="), "Invalid string to be decrypted");
        content = content.substring(0, content.length() - 2);
        int length = content.length() / 2;
        byte[] buffer = new byte[length];
        for (int i$ = 0; i$ < length; i$++) {
            buffer[i$] = (byte) Integer.parseInt(content.substring(i$ * 2, i$ * 2 + 2), 16);
        }
        return new String(buffer);
    }

    public static void checkEZSqlInject(String text) throws JingException {
        if (!text.matches("[a-zA-Z0-9_]*")) {
            throw new JingException("Found SQL Inject: " + text);
        }
    }
}
