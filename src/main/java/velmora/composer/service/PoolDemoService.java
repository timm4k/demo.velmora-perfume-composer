package velmora.composer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.dao.NoteDao;
import velmora.composer.pool.SimpleConnectionPool;

/**
 * Демонструє JDBC доступ через pool:
 *   Service -> NoteDao (JDBC) -> SimpleConnectionPool -> Supabase
 *
 * Співіснує з Spring Data JPA репозиторіями основної логіки застосунку
 */
@Service
@RequiredArgsConstructor
public class PoolDemoService {

  private final NoteDao noteDao;
  private final SimpleConnectionPool pool;

  public List<NoteDao.NoteSummary> fetchAllNotes() {
    return noteDao.findAllSummaries();
  }

  public int countByCategory(String category) {
    return noteDao.countByCategory(category);
  }

  /**
   * Прогрів пулу — виконує простий запит, щоб переконатись,
   * що з'єднання працюють і пул функціонує.
   */
  public void warmUp() {
    pool.getAvailableCount();
    System.out.println("[DEMO] Pool warmed up: " + getStatus());
  }

  /**
   * Імітує навантаження — виконує кілька запитів через пул.
   */
  public void simulateLoad() {
    System.out.println("[DEMO] Starting load simulation");
    for (int i = 0; i < 3; i++) {
      noteDao.findAllSummaries();
      System.out.println("[DEMO] Query " + (i + 1) + " done, pool state: " + getStatus());
    }
  }

  public PoolStatus getStatus() {
    return new PoolStatus(
        pool.getAvailableCount(),
        pool.getUsedCount(),
        pool.getMaxSize()
    );
  }

  public record PoolStatus(int available, int used, int maxSize) {}
}
