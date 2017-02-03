package m2_lab5;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
//import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneBooleanQueryDemo {

	public static String FILES_TO_INDEX_DIRECTORY = "./documents";
	public static String INDEX_DIRECTORY = "./index";
	static int hitsPerPage = 10;

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";

	public static void main(String[] args) throws Exception {

                utils.MyUtils.delete(new File (INDEX_DIRECTORY));
		createIndex();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		BooleanQuery booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "mushrooms")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "steak")), Occur.MUST)
										.build();
		displayQuery(booleanQuery);
			 
                
		TopDocs results = searcher.search(booleanQuery, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		displayHits(hits);
                
                
                
                
		booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "milk")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "eggs")), Occur.MUST)
										.build();
		displayQuery(booleanQuery);
		results = searcher.search(booleanQuery, 5 * hitsPerPage);
                hits = results.scoreDocs;
		displayHits(hits);
                
                

		booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "mushrooms")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "steak")), Occur.SHOULD)
										.build();
		displayQuery(booleanQuery);
		results = searcher.search(booleanQuery, 5 * hitsPerPage);
                hits = results.scoreDocs;
		displayHits(hits);
                
		booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "cheese")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "mushrooms")), Occur.SHOULD)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "steak")), Occur.SHOULD)
										.build();
		displayQuery(booleanQuery);
		results = searcher.search(booleanQuery, 5 * hitsPerPage);
                hits = results.scoreDocs;
		displayHits(hits);
                
		booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "cheese")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "mushrooms")), Occur.MUST_NOT)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "steak")), Occur.SHOULD)
										.build();
		displayQuery(booleanQuery);
		results = searcher.search(booleanQuery, 5 * hitsPerPage);
                hits = results.scoreDocs;
		displayHits(hits);    
                
		booleanQuery = new BooleanQuery.Builder()
										.add(new TermQuery(new Term(FIELD_CONTENTS, "cheese")), Occur.MUST)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "mushrooms")), Occur.MUST_NOT)
										.add(new TermQuery(new Term(FIELD_CONTENTS, "steak")), Occur.MUST_NOT)
										.build();
		displayQuery(booleanQuery);
		results = searcher.search(booleanQuery, 5 * hitsPerPage);
                hits = results.scoreDocs;
		displayHits(hits);                 
                

	}
        //////////////////////////////////////////////////////////////////////// 
	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException 
	{
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		Analyzer analyzer = new StandardAnalyzer();
		boolean recreateIndexIfExists = true;
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter Writer = new IndexWriter(dir, iwc);
		File dir1 = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir1.listFiles();
		for (File file : files) {
			Document document = new Document();

			String path = file.getName();
			document.add(new StringField(FIELD_PATH, path, Field.Store.YES));

			Reader reader = new FileReader(file);
			document.add(new TextField(FIELD_CONTENTS, reader));

			Writer.addDocument(document);
		}
		//indexWriter.optimize();
		Writer.close();
	}

        ////////////////////////////////////////////////////////////////////////
	public static void searchIndexWithQueryParser(String searchString) throws IOException, ParseException {
		System.out.println("\nSearching for '" + searchString + "' using QueryParser");
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
		IndexSearcher searcher = new IndexSearcher(reader);

		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
		Query query = queryParser.parse(searchString);
		System.out.println("Type of query: " + query.getClass().getSimpleName());
		//Hits hits = indexSearcher.search(query);
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
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
			IndexSearcher searcher = new IndexSearcher(reader);
			int docId = hitt.doc;
			Document document = searcher.doc(docId);
			String path = document.get(FIELD_PATH);
			System.out.println("Hit: " + path); 
		}
	}
        ////////////////////////////////////////////////////////////////////////
	public static void displayQuery(Query query) {
                System.out.println("\n----------------------------------------------------");
		System.out.println("Query: " + query.toString());
	}
        ////////////////////////////////////////////////////////////////////////
}