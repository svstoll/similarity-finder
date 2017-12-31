package ch.svenstoll.similarityfinder.domain;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArticleTest {
    private Article article;

    @Before
    public void setup() {
        article = new Article(1);
    }

    @Test
    public void setContent_givenContentIsNotProvided_shouldAdjustContentAndNGrams() {
        // When:
        article.setContent(null);

        // Then:
        assertEquals(null , article.getContent());
        assertEquals(0 , article.getContentNGrams().size());
    }

    @Test
    public void setContent_givenContentIsEmpty_shouldAdjustContentAndNGrams() {
        // Given:
        String content = "";

        // When:
        article.setContent("");

        // Then:
        assertEquals(content , article.getContent());
        assertEquals(0 , article.getContentNGrams().size());
    }

    @Test
    public void setContent_givenContentIsNotEmpty_shouldAdjustContentAndNGrams() {
        // Given:
        String content = "123";

        // When:
        article.setContent(content);

        // Then:
        assertEquals(content , article.getContent());
        assertEquals(1 , article.getContentNGrams().size());
    }
}
