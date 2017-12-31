package ch.svenstoll.similarityfinder.domain;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SimilarityDetectorTest {
    private SimilarityDetector detector;

    @Before
    public void setUp() {
        detector = new SimilarityDetector();
    }

    @Test
    public void detectArticlesWithSimilarContent_givenArticlesIsEmpty_shouldReturnEmptyList() {
        // Given:
        List<Article> articles = new ArrayList<>();

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 0);

        // Then:
        assertEquals(0, similarities.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void detectArticlesWithSimilarContent_givenListIsNotProvided_shouldThrowException() {
        // When:
        detector.detectArticlesWithSimilarContents(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void
    detectArticlesWithSimilarContent_givenArticlesContainsNullElements_shouldThrowException() {
        // Given:
        Article c1 = generateArticle(1, "content1");
        Article c2 = generateArticle(2, "content2");
        List<Article> articles = Arrays.asList(c1, c2, null);

        // When:
        detector.detectArticlesWithSimilarContents(articles, 1);
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenIdenticalContentWithMinThreshold_shouldReturnListWithOneSet() {
        // Given:
        Article c1 = generateArticle(1, "content");
        Article c2 = generateArticle(2, "content");
        Article c3 = generateArticle(3, "content");
        List<Article> articles = Arrays.asList(c1, c2, c3);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 0);

        // Then:
        assertEquals(1, similarities.size());
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenIdenticalContentAndMaxThreshold_shouldReturnListWithOneSet() {
        // Given:
        Article c1 = generateArticle(1, "content");
        Article c2 = generateArticle(2, "content");
        Article c3 = generateArticle(3, "content");
        List<Article> articles = Arrays.asList(c1, c2, c3);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 1);

        // Then:
        assertEquals(1, similarities.size());
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenNearlyIdenticalContentAndMaxThreshold_shouldReturnEmptyList() {
        // Given:
        Article c1 = generateArticle(1, "content 1");
        Article c2 = generateArticle(2, "content 2");
        Article c3 = generateArticle(3, "content 3");
        List<Article> articles = Arrays.asList(c1, c2, c3);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 1);

        // Then:
        assertEquals(0, similarities.size());
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenNearlyIdenticalContentAndMinThreshold_shouldReturnListWithOneSet() {
        // Given:
        Article c1 = generateArticle(1, "content 1");
        Article c2 = generateArticle(2, "content 2");
        Article c3 = generateArticle(3, "content 3");
        List<Article> articles = Arrays.asList(c1, c2, c3);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 0);

        // Then:
        assertEquals(1, similarities.size());
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenCompletelyDifferentContentAndMinThreshold_shouldReturnListWithOneSet() {
        // Given:
        Article c1 = generateArticle(1, "0123456789");
        Article c2 = generateArticle(2, "This is some test content.");
        Article c3 = generateArticle(3, "¦@#°§¬|¢äüö_,<>!?+*");
        List<Article> articles = Arrays.asList(c1, c2, c3);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 0);

        // Then:
        assertEquals(1, similarities.size());
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenTwoPairsWithIdenticalContentAndMaxThreshold_shouldReturnListWithTwoSets() {
        // Given:
        Article c1 = generateArticle(1, "text 1");
        Article c2 = generateArticle(2, "text 1");
        Article c3 = generateArticle(3, "text 2");
        Article c4 = generateArticle(4, "text 2");
        List<Article> articles = Arrays.asList(c1, c2, c3, c4);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 1);

        // Then:
        assertEquals(2, similarities.size());
        Set<Article> actualSet1 = similarities.get(0);
        Set<Article> actualSet2 = similarities.get(1);
        Set<Article> expectedSet1 = new HashSet<>();
        expectedSet1.add(c1);
        expectedSet1.add(c2);
        Set<Article> expectedSet2 = new HashSet<>();
        expectedSet2.add(c3);
        expectedSet2.add(c4);

        assertNotEquals(actualSet1, actualSet2);
        assertThat(actualSet1, Matchers.either(Matchers.equalTo(expectedSet1))
                .or(Matchers.equalTo(expectedSet2)));
        assertThat(actualSet2, Matchers.either(Matchers.equalTo(expectedSet1))
                .or(Matchers.equalTo(expectedSet2)));
    }

    @Test
    public void
    detectArticlesWithSimilarContent_givenSimilarContentAndHighThreshold_shouldReturnListWithOneSet() {
        // Given:
        Article c1 = generateArticle(1, "123456789");
        Article c2 = generateArticle(2, "1234567890...");
        Article c3 = generateArticle(3, "1234567890..");
        Article c4 = generateArticle(4, "1234567890.");
        Article c5 = generateArticle(5, "1234567890");
        List<Article> articles = Arrays.asList(c1, c2, c3, c4, c5);

        // When:
        List<Set<Article>> similarities =
                detector.detectArticlesWithSimilarContents(articles, 0.9);

        // Then:
        assertEquals(1, similarities.size());
        assertThat(similarities.get(0), containsInAnyOrder(articles.toArray()));
    }

    private Article generateArticle(int id, String content) {
        Article article = new Article(id);
        article.setContent(content);

        return article;
    }
}
