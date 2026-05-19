package velmora.composer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.pool.SimpleConnectionPool;

@Repository
@RequiredArgsConstructor
public class NoteDao {

  private final SimpleConnectionPool pool;

  public Note createNote(Note note) {
    String sql = "INSERT INTO notes (name, category, note_type, intensity, description, color_code) "
        + "VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, note.getName());
      ps.setString(2, note.getCategory());
      ps.setString(3, note.getType() != null ? note.getType().name() : null);
      if (note.getIntensity() != null) {
        ps.setInt(4, note.getIntensity());
      } else {
        ps.setNull(4, java.sql.Types.INTEGER);
      }
      ps.setString(5, note.getDescription());
      ps.setString(6, note.getColorCode());
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          note.setId(keys.getLong(1));
        }
      }
      return note;
    } catch (Exception e) {
      throw new RuntimeException("NoteDao createNote failed", e);
    }
  }

  public Note findById(Long id) {
    String sql = "SELECT * FROM notes WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapNote(rs);
        }
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException("NoteDao findById failed", e);
    }
  }

  public List<Note> findAll() {
    List<Note> result = new ArrayList<>();
    String sql = "SELECT * FROM notes ORDER BY name";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        result.add(mapNote(rs));
      }
    } catch (Exception e) {
      throw new RuntimeException("NoteDao findAll failed", e);
    }
    return result;
  }

  public void updateNote(Note note) {
    String sql = "UPDATE notes SET name = ?, category = ?, note_type = ?, intensity = ?, "
        + "description = ?, color_code = ? WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, note.getName());
      ps.setString(2, note.getCategory());
      ps.setString(3, note.getType() != null ? note.getType().name() : null);
      if (note.getIntensity() != null) {
        ps.setInt(4, note.getIntensity());
      } else {
        ps.setNull(4, java.sql.Types.INTEGER);
      }
      ps.setString(5, note.getDescription());
      ps.setString(6, note.getColorCode());
      ps.setLong(7, note.getId());
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("NoteDao updateNote failed", e);
    }
  }

  public void deleteById(Long id) {
    String sql = "DELETE FROM notes WHERE id = ?";
    try (Connection conn = pool.borrowConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException("NoteDao deleteById failed", e);
    }
  }

  private Note mapNote(ResultSet rs) throws Exception {
    Note note = new Note();
    note.setId(rs.getLong("id"));
    note.setName(rs.getString("name"));
    note.setCategory(rs.getString("category"));
    String typeStr = rs.getString("note_type");
    if (typeStr != null) {
      note.setType(NoteType.valueOf(typeStr));
    }
    note.setIntensity(rs.getObject("intensity", Integer.class));
    note.setDescription(rs.getString("description"));
    note.setColorCode(rs.getString("color_code"));
    return note;
  }

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
