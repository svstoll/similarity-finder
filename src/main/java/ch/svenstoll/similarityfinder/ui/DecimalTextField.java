/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.ui;

import javafx.scene.control.TextField;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * A subclass of {@link TextField} that can be used for user input that needs to be a decimal
 * number.
 */
public class DecimalTextField extends TextField {
    private static final double DEFAULT_MIN_VALUE = -Double.MAX_VALUE;
    private static final double DEFAULT_MAX_VALUE = Double.MAX_VALUE;

    @NotNull
    private DecimalFormat decimalFormat = new DecimalFormat();
    private double minValue = DEFAULT_MIN_VALUE;
    private double maxValue = DEFAULT_MAX_VALUE;

    /**
     * Constructs a {@code DecimalTextField}.
     */
    public DecimalTextField() {
        super();
    }

    /**
     * Formats the text property of this {@code DecimalTextField} so that it meets the
     * requirements of the specified {@code DecimalFormat}. If the text property can not be
     * parsed, because it is not a number, the specified {@code defaultText} will be set.
     *
     * @param defaultText a string that will be set if the current text property can not be parsed
     */
    public void formatTextProperty(String defaultText) {
        try {
            double numberToFormat = decimalFormat.parse(getText()).doubleValue();

            if (numberToFormat < minValue) {
                numberToFormat = minValue;
            } else if (numberToFormat > maxValue) {
                numberToFormat = maxValue;
            }
            setText(decimalFormat.format(numberToFormat));
        } catch (ParseException e) {
            setText(defaultText);
        }
    }

    /**
     * Returns the value that is represented by this {@code DecimalTextField}.
     *
     * @param defaultValue the value to be returned if the text property of this {@code
     *                     DecimalTextField} could not be parsed
     * @return the value that is represented by this {@code DecimalTextField}.
     */
    public Number parseTextFieldNumber(Number defaultValue) {
        try {
            return decimalFormat.parse(getText());
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    /**
     * Sets the text of this {@code DecimalTextField} to the string representation of the specified
     * {@code value}.
     *
     * @param value the value which will be formatted and set as the text of this {@code
     *              DecimalTextField}
     * @throws IllegalArgumentException if {@code value} was {@code <} the minimum value or
     *                                  {@code >} the maximum value of this {@code DecimalTextField}
     */
    public void setTextFieldNumber(Number value) {
        Validate.inclusiveBetween(minValue, maxValue, value.doubleValue(),
                "Value must be >= " + minValue + " and <= " + maxValue + ".");

        setText(decimalFormat.format(value));
    }

    public @NotNull DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    /**
     * Sets the decimal format property of this {@code DecimalTextfield}. This will automatically
     * reformat the text property.
     *
     * @param decimalFormat the {@code DecimalFormat} to set
     */
    public void setDecimalFormat(@NotNull DecimalFormat decimalFormat) {
        Validate.notNull(decimalFormat, "DecimalFormat must not be null.");

        this.decimalFormat = decimalFormat;
        formatTextProperty("");
    }

    public double getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value that the text property of this {@code DecimalTextField} can
     * represent. This will automatically reformat the text property.
     *
     * @param minValue the maximum value that this {@code DecimalTextField} can represent
     * @throws IllegalArgumentException if {@code minValue} is {@code > maxValue}
     */
    public void setMinValue(double minValue) {
        Validate.isTrue(Double.compare(minValue, maxValue) <= 0, "MinValue must be <= maxValue.");

        this.minValue = minValue;
        formatTextProperty("");
    }

    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value that the text property of this {@code DecimalTextField} can represent.
     *
     * @param maxValue the maximum value that this {@code DecimalTextField} can represent
     * @throws IllegalArgumentException if {@code maxValue} is {@code < minValue}
     */
    public void setMaxValue(double maxValue) {
        Validate.isTrue(Double.compare(maxValue, minValue) >= 0, "MaxValue must be >= minValue.");

        this.maxValue = maxValue;
        formatTextProperty("");
    }
}
