/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

/**
 * This is the main JavaFX controller class of this application. Apart from managing its own
 * view, this class is responsible to display views manged by other controllers in the
 * application's primary stage.
 */
@Singleton
public final class AppController implements Controller {
    @NotNull
    private final SideBarController sideBarController;
    @NotNull
    private final FilterController filterController;
    @NotNull
    private final FilteredSimilaritiesController filteredSimilaritiesController;
    @NotNull
    private final SettingsController settingsController;

    @FXML
    private BorderPane rootPane;
    @FXML
    private BorderPane contentPane;

    /**
     * Constructs a {@code AppController} instance.
     *
     * @param sideBarController the {@code SideBarController} instance of the application
     * @param filterController the {@code FilterController} instance of the application
     * @param filteredSimilaritiesController the {@code FilteredSimilaritiesController} instance
     *                                       of the application
     * @param settingsController the {@code SettingsController} instance of the application
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public AppController(@NotNull SideBarController sideBarController,
                         @NotNull FilterController filterController,
                         @NotNull FilteredSimilaritiesController filteredSimilaritiesController,
                         @NotNull SettingsController settingsController) {
        this.sideBarController = Validate.notNull(sideBarController,
                "SideBarController must not be null.");
        this.filterController = Validate.notNull(filterController,
                "FilterController must not be null.");
        this.filteredSimilaritiesController = Validate.notNull(filteredSimilaritiesController,
                "FilteredSimilaritiesController must not be null.");
        this.settingsController = Validate.notNull(settingsController,
                "SettingsController must not be null.");
    }

    /**
     * Initializes JavaFX UI controls of this {@code AppController}. This method will be called
     * when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        initializeComponents();
        sideBarController.addFilterVisibilityListener(event -> switchFilterVisibility());
    }

    /**
     * Initializes the components that will be shown in the primary stage of the application.
     */
    private void initializeComponents() {
        rootPane.setLeft(sideBarController.loadFxml());
        contentPane.setLeft(filterController.loadFxml());
        contentPane.setCenter(filteredSimilaritiesController.loadFxml());
    }

    /**
     * Switches the visibility of the filter component.
     */
    public void switchFilterVisibility() {
        Pane filterRootPane = filterController.getRoot();

        if (contentPane == null || filterRootPane == null) {
            return;
        }

        if (contentPane.getLeft() == filterRootPane) {
            contentPane.setLeft(null);
            sideBarController.adjustFilterVisibilityLabel(false);
        } else {
            contentPane.setLeft(filterRootPane);
            sideBarController.adjustFilterVisibilityLabel(true);
        }
    }

    /**
     * Shows a welcome dialog that informs the user that database settings should be checked
     * before using the application. Clicking the button of the alert will open the settings
     * component in a new stage.
     */
    public void showFirstLaunchDialog() {
        final ButtonType goToSettings = new ButtonType("Go to Settings", ButtonBar.ButtonData.YES);
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "It seems that " +
                "you've launched this application for the first time. Before you " +
                "start, please check the database settings.", goToSettings);
        alert.setHeaderText("Welcome!");
        AlertUtil.styleAlert(alert);

        Platform.runLater(() -> {
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(goToSettings)) {
                settingsController.openInNewStage();
            }
        });
    }

    /**
     * Stops ongoing background tasks of the application.
     */
    public void stopOngoingBackgroundTasks() {
        filterController.stopOngoingBackgroundTasks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxml/app.fxml"));
        fxmlLoader.setControllerFactory(param -> this);

        Pane rootPane = null;
        try {
            rootPane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized @Nullable Pane getRoot() {
        return rootPane;
    }
}
