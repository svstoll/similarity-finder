/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

/**
 * A functional interface that allows the observation of a similarity detection process
 * of a {@code SimilarityDetector} instance.
 */
@FunctionalInterface
public interface DetectionProgressListener extends EventListener {
    /**
     * This method will be called whenever the observed {@code SimilarityDetector} instance fires a
     * {@code DetectionProgressEvent}.
     *
     * @param event the {@code DetectionProgressEvent} that was fired
     */
    void onDetectionProgress(@NotNull DetectionProgressEvent event);
}
