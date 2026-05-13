package velmora.composer.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Легковаговий JDBC connection pool для демонстрації
 *
 * Керує фіксованим набором з'єднань, щоб уникнути повторюваних
 * викликів DriverManager.getConnection(). Реалізує простий
 * borrow/return життєвий цикл з налаштовуваним максимальним розміром
 *
 * Архітектура:
 *   UI -> Service -> DAO -> SimpleConnectionPool -> Supabase (Postgres)
 */
public class SimpleConnectionPool {

  private final List<Connection> available;
  private final List<Connection> used;
  private final String url;
  private final String user;
  private final String password;
  private final int maxSize;
  private final int initSize;

  public SimpleConnectionPool(String url, String user, String password,
                              int maxSize, int initSize) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.maxSize = maxSize;
    this.initSize = Math.min(initSize, maxSize);
    this.available = new ArrayList<>();
    this.used = new ArrayList<>();
    initialize();
  }

  public SimpleConnectionPool(String url, String user, String password) {
    this(url, user, password, 10, 2);
  }

  private void initialize() {
    try {
      for (int i = 0; i < initSize; i++) {
        available.add(DriverManager.getConnection(url, user, password));
      }
      log("initialized " + initSize + " connections, " + stats());
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize connection pool", e);
    }
  }

  public synchronized Connection getConnection() throws SQLException {
    while (!available.isEmpty()) {
      Connection c = available.remove(available.size() - 1);
      if (!c.isClosed() && c.isValid(2)) {
        used.add(c);
        log("connection borrowed, " + stats());
        return c;
      }
    }

    if (used.size() < maxSize) {
      Connection c = DriverManager.getConnection(url, user, password);
      used.add(c);
      log("new connection created, " + stats());
      return c;
    }

    throw new SQLException("Connection pool exhausted (max " + maxSize + ")");
  }

  public Connection borrowConnection() throws SQLException {
    return new PooledConnection(getConnection(), this);
  }

  public synchronized void releaseConnection(Connection conn) {
    if (conn == null) return;
    if (!used.contains(conn)) return;
    used.remove(conn);
    try {
      if (!conn.isClosed()) {
        available.add(conn);
        log("connection returned, " + stats());
      }
    } catch (SQLException ignored) {
      log("failed to return connection: " + ignored.getMessage());
    }
  }

  public synchronized void closeAll() {
    log("closing all connections, " + stats());
    for (Connection c : available) {
      try { c.close(); } catch (SQLException ignored) {}
    }
    for (Connection c : used) {
      try { c.close(); } catch (SQLException ignored) {}
    }
    available.clear();
    used.clear();
  }

  public synchronized String stats() {
    return "available=" + available.size() + ", used=" + used.size() + ", max=" + maxSize;
  }

  public int getAvailableCount() { return available.size(); }
  public int getUsedCount() { return used.size(); }
  public int getMaxSize() { return maxSize; }

  private void log(String msg) {
    System.out.println("[POOL] " + msg);
  }
}
