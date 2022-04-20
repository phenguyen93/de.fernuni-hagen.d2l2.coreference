package de.fernunihagen.d2l2.coreference.stanford;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordCoreference {
	private static StringBuilder sb;

	public static void main (String [] args) throws IOException {
		sb = new StringBuilder();
		//TODO: Adjust output Path
		String outputPath = "D:\\HIWI\\coref\\output\\stanford\\neural.txt";
		//read .txt files
		String[] files = new String[12];
		for (int i = 1; i < 13; i++) {
			System.out.println(readTextFile("resources\\"+i+".txt"));
			files[i-1] = readTextFile("resources\\"+i+".txt");
		}
		for (int i = 0; i < files.length; i++) {
			neuralSystem(files[i]);
//			deterministicSystem(files[i]);
		}
		export(sb.toString(),outputPath);
		
	}
	public static void neuralSystem(String inputText) throws IOException {
					
		Annotation document = new Annotation(inputText);
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
		props.setProperty("coref.algorithm", "neural");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		pipeline.annotate(document);
		System.out.println("---Printing text:  "+ inputText);
		sb.append("---Printing text: "+ inputText);
		System.out.println("coref chains");
		sb.append("coref chains"+"\n");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		  System.out.println("\t" + cc);
		  sb.append("\t" + cc+"\n");
		}
		sb.append("\n");
	}
	
	public static void statisticalSystem( String inputText) throws IOException {
		Annotation document = new Annotation(inputText);
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse,coref");
		props.setProperty("coref.algorithm", "statistical");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		pipeline.annotate(document);
		System.out.println("---Printing text:  "+ inputText);
		sb.append("---Printing text: "+ inputText);
		System.out.println("coref chains");
		sb.append("coref chains"+"\n");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		  System.out.println("\t" + cc);
		  sb.append("\t" + cc+"\n");
		}
		sb.append("\n");
	}
	
	public static void deterministicSystem( String inputText) throws IOException {
		Annotation document = new Annotation(inputText);
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		pipeline.annotate(document);
		System.out.println("---Printing text:  "+ inputText);
		sb.append("---Printing text: "+ inputText);
		System.out.println("coref chains");
		sb.append("coref chains"+"\n");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		  System.out.println("\t" + cc);
		  sb.append("\t" + cc+"\n");
		}
		sb.append("\n");
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
