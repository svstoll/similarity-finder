/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import com.google.inject.Singleton;
import javafx.collections.FXCollections;
import org.jetbrains.annotations.NotNull;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.Validate;

import java.util.Set;

/**
 * A class that can be used to store sets of articles with similar contents.
 */
@Singleton
public final class FilteredSimilarities {
    @NotNull
    private final ListProperty<Set<Article>> similarities
            = new SimpleListProperty<>(FXCollections.observableArrayList());

    /**
     * Constructs a {@code FilteredSimilarities}.
     */
    public FilteredSimilarities() {}

    /**
     * Counts all articles that are currently stored in this {@code FilteredSimilarities} instance.
     *
     * @return the total amount of articles stored in this {@code FilteredSimilarities} instance
     */
    public int countAllArticles() {
        int counter = 0;
        for (Set<Article> set : similarities) {
            counter = counter + set.size();
        }
        return counter;
    }

    public @NotNull ObservableList<Set<Article>> getSimilarities() {
        return similarities.get();
    }

    public @NotNull ListProperty<Set<Article>> similaritiesProperty() {
        return similarities;
    }

    public void setSimilarities(
            @NotNull ObservableList<Set<Article>> similarities) {
        Validate.notNull(similarities, "Similarities must not be null.");
        this.similarities.set(similarities);
    }
}
