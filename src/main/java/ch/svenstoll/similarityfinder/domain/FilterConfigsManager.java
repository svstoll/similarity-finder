/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.FilterConfigsAccess;
import ch.svenstoll.similarityfinder.dao.FilterConfigsAccessException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A class that manages various filter configs.
 */
@Singleton
public final class FilterConfigsManager {
    @NotNull
    private final FilterConfigsAccess filterConfigsAccess;
    @NotNull
    private final Filter filter;
    @NotNull
    private final ListProperty<FilterConfig> filterConfigs = new SimpleListProperty<>();

    /**
     * Creates a {@code FilterConfigsManager}.
     *
     * @param filterConfigsAccess A instance of {@code FilterConfigsAccess} that is used to
     *                            retrieve and store filter configs
     * @param filter the {@code Filter} that is used are used to generate and restore filter
     *               configs
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public FilterConfigsManager(@NotNull FilterConfigsAccess filterConfigsAccess,
                                @NotNull Filter filter) {
        this.filterConfigsAccess = Validate.notNull(
                filterConfigsAccess, "FilterConfigAccess must not be null.");
        this.filter = Validate.notNull(filter, "Filter must not be null.");

        retrieveFilterConfigsAsync();
    }

    /**
     * Asynchronously retrieves filter configs that might have been stored in a previous session.
     */
    private void retrieveFilterConfigsAsync() {
        Runnable worker = () -> {
            try {
                filterConfigs.set(FXCollections.observableArrayList(
                        filterConfigsAccess.retrieveFilterConfigsFromFile()));
            } catch (FilterConfigsAccessException e) {
                e.printStackTrace();
                filterConfigs.set(FXCollections.observableArrayList());
            }
        };

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(worker);
    }

    /**
     * Adds a filter config and saves the change in the filter configs file used by the application.
     *
     * @param name the name of the filter config to be added
     * @throws IllegalArgumentException is {@code name} was {@code null}
     * @throws FilterConfigsAccessException if an error occurred while adding the filter config
     */
    public void addFilterConfig(@NotNull String name) {
        Validate.notNull(name, "Name must not be null.");

        FilterConfig config = new FilterConfig(name, LocalDateTime.now(), filter);
        filterConfigs.add(config);

        try {
            filterConfigsAccess.saveFilterConfigsToFile(filterConfigs);
        } catch (FilterConfigsAccessException e) {
            filterConfigs.remove(config);
            throw e;
        }
    }

    /**
     * Loads the specified filter config which will overwrite the current properties of the
     * {@code Filter} specified in this {@code FilterConfigsManager}.
     *
     * @param config the filter config to be loaded
     * @throws IllegalArgumentException if {@code config} was {@code null}
     */
    public void loadFilterConfig(@NotNull FilterConfig config) {
        Validate.notNull(config, "Config must not be null.");
        filter.restoreFromConfig(config);
    }

    /**
     * Removes the specified filter config and saves the change in the filter configs file used by
     * the application.
     *
     * @param config the filter config to be removed
     * @throws IllegalArgumentException if {@code config} was {@code null}
     * @throws FilterConfigsAccessException if an error occurred while deleting the filter config
     */
    public void removeFilterConfig(@NotNull FilterConfig config) {
        Validate.notNull(config, "Config must not be null.");

        if (filterConfigs.remove(config)) {
            try {
                filterConfigsAccess.saveFilterConfigsToFile(filterConfigs);
            } catch (FilterConfigsAccessException e) {
                filterConfigs.add(config);
                throw e;
            }
        }
    }

    /**
     * Overwrites the specified filter config with the current properties of the {@code Filter}
     * specified in this {@code FilterConfigsManager} and saves the change in the filter configs
     * file used by the application.
     *
     * @param overwritable the filter config to be removed
     * @throws IllegalArgumentException if {@code overwritable} was {@code null}
     * @throws FilterConfigsAccessException if an error occurred while overwriting the filter config
     */
    public void overwriteFilterConfig(@NotNull FilterConfig overwritable) {
        Validate.notNull(overwritable, "Overwritable must not be null.");

        FilterConfig newConfig =
                new FilterConfig(overwritable.getName(), LocalDateTime.now(), filter);
        filterConfigs.remove(overwritable);
        filterConfigs.add(newConfig);

        try {
            filterConfigsAccess.saveFilterConfigsToFile(filterConfigs);
        } catch (FilterConfigsAccessException e) {
            filterConfigs.remove(newConfig);
            filterConfigs.add(overwritable);
            throw e;
        }
    }

    public @NotNull ObservableList<FilterConfig> getFilterConfigs() {
        return filterConfigs.get();
    }

    public @NotNull ListProperty<FilterConfig> filterConfigsProperty() {
        return filterConfigs;
    }
}
