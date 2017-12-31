package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Settings;
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

import java.text.DecimalFormat;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;

public class SettingsControllerTest extends ApplicationTest {
    private Settings settings;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        settings = mock(Settings.class);
        given(settings.getDbAddress()).willReturn("test");
        given(settings.getDbUser()).willReturn("postgres");
        given(settings.getDbPassword()).willReturn("admin");
        given(settings.getMaxArticles()).willReturn(1000);

        SettingsController settingsController = new SettingsController(settings);
        Parent root = settingsController.loadFxml();
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
    public void actionButtonState_givenDbAddressFieldEdited_shouldBeEnabled() {
        // When:
        robot.clickOn("#dbAddressField");
        robot.write("a");

        // Then:
        verifyThat("#saveButton", isEnabled());
        verifyThat("#revertButton", isEnabled());
    }

    @Test
    public void actionButtonState_givenDbUserFieldEdited_shouldBeEnabled() {
        // When:
        robot.clickOn("#dbUserField");
        robot.write("a");

        // Then:
        verifyThat("#saveButton", isEnabled());
        verifyThat("#revertButton", isEnabled());
    }

    @Test
    public void actionButtonState_givenDbPasswordFieldEdited_shouldBeEnabled() {
        // When:
        robot.clickOn("#dbPasswordField");
        robot.write("a");

        // Then:
        verifyThat("#saveButton", isEnabled());
        verifyThat("#revertButton", isEnabled());
    }

    @Test
    public void actionButtonState_givenMaxContributionsFieldEdited_shouldBeEnabled() {
        // When:
        robot.doubleClickOn("#maxContributionsField");
        robot.write("500");

        // Then:
        verifyThat("#saveButton", isEnabled());
        verifyThat("#revertButton", isEnabled());
    }

    @Test
    public void saveSettings_givenSettingsHaveBeenEdited_shouldSaveSettings() {
        // When:
        robot.clickOn("#dbAddressField");
        robot.write("a");
        robot.clickOn("#dbUserField");
        robot.write("b");
        robot.clickOn("#dbPasswordField");
        robot.write("c");
        robot.clickOn("#maxContributionsField");
        robot.doubleClickOn("#maxContributionsField");
        robot.write("500");
        robot.clickOn("#saveButton");

        // Then:
        verify(settings, times(1)).setDbAddress(anyString());
        verify(settings, times(1)).setDbUser(anyString());
        verify(settings, times(1)).setDbPassword(anyString());
        verify(settings, times(1)).setMaxArticles(anyInt());
        verify(settings, times(1)).saveSettings();
    }

    @Test
    public void revertSettings_givenSettingsHaveBeenEdited_shouldInputFields() {
        // When:
        robot.clickOn("#dbAddressField");
        robot.write("a");
        robot.clickOn("#dbUserField");
        robot.write("b");
        robot.clickOn("#dbPasswordField");
        robot.write("c");
        robot.clickOn("#maxContributionsField");
        robot.doubleClickOn("#maxContributionsField");
        robot.write("500");
        robot.clickOn("#revertButton");

        // Then:
        verifyThat("#dbAddressField", hasText(settings.getDbAddress()));
        verifyThat("#dbUserField", hasText(settings.getDbUser()));
        verifyThat("#dbPasswordField", hasText(settings.getDbPassword()));
        verifyThat("#maxContributionsField", hasText(
                new DecimalFormat().format(settings.getMaxArticles())));
    }

    @Test
    public void maxContributionsField_givenLettersEntered_shouldIgnoreInput() {
        // Given:
        DecimalTextField maxContributionsField = lookup("#maxContributionsField").query();
        String before = maxContributionsField.getText();

        // When:
        robot.clickOn("#maxContributionsField");
        robot.write("abc");
        robot.push(KeyCode.TAB);

        // Then:
        verifyThat("#maxContributionsField", hasText(before));
    }

    @Test
    public void maxContributionsField_givenNumberEntered_shouldAcceptInput() {
        // Given:
        String input = "1500";

        // When:
        robot.clickOn("#maxContributionsField");
        robot.doubleClickOn("#maxContributionsField");
        robot.write(input);

        // Then:
        verifyThat("#maxContributionsField", hasText(input));
    }

    @Test
    public void closeSettingsStage_givenShortcutIsPressed_shouldCloseStage() {
        // When:
        robot.push(KeyCode.ESCAPE);

        // Then:
        assertEquals(0, listWindows().size());
    }
}
