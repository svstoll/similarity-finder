/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFx controller class that controls the view of the side bar component.
 */
@Singleton
public final class SideBarController implements Controller {
    @NotNull
    private final SettingsController settingsController;
    @NotNull
    private final AboutController aboutController;
    @NotNull
    private final List<FilterVisibilityListener> filterVisibilityListeners = new ArrayList<>();

    @FXML
    private Pane sideBarRootPane;
    @FXML
    private Label filterIconLabel;

    /**
     * Constructs a {@code SideBarController}.
     *
     * @param settingsController a {@code SettingsController} used to show the settings component
     * @param aboutController a {@code AboutController} used to show the about component
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public SideBarController(@NotNull SettingsController settingsController,
                             @NotNull AboutController aboutController) {
        this.settingsController = Validate.notNull(settingsController,
                "SettingsController must not be null.");
        this.aboutController = Validate.notNull(aboutController,
                "AboutController must not be null.");
    }

    /**
     * Initializes JavaFX UI controls of this {@code SideBarController}. This method will be called
     * when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        sideBarRootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                initializeSceneShortcuts(newValue);
            }
        });
    }

    /**
     * Initializes shortcuts that can be used in the {@code Scene} of this {@code
     * SideBarController}.
     * <p><ul>
     * <li>Ctrl + F: Calls the {@link #requestFilterVisibilitySwitch()} method.
     * <li>Ctrl + S: Calls the {@link #openSettings()} method.
     * </ul><p>
     *
     * @param scene the {@code Scene} where the {@link #sideBarRootPane} is currently shown
     * @throws IllegalArgumentException if {@code scene} was {@code null}
     */
    private void initializeSceneShortcuts(@NotNull Scene scene) {
        Validate.notNull(scene, "Scene must not be null.");

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.F) { // Ctrl + F
                    requestFilterVisibilitySwitch();
                    event.consume();
                } else if (event.getCode() == KeyCode.S) { // Ctrl + S
                    openSettings();
                    event.consume();
                }
            }
        });
    }

    /**
     * Adjusts the label of the button that controls whether the filter component should be
     * visible or not.
     *
     * @param isFilterVisible a {@code boolean} that indicates whether the filter component is
     *                        currently visible or not
     */
    public void adjustFilterVisibilityLabel(boolean isFilterVisible) {
        if (isFilterVisible) {
            filterIconLabel.setText("Hide Filter");
        } else {
            filterIconLabel.setText("Show Filter");
        }
    }

    /**
     * Requests a visibility change of the filter component.
     */
    @FXML
    private void requestFilterVisibilitySwitch() {
        fireFilterVisibilityEvent(new FilterVisibilityEvent(this));
    }

    /**
     * Opens the settings component in a new stage.
     */
    @FXML
    private void openSettings() {
        settingsController.openInNewStage();
    }

    /**
     * Opens the about component in a new stage.
     */
    @FXML
    private void openAboutDialog() {
        aboutController.openInNewStage();
    }

    /**
     * Adds an instance of {@code FilterVisibilityListener} that will be notified whenever a
     * {@code FilterVisibilityEvent} has occurred.
     *
     * @param listener an instance of {@code FilterVisibilityListener} to be notified
     * @throws IllegalArgumentException if {@code listener} was {@code null}
     */
    public void addFilterVisibilityListener(FilterVisibilityListener listener) {
        filterVisibilityListeners.add(listener);
    }

    /**
     * Notifies listeners that a {@code FilterVisibilityEvent} has occurred.
     *
     * @param event the {@code FilterVisibilityEvent} to fire
     * @throws IllegalArgumentException if {@code event} was {@code null}
     */
    private void fireFilterVisibilityEvent(FilterVisibilityEvent event) {
        filterVisibilityListeners.forEach(listener -> listener.onFilterVisibilityChanged(event));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxml/side-bar.fxml"));
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
        return sideBarRootPane;
    }
}
