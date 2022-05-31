package de.fernunihagen.d2l2.coreference.dkpro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.fernunihagen.d2l2.types.CoreferenceEntity;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import edu.stanford.nlp.process.TokenizerAdapter;

public class CoreferenceResolution extends JCasAnnotator_ImplBase{
	
	static StringBuilder stringBuilder;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		// TODO Auto-generated method stub
		super.initialize(context);
		stringBuilder = new StringBuilder();
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		DocumentMetaData meta = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
		String content = meta.getDocumentTitle();
		System.out.println("Printing essay: "+content);
		int observedChain=0;
		ArrayList<CoreferenceChain> corefs = (ArrayList<CoreferenceChain>) JCasUtil.select(aJCas, CoreferenceChain.class);
		for (int i = 0; i < corefs.size(); i++) {
			if(corefs.get(i).getFirst().getNext()!=null) {
				observedChain= i;
			}
		}
		ArrayList<CoreferenceEntity> ces = new ArrayList<>();
		String sbChains = readChains(corefs.get(observedChain).getFirst());
		stringBuilder.delete(0, stringBuilder.length()-1);
		System.out.println("------------------"+sbChains);
//		System.out.println(corefs);
		int coreferenceLinkId = 1;
		String [] strArr = sbChains.split(";");
		

		for (int i = 0; i < strArr.length; i++) {
			if(numOfWord(strArr[i])>3) {
				strArr[i] = newCorefEntity(strArr[i]);
			}
		}
		String [] temp1 = strArr[0].split(" ");
		String firstMention = temp1[0];
		for (int i = 0; i < strArr.length; i++) {
			System.out.println(strArr[i]);
		}
		
		for (int i = 0; i < strArr.length; i++) {			
//			System.out.println("--"+strArr[i]);
			String [] temp = strArr[i].split(" ");
			if(!temp[0].equals("")) {
				System.out.println(temp[0]);
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
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
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

}
