package de.fernunihagen.d2l2.coreference.dkpro;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.corenlp.CoreNlpCoreferenceResolver;
import org.dkpro.core.corenlp.CoreNlpDependencyParser;
import org.dkpro.core.corenlp.CoreNlpLemmatizer;
import org.dkpro.core.corenlp.CoreNlpNamedEntityRecognizer;
import org.dkpro.core.corenlp.CoreNlpParser;
import org.dkpro.core.corenlp.CoreNlpPosTagger;
import org.dkpro.core.corenlp.CoreNlpSegmenter;
import org.dkpro.core.matetools.MateParser;
import org.dkpro.core.opennlp.OpenNlpChunker;
import org.dkpro.core.opennlp.OpenNlpParser;
import org.dkpro.core.stanfordnlp.StanfordCoreferenceResolver;

import de.fernunihagen.d2l2.io.CorefReader;


public class BaseExperiment {

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		preprocess();

	}
	private static void preprocess() throws ResourceInitializationException, UIMAException, IOException {

		// TODO: adjust paths and param_Language 
		String documentPath ="resources/ExampleDE.csv";
		String outputPath = "D:\\HIWI\\CF\\outputDE.txt";
//		String param_Language = "en";
		String param_Language = "de";
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
		  CorefReader.class, CorefReader.PARAM_INPUT_FILE, documentPath,CorefReader.PARAM_LANGUAGE, param_Language);
		AnalysisEngineDescription posTagger = createEngineDescription(CoreNlpPosTagger.class,
					CoreNlpPosTagger.PARAM_LANGUAGE, param_Language);  						

		AnalysisEngineDescription seg = createEngineDescription(CoreNlpSegmenter.class,
				CoreNlpSegmenter.PARAM_LANGUAGE, param_Language);
		AnalysisEngineDescription depparser = createEngineDescription(CoreNlpDependencyParser.class,CoreNlpDependencyParser.PARAM_LANGUAGE,param_Language);
		AnalysisEngineDescription forwardLookingCenters = createEngineDescription(ForwardLookingCenters.class,ForwardLookingCenters.PARAM_OUTPUT_FILE,outputPath);
		
		
//		AnalysisEngineDescription analyzer = createEngineDescription(Analyzer.class,Analyzer.PARAM_OUTPUT_FILE,outputPath);
//		AnalysisEngineDescription stanfordAnnotator = createEngineDescription(StanfordCoreferenceUIMAAnnotator.class,StanfordCoreferenceUIMAAnnotator.PARAM_OUTPUT_FILE,outputPath);
		SimplePipeline.runPipeline(reader, 
				seg, 
				posTagger,
				depparser,
				forwardLookingCenters
				);
	}

}
