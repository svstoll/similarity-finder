/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Article;
import ch.svenstoll.similarityfinder.domain.Filter;
import ch.svenstoll.similarityfinder.domain.Medium;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface that should be implemented by classes that provide the possibility to query the
 * database used by the application.
 */
public interface DatabaseAccess {
    /**
     * Queries the database for all media that are stored.
     *
     * @return a list of {@code Medium} instances
     * @throws DatabaseAccessException if an error occurred while querying the database
     */
    @NotNull List<Medium> queryAllMedia();

    /**
     * Queries the database for articles that meet the requirements specified by the provided
     * {@code filter}.
     *
     * @param filter a {@code Filter} that specifies the contributions to be returned
     * @return a list of {@code Article} instances that meet the requirements of the {@code filter}
     * @throws IllegalArgumentException if {@code filter} was {@code null}
     * @throws DatabaseAccessException if an error occurred while querying the database
     */
    @NotNull List<Article> queryArticles(@NotNull Filter filter);
}
