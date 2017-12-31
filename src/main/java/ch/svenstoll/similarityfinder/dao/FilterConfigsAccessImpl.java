/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.FilterConfig;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;

/**
 * An implementation of {@code FilterConfigsAccess} that allows the storing and retrieval of
 * filer configs.
 */
@Singleton
public final class FilterConfigsAccessImpl implements FilterConfigsAccess {
    @NotNull
    static final String EMPTY_JSON_ARRAY = "[]";

    @NotNull
    private final String configsLocation;
    @NotNull
    private final Gson gson = new Gson();

    /**
     * Creates a {@code FilterConfigAccess}.
     *
     * @param configsLocation the location of the JSON file that contains previously stored
     *                        filter configs
     * @throws IllegalArgumentException if {@code configsLocation} was {@code null}
     */
    @Inject
    public FilterConfigsAccessImpl(
            @NotNull @Named("FILTER_CONFIGS_LOCATION") String configsLocation) {
        this.configsLocation
                = Validate.notNull(configsLocation, "ConfigsLocation must not be null.");
    }

    /**
     * Retrieves a list of {@code FilterConfig} instances from the filter configs file (encoded
     * with UTF-8). Gson is used to translate the JSON file to Java objects (encoded with UTF-8).
     *
     * @return a list of retrieved {@code FilterConfig} instances
     * @throws FilterConfigsAccessException if an error occurred while parsing the filter configs
     *                                      file
     */
    @Override
    public synchronized @NotNull List<FilterConfig> retrieveFilterConfigsFromFile() {
        List<FilterConfig> filterConfigs = new ArrayList<>();

        File file = new File(configsLocation);

        if (!file.exists()) {
            createFileWithEmptyJsonArray(file);
            return filterConfigs;
        }

        try (InputStream in = new FileInputStream(file);
             JsonReader jsonReader = new JsonReader(new InputStreamReader(in, UTF_8))){
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                FilterConfig config = gson.fromJson(jsonReader, FilterConfig.class);
                filterConfigs.add(config);
            }
            jsonReader.endArray();
        } catch (IOException | JsonParseException e) {
            throw new FilterConfigsAccessException(e.getMessage(), e.getCause());
        }

        return filterConfigs;
    }

    /**
     * Saves a list of {@code FilterConfig} instances to the filter configs file (encoded with
     * UTF-8). Gson is used to translate the Java objects to the JSON format.
     *
     * @param filterConfigs a list of {@code FilterConfig} instances to be saved
     * @throws IllegalArgumentException if {@code filterConfigs} was {@code null} or contained
     *                                  {@code null} elements
     * @throws FilterConfigsAccessException if an error occurred while writing the {@code
     *                                      filterConfigs} to the filter configs file
     */
    @Override
    public synchronized void saveFilterConfigsToFile(@NotNull List<FilterConfig> filterConfigs) {
        Validate.notNull(filterConfigs, "FilterConfigs must not be null.");
        Validate.noNullElements(filterConfigs, "FilterConfig must not contain null elements.");

        File file = new File(configsLocation);

        try (OutputStream out = new FileOutputStream(file);
             JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, UTF_8))) {
            // Two white spaces are used for pretty printing.
            jsonWriter.setIndent("  ");

            jsonWriter.beginArray();
            for (FilterConfig config : filterConfigs) {
                gson.toJson(config, FilterConfig.class, jsonWriter);
            }
            jsonWriter.endArray();
        } catch (IOException | JsonParseException e) {
            throw new FilterConfigsAccessException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Creates a new file that contains an empty JSON array (encoded with UTF-8).
     *
     * @param file a {@code File} that does not exist, yet
     */
    private void createFileWithEmptyJsonArray(@NotNull File file) {
        try (OutputStream out = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(out, UTF_8)) {
            writer.write(EMPTY_JSON_ARRAY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
