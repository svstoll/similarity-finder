/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import org.apache.commons.lang3.Validate;

/**
 * A runtime exception that can be used to indicate that too many articles have been found to
 * process.
 */
public final class MaxArticlesException extends RuntimeException {
    private final int maxArticles;
    private final int foundArticles;

    /**
     * Constructs a {@code MaxArticlesException}.
     *
     * @param maxArticles the maximum number of articles that will be processed. This value must
     *                    be smaller than {@code foundArticles}
     * @param foundArticles the number of articles found. This value must be greater than {@code
     *                      maxArticles}
     * @throws IllegalArgumentException if {@code foundArticles} was smaller than {@code
     *                                  maxArticles}
     */
    public MaxArticlesException(int maxArticles, int foundArticles) {
        super();

        Validate.isTrue(foundArticles > maxArticles,
                "FoundArticles must be greater than maxArticles.");

        this.maxArticles = maxArticles;
        this.foundArticles = foundArticles;
    }

    public int getMaxArticles() {
        return maxArticles;
    }

    public int getFoundArticles() {
        return foundArticles;
    }
}
