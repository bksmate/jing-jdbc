package test;

import org.jing.core.lang.JingException;
import org.jing.core.lang.Pair2;
import org.jing.jdbc.lang.JingJDBC;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-08-07 <br>
 */
public class TestThread {
    private static int temp = 0;

    public TestThread() {
    }

    public void run() throws Exception {
        for (int i$ = 0; i$ < 10; i$++) {
            JingJDBC oracle = JingJDBC.getJDBC("oracle");
            oracle.qry("SELECT 1 FROM DUAL WHERE 1 - ? = 0", new Pair2<Class<?>, Object>(Integer.class, temp));
            temp = 1 - temp;
        }
    }
}
