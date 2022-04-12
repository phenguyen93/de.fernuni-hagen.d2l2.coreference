package de.fernunihagen.d2l2.coreference;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class StanfordCorefDeterministic {
	private static StringBuilder sb;
	
	public static void main(String[] args) throws Exception{
		
		//TODO: Adjust output Path
		String outputPath = "D:\\HIWI\\coref\\output\\stanford\\deterministic.txt";
		sb = new StringBuilder();			
		for (int i = 1; i <= 12; i++) {
			String inputPath = i+ ".txt";
			String text = readTextFile(inputPath);
			Annotation document = new Annotation(text);
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			pipeline.annotate(document);
			System.out.println("---Printing "+inputPath+"---: " + text);
			sb.append("---Printing "+inputPath+"---: " + text+"\n");
			System.out.println("coref chains");
			sb.append("coref chains"+"\n");
			for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			  System.out.println("\t" + cc);
			  sb.append("\t" + cc+"\n");
			}
			export(sb.toString(), outputPath);
			
			/*
			 * for (CoreMap sentence :
			 * document.get(CoreAnnotations.SentencesAnnotation.class)) {
			 * System.out.println("---"); System.out.println("mentions"); for (Mention m :
			 * sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
			 * System.out.println("\t" + m); } }
			 */
		}
		
		
	}
	public static String readTextFile(String inputPath) throws IOException {
		FileInputStream inputStream = new FileInputStream(inputPath);
		String text = "";
		try {
		    text = IOUtils.toString(inputStream);
		} finally {
		    inputStream.close();		    
		}
		return text;
		
	}
	public static void export(String text,String path) {
		BufferedWriter writer = null;
		try{
		    writer = new BufferedWriter( new FileWriter(path));
		    writer.write(text);
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