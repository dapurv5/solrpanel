package com.github.dapurv5.solrpanel.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.TokenizerChain;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;

/**
 * Solr doesn't provide a straightforward way to define analyzers that can be shared across various components
 * in a seamless fashion. 
 * Provides various utility methods to quickly initialize a custom analyzer from a named list configuration.
 * 
 * @author apurv@bloomreach.com
 * 
 */
public class AnalyzerUtil {

 /**
  * A utility class to build an analyzer from a NamedList configuration specified as follows
  * 
  *    <lst name="analyzer">
  *      <lst name="tokenizer">
  *         <str name="class">solr.KeywordTokenizer</str>
  *      </lst>
  *      <lst name="filter">
  *        <str name="class">solr.StopFilterFactory</str>
  *        <str name="ignoreCase">true</str>
  *        <str name="words">stopwords.txt</str>
  *      </lst>
  *      <lst name="filter">
  *        <str name="class">solr.SynonymFilterFactory</str>
  *        <str name="synonyms">query_term_tag_corpus.txt</str>
  *        <str name="expand">false</str>
  *        <str name="ignoreCase">true</str>
  *      </lst>
  *    </lst>
  * 
  * Note: This chain must have a single tokenizer followed by a list of filters.
  */
  public static Analyzer buildAnalyzerFromConf(NamedList analyzerConf, SolrCore solrCore) throws IOException {
    Version luceneMatchVersion = solrCore.getSolrConfig().luceneMatchVersion;
    SolrResourceLoader loader = solrCore.getResourceLoader();
    TokenizerFactory tokenizerFactory = null;
    
    List<TokenFilterFactory> filterFactories = new LinkedList<TokenFilterFactory>();
    for(int i = 0; i < analyzerConf.size(); i++) {
      NamedList tokenizerConf = (NamedList) analyzerConf.getVal(i);
      String classname = (String) tokenizerConf.get("class");
      Map<String, String> params = SolrParams.toMap(tokenizerConf);
      params.put("luceneMatchVersion", luceneMatchVersion.toString());
      
      if(analyzerConf.getName(i).equals("tokenizer")) {               
        tokenizerFactory = loader.newInstance(classname, TokenizerFactory.class, new String[]{}, new Class[] { Map.class }, new Object[] { params });
        if (tokenizerFactory instanceof ResourceLoaderAware) {
          ((ResourceLoaderAware)tokenizerFactory).inform(loader);
        }

      } else if(analyzerConf.getName(i).equals("filter")) {        
        TokenFilterFactory filterFactory = loader.newInstance(classname, TokenFilterFactory.class, new String[]{}, new Class[] { Map.class }, new Object[] { params });
        if (tokenizerFactory instanceof ResourceLoaderAware) {
          ((ResourceLoaderAware)filterFactory).inform(loader);
        }
      }
    }
    TokenizerChain analyzer = new TokenizerChain(tokenizerFactory,
        filterFactories.toArray(new TokenFilterFactory[filterFactories.size()]));
    return analyzer;
  }
}