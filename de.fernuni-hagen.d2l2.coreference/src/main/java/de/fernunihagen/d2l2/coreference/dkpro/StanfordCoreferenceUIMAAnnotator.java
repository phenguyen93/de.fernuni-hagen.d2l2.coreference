package de.fernunihagen.d2l2.coreference.dkpro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import nu.xom.Builder;

public class StanfordCoreferenceUIMAAnnotator extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	protected String outputFile;
	
	static Map<String, Object[]> data; 
	static Map<String, Object[]> fdata; 
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
	    super.initialize(context);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String text = aJCas.getDocumentText();
		System.out.println("Printing text: "+ text);			
		edu.stanford.nlp.pipeline.Annotation document = new edu.stanford.nlp.pipeline.Annotation(text);
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
		props.setProperty("coref.algorithm", "neural");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		pipeline.annotate(document);
		System.out.println("coref chains");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		  System.out.println("\t" + cc);
		}
	}
		
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		
		
	}
	public static void export(String str,String outputPath) {
		BufferedWriter writer = null;
		try{
		    writer = new BufferedWriter( new FileWriter(outputPath));
		    writer.write(str);
		    }
		catch ( IOException e){
			e.printStackTrace();
		}finally{
		    try{
		        if ( writer != null)
		        writer.close( );
		    }catch ( IOException e){
		    	e.printStackTrace();
		    }
		    System.out.println("written successfully on disk.");
		}
	}

}
