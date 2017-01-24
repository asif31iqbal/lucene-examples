
package m2_lab3;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;

/**
 *
 * @author Madan
 */
public class MyStemmingAnalyzer extends Analyzer {
    
      @Override
      protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new LowerCaseTokenizer(reader);
        return new Analyzer.TokenStreamComponents(source, new PorterStemFilter(source));
      }    
      
}
