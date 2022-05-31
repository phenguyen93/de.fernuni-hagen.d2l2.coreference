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
import java.util.StringTokenizer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.corenlp.CoreNlpCoreferenceResolver;

import de.fernunihagen.d2l2.types.CFEntity;
import de.fernunihagen.d2l2.types.CoreferenceEntity;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.pipeline.SentenceAnnotator;

public class ForwardLookingCenters extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	protected String outputFile;
	
	StringBuilder output;
	private int index;
	static StringBuilder stringBuilder ;
	
	@Override
	public void initialize(final UimaContext context) throws ResourceInitializationException {
	    super.initialize(context);
	    output = new StringBuilder();
	    stringBuilder = new StringBuilder();
	    
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String content = meta.getDocumentTitle();
		System.out.println("---------Printing essay-----------: "+content);
		int observedChain=0;
		Collection<CoreferenceLink> cls = JCasUtil.select(aJCas, CoreferenceLink.class);
		ArrayList<CoreferenceChain> corefs = (ArrayList<CoreferenceChain>) JCasUtil.select(aJCas, CoreferenceChain.class);
		ArrayList<CoreferenceEntity> ces = new ArrayList<>();
		for (int i = 0; i < corefs.size(); i++) {
			if(corefs.get(i).getFirst().getNext()!=null) {
				observedChain= i;
				
				String sbChains = readChains(corefs.get(observedChain).getFirst());
				stringBuilder.delete(0, stringBuilder.length()-1);
//				System.out.println(sbChains);
//				System.out.println(corefs);
				int coreferenceLinkId = i;
				String [] strArr = sbChains.split(";");
				
				//get only last word of each element in coref chains ex.: the children --> children
				for (int k = 0; k < strArr.length; k++) {
					if(numOfWord(strArr[k])>3) {
						strArr[k] = newCorefEntity(strArr[k]);
					}
				}
				String [] temp1 = strArr[0].split(" ");
				String firstMention = temp1[0];
				
//				System.out.println(firstMention);
				
				for (int j = 0; j < strArr.length; j++) {
//					System.out.println("--"+strArr[j]);
					String [] temp = strArr[j].split(" ");
					if(!temp[0].equals("")) {						
						CoreferenceEntity ce = new CoreferenceEntity();
						ce.setId(coreferenceLinkId);
						ce.setName(temp[0]);
						ce.setBegin(Integer.valueOf(temp[1]));
						ce.setEnd(Integer.valueOf(temp[2]));
						ce.setFirstMention(firstMention);
						ces.add(ce);
					}
				}
			}
		}
//		System.out.println(cls);
//		System.out.println("---------------------------------------------");
//		System.out.println(corefs);
		
		int sentenceIndex=1;
		ArrayList<Object[]> CfAndCpList = new ArrayList<>();
		for (final Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
//			System.out.println("----Sentence "+sentenceIndex +"---: "+sentence.getCoveredText() );
			ArrayList<CFEntity> possibleCandidate = new ArrayList<CFEntity>();
			final Collection<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, sentence);
			for (Token t: tokens) {
				if(t.getPos().getCoarseValue()!=null) {
					if(t.getPos().getCoarseValue().equals("PROPN")||t.getPos().getCoarseValue().equals("NOUN")||t.getPos().getCoarseValue().equals("PRON")) {
						CFEntity cfe = new CFEntity();
						cfe.setName(t.getCoveredText());
						cfe.setBegin(t.getBegin());
						cfe.setEnd(t.getEnd());
						possibleCandidate.add(cfe);
//						System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());				
					}
				}
//				System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());
			}
			final Collection<Dependency> dependencies = JCasUtil.selectCovered(aJCas, Dependency.class, sentence);
			StringBuilder sb = new StringBuilder();	
			//create a list of forward-looking centers
			ArrayList<CFEntity> forwardLookingCenters = new ArrayList<>();
			for(CFEntity cfe : possibleCandidate) {
				for (Dependency dep : dependencies){
					if(!dep.getDependencyType().equals("compound")&&!dep.getDependencyType().equals("punct")) {
						if(cfe.getName().equals(dep.getDependent().getCoveredText())) {
							cfe.setDependencyType(dep.getDependencyType());
							forwardLookingCenters.add(cfe);
							break;
						}				
					}
					
				}	
			}
		
			//Named Entity bind together(90-136)
			ArrayList<CFEntity> listNER = new ArrayList<>();
			
			for (Dependency dep : dependencies){
				if(!dep.getDependencyType().equals("punct")) {					
					if(dep.getDependencyType().equals("compound")) {
						CFEntity cfe1 = new CFEntity();
						cfe1.setName(dep.getDependent().getCoveredText());
						cfe1.setBegin(dep.getDependent().getBegin());
						cfe1.setEnd(dep.getDependent().getEnd());
						cfe1.setDependencyType(dep.getDependencyType());
						listNER.add(cfe1);
						CFEntity cfe2 = new CFEntity();
						cfe2.setName(dep.getGovernor().getCoveredText());
						cfe2.setBegin(dep.getGovernor().getBegin());
						cfe2.setEnd(dep.getGovernor().getEnd());
						cfe2.setDependencyType(dep.getDependencyType());
						listNER.add(cfe2);
					}			 
					sb.append(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText()+"\n");		
//					System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText());	
				}
					
			}
			ArrayList<CFEntity> listNERCopie = new ArrayList<CFEntity>();
			for (CFEntity string : listNER) {
				listNERCopie.add(string);
			}
			for (int i = listNERCopie.size()-2; i >=0 ; i--) {
				if(listNERCopie.get(listNERCopie.size()-1).getName().equals(listNERCopie.get(i).getName())) {
					listNER.remove(i);
				}
			}
			String dependencyTypeOfNER = "";
			for (Dependency dep : dependencies){
				if(!listNER.isEmpty()) {
					if(listNER.get(listNER.size()-1).getName().equals(dep.getDependent().getCoveredText()))
						dependencyTypeOfNER = dep.getDependencyType();
				}
				
			}
			String namedEntity = "";
			for (CFEntity cfe : listNER) {
				namedEntity += cfe.getName() +" ";
			}
			ArrayList<CFEntity> forwardLookingCentersCopie = new ArrayList<>();
			for(CFEntity cfe : forwardLookingCenters) {
				forwardLookingCentersCopie.add(cfe);
			}

			for (CFEntity cfe : forwardLookingCentersCopie) {
				for(CFEntity cfe2 : listNER) {
					if(cfe.getName().equals(cfe2.getName())) {
						forwardLookingCenters.remove(cfe);
					}
				}
			}
			CFEntity cfe = new CFEntity();
			cfe.setName(namedEntity);
			cfe.setDependencyType(dependencyTypeOfNER);
			//TODO: Hier muss noch die Begin und End von Token ergänzen
			forwardLookingCenters.add(cfe);
			
			
			// solve the problem "and" and "or" 
			Set<String> setConj = new HashSet<>();
			for (Dependency dep : dependencies){
				if(!dep.getDependencyType().equals("punct")) {
				  
					if(dep.getDependencyType().contains("conj")) {
						setConj.add(dep.getDependent().getCoveredText());
						setConj.add(dep.getGovernor().getCoveredText()); 
					} 
				} 
			} //convert set toarraylist 
				ArrayList<String> listConj = new ArrayList<>(); 
			for(String str : setConj) { 
				listConj.add(str); 
				} 
			String dependencyTypeForConj = ""; 
			String firstElementOfCompound = ""; 
			for (Dependency dep : dependencies){
				if(dep.getDependencyType().equals("conj")){ firstElementOfCompound=
				dep.getGovernor().getCoveredText(); 
			}
				  
				} 
			for (Dependency dep : dependencies){
				if(dep.getDependent().getCoveredText().equals(firstElementOfCompound) ){
				dependencyTypeForConj = dep.getDependencyType(); 
				} 
			} 
			for (CFEntity cfe2 : forwardLookingCentersCopie) { 
				for(String str : listConj) {
					if(cfe2.getName().equals(str)) { 
						forwardLookingCenters.remove(cfe2); 
						} 
				} 
			}
			for(String str : listConj) { 
				CFEntity cfe2 = new CFEntity();
				cfe2.setName(str); cfe2.setDependencyType(dependencyTypeForConj);
				forwardLookingCenters.add(cfe2); 
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
			for (CFEntity cfe2 : forwardLookingCentersCopie) {
				for(String str : listFalseNsubj) {
					if(cfe2.getName().equals(str)) {
						forwardLookingCenters.remove(cfe2);
					}
				}
			}
			for(String str : listFalseNsubj) {
				CFEntity cfe2 = new CFEntity();
				cfe2.setName(str);	
				cfe2.setDependencyType("other");
				forwardLookingCenters.add(cfe2);
			}
			ArrayList<CFEntity> possibleSubject = new ArrayList<>();
			ArrayList<CFEntity> possibleObject = new ArrayList<>();
			ArrayList<CFEntity> possibleOthers = new ArrayList<>();
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			StringBuilder sb3 = new StringBuilder();
			for(CFEntity cfe2 : forwardLookingCenters) {
				if(cfe2.getDependencyType().contains("subj")) {
					sb1.append(cfe2.getName()+" "+cfe2.getBegin()+";"+cfe2.getEnd()+" ");
					possibleSubject.add(cfe2);
				}else if(cfe2.getDependencyType().contains("obj")) {
					sb2.append(cfe2.getName()+" "+cfe2.getBegin()+";"+cfe2.getEnd()+" ");
					possibleObject.add(cfe2);	
				}else{
					sb3.append(cfe2.getName()+" "+cfe2.getBegin()+";"+cfe2.getEnd()+" ");
					possibleOthers.add(cfe2);				
				}
			}
			//CF of current sentence
			ArrayList<CFEntity> cF = new ArrayList<>();
			cF.addAll(possibleSubject);
			cF.addAll(possibleObject);
			cF.addAll(possibleOthers);			
			//Use Coreference Resolution
			if(sentenceIndex != 1) {
				for(CFEntity cFEntity: cF) {
					for(CoreferenceEntity coreferenceEntity: ces ) {
						if((cFEntity.getBegin()== coreferenceEntity.getBegin())&&(cFEntity.getEnd()==coreferenceEntity.getEnd())) {
							cFEntity.setName(coreferenceEntity.getFirstMention());
						}
					}
				}
			}						
			//get Cp from Cf
			String cP = "";
			cP = cF.get(0).getName();
			
			CfAndCpList.add(new Object[]{sentenceIndex,cF,cP});
			
			String result ="CF"+ " in sentence "+sentenceIndex+ " = "+ "{"+sb1.toString()+ " < " +sb2.toString()+" < "+sb3.toString()+"}";		
			System.out.println(result);
			output.append(content + "\n" +"\n");
			output.append(sb+"\n");
			output.append(result +"\n");
			output.append("-------------------------------------------------------"+"\n");
			sentenceIndex++;
		}
		
		ArrayList<Object[]>CfAndCpListCopy = new ArrayList<>();
		for(Object[] o : CfAndCpList) {
			CfAndCpListCopy.add(o);
		}
		ArrayList<Object[]> CbAndCpList = new ArrayList<>();
		//first sentence
		CbAndCpList.add(new Object[] {1,(String)CfAndCpList.get(0)[2],"undefine"});
		
		for (int i = 1; i < CfAndCpList.size(); i++) {
			String cB = "undefine";
			String cP = (String) CfAndCpList.get(i-1)[2];
			if(CfAndCpList.get(i-1)[2].equals(CfAndCpList.get(i)[2])) {
				cB = (String) CfAndCpList.get(i-1)[2];					
			}
			/*
			 * for (int j = 0; j < CfAndCpList.size(); j++) {
			 * if(CfAndCpList.get(j)[2]==CfAndCpList.get(i)[2]) { cB = (String)
			 * CfAndCpList.get(j)[2]; cP = (String) CfAndCpList.get(j)[2]; } }
			 */
			CbAndCpList.add(new Object[] {i+1,cP,cB});			
		}
		
		for(Object[] o : CbAndCpList) {
			System.out.print(o[0]+" ");
			System.out.print(o[1]+" ");
			System.out.print(o[2]+" ");
			System.out.println();
		}
		
		
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
// 	  		 System.out.println(lastElement);
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
   	//to read CoreferenceLink data type
  	public static String readChains(CoreferenceLink l) {
  		stringBuilder.append(l.getCoveredText()+" "+l.getBegin()+" "+l.getEnd()+""+";");
  		if(l.getNext()==null) {
  			return stringBuilder.toString();
  		}else {
  			readChains(l.getNext());
  		}		
  		return stringBuilder.toString();
  	}
  	public static int numOfWord(String str) {
  		StringTokenizer st = new StringTokenizer(str);  		
  		return st.countTokens();  	
  	}
  	//get only the last word of CorefEntity
  	public static String newCorefEntity(String str) {  		
  		String result = "";
  		String[] words = str.trim().split("\\s+");
  		int lengthOfElement  = words[words.length-3].length();
  		int begin = Integer.valueOf(words[words.length-1])-lengthOfElement;
  		int end = Integer.valueOf(words[words.length-1]);
  		result +=words[words.length-3]+" "+begin+" "+end;
  		return result;
  	}
  	public static String getLastWord(String str) {
  		String[] words = str.trim().split("\\s+");
  		return  words[words.length-3];
  	}
  	public static int getBegin(String str) {
  		String[] words = str.trim().split("\\s+");
  		int lengthOfElement  = words[words.length-3].length();
  		return Integer.valueOf(words[words.length-1])-lengthOfElement;
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


