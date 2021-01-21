package org.jing.jdbc.lang;

import org.jing.core.lang.Carrier;
import org.jing.core.lang.ExceptionHandler;
import org.jing.core.lang.JingException;
import org.jing.core.logger.JingLogger;
import org.jing.core.util.StringUtil;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-08-06 <br>
 */
public class Connection {
    private static final JingLogger LOGGER  = JingLogger.getLogger(Connection.class);

    enum Type{
        COMM("COMM"), LONG("LONG");

        private final String typeStr;

        Type (String fontName) {
            this.typeStr = fontName;
        }

        String getValue() {
            return typeStr;
        }

        static Type getType(String type) {
            Type[] types = values();
            for (Type t$: types) {
                if (t$.typeStr.equalsIgnoreCase(type)) {
                    return t$;
                }
            }
            return null;
        }
    }

    private String sign;

    private String url;

    private String name;

    private String password;

    private String driver;

    private Type type;

    private String extra;

    private String schema;

    private Encrypt encrypt;

    private boolean autoCommit = false;

    private Connection() {
    }

    String getSign() {
        return sign;
    }

    String getName() {
        return name;
    }

    String getDriver() {
        return driver;
    }

    String getPassword() {
        return password;
    }

    String getUrl() {
        return url;
    }

    Type getType() {
        return type;
    }

    String getExtra(){
        return extra;
    }

    String getSchema() {
        return schema;
    }

    boolean isAutoCommit() {
        return autoCommit;
    }

    private Connection setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    private Connection setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    private Connection setSign(String sign) {
        this.sign = sign;
        return this;
    }

    private Connection setUrl(String url) {
        this.url = url;
        return this;
    }

    private Connection setName(String name) {
        this.name = name;
        return this;
    }

    private Connection setPassword(String password) {
        this.password = password;
        return this;
    }

    private Connection setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    private Connection setType(String type) {
        this.type = Type.getType(type);
        return this;
    }

    private Connection setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public Encrypt getEncrypt() {
        return encrypt;
    }

    public Connection setEncrypt(Encrypt encrypt) {
        this.encrypt = encrypt;
        return this;
    }

    static Connection createConnection(Carrier connectionNode) throws JingException {
        String sign = connectionNode.getString("sign", "");
        ExceptionHandler.publishIfMatch(StringUtil.isEmpty(sign), "Empty sign of JDBC");
        LOGGER.debug("Record connection: {}", sign);
        return new Connection()
            .setSign(sign)
            .setUrl(connectionNode.getString("url", ""))
            .setName(connectionNode.getString("name", ""))
            .setPassword(connectionNode.getString("password", ""))
            .setDriver(connectionNode.getString("driver", ""))
            .setType(connectionNode.getString("type", ""))
            .setExtra(connectionNode.getString("extra", ""))
            .setSchema(connectionNode.getString("schema", ""))
            .setEncrypt(Encrypt.getEncrypt(connectionNode.getString("encrypt", "")))
            .setAutoCommit("TRUE".equalsIgnoreCase(connectionNode.getString("autoCommit", "FALSE")));
    }
}
