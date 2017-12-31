package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Selectable;
import ch.svenstoll.similarityfinder.domain.Medium;
import javafx.scene.control.CheckBox;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CheckBoxUtilTest {
    @Rule
    public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

    private CheckBox checkBox;
    private List<Selectable> selectableItems;

    @Before
    public void setUp() {
        checkBox = new CheckBox();
        selectableItems = createListOfSelectableObjects();
    }

    @Test
    public void adjustCheckBoxSelectionState_givenNoItemSelected_shouldHaveNoSelectionMark() {
        // Given:
        selectableItems.forEach(selectable -> selectable.setSelected(false));

        // When:
        CheckBoxUtil.adjustCheckBoxSelectionState(checkBox, selectableItems);

        // Then:
        assertEquals(false, checkBox.isSelected());
        assertEquals(false, checkBox.isIndeterminate());
    }

    @Test
    public void adjustCheckBoxSelectionState_givenOneItemSelected_shouldHaveIndeterminateMark() {
        // Given:
        selectableItems.forEach(selectable -> selectable.setSelected(false));
        selectableItems.get(0).setSelected(true);

        // When:
        CheckBoxUtil.adjustCheckBoxSelectionState(checkBox, selectableItems);

        // Then:
        assertEquals(false, checkBox.isSelected());
        assertEquals(true, checkBox.isIndeterminate());
    }

    @Test
    public void adjustCheckBoxSelectionState_givenAllItemsSelected_shouldHaveSelectionMark() {
        // Given:
        selectableItems.forEach(selectable -> selectable.setSelected(true));

        // When:
        CheckBoxUtil.adjustCheckBoxSelectionState(checkBox, selectableItems);

        // Then:
        assertEquals(true, checkBox.isSelected());
        assertEquals(false, checkBox.isIndeterminate());
    }

    private List<Selectable> createListOfSelectableObjects() {
        Selectable s1 = new Medium("1");
        Selectable s2 = new Medium("2");
        Selectable s3 = new Medium("3");
        s1.setSelected(true);
        s2.setSelected(true);
        s3.setSelected(true);

        return Arrays.asList(s1, s2, s3);
    }
}
