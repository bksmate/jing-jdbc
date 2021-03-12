package org.jing.jdbc.lang;

import org.jing.core.lang.JingException;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2021-03-12 <br>
 */
public class JingJDBCException extends JingException {
    public JingJDBCException() {
    }

    public JingJDBCException(String message) {
        super(message);
    }

    public JingJDBCException(String message, Object... parameters) {
        super(message, parameters);
    }

    public JingJDBCException(Throwable cause, String message) {
        super(message, cause);
    }

    public JingJDBCException(Throwable cause, String message, Object... parameters) {
        super(cause, message, parameters);
    }

    public JingJDBCException(Throwable cause) {
        super(cause);
    }
}
