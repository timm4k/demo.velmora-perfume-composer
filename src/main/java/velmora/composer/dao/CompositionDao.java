package velmora.composer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Composition;
import velmora.composer.pool.SimpleConnectionPool;

@Repository
@RequiredArgsConstructor
public class CompositionDao {

  private final SimpleConnectionPool pool;

  public Composition createCompositionTransactional(Composition composition) {
    String sql = "INSERT INTO compositions (user_id, perfume_id, name, description, is_public, created_at, updated_at) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    Connection conn = null;
    try {
      conn = pool.borrowConnection();
      conn.setAutoCommit(false);
      try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setObject(1, composition.getUser() != null ? composition.getUser().getId() : null);
        ps.setObject(2, composition.getPerfume() != null ? composition.getPerfume().getId() : null);
        ps.setString(3, composition.getName());
        ps.setString(4, composition.getDescription());
        ps.setBoolean(5, composition.isPublic());
        ps.setTimestamp(6, Timestamp.valueOf(composition.getCreatedAt() != null
            ? composition.getCreatedAt() : LocalDateTime.now()));
        ps.setTimestamp(7, Timestamp.valueOf(composition.getUpdatedAt() != null
            ? composition.getUpdatedAt() : LocalDateTime.now()));
        ps.executeUpdate();
        try (ResultSet keys = ps.getGeneratedKeys()) {
          if (keys.next()) {
            composition.setId(keys.getLong(1));
          }
        }
      }
      conn.commit();
      return composition;
    } catch (Exception e) {
      if (conn != null) {
        try { conn.rollback(); } catch (Exception rb) {
          System.err.println("[TX] Rollback failed: " + rb.getMessage());
        }
      }
      throw new RuntimeException("CompositionDao createCompositionTransactional failed", e);
    } finally {
      if (conn != null) {
        try { conn.setAutoCommit(true); } catch (Exception ignored) {}
        try { conn.close(); } catch (Exception e) {
          System.err.println("[TX] Connection close failed: " + e.getMessage());
        }
      }
    }
  }

  public Composition createComposition(Composition composition) {
    String sql = "INSERT INTO compositions (user_id, perfume_id, name, description, is_public, created_at, updated_at) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setObject(1, composition.getUser() != null ? composition.getUser().getId() : null);
      ps.setObject(2, composition.getPerfume() != null ? composition.getPerfume().getId() : null);
      ps.setString(3, composition.getName());
      ps.setString(4, composition.getDescription());
      ps.setBoolean(5, composition.isPublic());
      ps.setTimestamp(6, Timestamp.valueOf(composition.getCreatedAt() != null
          ? composition.getCreatedAt() : LocalDateTime.now()));
      ps.setTimestamp(7, Timestamp.valueOf(composition.getUpdatedAt() != null
          ? composition.getUpdatedAt() : LocalDateTime.now()));
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          composition.setId(keys.getLong(1));
        }
      }
      return composition;
    } catch (Exception e) {
      throw new RuntimeException("CompositionDao createComposition failed", e);
    }
  }

  public Composition findById(Long id) {
    String sql = "SELECT * FROM compositions WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapComposition(rs);
        }
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException("CompositionDao findById failed", e);
    }
  }

  public List<Composition> findByUserId(Long userId) {
    List<Composition> result = new ArrayList<>();
    String sql = "SELECT * FROM compositions WHERE user_id = ? ORDER BY updated_at DESC";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapComposition(rs));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("CompositionDao findByUserId failed", e);
    }
    return result;
  }

  public void updateComposition(Composition composition) {
    String sql = "UPDATE compositions SET name = ?, description = ?, is_public = ?, updated_at = ? "
        + "WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, composition.getName());
      ps.setString(2, composition.getDescription());
      ps.setBoolean(3, composition.isPublic());
      ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
      ps.setLong(5, composition.getId());
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("CompositionDao updateComposition failed", e);
    }
  }

  public void deleteById(Long id) {
    String sql = "DELETE FROM compositions WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("CompositionDao deleteById failed", e);
    }
  }

  private Composition mapComposition(ResultSet rs) throws Exception {
    Composition comp = new Composition();
    comp.setId(rs.getLong("id"));
    comp.setName(rs.getString("name"));
    comp.setDescription(rs.getString("description"));
    comp.setPublic(rs.getBoolean("is_public"));
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      comp.setCreatedAt(createdAt.toLocalDateTime());
    }
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      comp.setUpdatedAt(updatedAt.toLocalDateTime());
    }
    return comp;
  }
}
