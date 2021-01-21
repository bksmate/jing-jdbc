package org.jing.jdbc.lang;

import org.jing.core.lang.Carrier;
import org.jing.core.lang.JingException;
import org.jing.core.lang.itf.JInit;
import org.jing.core.logger.JingLogger;
import org.jing.core.util.CarrierUtil;
import org.jing.core.util.FileUtil;

import java.util.HashMap;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-08-06 <br>
 */
public class JDBCInit implements JInit {
    private static final JingLogger LOGGER = JingLogger.getLogger(JDBCInit.class);

    private static Carrier parameters = null;

    private static final HashMap<String, Connection> CONNECTIONS = new HashMap<String, Connection>();

    @Override
    public void init(Carrier params) throws JingException {
        parameters = params;
        if (null == parameters) {
            LOGGER.imp("Empty parameter for JService Initialize");
        } else {
            CONNECTIONS.clear();
            Carrier carrier = CarrierUtil.string2Carrier(FileUtil.readFile(FileUtil.buildPathWithHome(parameters.getString("path", "")), "UTF-8"));
            int count = carrier.getCount("connection");
            Connection conn;
            for (int i$ = 0; i$ < count; i$++) {
                conn = Connection.createConnection(carrier.getCarrier("connection", i$));
                CONNECTIONS.put(conn.getSign(), conn);
            }
        }
    }

    static Connection getConnectionBySign(String sign) {
        synchronized (JDBCInit.class) {
            return CONNECTIONS.get(sign);
        }
    }

}
