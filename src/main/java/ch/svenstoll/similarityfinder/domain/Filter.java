/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.DatabaseAccess;
import ch.svenstoll.similarityfinder.dao.DatabaseAccessException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ch.svenstoll.similarityfinder.domain.SimilarityDetector.MAX_SIMILARITY_INDEX;

/**
 * A class that can be used to configure articles among which similar contents should be detected.
 */
@Singleton
public final class Filter {
    public static final int DEFAULT_SIMILARITY_THRESHOLD = MAX_SIMILARITY_INDEX;
    @Nullable
    public static final LocalDate DEFAULT_FROM_DATE = null;
    @Nullable
    public static final LocalDate DEFAULT_TO_DATE = null;
    @Nullable
    public static final String DEFAULT_TITLE = "";
    public static final int DEFAULT_MIN_LETTERS = 0;
    public static final boolean DEFAULT_RELEVANT_ONLY = false;
    public static final double MIN_PROGRESS = 0;
    public static final double MAX_PROGRESS = 1;

    @NotNull
    private final Settings settings;
    @NotNull
    private final DatabaseAccess databaseAccess;
    @NotNull
    private final FilteredSimilarities filteredSimilarities;
    @NotNull
    private final SimilarityDetector similarityDetector;

    @NotNull
    private final ListProperty<Medium> media = new SimpleListProperty<>();
    @NotNull
    private final DoubleProperty similarityThreshold
            = new SimpleDoubleProperty(DEFAULT_SIMILARITY_THRESHOLD);
    @NotNull
    private final BooleanProperty relevantOnly
            = new SimpleBooleanProperty(DEFAULT_RELEVANT_ONLY);
    @NotNull
    private final ObjectProperty<LocalDate> fromDate
            = new SimpleObjectProperty<>(DEFAULT_FROM_DATE);
    @NotNull
    private final ObjectProperty<LocalDate> toDate
            = new SimpleObjectProperty<>(DEFAULT_TO_DATE);
    @NotNull
    private final StringProperty title
            = new SimpleStringProperty(DEFAULT_TITLE);
    @NotNull
    private final IntegerProperty minLetters
            = new SimpleIntegerProperty(DEFAULT_MIN_LETTERS);
    @NotNull
    private final DoubleProperty progress
            = new SimpleDoubleProperty(MIN_PROGRESS);

    /**
     * Construct a {@code Filter} instance.
     * <p>
     * This will trigger an async database call to load the data that is needed for the media
     * list. Every time the settings are updated, this database call is repeated in case the
     * used database has been changed.
     * </p>
     *
     * @param databaseAccess an instance of {@code DatabaseAccess} used to query the database
     * @param filteredSimilarities a {@code FilteredSimilarities} instance used to store articles
     *                            with similar contents
     * @param similarityDetector a {@code SimilarityDetector} used to detect articles with
     *                           similar contents
     * @param settings the {@code Settings} instance used throughout the application
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public Filter(@NotNull DatabaseAccess databaseAccess,
                  @NotNull FilteredSimilarities filteredSimilarities,
                  @NotNull SimilarityDetector similarityDetector,
                  @NotNull Settings settings) {
        this.databaseAccess = Validate.notNull(databaseAccess, "DatabaseAccessImpl must not be null.");
        this.filteredSimilarities
                = Validate.notNull(filteredSimilarities, "FilteredSimilarities must not be null.");
        this.similarityDetector
                = Validate.notNull(similarityDetector, "SimilarityDetector must not be null.");
        this.settings = Validate.notNull(settings, "Settings must not be null.");

        similarityDetector.addProgressListener(event -> updateFilterProgress(event.getProgress()));
        settings.addSettingsUpdatedListener(event -> updateMediaListAsync());
        if (!settings.isFirstLaunch()) {
            updateMediaListAsync();
        }
    }

    /**
     * Updates the {@link #progress} property with respect to the minimum and maximum progress
     * value.
     *
     * @param newProgress a number between {@link #MIN_PROGRESS} and {@link #MAX_PROGRESS}
     *                    (inclusive)
     */
    private void updateFilterProgress(double newProgress) {
        if (newProgress < MIN_PROGRESS) {
            progress.set(MIN_PROGRESS);
        } else if (newProgress > MAX_PROGRESS) {
            progress.set(MAX_PROGRESS);
        } else {
            progress.set(newProgress);
        }
    }

    /**
     * Tries to update the {@link #media} property by querying the database asynchronously.
     */
    private void updateMediaListAsync() {
        Runnable worker = () -> {
            try {
                List<Medium> mediumList = databaseAccess.queryAllMedia();
                mediumList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                this.media.set(FXCollections.observableArrayList(mediumList));
            } catch (DatabaseAccessException e) {
                e.printStackTrace();
                this.media.clear();
            }
        };

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(worker);
    }

