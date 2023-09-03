package org.apache.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestIndex {

    @Test
    public void testIndex() throws URISyntaxException, IOException {
        String longTerm = "longtermlongtermlongtermlongtermlongtermlongtermlongtermlongtermlongtermlongtermlongtermlongtermlongterm";
        String text = "This is the text to be indexed. " + longTerm;
        URL resource = this.getClass().getResource("/");
        // 在指定位置，创建临时目录，方便查看分析文件；且测试方法末尾不删除临时文件
        Path indexPath = Files.createTempDirectory(Paths.get(resource.toURI()), "tempIndex");
        try (Directory dir = FSDirectory.open(indexPath)) {
            Analyzer analyzer = new StandardAnalyzer();
            try (IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(analyzer))) {
                Document doc = new Document();
                TextField field = new TextField("fieldname", text, Field.Store.YES);
                doc.add(field);
                iw.addDocument(doc);
            }
        }
    }
}
