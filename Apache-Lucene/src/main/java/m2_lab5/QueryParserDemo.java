package m2_lab5;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class QueryParserDemo {

	public static String FILES_TO_INDEX_DIRECTORY = "./documents";
	public static String INDEX_DIRECTORY = "./index";
	static int hitsPerPage = 10;

	public static final String FIELD_PATH     = "path";
	public static final String FIELD_CONTENTS = "contents";

        ////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
            utils.MyUtils.delete(new File (INDEX_DIRECTORY));
            createIndex();
            searchIndexWithQueryParser(args[0]);
	}
        ////////////////////////////////////////////////////////////////////////
        public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
            Directory dir = FSDirectory.open(new File(INDEX_DIRECTORY));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
            IndexWriter Writer = new IndexWriter(dir, iwc);
            File dir1 = new File(FILES_TO_INDEX_DIRECTORY);
            File[] files = dir1.listFiles();
            for (File file : files) {
                    Document document = new Document();

                    String path = file.getName();
                    document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.NOT_ANALYZED));

                    Reader reader = new FileReader(file);
                    document.add(new Field(FIELD_CONTENTS, reader));

                    Writer.addDocument(document);
            }
          Writer.close();
        }        
        ////////////////////////////////////////////////////////////////////////
	public static void searchIndexWithQueryParser(String searchString) throws IOException, ParseException {
		//System.out.println("\nSearching for '" + searchString + "' using QueryParser");

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIRECTORY)));
		IndexSearcher searcher = new IndexSearcher(reader);

		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
		Query query = queryParser.parse(searchString);
		//System.out.println("Type of query: " + query.getClass().getSimpleName());

		 displayQuery(query);
		 TopDocs results = searcher.search(query, 5 * hitsPerPage);
		 ScoreDoc[] hits = results.scoreDocs;
		 displayHits(hits);
	}
        //////////////////////////////////////////////////////////////////////// 
	public static void displayHits(ScoreDoc[] hits) throws CorruptIndexException, IOException {
		System.out.println("Number of hits: " + hits.length);

		Iterator<ScoreDoc> it = Arrays.asList(hits).iterator();
		while (it.hasNext()) {
			ScoreDoc hitt = it.next();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIRECTORY)));
			IndexSearcher searcher = new IndexSearcher(reader);
			int docId = hitt.doc;
			Document document = searcher.doc(docId);
			String path = document.get(FIELD_PATH);
			System.out.println("Hit: " + path); 
		}
	}
        ////////////////////////////////////////////////////////////////////////
	public static void displayQuery(Query query) {
		System.out.println("Query: " + query.toString());
	}
        ////////////////////////////////////////////////////////////////////////
}