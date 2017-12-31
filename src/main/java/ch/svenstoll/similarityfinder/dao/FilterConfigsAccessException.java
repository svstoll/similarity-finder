/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import org.jetbrains.annotations.Nullable;

/**
 * A runtime exception that can be thrown if an error occurred while storing or retrieving filter
 * configs.
 */
public class FilterConfigsAccessException extends RuntimeException {
    /**
     * Constructs a {@code FilterConfigsAccessException}.
     */
    public FilterConfigsAccessException() {}

    /**
     * Constructs a {@code FilterConfigsAccessException} instance.
     *
     * @param  message the detail message (which is saved for later retrieval
     *                 by the {@link #getMessage()} method)
     * @param  cause the cause (which is saved for later retrieval by the
     *               {@link #getCause()} method)
     */
    public FilterConfigsAccessException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
