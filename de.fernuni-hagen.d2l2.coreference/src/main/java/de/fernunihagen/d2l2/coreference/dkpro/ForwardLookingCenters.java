package de.fernunihagen.d2l2.coreference.dkpro;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ForwardLookingCenters extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	protected String outputFile;
	
	StringBuilder output;
	private int index;
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
	    super.initialize(context);
	    output = new StringBuilder();
	    
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
//		String id = meta.getDocumentId();
//		System.out.println("Printing essay: "+id);
		String content = meta.getDocumentTitle();
		System.out.println("Printing essay: "+content);

		//create a list of possible candidate (noun, pronoun, proper noun,...)
		ArrayList<String> possibleCandidate = new ArrayList<String>();
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t: tokens) {
			if(t.getPos().getCoarseValue()!=null) {
				if(t.getPos().getCoarseValue().equals("PROPN")||t.getPos().getCoarseValue().equals("NOUN")||t.getPos().getCoarseValue().equals("PRON")) {
					possibleCandidate.add(t.getCoveredText());
					System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());				
				}
			}
//			System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());
		}
		
		StringBuilder sb = new StringBuilder();		
		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);		
		//create a list of forward-looking centers
		ArrayList<Dep> forwardLookingCenters = new ArrayList<>();
		for(String li : possibleCandidate) {
			for (Dependency dep : dependencies){
				if(!dep.getDependencyType().equals("compound")&&!dep.getDependencyType().equals("punct")) {
					if(li.equals(dep.getDependent().getCoveredText())) {
						forwardLookingCenters.add(new Dep (dep.getDependent().getCoveredText(),dep.getDependencyType()));
					}				
				}			
			}	
		}
		ArrayList<String> namedEntityList = new ArrayList<>();
		Collection<NamedEntity> ners = JCasUtil.select(aJCas, NamedEntity.class);
		for (NamedEntity namedEntity : ners) {
			namedEntityList.add(namedEntity.getCoveredText());
//			System.out.println(namedEntity.getValue()+" "+ namedEntity.getIdentifier()+" ");
		}
