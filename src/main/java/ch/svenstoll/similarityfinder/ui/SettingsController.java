/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.domain.Settings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.jetbrains.annotations.NotNull;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A JavaFx controller class that controls the view of the settings component that allows a user
 * to change the application settings.
 */
@Singleton
public final class SettingsController implements Controller, Stageable {
    @NotNull
    private final Settings settings;
    @NotNull
    private final BooleanProperty settingsEdited = new SimpleBooleanProperty(false);

    @FXML
    private Pane settingsRootPane;
    @FXML
    private TextField dbAddressField;
    @FXML
    private TextField dbUserField;
    @FXML
    private PasswordField dbPasswordField;
    @FXML
    private DecimalTextField maxContributionsField;
    @FXML
    private Button saveButton;
    @FXML
    private Button revertButton;

    /**
     * Constructs a {@code SettingsController}.
     *
     * @param settings the {@code Settings} used throughout the application
     * @throws IllegalArgumentException if {@code settings} was {@code null}
     */
    @Inject
    public SettingsController(@NotNull Settings settings) {
        this.settings = Validate.notNull(settings, "Settings must not be null.");
    }

    /**
     * Initializes JavaFX UI controls of this {@code SettingsController}. This method will be
     * called when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        initializeSettingsProperties();
        initializeEditingButtons();
        initializeSettingsEdited();

        settingsRootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getRoot() == settingsRootPane) {
                initializeSceneShortcuts(newValue);
            }
        });
    }

    /**
     * Initializes the properties of the settings.
     */
    private void initializeSettingsProperties() {
        dbAddressField.setText(settings.getDbAddress());
        dbUserField.setText(settings.getDbUser());
        dbPasswordField.setText(settings.getDbPassword());

        DecimalFormat maxContributionsFormat = new DecimalFormat();
        maxContributionsFormat.setRoundingMode(RoundingMode.HALF_UP);
        maxContributionsFormat.setParseIntegerOnly(true);
        maxContributionsField.setDecimalFormat(maxContributionsFormat);
        maxContributionsField.setMinValue(0);
        maxContributionsField.setMaxValue(Integer.MAX_VALUE);
        maxContributionsField.setTextFieldNumber(settings.getMaxArticles());
        maxContributionsField.focusedProperty().addListener((observable, oldValue, newValue) ->
                maxContributionsField.formatTextProperty("0"));
    }

    /**
     * Initializes the {@link #saveButton} and {@link #revertButton}.
     */
    private void initializeEditingButtons() {
        saveButton.setOnMouseClicked(event -> saveSettings());
        revertButton.setOnMouseClicked(event -> revertChanges());

        disableEditingButtons();
    }

    /**
     * Disables the {@link #saveButton} and {@link #revertButton}.
     */
    private void disableEditingButtons() {
        saveButton.setDisable(true);
        revertButton.setDisable(true);
    }

    /**
     * Enables the {@link #saveButton} and {@link #revertButton}.
     */
    private void enableEditingButtons() {
        saveButton.setDisable(false);
        revertButton.setDisable(false);
    }

    /**
     * Initializes the {@link #settingsEdited} property of this {@code SettingsController}. The
     * settings are considered to be edited, if the text field of any settings property has been
     * edited. The {@link #saveButton} and {@link #revertButton} will be enabled, if the {@code
     * #settingsEdited} property is set to {@code true}.
     */
    private void initializeSettingsEdited() {
        dbAddressField.textProperty().addListener(observable ->
                settingsEdited.set(true));
        dbUserField.textProperty().addListener(observable ->
                settingsEdited.set(true));
        dbPasswordField.textProperty().addListener(observable ->
                settingsEdited.set(true));
        maxContributionsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                settingsEdited.set(true);
            }
        });

        settingsEdited.set(false);
        settingsEdited.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                enableEditingButtons();
            } else {
                disableEditingButtons();
            }
        });
    }

    /**
     * Initializes shortcuts that can be used in the scene of this {@code SettingsController}.
     * <p><ul>
     * <li>Esc: Closes the stage of this {@code SettingsController}.
     * </ul><p>
     * <p>NOTE: These shortcuts should only be initialized if the view of this {@code
     * SettingsController} has been opened in a separate stage.</p>
     *
     * @param scene the {@code Scene} where the {@link #settingsRootPane} is currently shown
     * @throws IllegalArgumentException if {@code scene} was {@code null}
     */
    private void initializeSceneShortcuts(Scene scene) {
        Validate.notNull(scene);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) { // ESC
                StageUtil.closeStage(settingsRootPane);
                event.consume();
            }
        });
    }

    /**
     * Saves all settings' properties so that they can be retrieved in the next launch of the
     * application. This will also reset the {@link #settingsEdited} property and the related
     * buttons will be disabled.
     */
    private void saveSettings() {
        settings.setDbAddress(dbAddressField.getText());
        settings.setDbUser(dbUserField.getText());
        settings.setDbPassword(dbPasswordField.getText());
        settings.setMaxArticles(maxContributionsField.parseTextFieldNumber(0).intValue());
        settings.saveSettings();

        settingsEdited.set(false);
        disableEditingButtons();
    }

    /**
     * Reverts any changes of the settings' properties since the settings have been saved for the
     * last time. This will also reset the {@link #settingsEdited} property and the related
     * buttons will be disabled.
     */
    private void revertChanges() {
        dbAddressField.setText(settings.getDbAddress());
        dbUserField.setText(settings.getDbUser());
        dbPasswordField.setText(settings.getDbPassword());
        maxContributionsField.setTextFieldNumber(settings.getMaxArticles());

        settingsEdited.setValue(false);
        disableEditingButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInNewStage() {
        Pane rootPane = loadFxml();
        if (rootPane != null) {
            StageUtil.openNewStage(rootPane, "Settings", Modality.APPLICATION_MODAL, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxml/settings.fxml"));
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
        return settingsRootPane;
    }
}
