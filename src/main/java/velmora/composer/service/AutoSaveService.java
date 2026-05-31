package velmora.composer.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import velmora.composer.model.Composition;
import velmora.composer.model.CompositionItem;
import velmora.composer.model.CompositionStatus;
import velmora.composer.model.User;
import velmora.composer.repository.CompositionRepository;
import velmora.composer.ui.UserSession;

@Service
@RequiredArgsConstructor
public class AutoSaveService {

  private final CompositionService compositionService;
  private final CompositionRepository compositionRepository;
  private final UserSession userSession;

  private Timer timer;
  private boolean dirty;
  private SaveStatusListener listener;
  private Long editCompositionId;
  private String currentName;
  private String currentDescription;
  private List<CompositionItem> currentItems;

  private static final long DEBOUNCE_MS = 30_000;

  public enum SaveStatus {
    SAVED,
    SAVING,
    UNSAVED
  }

  public interface SaveStatusListener {
    void onStatusChanged(SaveStatus status);
  }

  public void setListener(SaveStatusListener listener) {
    this.listener = listener;
  }

  public void markDirty() {
    dirty = true;
    notifyStatus(SaveStatus.UNSAVED);
    resetTimer();
  }

  public void setCurrentComposition(Long compositionId, String name, String description) {
    this.editCompositionId = compositionId;
    this.currentName = name;
    this.currentDescription = description;
  }

  public void setCurrentName(String name) {
    this.currentName = name;
  }

  public void setCurrentDescription(String description) {
    this.currentDescription = description;
  }

  public void setCurrentItems(List<CompositionItem> items) {
    this.currentItems = items;
  }

  private void resetTimer() {
    if (timer != null) {
      timer.cancel();
    }
    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (dirty) {
          performAutoSave();
        }
      }
    }, DEBOUNCE_MS);
  }

  private void performAutoSave() {
    if (!dirty || userSession.getUserId() == null) return;
    notifyStatus(SaveStatus.SAVING);

    try {
      Composition composition;
      if (editCompositionId != null) {
        composition = compositionRepository.findById(editCompositionId)
            .orElse(new Composition());
      } else {
        composition = new Composition();
      }

      composition.setName(currentName != null ? currentName : "Untitled");
      composition.setDescription(currentDescription != null ? currentDescription : "");
      composition.setUser(User.builder().id(userSession.getUserId()).build());

      if (composition.getStatus() == null) {
        composition.setStatus(CompositionStatus.DRAFT);
      }

      if (currentItems != null && !currentItems.isEmpty()) {
        composition.getItems().clear();
        for (CompositionItem item : currentItems) {
          CompositionItem copy = new CompositionItem();
          copy.setNoteId(item.getNoteId());
          copy.setPercentage(item.getPercentage());
          composition.getItems().add(copy);
        }
      }

      composition = compositionService.saveComposition(composition, "Auto-saved");

      if (editCompositionId == null) {
        editCompositionId = composition.getId();
      }

      dirty = false;
      notifyStatus(SaveStatus.SAVED);
    } catch (Exception e) {
      System.err.println("[AUTOSAVE] Failed: " + e.getMessage());
      notifyStatus(SaveStatus.UNSAVED);
    }
  }

  public void forceSave() {
    if (dirty) {
      if (timer != null) timer.cancel();
      performAutoSave();
    }
  }

  public void clear() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    dirty = false;
    editCompositionId = null;
    currentName = null;
    currentDescription = null;
    currentItems = null;
    notifyStatus(SaveStatus.SAVED);
  }

  public Long getEditCompositionId() {
    return editCompositionId;
  }

  private void notifyStatus(SaveStatus status) {
    if (listener != null) {
      Platform.runLater(() -> listener.onStatusChanged(status));
    }
  }
}