package se.callista.workshop.karate.util.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/** Utility class for producing JMS messages. */
public class DatabaseAccess {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAccess.class);

    protected final String url;
    protected final String username;
    protected final String password;

    /**
     * Construct a queue consumer for a specific queue on a specific broker.
     *
     * @param url PostgreSQL server url
     * @param username database user
     * @param password database password
     */
    public DatabaseAccess(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public List<Map<String, String>> selectAllFrom(String table) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            Connection connection = ConnectionManager.getConnection(url, username, password);
            statement = connection.prepareStatement("SELECT * FROM " + table);
            result = statement.executeQuery();
            return convertToMapList(result);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(result);
            closeStatement(statement);
            ConnectionManager.closeConnection(url);
        }
    }

    public List<Map<String, String>> select(String sql) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            Connection connection = ConnectionManager.getConnection(url, username, password);
            statement = connection.prepareStatement(sql);
            result = statement.executeQuery();
            return convertToMapList(result);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(result);
            closeStatement(statement);
            ConnectionManager.closeConnection(url);
        }
    }

    public int update(String sql) {
        PreparedStatement statement = null;
        try {
            Connection connection = ConnectionManager.getConnection(url, username, password);
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeStatement(statement);
            ConnectionManager.closeConnection(url);
        }
    }

    public void insertInto(String table, Map<String, Object> row) {
        PreparedStatement statement = null;
        try {
            Connection connection = ConnectionManager.getConnection(url, username, password);
            String columnsAndValues = asColumnsAndValues(row);
            statement = connection.prepareStatement("INSERT INTO " + table + " " + columnsAndValues);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeStatement(statement);
            ConnectionManager.closeConnection(url);
        }
    }

    public int deleteAllFrom(String table) {
        PreparedStatement statement = null;
        try {
            Connection connection = ConnectionManager.getConnection(url, username, password);
            statement = connection.prepareStatement("DELETE FROM " + table);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeStatement(statement);
            ConnectionManager.closeConnection(url);
        }
    }

    private static void closeStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("Failed to close statement: ", e);
            }
        }
    }

    private static void closeResultSet(ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                logger.error("Failed to close resultset: ", e);
            }
        }
    }

    static List<Map<String, String>> convertToMapList(ResultSet resultSet) throws SQLException {
        ResultSetMetaData md = resultSet.getMetaData();
        int numCols = md.getColumnCount();
        List<String> colNames =
            IntStream
                .range(0, numCols)
                .mapToObj(
                    i -> {
                        try {
                            return md.getColumnName(i + 1);
                        } catch (SQLException e) {
                            logger.error("Failed to retrieve column name resultset metadata: ", e);
                            return "?";
                        }
                    })
                .toList();
        List<Map<String, String>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, String> row = new HashMap<>();
            colNames.forEach(
                cn -> {
                    try {
                        row.put(cn,
                            resultSet
                                .getObject(cn)
                                .toString());
                    } catch (SQLException e) {
                        logger.error("Failed to retrieve column value: ", e);
                    }
                });
            result.add(row);
        }
        return result;
    }

    static String asColumnsAndValues(Map<String, Object> row) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        boolean firstColumn = true;
        for (Map.Entry<String, Object> column : row.entrySet()) {
            columns.append(firstColumn ? "(" : ", ");
            columns.append(column.getKey());
            values.append(firstColumn ? "(" : ", ");
            if (column.getValue() instanceof String) {
                values.append("'");
                values.append(column.getValue());
                values.append("'");
            } else {
                values.append(column.getValue());
            }
            firstColumn = false;
        }
        columns.append(")");
        values.append(")");
        return columns + " VALUES " + values;
    }
}
