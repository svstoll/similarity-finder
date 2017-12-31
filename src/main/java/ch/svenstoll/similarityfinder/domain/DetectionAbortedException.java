/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A runtime exception that can be thrown if the similarity detection process of a {@code
 * SimilarityDetector} has been aborted.
 */
public final class DetectionAbortedException extends RuntimeException {

    /**
     * Constructs a {@code DetectionAbortedException} instance.
     */
    public DetectionAbortedException() {}

    /**
     * Constructs a {@code DetectionAbortedException} instance.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method)
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method)
     */
    public DetectionAbortedException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
