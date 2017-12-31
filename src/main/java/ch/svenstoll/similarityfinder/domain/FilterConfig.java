/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ch.svenstoll.similarityfinder.domain.Filter.*;

/**
 * A class that can be used to store the state of a {@code Filter} instance. In opposite to
 * the {@code Filter} class, no JavaFX Properties are used, because they can not be transformed
 * into the JSON format by default.
 */
public final class FilterConfig {
    @Nullable
    private String name;
    @Nullable
    private LocalDateTime lastEdited;
    private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    private boolean relevantOnly = DEFAULT_RELEVANT_ONLY;
    @Nullable
    private List<String> selectedMedia = new ArrayList<>();
    @Nullable
    private LocalDate fromDate = DEFAULT_FROM_DATE;
    @Nullable
    private LocalDate toDate = DEFAULT_TO_DATE;
    @Nullable
    private String title = DEFAULT_TITLE;
    private int minLetters = DEFAULT_MIN_LETTERS;

    /**
     * Constructs a {@code FilterConfig}.
     *
     * @param name the name of the filter config
     * @param lastEdited a {@code LocalDateTime} that represents the time this filter config was
     *                  last edited
     * @param filter a {@code Filter} whose properties will be stored in the constructed
     *               {@code FilterConfig}. If none is provided, the default filter values will be
     *               used.
     */
    public FilterConfig(@Nullable String name, @Nullable LocalDateTime lastEdited,
                        @Nullable Filter filter) {
        this.name = name;
        this.lastEdited = lastEdited;

        if (filter != null) {
            this.similarityThreshold = filter.getSimilarityThreshold();
            this.selectedMedia = filter.getNamesOfSelectedMedia();
            this.fromDate = filter.getFromDate();
            this.toDate = filter.getToDate();
            this.title = filter.getTitle();
            this.minLetters = filter.getMinLetters();
            this.relevantOnly = filter.isRelevantOnly();
        }
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable LocalDateTime getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(@Nullable LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public boolean isRelevantOnly() {
        return relevantOnly;
    }

    public void setRelevantOnly(boolean relevantOnly) {
        this.relevantOnly = relevantOnly;
    }

    public @Nullable List<String> getSelectedMedia() {
        return selectedMedia;
    }

    public void setSelectedMedia(@Nullable List<String> selectedMedia) {
        this.selectedMedia = selectedMedia;
    }

    public @Nullable LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(@Nullable LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public @Nullable LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(@Nullable LocalDate toDate) {
        this.toDate = toDate;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public int getMinLetters() {
        return minLetters;
    }

    public void setMinLetters(int minLetters) {
        this.minLetters = minLetters;
    }
}
