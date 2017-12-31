package ch.svenstoll.similarityfinder.domain;

import ch.svenstoll.similarityfinder.dao.SettingsAccessImpl;
import ch.svenstoll.similarityfinder.dao.SettingsAccessException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class SettingsTest {
    private Settings settings;
    private SettingsAccessImpl settingsAccess;

    @Before
    public void setUp() {
        settingsAccess = mock(SettingsAccessImpl.class);
        settings = new Settings(settingsAccess);
    }

    @Test
    public void saveSettings_givenSettingsCanBeSaved_shouldNotifyListeners() {
        // Given:
        SettingsUpdateListener listener = mock(SettingsUpdateListener.class);
        settings.addSettingsUpdatedListener(listener);

        // When:
        settings.saveSettings();

        // Then:
        verify(settingsAccess, times(1)).storeSettings(settings);
        verify(listener, times(1)).onSettingsUpdated(any());
    }


    @Test(expected = SettingsAccessException.class)
    public void saveSettings_givenSettingsCanNotBeSaved_shouldThrowException() {
        // Given:
        SettingsUpdateListener listener = mock(SettingsUpdateListener.class);
        settings.addSettingsUpdatedListener(listener);

        willThrow(new SettingsAccessException()).given(settingsAccess).storeSettings(settings);

        // When:
        settings.saveSettings();

        // Then:
        verify(listener, times(0)).onSettingsUpdated(any());
    }
}
