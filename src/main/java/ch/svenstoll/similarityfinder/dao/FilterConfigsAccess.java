/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.FilterConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface that should be implemented by classes that allow the storing and retrieval of
 * filer configs.
 */
public interface FilterConfigsAccess {
    /**
     * Retrieves a list of {@code FilterConfig} instances from the filter configs file of the
     * application.
     *
     * @return a list of retrieved {@code FilterConfig} instances
     * @throws FilterConfigsAccessException if an error occurred while parsing the filter configs
     *                                      file
     */
    @NotNull List<FilterConfig> retrieveFilterConfigsFromFile();

    /**
     * Saves a list of {@code FilterConfig} instances to the filter configs file of the application.
     *
     * @param filterConfigs a list of {@code FilterConfig} instances to be saved
     * @throws IllegalArgumentException if {@code filterConfigs} was {@code null} or contained
     *                                  {@code null} elements
     * @throws FilterConfigsAccessException if an error occurred while writing the {@code
     *                                      filterConfigs} to the filter configs file
     */
    void saveFilterConfigsToFile(@NotNull List<FilterConfig> filterConfigs);
}
