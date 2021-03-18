package test;

import org.jing.core.lang.Pair2;
import org.jing.core.logger.JingLogger;
import org.jing.core.thread.ThreadFactory;
import org.jing.jdbc.lang.JingJDBC;

import java.lang.Exception;
import java.util.ArrayList;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-08-07 <br>
 */
public class Demo1 {
    private static final JingLogger LOGGER = JingLogger.getLogger(Demo1.class);
    private Demo1() throws Exception {
        JingJDBC jdbc = JingJDBC.getJDBC("mysql");
        jdbc.updateAndGetGeneratedKeys("INSERT INTO TEMP_TEST (CONTENT) VALUES (?)", "hello");
        jdbc.release();
    }

    public static void main(String[] args) throws Exception {
        new Demo1();
    }
}
