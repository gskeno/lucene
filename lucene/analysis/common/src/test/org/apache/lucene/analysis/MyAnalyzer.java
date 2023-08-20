package org.apache.lucene.analysis;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class MyAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new WhitespaceTokenizer());
    }

    public static void main(String[] args) throws IOException {
        // text to tokenize
        final String text = " This is a demo of the TokenStream 😊 API";

        MyAnalyzer analyzer = new MyAnalyzer();
        // tokenStream会回调上面的createComponents
        // 这里返回的stream就是上面的WhitespaceTokenizer
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

    /**
     * https://www.cnblogs.com/sariseBlog/p/14946408.html
     */
    @Test
    public void testCodePoint(){
        char[] arr = new char[]{'i', 'm' , ' ', 'g', 'o', 'o', 'd', ' ', '\uD83D', '\uDE03', '\u4E2D', '\u56FD'};
        for (int i = 0; i < arr.length; i++) {
            // 一个代码点，可能需要一个char，也可能需要两个char
            // 一个代码点，表示一个真正现实生活中的字符，比如笑脸表情😃就需要两个char, '\uD83D', '\uDE03'
            int codePoint = Character.codePointAt(arr, i, arr.length);
            System.out.println(codePoint);
            // 笑脸表情
            if (codePoint == 128515){
                System.out.println(new String(arr, i, 2));
            }
            // 中
            if (codePoint == 20013){
                System.out.println(new String(arr, i, 1));
            }
            // 国
            if (codePoint == 22269){
                System.out.println(new String(arr, i, 1));
            }
        }
    }
}
