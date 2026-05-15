package velmora.composer.state;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter @Setter
public class CompositionState {

  private List<String> currentNoteNames = new ArrayList<>();
  private List<Long> currentNoteIds = new ArrayList<>();
  private String formulaName;
  private Long editCompositionId;

  public void clear() {
    currentNoteNames.clear();
    currentNoteIds.clear();
    formulaName = null;
    editCompositionId = null;
  }
}
