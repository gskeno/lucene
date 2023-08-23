package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;

class PartOfSpeechTaggingFilter extends TokenFilter {
    PartOfSpeechAttribute posAtt = addAttribute(PartOfSpeechAttribute.class);
    CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected PartOfSpeechTaggingFilter(TokenStream input) {
        super(input);
    }

    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {return false;}
        posAtt.setPartOfSpeech(determinePOS(termAtt.buffer(), 0, termAtt.length()));
        return true;
    }

    // determine the part of speech for the given term
    protected PartOfSpeechAttribute.PartOfSpeech determinePOS(char[] term, int offset, int length) {
        // naive implementation that tags every uppercased word as noun
        if (length > 0 && Character.isUpperCase(term[0])) {
            return PartOfSpeechAttribute.PartOfSpeech.Noun;
        }
        return PartOfSpeechAttribute.PartOfSpeech.Unknown;
    }
}

public class PartOfSpeechAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        TokenStream result = new LengthFilter( source, 3, Integer.MAX_VALUE);
        result = new PartOfSpeechTaggingFilter(result);
        return new TokenStreamComponents(source, result);
    }

    public static void main(String[] args) throws IOException {
        // text to tokenize
        final String text = "This is a demo of the TokenStream API";

        PartOfSpeechAnalyzer analyzer = new PartOfSpeechAnalyzer();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        // get the CharTermAttribute from the TokenStream
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        // get the PartOfSpeechAttribute from the TokenStream
        PartOfSpeechAttribute posAtt = stream.addAttribute(PartOfSpeechAttribute.class);

        try {
            stream.reset();

            // print all tokens until stream is exhausted
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString() + ": " + posAtt.getPartOfSpeech());
            }

            stream.end();
        } finally {
            stream.close();
        }
    }
}
