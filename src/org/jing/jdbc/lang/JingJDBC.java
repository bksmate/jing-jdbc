package org.jing.jdbc.lang;

import org.jing.core.logger.JingLogger;
import org.jing.core.util.GenericUtil;
import org.jing.core.util.StringUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-08-06 <br>
 */
@SuppressWarnings({ "WeakerAccess", "unused", "SwitchStatementWithTooFewBranches", "Duplicates", "UnusedReturnValue" })
public class JingJDBC {
    private static final JingLogger LOGGER = JingLogger.getLogger(JingJDBC.class);

    private static ThreadLocal<HashMap<String, JingJDBC>> connectionMap = new ThreadLocal<>();

    private volatile Connection connection = null;

    private org.jing.jdbc.lang.Connection connectionDTO;

    public static JingJDBC getJDBC(String sign) throws JingJDBCException {
        if (null == connectionMap.get()) {
            connectionMap.set(new HashMap<String, JingJDBC>());
        }
        if (!connectionMap.get().containsKey(sign)) {
            connectionMap.get().put(sign, createJDBC(sign));
        }
        return connectionMap.get().get(sign);
    }

    private JingJDBC(org.jing.jdbc.lang.Connection connectionDTO) {
        this.connectionDTO = connectionDTO;
        // this.openNewSession();
    }

    @Override protected void finalize() throws Throwable {
        if (null != connectionMap.get()) {
            for (Map.Entry<String, JingJDBC> entry : connectionMap.get().entrySet()) {
                entry.getValue().close();
            }
        }
        super.finalize();
    }

    private static JingJDBC createJDBC(String sign) throws JingJDBCException {
        org.jing.jdbc.lang.Connection conn = JDBCInit.getConnectionBySign(sign);
        if (null == conn) {
            throw new JingJDBCException("Invalid sign : {}", sign);
        }
        return new JingJDBC(conn);
    }

    private synchronized void validate() throws JingJDBCException {
        if (null == connection) {
            openNewSession();
        }
    }

