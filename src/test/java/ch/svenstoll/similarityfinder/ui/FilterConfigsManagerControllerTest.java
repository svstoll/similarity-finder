package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.FilterConfig;
import ch.svenstoll.similarityfinder.domain.FilterConfigsManager;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

public class FilterConfigsManagerControllerTest extends ApplicationTest {
    private FilterConfigsManager configsManager;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        configsManager = mock(FilterConfigsManager.class);
        FilterConfig c1 = new FilterConfig("test config 1", LocalDateTime.now(), null);
        FilterConfig c2 = new FilterConfig("test config 2", LocalDateTime.now(), null);
        ObservableList<FilterConfig> mockedConfigs
                = FXCollections.observableList(Arrays.asList(c1, c2));
        given(configsManager.getFilterConfigs()).willReturn(mockedConfigs);
        given(configsManager.filterConfigsProperty())
                .willReturn(new SimpleListProperty<>(mockedConfigs));

        FilterConfigsManagerController configManagerController
                = new FilterConfigsManagerController(configsManager);
        Parent root = configManagerController.loadFxml();
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
    public void addButtonState_givenNameIsEntered_shouldBeEnabled() {
        // When:
        robot.clickOn("#configNameField");
        robot.write("test");

        // Then:
        verifyThat("#addButton", isEnabled());
    }

    @Test
    public void loadAndEditButtonsState_givenConfigIsSelected_shouldBeEnabled() {
        // Given:
        Node firstRow = robot.lookup(".table-row-cell").nth(0).query();

        // When:
        robot.clickOn(firstRow);

        // Then:
        verifyThat("#loadButton", isEnabled());
        verifyThat("#overwriteButton", isEnabled());
        verifyThat("#deleteButton", isEnabled());
    }

    @Test
    public void addFilterConfig_givenNameIsEnteredAndButtonIsClicked_shouldAddConfig() {
        // Given:
        String name = "test";

        // When:
        robot.clickOn("#configNameField");
        robot.write(name);
        robot.clickOn("#addButton");

        // Then:
        verify(configsManager, times(1)).addFilterConfig(name);
        verifyThat("#addButton", isDisabled());
        verifyThat("#configNameField", hasText(""));
    }

    @Test
    public void addFilterConfig_givenNameIsEnteredAndButtonShortcutIsPressed_shouldAddConfig() {
        // Given:
        String name = "test";

        // When:
        robot.clickOn("#configNameField");
        robot.write(name);
        robot.push(KeyCode.ENTER);

        // Then:
        verify(configsManager, times(1)).addFilterConfig(name);
        verifyThat("#addButton", isDisabled());
        verifyThat("#configNameField", hasText(""));
    }

    @Test
    public void overwriteFilterConfig_givenConfigIsSelected_shouldOverwriteConfig() {
        // Given:
        Node firstRow = robot.lookup(".table-row-cell").nth(0).query();

        // When:
        robot.clickOn(firstRow);
        robot.clickOn("#overwriteButton");
        robot.push(KeyCode.ENTER);

        // Then:
        verify(configsManager, times(1)).overwriteFilterConfig(any());
    }

    @Test
    public void deleteFilterConfig_givenConfigIsSelected_shouldDeleteConfig() {
        // Given:
        Node firstRow = robot.lookup(".table-row-cell").nth(0).query();

        // When:
        robot.clickOn(firstRow);
        robot.clickOn("#deleteButton");
        robot.push(KeyCode.ENTER);

        // Then:
        verify(configsManager, times(1)).removeFilterConfig(any());
    }

    @Test
    public void loadFilterConfig_givenConfigIsSelected_shouldLoadConfig() {
        // Given:
        Node firstRow = robot.lookup(".table-row-cell").nth(0).query();

        // When:
        robot.clickOn(firstRow);
        robot.clickOn("#loadButton");

        // Then:
        verify(configsManager, times(1)).loadFilterConfig(any());
    }

    @Test
    public void closeFilterConfigsManagerStage_givenShortcutIsPressed_shouldCloseStage() {
        // When:
        robot.push(KeyCode.ESCAPE);

        // Then:
        assertEquals(0, listWindows().size());
    }
}
