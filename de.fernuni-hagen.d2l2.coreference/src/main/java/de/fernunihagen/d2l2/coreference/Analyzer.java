package de.fernunihagen.d2l2.coreference;

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

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpCoreferenceResolver;

import nu.xom.Builder;

public class Analyzer extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	protected String outputFile;
	
	static Map<String, Object[]> data; 
	static Map<String, Object[]> fdata; 
	int index = 1;	
	static StringBuilder sb ;
	StringBuilder sb1;
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
	    super.initialize(context);
	    sb = new StringBuilder();
	    sb1 = new StringBuilder();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		System.out.println("-Printing text-: "+ meta.getDocumentTitle());
		sb1.append("-Printing text-: "+ meta.getDocumentTitle()+"\n");
//		sb.append("-Printing text-: "+ meta.getDocumentTitle()+ "\n");
		Collection<CoreferenceChain> coreferenceChain = JCasUtil.select(aJCas, CoreferenceChain.class);
		
		for (CoreferenceChain coreferenceChain2 : coreferenceChain) {
			StringBuilder sb3 = new StringBuilder();
			sb3.append("---Core Chains:"+"\n");
//			System.out.println(coreferenceChain2.getFirst());
			System.out.println(readChains(coreferenceChain2.getFirst()));
			sb3.append(readChains(coreferenceChain2.getFirst()));			
			sb3.delete((sb3.length()/2)+6, sb3.length());
			sb3.append("\n");
			sb.delete(0, sb.length());
			sb1.append(sb3.toString());
		}
		Collection<CoreferenceLink> coreferenceLink = JCasUtil.select(aJCas,CoreferenceLink.class); 
		for (CoreferenceLink coreferenceLink2 : coreferenceLink) {
//			System.out.println(getChain(coreferenceLink2));
		}
		
	}
		
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		export(sb1.toString(),outputFile);
		
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
	//to read CoreferenceLink data type
	public static String readChains(CoreferenceLink l) {
		sb.append(l.getCoveredText()+"("+l.getBegin()+"-"+l.getEnd()+" "+l.getReferenceType()+")"+" - ");
		if(l.getNext()==null) {
			return sb.toString();
		}else {
			readChains(l.getNext());
		}		
		return sb.toString();
	}
}
