/* Copyright 2017 Sven Stoll. All rights reserved.

   Licensed under the MIT License. See LICENSE file in the project root for full license
   information. */

package ch.svenstoll.similarityfinder.domain;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;

/**
 * Represents articles that have been published in a medium.
 */
public final class Article {
    private static final int N_GRAM_SIZE = 3;

    private final int id;
    @Nullable
    private String title = "";
    @Nullable
    private String content = "";
    @Nullable
    private Medium medium = null;
    @Nullable
    private LocalDate publicationDate = null;
    @Nullable
    private String author = "";

    @NotNull
    private final Multiset<String> contentNGrams = HashMultiset.create();

    /**
     * Constructs an {@code Article} instance.
     *
     * @param id the id that identifies an article
     */
    public Article(int id) {
        this.id = id;
    }

    /**
     * Generates n-grams of the size specified by {@link Article#N_GRAM_SIZE} from the
     * {@link Article#content}.
     */
    private void generateContentNGrams() {
        contentNGrams.clear();

        if (content != null) {
            Reader reader = new StringReader(content);
            NGramTokenizer tokenizer = new NGramTokenizer(N_GRAM_SIZE, N_GRAM_SIZE);
            tokenizer.setReader(reader);
            try {
                tokenizer.reset();
                CharTermAttribute termAtt = tokenizer.getAttribute(CharTermAttribute.class);
                while(tokenizer.incrementToken()) {
                    String token = termAtt.toString();
                    contentNGrams.add(token);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public @Nullable String getContent() {
        return content;
    }

    /**
     * Sets the content property and generates the corresponding n-grams.
     *
     * @param content the content to set
     */
    public void setContent(@Nullable String content) {
        this.content = content;
        if (content != null) {
            generateContentNGrams();
        }
    }

    public @Nullable Medium getMedium() {
        return medium;
    }

    public void setMedium(@Nullable Medium medium) {
        this.medium = medium;
    }

    public @Nullable LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(@Nullable LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public @Nullable String getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    /**
     * Gets the n-grams of the content property whose size are specified by
     * {@link Article#N_GRAM_SIZE}.
     */
    public @NotNull Multiset<String> getContentNGrams() {
        return contentNGrams;
    }
}
