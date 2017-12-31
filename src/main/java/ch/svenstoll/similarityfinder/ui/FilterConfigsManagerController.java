/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.dao.FilterConfigsAccessException;
import ch.svenstoll.similarityfinder.domain.FilterConfig;
import ch.svenstoll.similarityfinder.domain.FilterConfigsManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.jetbrains.annotations.NotNull;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * A JavaFx controller class that controls the view of the filter configs manager component. This
 * component allows a user to store, load and manipulate filter configs.
 */
@Singleton
public final class FilterConfigsManagerController implements Controller, Stageable {
    @NotNull
    private static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");

    @NotNull
    private final FilterConfigsManager configManager;

    @FXML
    private Pane configsManagerRootPane;
    @FXML
    private TableView<FilterConfig> configsTableView;
    @FXML
    private TableColumn<FilterConfig, String> nameColumn;
    @FXML
    private TableColumn<FilterConfig, LocalDateTime> lastEditedColumn;
    @FXML
    private Button loadButton;
    @FXML
    private Button addButton;
    @FXML
    private Button overwriteButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField configNameField;

    /**
     * Constructs a {@code FilterConfigsManagerController}.
     *
     * @param configManager the {@code FilterConfigsManager} used to store, retrieve and
     *                      manipulate filter configs
     */
    @Inject
    public FilterConfigsManagerController(@NotNull FilterConfigsManager configManager) {
        this.configManager = Validate.notNull(configManager, "ConfigManager must not be null");
    }

