package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

            // print all tokens until stream is exhausted 过滤掉长度小于3的token
            // This
            // demo
            // the
            // TokenStream
            // API
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }

            stream.end();
        } finally {
            stream.close();
        }
    }
}

