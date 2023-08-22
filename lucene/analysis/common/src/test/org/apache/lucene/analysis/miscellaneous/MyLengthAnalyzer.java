package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.IOException;
import java.io.StringReader;

public class MyLengthAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        WhitespaceTokenizer whitespaceTokenizer = new WhitespaceTokenizer();
        // 包装者设计模式
        // 会过滤掉长度<3的token
        LengthFilter lengthFilter = new LengthFilter(whitespaceTokenizer, 3, Integer.MAX_VALUE);
        TokenStreamComponents tokenStreamComponents = new TokenStreamComponents(whitespaceTokenizer, lengthFilter);
        return tokenStreamComponents;
    }

    public static void main(String[] args) throws IOException {
        // text to tokenize, 长度小于3的词条不会被输出
        final String text = "This is a demo of the TokenStream API";

        MyLengthAnalyzer analyzer = new MyLengthAnalyzer();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        // get the CharTermAttribute from the TokenStream
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        try {
            stream.reset();

            // print all tokens until stream is exhausted
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }

            stream.end();
        } finally {
            stream.close();
        }
    }
}

/**
 * https://lucene.apache.org/core/9_7_0/core/org/apache/lucene/analysis/package-summary.html
 */
interface PartOfSpeechAttribute extends Attribute {
    public enum PartOfSpeech {
        Noun, Verb, Adjective, Adverb, Pronoun, Preposition, Conjunction, Article, Unknown
    }

    public void setPartOfSpeech(PartOfSpeech pos);

    public PartOfSpeech getPartOfSpeech();
}

final class PartOfSpeechAttributeImpl extends AttributeImpl
        implements PartOfSpeechAttribute {

    private PartOfSpeech pos = PartOfSpeech.Unknown;

    public void setPartOfSpeech(PartOfSpeech pos) {
        this.pos = pos;
    }

    public PartOfSpeech getPartOfSpeech() {
        return pos;
    }

    @Override
    public void clear() {
        pos = PartOfSpeech.Unknown;
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {

    }

    @Override
    public void copyTo(AttributeImpl target) {
        ((PartOfSpeechAttribute) target).setPartOfSpeech(pos);
    }
}
