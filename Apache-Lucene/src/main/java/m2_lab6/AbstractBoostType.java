package m2_lab6;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public abstract class AbstractBoostType {

    protected Analyzer analyzer = new StandardAnalyzer();
    protected IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
    protected RAMDirectory ramDirectory = new RAMDirectory();
    protected IndexWriter indexWriter;
    protected boolean printExplanation;

    public AbstractBoostType(boolean printExplanation) {
        super();
        this.printExplanation = printExplanation;
    }

    public void populateIndex() {
        try {
            System.out.println(printBoostTypeInformation());
            indexWriter = new IndexWriter(ramDirectory, config);
            boostPerType("Lada Niva", "Brown", "2000000", "Russia", "SUV");
            boostPerType("Tata Aria", "Red", "1600000", "India", "SUV");
            boostPerType("Nissan Terrano", "Blue", "2000000", "Japan", "SUV");
            boostPerType("Mahindra XUV500", "Black", "1600000", "India", "SUV");
            boostPerType("Ford Ecosport", "White", "1000000", "USA", "SUV");
            boostPerType("Mahindra Thar", "White", "1200000", "India", "SUV");
            indexWriter.close();
        } catch (IOException | NullPointerException ex) {
            System.out.println("Something went wrong in this sample code -- "
                    + ex.getLocalizedMessage());
        }
    }

    public void searchAndPrintResults() {
        try {
            IndexReader idxReader = DirectoryReader.open(ramDirectory);
            IndexSearcher idxSearcher = new IndexSearcher(idxReader);

            Query queryToSearch = new QueryParser(Version.LUCENE_46, "itemType", analyzer).parse(getQueryForSearch());

            System.out.println(queryToSearch);

            TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
            idxSearcher.search(queryToSearch, collector);
            ScoreDoc[] hitsTop = collector.topDocs().scoreDocs;

            System.out.println("Search produced " + hitsTop.length + " hits.");
            System.out.println("----------");
            for (int i = 0; i < hitsTop.length; ++i) {
                int docId = hitsTop[i].doc;
                Document docAtHand = idxSearcher.doc(docId);
                System.out.println(docAtHand.get("itemName") + "\t" + docAtHand.get("originOfItem")
                        + "\t" + docAtHand.get("itemColour") + "\t" + docAtHand.get("itemPrice")
                        + "\t" + docAtHand.get("itemType"));

                if (printExplanation) {
                    Explanation explanation = idxSearcher.explain(queryToSearch, hitsTop[i].doc);
                    System.out.println("----------");
                    System.out.println(explanation.toString());
                    System.out.println("----------");
                }
            }
        } catch (IOException | ParseException ex) {
            System.out.println("Something went wrong in this sample code -- "
                    + ex.getLocalizedMessage());
        } finally {
            ramDirectory.close();
        }

    }

    protected abstract String getQueryForSearch();

    protected abstract void boostPerType(String itemName, String itemColour,
            String itemPrice, String originOfItem, String itemType)
            throws IOException;

    protected abstract String printBoostTypeInformation();
}
