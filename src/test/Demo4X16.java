package test;

import org.jing.core.lang.JingException;
import org.jing.core.logger.JingLogger;
import org.jing.jdbc.lang.Security;

import java.lang.Exception;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2021-01-07 <br>
 */
public class Demo4X16 {
    private static final JingLogger LOGGER = JingLogger.getLogger(Demo4X16.class);

    private Demo4X16() throws Exception {
    }

    private void encrypt(String password) {
        LOGGER.imp("Original String: [{}]", password);
        String encryptRes = Security.encryptX16(password);
        LOGGER.imp("X16 String: [{}]", encryptRes);
    }

    private void decrypt(String password) throws JingException {
        LOGGER.imp("Original String: [{}]", password);
        String encryptRes = Security.decryptX16(password);
        LOGGER.imp("X16 String: [{}]", encryptRes);
    }

    public static void main(String[] args) throws Exception {
        if (null != args && args.length > 2) {
            if ("0".equals(args[0])) {
                new Demo4X16().encrypt(args[1]);
            }
            else {
                new Demo4X16().decrypt(args[1]);
            }
        }
        else {
            new Demo4X16().encrypt("password");
        }
    }
}
