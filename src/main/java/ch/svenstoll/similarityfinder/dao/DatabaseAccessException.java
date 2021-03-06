/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.dao;

import org.jetbrains.annotations.Nullable;

/**
 * A runtime exception that can be thrown if an error occurred while querying the database.
 */
public class DatabaseAccessException extends RuntimeException {
    /**
     * Constructs a {@code DatabaseAccessException}.
     */
    public DatabaseAccessException() {}

    /**
     * Constructs a {@code DatabaseAccessException} instance.
     *
     * @param  message the detail message (which is saved for later retrieval by the
     *                 {@link #getMessage()} method)
     * @param  cause the cause (which is saved for later retrieval by the
     *               {@link #getCause()} method)
     */
    public DatabaseAccessException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
