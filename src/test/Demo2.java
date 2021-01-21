package test;

import org.jing.core.lang.Pair2;
import org.jing.jdbc.lang.JingJDBC;

import java.lang.Exception;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-09-02 <br>
 */
public class Demo2 {
    private Demo2() throws Exception {
        JingJDBC jdbc = JingJDBC.getJDBC("local");
        jdbc.update("INSERT INTO UT_TEST (TEST1, TEST2) VALUES ('2', '2')");
        jdbc.commit();
        System.out.println(jdbc.qry("SELECT * FROM UT_TEST"));
        jdbc.update("DELETE UT_TEST T WHERE T.TEST1 = ?", new Pair2<Class<?>, Object>(String.class, "2"));
        jdbc.commit();
        System.out.println(jdbc.qry("SELECT * FROM UT_TEST"));
    }

    public static void main(String[] args) throws Exception {
        new Demo2();
    }
}
