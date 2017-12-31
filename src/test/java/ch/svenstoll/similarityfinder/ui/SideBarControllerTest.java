package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.ui.AboutController;
import ch.svenstoll.similarityfinder.ui.FilterVisibilityListener;
import ch.svenstoll.similarityfinder.ui.SettingsController;
import ch.svenstoll.similarityfinder.ui.SideBarController;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class SideBarControllerTest extends ApplicationTest {
    private SideBarController sideBarController;
    private SettingsController settingsController;
    private AboutController aboutController;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        settingsController = mock(SettingsController.class);
        aboutController = mock(AboutController.class);

        sideBarController = new SideBarController(settingsController, aboutController);
        Parent root = sideBarController.loadFxml();
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
    public void switchFilterVisibility_givenButtonIsClicked_shouldNotifyListeners() {
        // Given:
        FilterVisibilityListener listener = mock(FilterVisibilityListener.class);
        sideBarController.addFilterVisibilityListener(listener);

        // When:
        robot.clickOn("#filterVisibilityButton");

        // Then:
        verify(listener, times(1)).onFilterVisibilityChanged(any());
    }

    @Test
    public void switchFilterVisibility_givenShortcutIsPressed_shouldNotifyListeners() {
        // Given:
        FilterVisibilityListener listener = mock(FilterVisibilityListener.class);
        sideBarController.addFilterVisibilityListener(listener);

        // When:
        robot.push(KeyCode.CONTROL, KeyCode.F);

        // Then:
        verify(listener, timeout(1)).onFilterVisibilityChanged(any());
    }

    @Test
    public void filterVisibilityButtonLabel_givenFilterIsVisible_shouldAdjustLabel()
            throws TimeoutException {
        // When:
        Platform.runLater(() -> sideBarController.adjustFilterVisibilityLabel(true));

        // Then:
        waitFor(10, TimeUnit.MILLISECONDS, () -> ((Label) robot.lookup("#filterIconLabel")
                .query()).getText().equals("Hide Filter"));
    }

    @Test
    public void filterVisibilityButtonLabel_givenFilterIsInvisible_shouldAdjustLabel()
            throws TimeoutException {
        // When:
        Platform.runLater(() -> sideBarController.adjustFilterVisibilityLabel(false));

        // Then:
        waitFor(10, TimeUnit.MILLISECONDS, () -> ((Label) robot.lookup("#filterIconLabel")
                .query()).getText().equals("Show Filter"));
    }

    @Test
    public void openSettings_givenButtonIsClicked_shouldRequestSettingsStage() {
        // When:
        robot.clickOn("#settingsButton");

        // Then:
        verify(settingsController, times(1)).openInNewStage();
    }

    @Test
    public void openSettings_givenShortcutIsPressed_shouldRequestSettingsStage() {
        // When:
        robot.push(KeyCode.CONTROL, KeyCode.S);

        // Then:
        verify(settingsController, timeout(1)).openInNewStage();
    }

    @Test
    public void openAbout_givenButtonIsClicked_shouldRequestAboutStage() {
        // When:
        robot.clickOn("#aboutButton");

        // Then:
        verify(aboutController, times(1)).openInNewStage();
    }
}