//		System.out.println(namedEntityList);
		
		//Named Entity bind together(90-136)
		ArrayList<String> listNER = new ArrayList<>();
		
		for (Dependency dep : dependencies){
			if(!dep.getDependencyType().equals("punct")) {
				
				if(dep.getDependencyType().equals("compound")) { 
					listNER.add(dep.getDependent().getCoveredText());
					listNER.add(dep.getGovernor().getCoveredText());
				}			 
				sb.append(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText()+"\n");		
			}
			System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText());		
		}
		ArrayList<String> listNERCopie = new ArrayList<String>();
		for (String string : listNER) {
			listNERCopie.add(string);
		}
		for (int i = listNERCopie.size()-2; i >=0 ; i--) {
			if(listNERCopie.get(listNERCopie.size()-1).equals(listNERCopie.get(i))) {
				listNER.remove(i);
			}
		}
		String dependencyTypeOfNER = "";
		for (Dependency dep : dependencies){
			if(!listNER.isEmpty()) {
				if(listNER.get(listNER.size()-1).equals(dep.getDependent().getCoveredText()))
					dependencyTypeOfNER = dep.getDependencyType();
			}
			
		}
		String namedEntity = "";
		for (String string : listNER) {
			namedEntity += string +" ";
		}
		ArrayList<Dep> forwardLookingCentersCopie = new ArrayList<>();
		for(Dep dep : forwardLookingCenters) {
			forwardLookingCentersCopie.add(dep);
		}

		for (Dep dep : forwardLookingCentersCopie) {
			for(String str : listNER) {
				if(dep.getDependency(0).equals(str)) {
					forwardLookingCenters.remove(dep);
				}
			}
		}
		forwardLookingCenters.add(new Dep(namedEntity,dependencyTypeOfNER));
		
		
		// solve the problem  "and" and "or"
		Set<String> setConj = new HashSet<>();
		for (Dependency dep : dependencies){
			if(!dep.getDependencyType().equals("punct")) {
				
				if(dep.getDependencyType().contains("conj")) { 
					setConj.add(dep.getDependent().getCoveredText());
					setConj.add(dep.getGovernor().getCoveredText());
				}			 
			}
		}
		//convert set to arraylist
		ArrayList<String> listConj = new ArrayList<>();
		for(String str : setConj) {
			listConj.add(str);
		}
		System.out.println(listConj);
		String dependencyTypeForConj = "";
		String firstElementOfCompound = "";
		for (Dependency dep : dependencies){
			if(dep.getDependencyType().equals("conj")){
				firstElementOfCompound= dep.getGovernor().getCoveredText();
			}
			
		}
		for (Dependency dep : dependencies){
			if(dep.getDependent().getCoveredText().equals(firstElementOfCompound) ){
				dependencyTypeForConj = dep.getDependencyType();
			}			
		}
		System.out.println(dependencyTypeForConj);
		for (Dep dep : forwardLookingCentersCopie) {
			for(String str : listConj) {
				if(dep.getDependency(0).equals(str)) {
					forwardLookingCenters.remove(dep);
				}
			}
		}
		for(String str : listConj) {
			forwardLookingCenters.add(new Dep(str,dependencyTypeForConj));
		}
		//solve the subordinate clause
		String rootVerb = "";
		for(Dependency dep : dependencies) {
			if(dep.getDependencyType().equals("root")){
				rootVerb = dep.getDependent().getCoveredText();
			}
		}
		ArrayList<String> listFalseNsubj = new ArrayList<>();
		for(Dependency dep : dependencies) {
			if(dep.getDependencyType().equals("nsubj")&&!dep.getGovernor().getCoveredText().equals(rootVerb)) {				
				listFalseNsubj.add(dep.getDependent().getCoveredText());			
			}
		}
		System.out.println(listFalseNsubj);
		for (Dep dep : forwardLookingCentersCopie) {
			for(String str : listFalseNsubj) {
				if(dep.getDependency(0).equals(str)) {
					forwardLookingCenters.remove(dep);
				}
			}
		}
		for(String str : listFalseNsubj) {
			forwardLookingCenters.add(new Dep(str,"other"));
		}
		ArrayList<Object[]> lo = new ArrayList<Object[]>();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		StringBuilder sb3 = new StringBuilder();
		for(Dep d : forwardLookingCenters) {
			if(d.getDependency(1).contains("subj")) {
				sb1.append(d.getDependency(0)+" ");
				lo.add(new Object[] {1,d.getDependency(0)});				
			}else if(d.getDependency(1).contains("obj")) {
				sb2.append(d.getDependency(0)+" ");
				lo.add(new Object[] {2,d.getDependency(0)});				
			}else{
				sb3.append(d.getDependency(0)+" ");
				lo.add(new Object[] {3,d.getDependency(0)});				
			}
		}
		String result ="CF = "+ "{"+sb1.toString()+ " < " +sb2.toString()+" < "+sb3.toString()+"}";		
		System.out.println(result);
		output.append(content + "\n" +"\n");
		output.append(sb+"\n");
		output.append(result +"\n");
		output.append("-------------------------------------------------------"+"\n");
	}
		
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
//		export(output.toString(), outputFile);
		
		
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
	// create text of unique word from a given text
   	public static String uniqueText(String text) {
   		String text1 = text.trim().replaceAll("\\s{2,}", " ");
   		 String[] words = text1.split(" ");		         		        
   		 HashSet<String> uniqueWords = new HashSet<String>(Arrays.asList(words));
   		 int num = uniqueWords.size();
   		 StringBuilder sb = new StringBuilder();

   		 Iterator<String> iterator = uniqueWords.iterator();
   		 String lastElement = "";
   		 while(iterator.hasNext()){
   	        lastElement = iterator.next();
   		 }
 	  		 System.out.println(lastElement);
   		 for(String a : uniqueWords) {
   			 if(!a.equals(lastElement)) {
   				 sb.append(a);
   				sb.append(" ");
   			 }else {
   				 sb.append(a);
   				 sb.append("");
 	  			 }
   		 }
   		 String temp= sb.toString();
   		 String[] temp1 = temp.split(" ");
   		 String[] temp2 = new String[temp1.length];
   		temp2[0] = temp1[temp1.length-1];
  		 for (int i = 1; i < temp1.length; i++) {
			temp2[i] = temp1[i-1];							
		}
   		 return String.join(" ",temp2);
   		 
   	}
	
	class Dep{
		private String[] dep = new String[2];
		public Dep(String s1, String s2) {
			dep[0]= s1;
			dep[1]= s2;
		}
		public String getDependency(int index){
	        return dep[index];
	    }
	}
	
}


