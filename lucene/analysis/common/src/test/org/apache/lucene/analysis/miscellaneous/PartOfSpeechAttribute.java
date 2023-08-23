package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.util.Attribute;

/**
 * https://lucene.apache.org/core/9_7_0/core/org/apache/lucene/analysis/package-summary.html
 */
public interface PartOfSpeechAttribute extends Attribute {
    enum PartOfSpeech {
        Noun, Verb, Adjective, Adverb, Pronoun, Preposition, Conjunction, Article, Unknown
    }

    public void setPartOfSpeech(PartOfSpeech pos);

    public PartOfSpeech getPartOfSpeech();
}