    public void open() throws JingJDBCException {
        try {
            if (null == connection || connection.isClosed()) {
                openNewSession();
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
    }

    public void openNewSession() throws JingJDBCException {
        try {
            if (null != connection && !connection.isClosed()) {
                close();
            }
            org.jing.jdbc.lang.Connection.Type type = connectionDTO.getType();
            Class.forName(connectionDTO.getDriver());

            String url = connectionDTO.getUrl();
            String name = connectionDTO.getName();
            String password = connectionDTO.getPassword();
            String extra = connectionDTO.getExtra();
            Encrypt encrypt = connectionDTO.getEncrypt();

            if (null != encrypt) {
                switch (encrypt) {
                    case X16:
                        password = Security.decryptX16(password);
                        break;
                    default: break;
                }
            }

            if (org.jing.jdbc.lang.Connection.Type.COMM == type) {
                connection = DriverManager.getConnection(url, name, password);
            }
            else if (org.jing.jdbc.lang.Connection.Type.LONG == type) {
                connection = DriverManager.getConnection(url + "?user=" + name + "&password=" + password + "&" + extra);
            }
            else {
                throw new JingJDBCException("Unknown Connection type");
            }
            connection.setAutoCommit(connectionDTO.isAutoCommit());
            String schema = connectionDTO.getSchema();
            if (StringUtil.isNotEmpty(schema)) {
                LOGGER.imp("[Session: {}] Change current schema to {}", connection.hashCode(), schema);
                execute("ALTER SESSION SET CURRENT_SCHEMA = " + schema);
            }
            LOGGER.imp("[Session: {}] Success to connect [sign: {}] with [model: {}]", connection.hashCode(),
                connectionDTO.getSign(), type.getValue());
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
    }

    public void setAutoCommit(boolean autoCommit) throws JingJDBCException {
        try {
            if (!connection.isClosed()) {
                LOGGER.sqlWithHash(connection, "Set autoCommit: {}", autoCommit);
                connection.setAutoCommit(autoCommit);
                return;
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
        throw new JingJDBCException("Connection closed");
    }

    public void commit() throws JingJDBCException {
        try {
            if (!connection.isClosed()) {
                LOGGER.sqlWithHash(connection, "Commit.");
                connection.commit();
                return;
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
        throw new JingJDBCException("Connection closed");
    }

    public void rollback() throws JingJDBCException {
        try {
            if (!connection.isClosed()) {
                LOGGER.sqlWithHash(connection, "Rollback.");
                connection.rollback();
                return;
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
        throw new JingJDBCException("Connection closed");
    }

    public void close() throws JingJDBCException {
        try {
            if (!connection.isClosed()) {
                LOGGER.sqlWithHash(connection, "Close.");
                connection.close();
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
    }

    public void release() throws JingJDBCException {
        try {
            if (!connection.isClosed()) {
                LOGGER.sqlWithHash(connection, "Release.");
                connection.commit();
                connection.close();
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
    }

    /* Query */
    public ArrayList<HashMap<String, String>> qryWithList(boolean recordSql, String sql, List<Object> parameters) throws JingJDBCException {
        validate();
        ArrayList<HashMap<String, String>> retList = null;
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            String[] colNames = new String[count];
            for (int i$ = 0; i$ < count; i$++) {
                colNames[i$] = rsmd.getColumnName(i$ + 1);
            }
            while (rs.next()) {
                HashMap<String, String> row = new HashMap<>();
                String colName;
                for (int i$ = 0; i$ < count; i$++) {
                    colName = colNames[i$];
                    row.put(colName, StringUtil.ifEmpty(rs.getString(colName)).trim());
                }
                if (null == retList) {
                    retList = new ArrayList<>();
                }
                retList.add(row);
            }
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows selected.", GenericUtil.countList(retList));
            }
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Query. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Query. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
        return retList;
    }

    public ArrayList<HashMap<String, String>> qryWithList(String sql, List<Object> parameters) throws JingJDBCException {
        return qryWithList(true, sql, parameters);
    }

    public ArrayList<HashMap<String, String>> qry(boolean recordSql, String sql, Object... parameters) throws JingJDBCException {
        validate();
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            String[] colNames = new String[count];
            for (int i$ = 0; i$ < count; i$++) {
                colNames[i$] = rsmd.getColumnName(i$ + 1);
            }
            ArrayList<HashMap<String, String>> retList = null;
            while (rs.next()) {
                HashMap<String, String> row = new HashMap<>();
                String colName;
                for (int i$ = 0; i$ < count; i$++) {
                    colName = colNames[i$];
                    row.put(colName, StringUtil.ifEmpty(rs.getString(colName)).trim());
                }
                if (null == retList) {
                    retList = new ArrayList<>();
                }
                retList.add(row);
            }
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows selected.", GenericUtil.countList(retList));
            }
            return retList;
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Query. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Query. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
    }

    public ArrayList<HashMap<String, String>> qry(String sql, Object... parameters) throws JingJDBCException {
        return qry(true, sql, parameters);
    }

    public ArrayList<HashMap<String, String>> qry(String sql) throws JingJDBCException {
        return qry(true, sql);
    }

    /* Update */
    public int update(boolean recordSql, String sql, List<Object> parameters) throws JingJDBCException {
        validate();
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            int resInt = ps.executeUpdate();
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows affected.", resInt);
            }
            return resInt;
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
    }

    public int update(String sql, List<Object> parameters) throws JingJDBCException {
        return update(true, sql, parameters);
    }

    public int update(boolean recordSql, String sql, Object... parameters) throws JingJDBCException {
        validate();
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            int resInt = ps.executeUpdate();
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows affected.", resInt);
            }
            return resInt;
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
    }

    public int update(String sql, Object... parameters) throws JingJDBCException {
        return update(true, sql, parameters);
    }

    public int update(String sql) throws JingJDBCException {
        return update(true, sql);
    }

    /* Batch Update */
    public long batchUpdate(String sql, List<Object[]> parameters) throws JingJDBCException {
        validate();
        long resLong = 0;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            LOGGER.sqlWithHash(connection, sql, "BATCH_MOD");
            int size = null == parameters ? 0 : parameters.size();
            HashMap<String, String> row;
            Object[] parameter;
            for (int i$ = 0; i$ < size; i$++) {
                parameter = parameters.get(i$);
                setParameters(ps, false, parameter);
                ps.addBatch();
                if (i$ % 999 == 0) {
                    resLong += sumAffectedRow(ps.executeBatch());
                    ps.clearBatch();
                }
            }
            resLong += sumAffectedRow(ps.executeBatch());
            LOGGER.sqlWithHash(connection, "{} rows affected.", resLong);
        }
        catch (Exception e) {
            throw new JingJDBCException(e, "[Session: {}] Failed to Batch Update. [sql: {}]", connection.hashCode(), sql);
        }
        return resLong;
    }

    /* Execute */
    public void execute(String sql) throws JingJDBCException {
        validate();
        try (
            Statement statement = connection.createStatement()
            ) {
            LOGGER.sqlWithHash(connection, "Execute {}", sql);
            statement.execute(sql);
        }
        catch (Exception e) {
            throw new JingJDBCException(e, "[session: {}] Failed to Update. [sql: {}][{}]", connection.hashCode(), sql);
        }
    }

    private String setParameters(PreparedStatement ps, boolean recordSql, Object... parameters) throws JingJDBCException {
        StringBuilder stbr = new StringBuilder();
        try {
            if (null != parameters) {
                int index = 0;
                String tempStr;
                for (Object parameter : parameters) {
                    tempStr = null;
                    if (recordSql && index != 0) {
                        stbr.append(", ");
                    }
                    if (recordSql) {
                        stbr.append(tempStr = StringUtil.parseString(parameter));
                    }
                    if (parameter instanceof Integer) {
                        ps.setInt(++index, (Integer) parameter);
                    }
                    else if (parameter instanceof Long) {
                        ps.setLong(++index, (Long) parameter);
                    }
                    else if (parameter instanceof Float) {
                        ps.setFloat(++index, (Float) parameter);
                    }
                    else if (parameter instanceof Double) {
                        ps.setDouble(++index, (Double) parameter);
                    }
                    else {
                        ps.setString(++index, StringUtil.ifEmpty(tempStr, StringUtil.parseString(parameter)));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
        return stbr.toString();
    }

    private String setParameters(PreparedStatement ps, boolean recordSql, List<Object> parameters) throws JingJDBCException {
        StringBuilder stbr = new StringBuilder();
        try {
            if (null != parameters) {
                int index = 0;
                String tempStr;
                for (Object parameter : parameters) {
                    tempStr = null;
                    if (recordSql && index != 0) {
                        stbr.append(", ");
                    }
                    if (recordSql) {
                        stbr.append(tempStr = StringUtil.parseString(parameter));
                    }
                    if (parameter instanceof Integer) {
                        ps.setInt(++index, (Integer) parameter);
                    }
                    else if (parameter instanceof Long) {
                        ps.setLong(++index, (Long) parameter);
                    }
                    else if (parameter instanceof Float) {
                        ps.setFloat(++index, (Float) parameter);
                    }
                    else if (parameter instanceof Double) {
                        ps.setDouble(++index, (Double) parameter);
                    }
                    else {
                        ps.setString(++index, StringUtil.ifEmpty(tempStr, StringUtil.parseString(parameter)));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new JingJDBCException(e);
        }
        return stbr.toString();
    }

    private long sumAffectedRow(int[] affectedRows) {
        int length = null == affectedRows ? 0 : affectedRows.length;
        long sum = 0;
        for (int i$ = 0; i$ < length; i$++) {
            sum += affectedRows[i$];
        }
        return sum;
    }

    /* Update */
    public String updateAndGetGeneratedKeys(boolean recordSql, String sql, Object... parameters) throws JingJDBCException {
        validate();
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ){
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            ArrayList<HashMap<String, String>> retList = null;
            int resInt = ps.executeUpdate();
            String resString = null;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                resString = rs.getString(1);
            }
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows affected, generated key: [{}]", resInt, resString);
            }
            return resString;
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
    }

    public String updateAndGetGeneratedKeys(boolean recordSql, String sql, List<Object> parameters) throws JingJDBCException {
        validate();
        String parameterString = null;
        try (
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
            parameterString = setParameters(ps, recordSql, parameters);
            if (recordSql) {
                if (StringUtil.isEmpty(parameterString)) {
                    LOGGER.sqlWithHash(connection, sql);
                }
                else {
                    LOGGER.sqlWithHash(connection, sql, parameterString);
                }
            }
            ArrayList<HashMap<String, String>> retList = null;
            int resInt = ps.executeUpdate();
            String resString = null;
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                resString = rs.getString(1);
            }
            if (recordSql) {
                LOGGER.sqlWithHash(connection, "{} rows affected, generated key: [{}]", resInt, resString);
            }
            return resString;
        }
        catch (Exception e) {
            if (StringUtil.isEmpty(parameterString)) {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}]", connection.hashCode(), sql);
            }
            else {
                throw new JingJDBCException(e, "[Session: {}] Failed to Update. [sql: {}][{}]", connection.hashCode(), sql, parameterString);
            }
        }
    }

    public String updateAndGetGeneratedKeys(String sql, List<Object> parameters) throws JingJDBCException {
        return updateAndGetGeneratedKeys(true, sql, parameters);
    }

    public String updateAndGetGeneratedKeys(String sql, Object... parameters) throws JingJDBCException {
        return updateAndGetGeneratedKeys(true, sql, parameters);
    }

    public String updateAndGetGeneratedKeys(String sql) throws JingJDBCException {
        return updateAndGetGeneratedKeys(true, sql);
    }
}
