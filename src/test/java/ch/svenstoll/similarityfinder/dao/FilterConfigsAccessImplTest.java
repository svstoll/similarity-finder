package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.FilterConfig;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ch.svenstoll.similarityfinder.dao.FilterConfigsAccessImpl.EMPTY_JSON_ARRAY;
import static org.junit.Assert.assertEquals;

public class FilterConfigsAccessImplTest {
    private static final String PATH_TO_NOT_EXISTING_CONFIGS_FILE
            = "src\\test\\resources\\filter-configs-access\\should-not-exist.json";
    private static final String PATH_TO_CORRUPTED_CONFIGS_FILE
            = "src\\test\\resources\\filter-configs-access\\corrupted-filter-configs.json";
    private static final String PATH_TO_CONFIGS_FILE_OF_SIZE_1
            = "src\\test\\resources\\filter-configs-access\\filter-configs-of-size-1.json";
    private static final String PATH_TO_OVERWRITABLE_CONFIGS_FILE
            = "src\\test\\resources\\filter-configs-access\\overwritable-filter-configs.json";

    // Values for the FilterConfig that is contained in every configs file of size 1 or greater.
    private static final String CONFIG_1_NAME = "config name 1";
    private static final LocalDateTime CONFIG_1_LAST_EDITED
            = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
    private static final List<String> CONFIG_1_SELECTED_MEDIA
            = Collections.singletonList("media 1");
    private static final double CONFIG_1_SIMILARITY_THRESHOLD = 0.5;
    private static final LocalDate CONFIG_1_START_DATE = null;
    private static final LocalDate CONFIG_1_END_DATE = null;
    private static final String CONFIG_1_TITLE = "title 1";
    private static final int CONFIG_1_MIN_LETTERS = 1000;
    private static final int CONFIG_1_MIN_SECONDS = 60;
    private static final boolean CONFIG_1_RELEVANT_ONLY = true;

    private FilterConfigsAccessImpl configsAccess;

    @After
    public void cleanUp() throws IOException {
        File notExistingFile = new File(PATH_TO_NOT_EXISTING_CONFIGS_FILE);
        if (notExistingFile.exists()) {
            notExistingFile.delete();
        }

        File overwritableFile = new File(PATH_TO_OVERWRITABLE_CONFIGS_FILE);
        StringReader reader = new StringReader(EMPTY_JSON_ARRAY);
        Files.asCharSink(overwritableFile, StandardCharsets.UTF_8).writeFrom(reader);
    }

    @Test
    public void readConfigsFromFile_givenFileDoesNotExist_shouldCreateNewFile() throws IOException {
        // Given:
        configsAccess = new FilterConfigsAccessImpl(PATH_TO_NOT_EXISTING_CONFIGS_FILE);

        // When:
        configsAccess.retrieveFilterConfigsFromFile();

        // Then:
        assertEquals(EMPTY_JSON_ARRAY, getFileAsString(PATH_TO_NOT_EXISTING_CONFIGS_FILE));
    }

    @Test(expected = FilterConfigsAccessException.class)
    public void readConfigsFromFile_givenFileIsCorrupted_shouldThrowException() {
        // Given:
        configsAccess = new FilterConfigsAccessImpl(PATH_TO_CORRUPTED_CONFIGS_FILE);

        // When:
        configsAccess.retrieveFilterConfigsFromFile();
    }

    @Test
    public void readConfigsFromFile_givenFileContainsOneConfig_shouldReturnOneConfig() {
        // Given:
        configsAccess = new FilterConfigsAccessImpl(PATH_TO_CONFIGS_FILE_OF_SIZE_1);

        // When:
        List<FilterConfig> result = configsAccess.retrieveFilterConfigsFromFile();

        // Then:
        assertEquals(1, result.size());
        assertEquals(CONFIG_1_NAME, result.get(0).getName());
        assertEquals(CONFIG_1_LAST_EDITED, result.get(0).getLastEdited());
        assertEquals(CONFIG_1_SIMILARITY_THRESHOLD , result.get(0).getSimilarityThreshold(), 0.001);
        assertEquals(CONFIG_1_SELECTED_MEDIA, result.get(0).getSelectedMedia());
        assertEquals(CONFIG_1_START_DATE, result.get(0).getFromDate());
        assertEquals(CONFIG_1_END_DATE, result.get(0).getToDate());
        assertEquals(CONFIG_1_TITLE, result.get(0).getTitle());
        assertEquals(CONFIG_1_MIN_LETTERS, result.get(0).getMinLetters());
        assertEquals(CONFIG_1_RELEVANT_ONLY, result.get(0).isRelevantOnly());
    }

    @Test
    public void saveConfigsToFile_givenFileDoesNotExist_shouldCreateNewFileWithConfig()
            throws IOException {
        // Given:
        configsAccess = new FilterConfigsAccessImpl(PATH_TO_NOT_EXISTING_CONFIGS_FILE);
        FilterConfig config = getFilterConfig1();

        // When:
        configsAccess.saveFilterConfigsToFile(Collections.singletonList(config));

        // Then:
        String actual = getFileAsString(PATH_TO_NOT_EXISTING_CONFIGS_FILE);
        String expected = getFileAsString(PATH_TO_CONFIGS_FILE_OF_SIZE_1);
        assertEquals(expected, actual);
    }

    @Test
    public void saveConfigsToFile_givenFileDoesExist_shouldOverwriteFileWithConfig()
            throws IOException {
        // Given:
        configsAccess = new FilterConfigsAccessImpl(PATH_TO_OVERWRITABLE_CONFIGS_FILE);
        FilterConfig config = getFilterConfig1();

        // When:
        configsAccess.saveFilterConfigsToFile(Collections.singletonList(config));

        // Then:
        String actual = getFileAsString(PATH_TO_OVERWRITABLE_CONFIGS_FILE);
        String expected = getFileAsString(PATH_TO_CONFIGS_FILE_OF_SIZE_1);
        assertEquals(expected, actual);
    }

    private String getFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        CharSource charSource = Files.asCharSource(file, StandardCharsets.UTF_8);
        return charSource.read().replaceAll("\r\n", "").replaceAll("\n", "");
    }

    private FilterConfig getFilterConfig1() {
        FilterConfig config = new FilterConfig(CONFIG_1_NAME, CONFIG_1_LAST_EDITED, null);
        config.setSimilarityThreshold(CONFIG_1_SIMILARITY_THRESHOLD);
        config.setSelectedMedia(CONFIG_1_SELECTED_MEDIA);
        config.setFromDate(CONFIG_1_START_DATE);
        config.setToDate(CONFIG_1_END_DATE);
        config.setTitle(CONFIG_1_TITLE);
        config.setMinLetters(CONFIG_1_MIN_LETTERS);
        config.setRelevantOnly(CONFIG_1_RELEVANT_ONLY);

        return config;
    }
}
