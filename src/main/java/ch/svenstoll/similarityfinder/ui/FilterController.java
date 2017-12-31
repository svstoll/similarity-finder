/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import ch.svenstoll.similarityfinder.dao.DatabaseAccessException;
import ch.svenstoll.similarityfinder.domain.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.fxml.FXMLLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

import static ch.svenstoll.similarityfinder.domain.Filter.DEFAULT_MIN_LETTERS;
import static ch.svenstoll.similarityfinder.domain.SimilarityDetector.MIN_SIMILARITY_INDEX;
import static ch.svenstoll.similarityfinder.ui.CheckBoxUtil.adjustCheckBoxSelectionState;

/**
 * A JavaFx controller class that controls the view of the filter component. This component
 * allows a user to configure the articles among which similar contents should be detected.
 */
@Singleton
public final class FilterController implements Controller {
    @NotNull
    private static final String SEARCH_BUTTON_TEXT = "Search";
    @NotNull
    private static final String SEARCH_BUTTON_TEXT_BUSY = "Cancel";

    @NotNull
    private final Filter filter;
    @NotNull
    private final FilterConfigsManagerController configsManagerController;
    @NotNull
    private final List<Task> filterTasks = new ArrayList<>();
    @NotNull
    private final ObservableList<Medium> mediaList = FXCollections.observableArrayList();
    @NotNull
    private final FilteredList<Medium> filteredMediaList = new FilteredList<>(mediaList, m -> true);

    @FXML
    private Pane filterRootPane;
    @FXML
    private DecimalTextField similarityThresholdField;
    @FXML
    private Slider similarityThresholdSlider;
    @FXML
    private CheckBox relevantOnlyCheckBox;
    @FXML
    private ListView<Medium> mediaListView;
    @FXML
    private TextField mediaFilterField;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TextField titleField;
    @FXML
    private DecimalTextField minLettersField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private CheckBox selectAllMediaCheckBox;
    @FXML
    private Button filterButton;

