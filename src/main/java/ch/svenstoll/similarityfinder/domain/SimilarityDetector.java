/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import com.google.common.collect.*;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.Multisets.union;
import static java.lang.Math.sqrt;

/**
 * A class that provides the possibility to detect articles that have similar contents according
 * to a specified similarity threshold value.In order to detect such similarities, frequency vectors
 * of n-grams are compared with each other using the concept of cosine similarity.
 */
public final class SimilarityDetector {
    public static final int MIN_SIMILARITY_INDEX = 0;
    public static final int MAX_SIMILARITY_INDEX = 1;

    @NotNull
    private final List<DetectionProgressListener> DetectionProgressListeners = new ArrayList<>();

    /**
     * Detects articles with similar contents and returns them as a list of sets. Two article
     * contents are considered similar if their cosine similarity index is greater or equal to
     * the provided {@code similarityThreshold} or there exists another article that is similar
     * to both of them.
     * <p>
     * Because this method has a polynomial time complexity, multithreading is used to improve
     * performance. Subscribed {@code DetectionProgressListener} instances will be notified about
     * the progress of the detection process.
     * </p>
     *
     * @param articles a list of articles used to detect similar contents
     * @param similarityThreshold a number between (inclusive) {@link #MIN_SIMILARITY_INDEX} (total
     *                            inequality) and {@link #MAX_SIMILARITY_INDEX} (total equality)
     * @return a list of sets that contain all articles that are similar to each other with
     *         respect to the specified {@code similarityThreshold}
     * @throws DetectionAbortedException if the thread executing this method has been interrupted
     * @throws IllegalArgumentException if {@code articles} was {@code null} or contained
     *                                  {@code null} elements or if the specified {@code
     *                                  similarityThreshold} was not within the bounds of
     *                                  {@link #MIN_SIMILARITY_INDEX} and
     *                                  {@link #MAX_SIMILARITY_INDEX}
     */
    public @NotNull List<Set<Article>> detectArticlesWithSimilarContents(
            @NotNull List<Article> articles, double similarityThreshold) {
        Validate.notNull(articles, "Articles must not be null.");
        Validate.noNullElements(articles, "Articles must not contain null elements.");
        String thresholdBoundsMessage = "SimilarityThreshold must be between "
                + MIN_SIMILARITY_INDEX + " and " + MAX_SIMILARITY_INDEX + " (inclusive).";
        Validate.inclusiveBetween(MIN_SIMILARITY_INDEX, MAX_SIMILARITY_INDEX, similarityThreshold,
                thresholdBoundsMessage);

        // Due to multithreading, a synchronized hash map is used to organize the sets of similar
        // articles. Each article must not be present in more than one set (see
        // similarity definition in the method comment). As a key to a set, any ID of an
        // article in that particular set can be used.
        Map<Integer, Set<Article>> similaritiesMap = Collections.synchronizedMap(new HashMap<>());

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Counter used to calculate progress.
        int loopCount = 0;

        for (Article c1 : articles) {
            loopCount++;

            // Prepare multithreading in inner loop.
            ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);

            for (Article c2 : articles) {
                String c1Content = c1.getContent();
                String c2Content = c2.getContent();

                if (c1Content != null && c2Content != null && !c1.equals(c2)) {
                    // For every pair of articles the similarity detection is done in a new
                    // thread.
                    Runnable worker = () -> {
                        // The worker is executed with minimal thread priority to keep the JavaFX
                        // thread from starving which would result in UI freezes.
                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                        if (hasSimilarContent(c1, c2, similarityThreshold)) {
                            addSimilarArticles(similaritiesMap, c1, c2);
                        }

                    };
                    executorService.execute(worker);
                }
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DetectionAbortedException(e.getMessage(), e.getCause());
            }

            // The size of articles will never be zero at this point.
            final double progress = (double) loopCount / articles.size();
            fireProgressEvent(new DetectionProgressEvent(this, progress));
        }

        fireProgressEvent(new DetectionProgressEvent(this, 1));

