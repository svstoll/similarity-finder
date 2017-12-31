package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.dao.DatabaseAccessException;
import ch.svenstoll.similarityfinder.domain.Filter;
import ch.svenstoll.similarityfinder.domain.MaxArticlesException;
import ch.svenstoll.similarityfinder.domain.Medium;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.util.NodeQueryUtils.isVisible;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class FilterControllerTest extends ApplicationTest {
    private FilterConfigsManagerController configsManagerController;
    private Filter filter;
    private ObservableList<Medium> media;
    private ListProperty<Medium> mediumListProperty;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        filter = mock(Filter.class);
        configsManagerController = mock(FilterConfigsManagerController.class);
        media = FXCollections.observableArrayList(new Medium("medium 1"), new Medium("medium 2"));
        mediumListProperty = new SimpleListProperty<>(media);
        given(filter.similarityThresholdProperty()).willReturn(new SimpleDoubleProperty());
        given(filter.mediaProperty()).willReturn(mediumListProperty);
        given(filter.getMedia()).willReturn(media);
        given(filter.fromDateProperty()).willReturn(new SimpleObjectProperty<>());
        given(filter.toDateProperty()).willReturn(new SimpleObjectProperty<>());
        given(filter.titleProperty()).willReturn(new SimpleStringProperty());
        given(filter.minLettersProperty()).willReturn(new SimpleIntegerProperty());
        given(filter.relevantOnlyProperty()).willReturn(new SimpleBooleanProperty());
        given(filter.progressProperty()).willReturn(new SimpleDoubleProperty());

        FilterController filterController = new FilterController(filter, configsManagerController);
        Parent root = filterController.loadFxml();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Before
    public void setUp() {
        robot = new FxRobot();
    }

    @After
    public void cleanUp() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void similarityThresholdField_givenNonDigitEntered_shouldIgnoreInput() {
        // Given:
        TextField similarityThresholdField = robot.lookup("#similarityThresholdField").query();
        String initialText = similarityThresholdField.getText();

        // When:
        robot.clickOn("#similarityThresholdField").doubleClickOn();
        robot.write("abc");
        robot.clickOn("#filterRootPane");

        // Then:
        verifyThat("#similarityThresholdField", hasText(initialText));
    }

    @Test
    public void similarityThresholdField_givenDigitWithinBoundsEntered_shouldAcceptInput() {
        // Given:
        String input = "0.5";

        // When:
        robot.clickOn("#similarityThresholdField").doubleClickOn();
        robot.write(input);
        robot.clickOn("#filterRootPane");

        // Then:
        verifyThat("#similarityThresholdField", hasText(input));
    }

    @Test
    public void similarityThresholdField_givenDigitOutOfBoundsEntered_shouldAdjustInput() {
        // Given:
        String input = "2.0";

        // When:
        robot.clickOn("#similarityThresholdField").doubleClickOn();
        robot.write(input);
        robot.clickOn("#filterRootPane");

        // Then:
        verifyThat("#similarityThresholdField", hasText("1"));
    }

    @Test
    public void selectAllMediaCheckBox_givenAllMediaCheckBoxIsSelected_shouldSelectAllMedia() {
        // When:
        robot.clickOn("#selectAllMediaCheckBox");

        // Then:
        long expectedSize = media.stream().filter(Medium::isSelected).count();
        assertEquals(media.size(), expectedSize);
    }

    @Test
    public void selectAllMediaCheckBox_givenAllMediaCheckBoxIsNotSelected_shouldDeselectAllMedia() {
        // When:
        robot.clickOn("#selectAllMediaCheckBox");
        robot.clickOn("#selectAllMediaCheckBox");

        // Then:
        long expectedSize = media.stream().filter(Medium::isSelected).count();
        assertEquals(0, expectedSize);
    }

    @Test
    public void selectAllMediaCheckBox_givenSomeMediaAreSelected_shouldBeIndeterminate() {
        // When:
        robot.clickOn("#selectAllMediaCheckBox");
        Node firstRowCheckBox = robot.lookup(".list-view .check-box").nth(0).query();
        robot.clickOn(firstRowCheckBox);

        // Then:
        CheckBox cb = robot.lookup("#selectAllMediaCheckBox").query();
        assertEquals(true, cb.isIndeterminate());
    }

    @Test
    public void filterMediaList_givenMediaFilterValueEntered_shouldAdjustMediaList() {
        // Given:
        String filterValue = "1";

        // When:
        robot.clickOn("#mediaFilterField");
        robot.write(filterValue);

        // Then:
        long expectedMediaListSize = media.stream()
                .filter(media -> media.getName().contains(filterValue)).count();
        Set<Node> visibleListCheckBoxes = robot.lookup(".list-view .check-box").queryAll();
        assertEquals(expectedMediaListSize, visibleListCheckBoxes.size());
    }

    @Test
    public void filterMediaList_givenMediaFilterValueDeleted_shouldMakeAllMediaVisible() {
        // When:
        robot.clickOn("#mediaFilterField");
        robot.write("t");
        robot.push(KeyCode.BACK_SPACE);

        // Then:
        Set<Node> visibleListCheckBoxes = robot.lookup(".list-view .check-box").queryAll();
        assertEquals(media.size(), visibleListCheckBoxes.size());
    }

    @Test
    public void mediaListView_givenMediaListUpdated_shouldUpdateMediaListView()
            throws TimeoutException {
        // Given:
        ObservableList<Medium> newMediumList
                = FXCollections.observableArrayList(new Medium("medium"));
        given(filter.getMedia()).willReturn(newMediumList);

        // When:
        mediumListProperty.set(newMediumList);

        // Then:
        waitFor(10, TimeUnit.MILLISECONDS, () -> {
            Set<Node> visibleListCheckBoxes = robot.lookup(".list-view .check-box").queryAll();
            return newMediumList.size() == visibleListCheckBoxes.size();
        });
    }

    @Test
    public void
    openFilterConfigsManager_givenButtonIsClicked_shouldRequestFilterConfigManagerWindow() {
        // When:
        robot.clickOn("#filterConfigsManagerButton");

        // Then:
        verify(configsManagerController, times(1)).openInNewStage();
    }

    @Test
    public void resetFilterValues_givenButtonIsClicked_shouldRequestFilterReset() {
        // When:
        robot.clickOn("#resetButton");

        // Then:
        verify(filter, times(1)).reset();
    }

    @Test
    public void
    findArticlesWithSimilarContent_givenDatabaseAccessExceptionOccurs_shouldShowAlert() {
        // Given:
        long timeout = 200;
        given(filter.findArticlesWithSimilarContent()).willAnswer(invocation -> {
            Thread.sleep(timeout);
            throw new DatabaseAccessException();
        });

        // When:
        robot.clickOn("#filterButton");

        // Then:
        verifyBusyFilterState();
        robot.sleep(timeout + 50);

        verifyThat(".dialog-pane", isVisible());
        verifyNonBusyFilterState();
    }

    @Test
    public void
    findArticlesWithSimilarContent_givenMaxContributionsExceptionOccurs_shouldShowAlert() {
        // Given:
        long timeout = 200;
        given(filter.findArticlesWithSimilarContent()).willAnswer(invocation -> {
            Thread.sleep(timeout);
            throw new MaxArticlesException(1000, 1100);
        });

        // When:
        robot.clickOn("#filterButton");

        // Then:
        verifyBusyFilterState();
        robot.sleep(timeout + 50);

        verifyThat(".dialog-pane", isVisible());
        verifyNonBusyFilterState();
    }

    @Test
    public void
    findArticlesWithSimilarContent_givenResultSetIsEmpty_shouldShowAlert() {
        // Given:
        long timeout = 100;
        given(filter.findArticlesWithSimilarContent()).willAnswer(invocation -> {
            Thread.sleep(timeout);
            return Collections.emptyList();
        });

        // When:
        robot.clickOn("#filterButton");

        // Then:
        verifyBusyFilterState();
        robot.sleep(timeout + 50);

        verifyThat(".dialog-pane", isVisible());
        verifyNonBusyFilterState();
    }

    @Test
    public void
    findArticlesWithSimilarContent_givenRequestIsStillPending_shouldAbortPreviousRequest() {
        // Given:
        long timeout = 1000;
        when(filter.findArticlesWithSimilarContent()).thenAnswer(invocation -> {
            Thread.sleep(timeout);
            return Collections.emptyList();
        });

        // When:
        robot.clickOn("#filterButton");
        robot.clickOn("#filterButton");

        // Then:
        verifyNonBusyFilterState();
    }

    private void verifyBusyFilterState() {
        verifyThat("#progressIndicator", isVisible());
        verifyThat("#filterButton", hasText("Cancel"));
    }

    private void verifyNonBusyFilterState() {
        verifyThat("#progressIndicator", isInvisible());
        verifyThat("#filterButton", hasText("Search"));
    }
}