    /**
     * Finds articles with similar contents that match with the various properties of this {@code
     * Filter}.
     * <p>
     * Since this method might take some time to complete, this will initialize a new progress
     * cycle. Firstly, the {@link #progress} property is reset and will then be constantly
     * updated, because this {@code Filter} is registered as a {@code DetectionProgressListener}.
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     * @return a list of sets that contain articles with similar contents.
     * @throws MaxArticlesException if more articles are found than specified in the
     *                              {@code Settings} instance of the application
     */
    public synchronized List<Set<Article>> findArticlesWithSimilarContent() {
        progress.set(0);

        List<Article> filteredArticles;
        filteredArticles = databaseAccess.queryArticles(this);

        if (filteredArticles.size() > settings.getMaxArticles()) {
            throw new MaxArticlesException(settings.getMaxArticles(),
                    filteredArticles.size());
        }

        List<Set<Article>> result = similarityDetector.detectArticlesWithSimilarContents(
                filteredArticles, similarityThreshold.get());
        filteredSimilarities.setSimilarities(FXCollections.observableArrayList(result));

        progress.set(1);

        return result;
    }

    /**
     * Resets all filter properties except the filter progress to their default.
     */
    public void reset() {
        media.forEach(media -> media.setSelected(false));
        similarityThreshold.set(MAX_SIMILARITY_INDEX);
        relevantOnly.setValue(DEFAULT_RELEVANT_ONLY);
        fromDate.setValue(DEFAULT_FROM_DATE);
        toDate.setValue(DEFAULT_TO_DATE);
        title.setValue(DEFAULT_TITLE);
        minLetters.setValue(DEFAULT_MIN_LETTERS);
    }

    /**
     * Restores the filter properties that are contained in a {@code FilterConfig} instance.
     *
     * @param filterConfig the {@code FilterConfig} instance to be restored
     */
    public void restoreFromConfig(@NotNull FilterConfig filterConfig) {
        Validate.notNull(filterConfig, "FilterConfig must not be null.");

        similarityThreshold.set(filterConfig.getSimilarityThreshold());
        relevantOnly.set(filterConfig.isRelevantOnly());

        List<String> selectedMedia = filterConfig.getSelectedMedia();
        if (selectedMedia != null) {
            for (Medium medium : media) {
                medium.setSelected(false);
                selectedMedia.forEach(selected -> {
                    if (selected.equals(medium.getName())) {
                        medium.setSelected(true);
                    }
                });
            }
        }

        title.set(filterConfig.getTitle());
        fromDate.set(filterConfig.getFromDate());
        toDate.set(filterConfig.getToDate());
        minLetters.set(filterConfig.getMinLetters());
    }

    /**
     * Returns the names of all selected media.
     *
     * @return the name of all selected media
     */
    public @NotNull List<String> getNamesOfSelectedMedia() {
        return media.filtered(Medium::isSelected).stream().map(Medium::getName)
                .collect(Collectors.toList());
    }

    public double getSimilarityThreshold() {
        return similarityThreshold.get();
    }

    public @NotNull DoubleProperty similarityThresholdProperty() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold.set(similarityThreshold);
    }

    public boolean isRelevantOnly() {
        return relevantOnly.get();
    }

    public @NotNull BooleanProperty relevantOnlyProperty() {
        return relevantOnly;
    }

    public void setRelevantOnly(boolean relevantOnly) {
        this.relevantOnly.set(relevantOnly);
    }

    public @Nullable ObservableList<Medium> getMedia() {
        return media.getValue();
    }

    public @NotNull ListProperty<Medium> mediaProperty() {
        return media;
    }

    public @Nullable LocalDate getFromDate() {
        return fromDate.get();
    }

    public @NotNull ObjectProperty<LocalDate> fromDateProperty() {
        return fromDate;
    }

    public void setFromDate(@Nullable LocalDate fromDate) {
        this.fromDate.set(fromDate);
    }

    public @Nullable LocalDate getToDate() {
        return toDate.get();
    }

    public ObjectProperty<LocalDate> toDateProperty() {
        return toDate;
    }

    public void setToDate(@Nullable LocalDate toDate) {
        this.toDate.set(toDate);
    }

    public @Nullable String getTitle() {
        return title.get();
    }

    public @NotNull StringProperty titleProperty() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title.set(title);
    }

    public int getMinLetters() {
        return minLetters.get();
    }

    public @NotNull IntegerProperty minLettersProperty() {
        return minLetters;
    }

    public void setMinLetters(int minLetters) {
        this.minLetters.set(minLetters);
    }

    public @NotNull DoubleProperty progressProperty() {
        return progress;
    }
}
