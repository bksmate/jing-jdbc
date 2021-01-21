package org.jing.jdbc.lang;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2021-01-07 <br>
 */
public enum Encrypt {
    X16("X16");

    private final String string;

    Encrypt(String string) {
        this.string = string;
    }

    public static Encrypt getEncrypt(String encryptString) {
        Encrypt[] encrypts = values();
        for (Encrypt encrypt: encrypts) {
            if (encrypt.string.equals(encryptString)) {
                return encrypt;
            }
        }
        return null;
    }
}
