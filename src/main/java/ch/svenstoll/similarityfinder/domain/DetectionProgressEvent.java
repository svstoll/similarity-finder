/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.jetbrains.annotations.NotNull;

import java.util.EventObject;

/**
 * A subclass of {@code EventObject} that can be used to notify an instance of {@code
 * DetectionProgressListener} about the current progress of a {@code SimilarityDetector}.
 */
public final class DetectionProgressEvent extends EventObject {
    private final double progress;

    /**
     * Constructs a {@code DetectionProgressEvent} instance.
     *
     * @param source the {@code SimilarityDetector} instance on which the {@code
     *               DetectionProgressEvent} initially occurred
     * @param progress the current progress of a similarity detection process executed by the
     *                 {@code SimilarityDetector} specified in {@code source}
     * @throws IllegalArgumentException if source was {@code null}
     */
    public DetectionProgressEvent(@NotNull SimilarityDetector source, double progress) {
        super(source);
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }
}