    /**
     * Initializes JavaFX UI controls of this {@code FilterConfigsManagerController}. This method
     * will be called when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        configManager.filterConfigsProperty().addListener((InvalidationListener) observable ->
                Platform.runLater(
                        () -> configsTableView.setItems(configManager.getFilterConfigs())));

        initializeConfigsTableView();
        initializeConfigNameField();

        configsManagerRootPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getRoot() == configsManagerRootPane) {
                initializeSceneShortcuts(newValue);
            }
        });
    }

    /**
     * Initializes the {@link #configsTableView}.
     */
    private void initializeConfigsTableView() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastEditedColumn.setCellValueFactory(new PropertyValueFactory<>("lastEdited"));
        lastEditedColumn.setCellFactory(column -> new TableCell<FilterConfig, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.format(DATE_TIME_FORMATTER));
                        }
                    }
                }
        );

        // Make the width of the columns automatically adjust if the window gets resized. Because
        // the content of the lastEditedColumn has always the same size it should keep its
        // preferred width. 1 pixel of the remaining column is subtracted in order to
        // ensure that the horizontal scroll bar will not be visible.
        double lastEditedColumnPrefWidth = lastEditedColumn.prefWidthProperty().get();
        nameColumn.prefWidthProperty().bind(configsTableView.widthProperty()
                .subtract(lastEditedColumnPrefWidth).subtract(1));

        configsTableView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        disableSelectionDependentButtons();
                    } else {
                        enableSelectionDependentButtons();
                    }
                });

        configsTableView.setItems(configManager.getFilterConfigs());
    }

    /**
     * Disables the {@link #loadButton}, {@link #deleteButton} and {@link #overwriteButton}.
     */
    private void disableSelectionDependentButtons() {
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
        overwriteButton.setDisable(true);
    }

    /**
     * Enables the {@link #loadButton}, {@link #deleteButton} and {@link #overwriteButton}.
     */
    private void enableSelectionDependentButtons() {
        loadButton.setDisable(false);
        deleteButton.setDisable(false);
        overwriteButton.setDisable(false);
    }

    /**
     * Initializes the {@link #configNameField}.
     */
    private void initializeConfigNameField() {
        configNameField.textProperty().addListener(observable ->
                addButton.setDisable(!canAddConfig()));
        initializeConfigNameFieldShortcuts();
    }

    /**
     * Determines if a filter config can be added.
     *
     * @return {@code true} if the {@link #configNameField} contains user input; {@code false}
     *         otherwise
     */
    private boolean canAddConfig() {
        return configNameField.getText() != null && !configNameField.getText().isEmpty();
    }

    /**
     * Initializes shortcuts that can be on the {@link #configNameField} used:
     * <p><ul>
     * <li>Enter: Calls the {@link #addFilterConfig} method.
     * </ul><p>
     */
    private void initializeConfigNameFieldShortcuts() {
        configNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) { // Enter
                addFilterConfig();
                event.consume();
            }
        });
    }

    /**
     * Initializes shortcuts that can be used in the scene of this
     * {@code FilterConfigsManagerController}.
     * <p><ul>
     * <li>Esc: Closes the stage of this {@code FilterConfigsManagerController}.
     * </ul><p>
     * <p>NOTE: These shortcuts should only be initialized if the view of this {@code
     * FilterConfigsManagerController} has been opened in a separate stage.</p>
     *
     * @param scene the {@code Scene} where the {@link #configsManagerRootPane} is currently shown
     * @throws IllegalArgumentException if {@code scene} was {@code null}
     */
    private void initializeSceneShortcuts(Scene scene) {
        Validate.notNull(scene);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) { // ESC
                StageUtil.closeStage(configsManagerRootPane);
                event.consume();
            }
        });
    }

    /**
     * Adds a new filter config with the current filter values if the method
     * {@link #canAddConfig()} returns {@code true}. If the selected filter config could not be
     * added, an alert will be shown.
     */
    @FXML
    private void addFilterConfig() {
        if (!canAddConfig()) {
            return;
        }

        try {
            configManager.addFilterConfig(configNameField.getText());
        } catch (FilterConfigsAccessException e) {
            final Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                    "Adding a new filter configuration failed.", ButtonType.OK);
            AlertUtil.styleAlert(errorAlert);
            errorAlert.show();
        }
        configNameField.clear();
    }

    /**
     * Loads the selected filter config which will replace the current filter values (no operation
     * is performed if none was selected).
     */
    @FXML
    private void loadFilterConfig() {
        FilterConfig config = this.configsTableView.getSelectionModel().getSelectedItem();
        if (config == null) {
            return;
        } else {
            this.configManager.loadFilterConfig(config);
        }

        StageUtil.closeStage(configsManagerRootPane);
    }

    /**
     * Overwrites the selected filter config with the current filter values (no operation
     * is performed if none was selected). If the selected filter config could not be
     * overwritten, an alert will be shown.
     */
    @FXML
    private void overwriteFilterConfig() {
        FilterConfig config = this.configsTableView.getSelectionModel().getSelectedItem();
        if (config == null) {
            return;
        }

        final Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really " +
                "want to override this filter configuration?", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                    this.configManager.overwriteFilterConfig(config);
            } catch (FilterConfigsAccessException e) {
                final Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                        "Overwriting the selected filter configuration failed.", ButtonType.OK);
                AlertUtil.styleAlert(errorAlert);
                errorAlert.show();
            }
        }
        this.configsTableView.getSelectionModel().clearSelection();
    }

    /**
     * Deletes the selected filter config (no operation is performed if none was selected). If the
     * selected filter config could not be deleted, an alert will be shown.
     */
    @FXML
    private void deleteFilterConfig() {
        FilterConfig config = this.configsTableView.getSelectionModel().getSelectedItem();
        if (config == null) {
            return;
        }

        final Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to" +
                " delete this filter configuration?", ButtonType.NO, ButtonType.YES);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                this.configManager.removeFilterConfig(config);
            } catch (FilterConfigsAccessException e) {
                final Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                        "Deleting the selected filter configuration failed.", ButtonType.OK);
                AlertUtil.styleAlert(errorAlert);
                errorAlert.show();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInNewStage() {
        Pane rootPane = loadFxml();
        if (rootPane != null) {
            StageUtil.openNewStage(rootPane, "Filter Configurations",
                    Modality.APPLICATION_MODAL, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader()
                .getResource("fxml/filter-configs-manager.fxml"));
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
        return configsManagerRootPane;
    }
}
