
package m2_lab3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;

/**
 *
 * @author Asif
 */
public class MyStemmingAnalyzer extends Analyzer {
    
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new LowerCaseTokenizer();
        return new TokenStreamComponents(source, new PorterStemFilter(source));
	}    
      
}
