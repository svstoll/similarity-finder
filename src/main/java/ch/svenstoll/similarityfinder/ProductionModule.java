/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder;

import ch.svenstoll.similarityfinder.dao.*;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

/**
 * This class configures the dependency injection framework Guice in a production environment.
 */
final class ProductionModule extends AbstractModule {
    @NotNull
    private static final String JDBC_DRIVER = "jdbc:postgresql://";
    @NotNull
    private static final String FILTER_CONFIGS_LOCATION = "filterConfigs.json";
    @NotNull
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(App.class);

    /**
     * Configures a {@code Binder} to be used in a production environment.
     */
    @Override
    protected void configure() {
        bind(DatabaseAccess.class).to(DatabaseAccessImpl.class);
        bind(SettingsAccess.class).to(SettingsAccessImpl.class);
        bind(FilterConfigsAccess.class).to(FilterConfigsAccessImpl.class);
        bind(Preferences.class).toInstance(PREFERENCES);

        bindConstant().annotatedWith(Names.named("JDBC_DRIVER")).to(JDBC_DRIVER);
        bindConstant().annotatedWith(Names.named("FILTER_CONFIGS_LOCATION"))
                .to(FILTER_CONFIGS_LOCATION);
    }
}
