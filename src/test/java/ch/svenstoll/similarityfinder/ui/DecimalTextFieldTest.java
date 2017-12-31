package ch.svenstoll.similarityfinder.ui;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.junit.Assert.assertEquals;

public class DecimalTextFieldTest {
    @Rule
    public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

    private DecimalTextField decimalField;

    @Before
    public void setUp() {
        decimalField = new DecimalTextField();
    }

    @Test
    public void formatTextProperty_givenStandardFormatAndParsableText_shouldFormatValue() {
        // Given:
        decimalField.setText("100withTail");

        // When:
        decimalField.formatTextProperty("");

        // Then:
        assertEquals(decimalField.getText(), "100");
    }

    @Test
    public void formatTextProperty_givenStandardFormatAndUnparsableText_shouldSetProvidedString() {
        // Given:
        decimalField.setText("unparsable");

        // When:
        decimalField.formatTextProperty("default");

        // Then:
        assertEquals(decimalField.getText(), "default");
    }

    @Test
    public void parseTextFieldNumber_givenStandardFormatAndParsableText_shouldReturnParsedValue() {
        // Given:
        decimalField.setText("100");

        // When:
        Number value = decimalField.parseTextFieldNumber(0);

        // Then:
        assertEquals(100, value.intValue());
    }

    @Test
    public void
    parseTextFieldNumber_givenStandardFormatAndUnparsableText_shouldReturnProvidedDefaultValue() {
        // Given:
        decimalField.setText("unparsable");

        // When:
        Number value = decimalField.parseTextFieldNumber(0);

        // Then:
        assertEquals(0, value.intValue());
    }

    @Test
    public void
    setTextFieldNumber_givenStandardFormatAndNumberWithinBoundaries_shouldSetFormattedNumber() {
        // Given:
        decimalField.setMinValue(0);
        decimalField.setMaxValue(0);

        // When:
        decimalField.setTextFieldNumber(0);

        // Then:
        assertEquals(decimalField.getText(), "0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void
    setTextFieldNumber_givenStandardFormatAndNumberOutsideBoundaries_shouldThrowException() {
        // Given:
        decimalField.setMinValue(0);
        decimalField.setMaxValue(0);

        // When:
        decimalField.setTextFieldNumber(1);
    }

    @Test
    public void
    setDecimalFormat_givenDecimalFormatIsProvided_shouldSetNewFormatAndAdjustTextProperty() {
        // Given:
        decimalField.setText("1.01");
        DecimalFormat format = new DecimalFormat();
        format.setParseIntegerOnly(true);

        // When:
        decimalField.setDecimalFormat(format);

        // Then:
        assertEquals(decimalField.getDecimalFormat(), format);
        assertEquals(decimalField.getText(), "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMinValue_givenProvidedValueIsGreaterThanMaxValue_shouldThrowException() {
        // Given:
        decimalField.setMaxValue(0);

        // When:
        decimalField.setMinValue(1);
    }

    @Test
    public void
    setMinValue_givenProvidedValueIsNotGreaterThanTextFieldValue_shouldOnlySetMinValue() {
        // Given:
        String initialText = "0";
        decimalField.setText(initialText);
        decimalField.setMaxValue(0);

        // When:
        double newMinValue = 0;
        decimalField.setMinValue(newMinValue);

        // Then:
        assertEquals(decimalField.getMinValue(), newMinValue, 0.001);
        assertEquals(decimalField.getText(), initialText);
    }

    @Test
    public void
    setMinValue_givenProvidedValueIsGreaterThanTextFieldValue_shouldAdjustTextProperty() {
        // Given:
        decimalField.setText("0");
        decimalField.setMaxValue(1);

        // When:
        double newMinValue = 1;
        decimalField.setMinValue(newMinValue);

        // Then:
        assertEquals(decimalField.getMinValue(), newMinValue, 0.001);
        assertEquals(decimalField.getText(), "1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMaxValue_givenProvidedValueIsSmallerThanMinValue_shouldThrowException() {
        // Given:
        decimalField.setMinValue(1);

        // When:
        decimalField.setMaxValue(0);
    }

    @Test
    public void
    setMaxValue_givenProvidedValueIsNotSmallerThanTextFieldValue_shouldOnlySetMaxValue() {
        // Given:
        String initialText = "0";
        decimalField.setText(initialText);
        decimalField.setMinValue(0);

        // When:
        double newMaxValue = 0;
        decimalField.setMaxValue(newMaxValue);

        // Then:
        assertEquals(decimalField.getMaxValue(), newMaxValue, 0.001);
        assertEquals(decimalField.getText(), initialText);
    }

    @Test
    public void
    setMaxValue_givenProvidedValueIsSmallerThanTextFieldValue_shouldAdjustTextProperty() {
        // Given:
        decimalField.setText("1");
        decimalField.setMinValue(0);

        // When:
        double newMaxValue = 0;
        decimalField.setMaxValue(newMaxValue);

        // Then:
        assertEquals(decimalField.getMaxValue(), newMaxValue, 0.001);
        assertEquals(decimalField.getText(), "0");
    }
}
