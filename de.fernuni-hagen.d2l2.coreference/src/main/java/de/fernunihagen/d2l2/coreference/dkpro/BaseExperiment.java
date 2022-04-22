package de.fernunihagen.d2l2.coreference.dkpro;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import org.dkpro.core.corenlp.CoreNlpLemmatizer;
import org.dkpro.core.corenlp.CoreNlpNamedEntityRecognizer;
import org.dkpro.core.corenlp.CoreNlpPosTagger;
import org.dkpro.core.corenlp.CoreNlpSegmenter;
import org.dkpro.core.io.bincas.BinaryCasWriter;
import org.dkpro.core.opennlp.OpenNlpChunker;
import org.dkpro.core.opennlp.OpenNlpParser;
import org.dkpro.core.stanfordnlp.StanfordCoreferenceResolver;

import de.fernunihagen.d2l2.io.CorefReader;


public class BaseExperiment {

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		preprocess();

	}
	private static void preprocess() throws ResourceInitializationException, UIMAException, IOException {

		  //CorefExampleReader // TODO: adjust paths  
		String documentPath ="D:\\HIWI\\coref\\ExampleTexts.csv";
		String outputPath = "D:\\HIWI\\coref\\output\\dkpro\\CorefDeterministic.txt";
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
		  CorefReader.class, CorefReader.PARAM_INPUT_FILE, documentPath);
		AnalysisEngineDescription posTagger = createEngineDescription(CoreNlpPosTagger.class,
					CoreNlpPosTagger.PARAM_LANGUAGE, "en");  						
		AnalysisEngineDescription entityRecognizer = createEngineDescription(CoreNlpNamedEntityRecognizer.class,
				CoreNlpNamedEntityRecognizer.PARAM_LANGUAGE,"en");		
		AnalysisEngineDescription lemmatizer = createEngineDescription(CoreNlpLemmatizer.class);
		AnalysisEngineDescription seg = createEngineDescription(CoreNlpSegmenter.class,
				CoreNlpSegmenter.PARAM_LANGUAGE, "en");
		AnalysisEngineDescription parser = createEngineDescription(OpenNlpParser.class,OpenNlpParser.PARAM_LANGUAGE,"en");
		AnalysisEngineDescription chunker = createEngineDescription(OpenNlpChunker.class,
				OpenNlpChunker.PARAM_LANGUAGE, "en");
		AnalysisEngineDescription coreferenceResolver = createEngineDescription(StanfordCoreferenceResolver.class);
//		AnalysisEngineDescription coreferenceResolver = createEngineDescription(CoreNlpCoreferenceResolver.class);		
		AnalysisEngineDescription analyzer = createEngineDescription(Analyzer.class,Analyzer.PARAM_OUTPUT_FILE,outputPath);
		AnalysisEngineDescription stanfordAnnotator = createEngineDescription(StanfordCoreferenceUIMAAnnotator.class,StanfordCoreferenceUIMAAnnotator.PARAM_OUTPUT_FILE,outputPath);
		AnalysisEngineDescription binCasWriter = createEngineDescription(
				BinaryCasWriter.class, 
				BinaryCasWriter.PARAM_FORMAT, "6+",
				BinaryCasWriter.PARAM_OVERWRITE, true,
				BinaryCasWriter.PARAM_TARGET_LOCATION, "target/bincas"
				);
		SimplePipeline.runPipeline(reader, 
				seg, 
				posTagger,
				lemmatizer,
				entityRecognizer,
				parser,
				coreferenceResolver,
				analyzer
//				stanfordAnnotator
				);
	}

}
