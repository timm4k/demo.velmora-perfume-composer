package velmora.composer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Role;
import velmora.composer.model.User;
import velmora.composer.pool.SimpleConnectionPool;

@Repository
@RequiredArgsConstructor
public class UserDao {

  private final SimpleConnectionPool pool;

  public User createUser(User user) {
    String sql = "INSERT INTO users (nickname, email, password_hash, role, is_confirmed, created_at) "
        + "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, user.getNickname());
      ps.setString(2, user.getEmail());
      ps.setString(3, user.getPassword());
      ps.setString(4, user.getRole() != null ? user.getRole().name() : Role.USER.name());
      ps.setBoolean(5, user.isEnabled());
      ps.setTimestamp(6, user.getCreatedAt() != null
          ? Timestamp.valueOf(user.getCreatedAt())
          : Timestamp.valueOf(LocalDateTime.now()));
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          user.setId(keys.getLong(1));
        }
      }
      return user;
    } catch (Exception e) {
      throw new RuntimeException("UserDao createUser failed", e);
    }
  }

  public User findById(Long id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapUser(rs);
        }
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException("UserDao findById failed", e);
    }
  }

  public User findByEmail(String email) {
    String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapUser(rs);
        }
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException("UserDao findByEmail failed", e);
    }
  }

  public void updateUser(User user) {
    String sql = "UPDATE users SET nickname = ?, email = ?, role = ? WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, user.getNickname());
      ps.setString(2, user.getEmail());
      ps.setString(3, user.getRole() != null ? user.getRole().name() : null);
      ps.setLong(4, user.getId());
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("UserDao updateUser failed", e);
    }
  }

  public void deleteById(Long id) {
    String sql = "DELETE FROM users WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("UserDao deleteById failed", e);
    }
  }

  private User mapUser(ResultSet rs) throws Exception {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setNickname(rs.getString("nickname"));
    user.setEmail(rs.getString("email"));
    user.setPassword(rs.getString("password_hash"));
    String roleStr = rs.getString("role");
    if (roleStr != null) {
      user.setRole(Role.valueOf(roleStr));
    }
    user.setEnabled(rs.getBoolean("is_confirmed"));
    Timestamp ts = rs.getTimestamp("created_at");
    if (ts != null) {
      user.setCreatedAt(ts.toLocalDateTime());
    }
    return user;
  }
}
