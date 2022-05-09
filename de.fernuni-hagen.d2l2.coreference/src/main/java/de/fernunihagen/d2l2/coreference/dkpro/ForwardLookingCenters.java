package de.fernunihagen.d2l2.coreference.dkpro;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
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

		ArrayList<String> possibleCandidate = new ArrayList<String>();
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t: tokens) {
			if(t.getPos().getCoarseValue().equals("PROPN")||t.getPos().getCoarseValue().equals("NOUN")||t.getPos().getCoarseValue().equals("PRON")) {
				possibleCandidate.add(t.getCoveredText());
//				System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());				
			}
//			System.out.println(t.getCoveredText() + " " + t.getPos().getCoarseValue() + " " + t.getLemmaValue());
		}
		
		StringBuilder sb = new StringBuilder();
		ArrayList<Dep> forwordLookingCenters = new ArrayList<>();
		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
		for (Dependency dep : dependencies){
			if(!dep.getDependencyType().equals("punct"))
				System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText());			
				sb.append(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText()+"\n");
		}		
		for(String li : possibleCandidate) {
			for (Dependency dep : dependencies){
				if(li.equals(dep.getDependent().getCoveredText())) {
					forwordLookingCenters.add(new Dep (dep.getDependent().getCoveredText(),dep.getDependencyType()));
				}						
//				System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " " + dep.getDependent().getCoveredText()+" "+dep.getFlavor());			
			}	
		}
		ArrayList<Object[]> lo = new ArrayList<Object[]>();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		StringBuilder sb3 = new StringBuilder();
		for(Dep d : forwordLookingCenters) {
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


