
package m2_lab9;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.io.WKTReader;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;
import org.locationtech.spatial4j.shape.impl.ShapeFactoryImpl;
/**
 *
 * @author Asif
 */
public class SpatialDemo {
    
  private SpatialContext ctx;
  private SpatialStrategy strategy;
  private Directory directory;      
  private SpatialContextFactory spatialContextFactory;
  private ShapeFactory shapeFactory;
  
  public static void main(String[] args) throws Exception {
    new SpatialDemo().test();
  }

  public void test() throws Exception {
    init();
    indexPoints();
    search();
  }
  
  protected void init() {
    //Typical geospatial context
    //  These can also be constructed from SpatialContextFactory
    this.ctx = SpatialContext.GEO;
    this.spatialContextFactory = new SpatialContextFactory();
    this.shapeFactory = new ShapeFactoryImpl(ctx, spatialContextFactory);

    int maxLevels = 11;//results in sub-meter precision for geohash
    //TODO demo lookup by detail distance
    //  This can also be constructed from SpatialPrefixTreeFactory
    SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

    this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");

    this.directory = new RAMDirectory();
  }

  private void indexPoints() throws Exception {
    IndexWriterConfig iwConfig = new IndexWriterConfig(null);
    IndexWriter indexWriter = new IndexWriter(directory, iwConfig);    
    
    //Spatial4j has a WKT parser which is also "x y" order
    WKTReader wktReader = new WKTReader(ctx, spatialContextFactory);       
    
    //Spatial4j is x-y order for arguments
    indexWriter.addDocument(newSampleDocument(2, shapeFactory.pointXY(-80.93, 33.77)));
    indexWriter.addDocument(newSampleDocument(21, shapeFactory.pointXY(-80.11, 33.12)));
    indexWriter.addDocument(newSampleDocument(4, wktReader.read("POINT(60.9289094 -50.7693246)")));
    indexWriter.addDocument(newSampleDocument(20, shapeFactory.pointXY(0.1,0.1), shapeFactory.pointXY(0, 0)));
    indexWriter.close();
  }

  private Document newSampleDocument(int id, Shape... shapes) {
    Document doc = new Document();
    doc.add(new IntPoint("id", id));
    doc.add(new StoredField("id_stored", id));
    //Potentially more than one shape in this field is supported by some
    // strategies; see the javadocs of the SpatialStrategy impl to see.
    for (Shape shape : shapes) {
      for (IndexableField f : strategy.createIndexableFields(shape)) {
        doc.add(f);
      }
      //store it too; the format is up to you
      //  (assume point in this example)
      Point pt = (Point) shape;
      doc.add(new StoredField(strategy.getFieldName(), pt.getX()+" "+pt.getY()));
    }

    return doc;
  }

  private void search() throws Exception {
    IndexReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

    //--Filter by circle (<= distance from a point)
    {
      //Search with circle
      //note: SpatialArgs can be parsed from a string
      SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, shapeFactory.circle(-80.0, 33.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
      Query query = strategy.makeQuery(args);
      TopDocs docs = indexSearcher.search(query, 10, idSort);
      displayHits(docs.scoreDocs,args);
    }
    indexReader.close();
  }
  //////////////////////////////////////////////////////////////////////////////
         
	public void displayHits(ScoreDoc[] hits,SpatialArgs args) throws CorruptIndexException, IOException {
		System.out.println("Number of hits: " + hits.length);

		Iterator<ScoreDoc> it = Arrays.asList(hits).iterator();
		while (it.hasNext()) {
			ScoreDoc hitt = it.next();
                        IndexReader indexReader = DirectoryReader.open(directory);
                        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    
			int docId = hitt.doc;
			Document document = indexSearcher.doc(docId);
			String path = document.get(strategy.getFieldName());
			System.out.println("Hit Point: " + path); 
                        String doc1Str = document.getField(strategy.getFieldName()).stringValue();
                        int spaceIdx = doc1Str.indexOf(' ');
                        double x = Double.parseDouble(doc1Str.substring(0, spaceIdx));
                        double y = Double.parseDouble(doc1Str.substring(spaceIdx+1));
                        double doc1DistDEG = ctx.calcDistance(args.getShape().getCenter(), x, y);
                        System.out.println("Distance(DEG) = "+ doc1DistDEG);                        
		}
	}  
  //////////////////////////////////////////////////////////////////////////////
    
}