    /**
     * Construct a {@code FilterController}.
     *
     * @param filter the {@code Filter} which will be used to configure articles among which
     *               similar contents should be detected
     * @param configsManagerController the {@code FilterConfigsManagerController} whose view
     *                                 may be opened in a new stage if requested by the user
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public FilterController(@NotNull Filter filter,
                            @NotNull FilterConfigsManagerController configsManagerController) {
        this.filter = Validate.notNull(filter, "Filter must not be null.");
        this.configsManagerController = Validate.notNull(configsManagerController,
                "ConfigManagerController must not be null.");
    }

    /**
     * Initializes JavaFX UI controls of this {@code FilterController}. This method will be
     * called when the fxml file is loaded by a {@code FXMLLoader}.
     */
    @FXML
    private void initialize() {
        initializeSimilarityThreshold();
        initializeMediaListView();
        initializeMinLetters();
        filter.relevantOnlyProperty().bindBidirectional(relevantOnlyCheckBox.selectedProperty());
        filter.fromDateProperty().bindBidirectional(fromDatePicker.valueProperty());
        filter.toDateProperty().bindBidirectional(toDatePicker.valueProperty());
        filter.titleProperty().bindBidirectional(titleField.textProperty());
        filter.progressProperty().addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> progressIndicator.progressProperty().setValue(newValue)));
    }

    /**
     * Initializes JavaFX controls that are related to the similarity threshold property. If a
     * user uses the {@link #similarityThresholdField} to provide a threshold value, the
     * corresponding filter property will be adjusted, whenever this field loses focus.
     */
    private void initializeSimilarityThreshold() {
        DecimalFormat thresholdFormat = new DecimalFormat();
        thresholdFormat.setMinimumFractionDigits(0);
        thresholdFormat.setMaximumFractionDigits(2);
        thresholdFormat.setRoundingMode(RoundingMode.HALF_UP);
        similarityThresholdField.setDecimalFormat(thresholdFormat);
        similarityThresholdField.setMinValue(0);
        similarityThresholdField.setMaxValue(1);
        similarityThresholdField.setTextFieldNumber(filter.getSimilarityThreshold());
        similarityThresholdField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                similarityThresholdField.formatTextProperty(
                        String.valueOf(MIN_SIMILARITY_INDEX));
                filter.setSimilarityThreshold(
                        similarityThresholdField.parseTextFieldNumber(0).doubleValue());
            }
        });

        similarityThresholdSlider.valueProperty().bindBidirectional(
                filter.similarityThresholdProperty());
        similarityThresholdSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                similarityThresholdField.setTextFieldNumber(newValue));
    }

    /**
     * Initializes the {@link #mediaListView}.
     */
    private void initializeMediaListView() {
        filter.mediaProperty().addListener((InvalidationListener) observable ->
                Platform.runLater(this::updateMediaListView));

        mediaFilterField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredMediaList.setPredicate(medium ->
                        medium.getName().toLowerCase().contains(newValue)));

        selectAllMediaCheckBox.setOnAction(event -> {
            boolean selected = selectAllMediaCheckBox.isSelected();
            Objects.requireNonNull(filter.getMedia())
                    .forEach(media -> media.setSelected(selected));
        });

        updateMediaListView();
    }

    /**
     * Updates the {@link #mediaListView}.
     */
    private void updateMediaListView() {
        mediaList.clear();
        if (filter.getMedia() != null) {
            mediaList.addAll(filter.getMedia());
        }

        mediaListView.setCellFactory(CheckBoxListCell.forListView(Medium::selectedProperty));
        mediaListView.setItems(filteredMediaList);

        InvalidationListener selectionListener = observable ->
                adjustCheckBoxSelectionState(selectAllMediaCheckBox, mediaList);
        mediaList.forEach(media -> media.selectedProperty().addListener(selectionListener));
    }

    /**
     * Initializes the {@link #minLettersField}. The corresponding filter property will be
     * adjusted whenever the {@code minLettersField} loses focus.
     */
    private void initializeMinLetters() {
        DecimalFormat minLettersFormat = new DecimalFormat();
        minLettersFormat.setParseIntegerOnly(true);
        minLettersFormat.setRoundingMode(RoundingMode.HALF_UP);
        minLettersField.setMinValue(0);
        minLettersField.setMaxValue(Integer.MAX_VALUE);
        minLettersField.setDecimalFormat(minLettersFormat);
        minLettersField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                minLettersField.formatTextProperty("");
                filter.setMinLetters(minLettersField.parseTextFieldNumber(0).intValue());
            }
        });

        filter.minLettersProperty().addListener((observable, oldValue, newValue) -> {
            if (minLettersField.parseTextFieldNumber(0).intValue() != newValue.intValue()) {
                if (newValue.intValue() == DEFAULT_MIN_LETTERS) {
                    minLettersField.setText("");
                } else {
                    minLettersField.setTextFieldNumber(newValue);

                }
            }
        });
    }

    /**
     * Finds articles with similar contents that match with the various properties of this {@code
     * Filter} instance.To prevent blocking the JavaFX thread, this method will be executed
     * asynchronously. In case of an error, an alert will be presented to the user providing
     * information of what went wrong.
     */
    @FXML
    private void findArticlesWithSimilarContent() {
        // Cancel all existing filter tasks to prevent unnecessary computations.
        if (filterTasks.size() >= 1) {
            for (Task task : filterTasks) {
                if (!task.isDone() && !task.isCancelled()) {
                    task.cancel(true);
                }
            }
            filterTasks.clear();
            return;
        }

        Task<List<Set<Article>>> task = new Task<List<Set<Article>>>() {
            @Override
            protected List<Set<Article>> call() {
                return filter.findArticlesWithSimilarContent();
            }
        };

        task.setOnCancelled(event -> setFilterStateToReady());
        task.setOnSucceeded(event -> handleSuccessfulFilterTask(task));
        task.setOnFailed(event -> handleFailedFilterTask(task));

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(task);

        filterTasks.add(task);
        setFilterStateToBusy();
    }

    /**
     * Handles a successful filter task. If no articles with similar contents could be found, an
     * alert will be shown. The {@code task} will be removed from {@link #filterTasks} and the state
     * of this {@code FilterController} will be adjusted so that the user may trigger the
     * execution of a new filter task.
     *
     * @param task the filter task that was executed successfully
     * @throws IllegalArgumentException if {@code task} was {@code null}
     */
    private void handleSuccessfulFilterTask(Task<List<Set<Article>>> task) {
        filterTasks.remove(task);
        setFilterStateToReady();

        if (task.getValue().size() == 0) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Try to adjust the " +
                    "filter values.", ButtonType.OK);
            alert.setHeaderText("No similarities found.");
            AlertUtil.styleAlert(alert);
            alert.show();
        }
    }

    /**
     * Handles a failed filter task by showing an alert to the user providing information what
     * has gone wrong. The {@code task} will be removed from {@link #filterTasks} and the state
     * of this {@code FilterController} will be adjusted so that the user may trigger the
     * execution of a new filter task.
     *
     * @param task the filter task that failed
     * @throws IllegalArgumentException if {@code task} was {@code null}
     */
    private void handleFailedFilterTask(Task<List<Set<Article>>> task) {
        filterTasks.remove(task);
        setFilterStateToReady();

        Throwable e = task.getException();
        if (e instanceof DatabaseAccessException) {
            final Alert errorAlert
                    = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            AlertUtil.styleAlert(errorAlert);
            errorAlert.show();
        } else if (e instanceof MaxArticlesException) {
            int found = ((MaxArticlesException) e).getFoundArticles();
            int max = ((MaxArticlesException) e).getMaxArticles();
            final Alert errorAlert = new Alert(Alert.AlertType.ERROR, found + " articles" +
                    " found. The maximum number of articles is currently set to " + max
                    + ".", ButtonType.OK);
            AlertUtil.styleAlert(errorAlert);
            errorAlert.show();
        } else {
            e.printStackTrace();
        }
    }

    /**
     * Shows the {@link #progressIndicator} and adjusts the state of this {@code
     * FilterController} so that the user may not trigger the execution of a new filter task.
     * Instead, the user may now cancel the ongoing filter task using the {@link #filterButton}.
     */
    private void setFilterStateToBusy() {
        filterButton.setText(SEARCH_BUTTON_TEXT_BUSY);
        progressIndicator.setVisible(true);
    }

    /**
     * Hides the {@link #progressIndicator} and adjusts the state of this {@code
     * FilterController} so that the user may trigger the execution of a new filter task.
     */
    private void setFilterStateToReady() {
        progressIndicator.setVisible(false);
        filterButton.setText(SEARCH_BUTTON_TEXT);
    }

    /**
     * Resets all filter properties except the progress to their default.
     */
    @FXML
    private void resetFilterValues() {
        filter.reset();
    }

    /**
     * Opens the filter configs manager component in a new stage.
     */
    @FXML
    private void openFilterConfigsManager() {
        configsManagerController.openInNewStage();
    }

    /**
     * Stops ongoing background tasks. This will cancel all active {@code Tasks} in
     * {@link #filterTasks}.
     */
    public void stopOngoingBackgroundTasks() {
        filterTasks.forEach(Task::cancel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Pane loadFxml() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxml/filter.fxml"));
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
    public @Nullable Pane getRoot() {
        return filterRootPane;
    }
}