        return createSimilaritiesListFromMap(similaritiesMap);
    }

    /**
     * Determines whether two {@code Article} objects have similar content properties based
     * on the given similarity threshold value.
     *
     * @param article1 an {@code Article} whose content will be compared with {@code article2}
     * @param article2 an {@code Article} whose content will be compared with {@code article1}
     * @param similarityThreshold A number between (inclusive) {@link #MIN_SIMILARITY_INDEX} (total
     *                            inequality) and {@link #MAX_SIMILARITY_INDEX} (total equality)
     * @return {@code true} if the content property of the given {@code Article} objects are
     *         similar, otherwise {@code false} is returned.
     * @throws IllegalArgumentException if {@code article1} or {@code article2} were {@code null}
     */
    private boolean hasSimilarContent(@NotNull Article article1, @NotNull Article article2,
                                      double similarityThreshold) {
        Validate.notNull(article1, "Article1 must not be null.");
        Validate.notNull(article2, "Article2 must not be null.");

        // If the similarityThreshold is equal to its min value, every content can be interpreted
        // as similar.
        if (Double.compare(similarityThreshold, MIN_SIMILARITY_INDEX) <= 0) {
            return true;
        }

        // No need to calculate a similarity score if the contents are equal.
        if (Objects.equals(article1.getContent(), article2.getContent())) {
            return true;
        }

        Multiset<String> content1NGrams = article1.getContentNGrams();
        Multiset<String> content2NGrams = article2.getContentNGrams();
        double score = calculateCosineSimilarity(content1NGrams, content2NGrams);

        return score >= similarityThreshold;
    }

    /**
     * Calculates the cosine similarity index of two n-gram multisets. Total inequality is
     * represented by the value {@code 0} and total equality by the value {@code 1}. If any of
     * the two multisets are empty, the value {@code 0} is returned.
     *
     * @param nGramsA a {@code Multiset} of n-grams
     * @param nGramsB a {@code Multiset} of n-grams
     * @return the cosine similarity index of {@code nGramsA} and {@code nGramsB}
     * @throws IllegalArgumentException if {@code nGramsA} or {@code nGramsB} was {@code null}
     */
    private double calculateCosineSimilarity(@NotNull final Multiset<String> nGramsA,
                                             @NotNull final Multiset<String> nGramsB) {
        Validate.notNull(nGramsA, "NGramsA must not be null.");
        Validate.notNull(nGramsB, "NGramsB must not be null.");

        if (nGramsA.size() == 0 || nGramsB.size() == 0) {
            return 0;
        }

        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;

        for (final String entry : union(nGramsA, nGramsB).elementSet()) {
            final double aCount = nGramsA.count(entry);
            final double bCount = nGramsB.count(entry);

            dotProduct += aCount * bCount;
            magnitudeA += aCount * aCount;
            magnitudeB += bCount * bCount;
        }

        // Cosine similarity formula: a·b / (||a|| * ||b||)
        return dotProduct / (sqrt(magnitudeA) * sqrt(magnitudeB));
    }

    /**
     * Adds two similar {@code Article} instance to the given {@code similaritiesMap}. Each
     * article will only be present in one set (see similarity definition in
     * {@link #detectArticlesWithSimilarContents(List, double)}. As a key to a set, any ID of an
     * article in that particular set can be used.
     *
     * @param similaritiesMap a map whose keys point to sets of similar articles
     * @param article1 an {@code Article} whose content is similar to {@code article2}
     * @param article2 an {@code Article} whose content is similar to {@code article1}
     * @throws IllegalArgumentException if {@code similaritiesMap}, {@code article1} or {@code
     *                                  article2} was {@code null}
     */
    private synchronized void addSimilarArticles(
            @NotNull Map<Integer, Set<Article>> similaritiesMap,
            @NotNull Article article1, @NotNull Article article2) {
        Validate.notNull(similaritiesMap, "SimilaritiesMap must not be null.");
        Validate.notNull(article1, "Article1 must not be null.");
        Validate.notNull(article2, "Article2 must not be null.");

        int c1Key = article1.getId();
        int c2Key = article2.getId();
        Set<Article> set1 = similaritiesMap.get(c1Key);
        Set<Article> set2 = similaritiesMap.get(c2Key);

        if (set1 == null) {
            if (set2 == null) { // No set exists.
                Set<Article> set = new HashSet<>();
                set.add(article1);
                set.add(article2);
                similaritiesMap.put(c1Key, set);
                similaritiesMap.put(c2Key, set);
            } else { // Only set2 exists.
                set2.add(article1);
                similaritiesMap.put(c1Key, set2);
            }
        } else {
            if (set2 == null) { // Only set1 exists.
                set1.add(article2);
                similaritiesMap.put(c2Key, set1);
            } else { // Both sets exist.
                if (!set1.equals(set2)) {
                    set1.addAll(set2);
                    set2.forEach(article -> {
                        int keyForSet2 = article.getId();
                        similaritiesMap.put(keyForSet2, set1);
                    });
                }
            }
        }
    }

    /**
     * Creates a list of sets that contain articles that are similar to each other based on
     * the specified {@code similaritiesMap}.
     *
     * @param similaritiesMap a map whose keys point to sets of similar articles
     * @return a list of sets that contain articles that have similar contents
     * @throws IllegalArgumentException if {@code similaritiesMap} was {@code null}
     */
    private @NotNull List<Set<Article>> createSimilaritiesListFromMap(
            @NotNull Map<Integer, Set<Article>> similaritiesMap) {
        Validate.notNull(similaritiesMap, "SimilaritiesMap must not be null.");

        List<Set<Article>> similarities = new ArrayList<>();
        similaritiesMap.forEach((key, similaritySet) -> {
            // Check if the set is not already in the list, because many keys may point to the
            // same set.
            if (!similarities.contains(similaritySet)) {
                similarities.add(similaritySet);
            }
        });
        return similarities;
    }

    /**
     * Adds a {@code DetectionProgressListener} that will be notified whenever a
     * {@code DetectionProgressEvent} has occurred.
     *
     * @param listener an instance of {@code DetectionProgressListener} to be notified
     * @throws IllegalArgumentException if {@code listener} was {@code null}
     */
    public void addProgressListener(@NotNull DetectionProgressListener listener) {
        Validate.notNull(listener, "Listener must not be null.");
        DetectionProgressListeners.add(listener);
    }

    /**
     * Notifies listeners that a {@code DetectionProgressEvent} has occurred.
     *
     * @param event the {@code DetectionProgressEvent} to fire
     * @throws IllegalArgumentException if {@code event} was {@code null}
     */
    private void fireProgressEvent(@NotNull DetectionProgressEvent event) {
        Validate.notNull(event, "Event must not be null.");
        DetectionProgressListeners.forEach(listener -> listener.onDetectionProgress(event));
    }
}
