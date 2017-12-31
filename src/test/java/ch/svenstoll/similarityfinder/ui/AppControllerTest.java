package ch.svenstoll.similarityfinder.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isNull;
import static org.testfx.matcher.base.NodeMatchers.isNotNull;

public class AppControllerTest extends ApplicationTest {
    private AppController appController;
    private SettingsController settingsController;
    private FilterController filterController;
    private FilteredSimilaritiesController filteredSimilaritiesController;
    private SideBarController sideBarController;
    private FxRobot robot;

    @Override
    public void start(Stage primaryStage) {
        settingsController = mock(SettingsController.class);
        filterController = mock(FilterController.class);
        filteredSimilaritiesController = mock(FilteredSimilaritiesController.class);
        sideBarController = mock(SideBarController.class);
        Pane mockedFilterRootPane = getMockedPane("filterRootPane");
        given(filterController.loadFxml()).willReturn(mockedFilterRootPane);
        given(filterController.getRoot()).willReturn(mockedFilterRootPane);
        given(filteredSimilaritiesController.loadFxml())
                .willReturn(getMockedPane("similaritiesRootPane"));
        given(sideBarController.loadFxml()).willReturn(getMockedPane("sideBarRootPane"));

        appController = new AppController(sideBarController, filterController,
                filteredSimilaritiesController, settingsController);
        Parent root = appController.loadFxml();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pane getMockedPane(String id) {
        Validate.notNull(id, "Id must not be null.");

        Pane mockedPane = new Pane();
        mockedPane.setId(id);
        mockedPane.setPrefWidth(150);
        mockedPane.setPrefHeight(150);
        mockedPane.getChildren().add(new Label(id));

        return mockedPane;
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
    public void firstLaunchDialog_givenUserWantsToOpenSettings_shouldRequestSettingsStage() {
        // Given:
        Platform.runLater(() -> appController.showFirstLaunchDialog());
        robot.sleep(100);

        // When:
        Node n = robot.lookup(hasText("Go to Settings")).query();
        robot.clickOn(n);

        // Then:
        verify(settingsController, times(1)).openInNewStage();
    }

    @Test
    public void switchFilterVisibility_givenFilterComponentIsDisplayed_removeFilterComponent() {
        // When:
       Platform.runLater(() -> appController.switchFilterVisibility());

        // Then:
        robot.sleep(100);
        verifyThat(lookup("#filterRootPane"), isNull());
    }

    @Test
    public void switchFilterVisibility_givenFilterComponentIsNotDisplayed_showFilterComponent() {
        // Given:
        Platform.runLater(() -> appController.switchFilterVisibility());

        // When:
        Platform.runLater(() -> appController.switchFilterVisibility());

        // Then:
        robot.sleep(100);
        verifyThat(lookup("#filterRootPane"), isNotNull());
    }
}
