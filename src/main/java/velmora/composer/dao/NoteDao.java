package velmora.composer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import velmora.composer.pool.SimpleConnectionPool;

/**
 * JDBC DAO, що використовує SimpleConnectionPool
 * Співіснує з Spring Data JPA репозиторіями для демонстрації пулу
 *
 * Потік: Controller -> Service -> NoteDao -> Pool -> Supabase
 */
@Repository
@RequiredArgsConstructor
public class NoteDao {

  private final SimpleConnectionPool pool;

  public List<NoteSummary> findAllSummaries() {
    List<NoteSummary> result = new ArrayList<>();
    String sql = "SELECT id, name, category, note_type, intensity FROM notes ORDER BY name";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        result.add(new NoteSummary(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("note_type"),
            rs.getObject("intensity", Integer.class)
        ));
      }
    } catch (Exception e) {
      throw new RuntimeException("NoteDao query failed", e);
    }
    return result;
  }

  public int countByCategory(String category) {
    String sql = "SELECT COUNT(*) FROM notes WHERE LOWER(category) = LOWER(?)";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, category);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    } catch (Exception e) {
      throw new RuntimeException("NoteDao count failed", e);
    }
  }

  public record NoteSummary(Long id, String name, String category, String type, Integer intensity) {}
}
