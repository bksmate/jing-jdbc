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
        jdbc.update("DELETE FROM UT_TEST");
        jdbc.commit();
        String sql = "INSERT INTO UT_TEST (TEST1, TEST2) VALUES (?, ?)";
        ArrayList<Object[]> parameters = new ArrayList<Object[]>();
        parameters.add(new Object[]{"1", 2});
        parameters.add(new Object[]{"3", 4});
        jdbc.batchUpdate(sql, parameters);
        jdbc.commit();
        System.out.println(jdbc.qry("SELECT * FROM UT_TEST WHERE TEST1 = ?", "1"));
    }

    public static void main(String[] args) throws Exception {
        new Demo1();
    }
}
