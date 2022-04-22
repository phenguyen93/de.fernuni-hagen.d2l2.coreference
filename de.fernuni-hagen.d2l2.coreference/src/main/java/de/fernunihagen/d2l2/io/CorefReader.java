package de.fernunihagen.d2l2.io;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.core.api.resources.ResourceUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;


/*
 * read feedbacks, questions, target answers and answers from excel file
 * 
 */

public class CorefReader extends JCasCollectionReader_ImplBase {

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_SCORE_FILE = "ScoreFile";
	@ConfigurationParameter(name = PARAM_SCORE_FILE, mandatory = false)
	protected String scoreFileString;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = "en")
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	protected int currentIndex;

	protected Queue<QueueItem> items;
	int index;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		items = new LinkedList<QueueItem>();
		index = 0;
		
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			File file = new File(inputFileString);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = " ";
			String[] tempArr;
			while ((line = br.readLine()) != null){
				tempArr = line.split(";");
				String id =tempArr[0];
				String text = tempArr[1];
				items.add(new QueueItem(id, text));
			}
			
	      	br.close();
	    }
	    catch(IOException ioe) {
	      ioe.printStackTrace();
	    }
		//delete label in list
		items.remove();
		currentIndex = 0;
	}
	// HOTFIX for Issue 445 in DKPro Core
	private static String cleanString(String textForCas) {
		textForCas = textForCas.replaceAll("[^a-zA-Z0-9\\-\\.,:;\\(\\)\\? ]", "");
		textForCas = textForCas.replace("…", "...");
		textForCas = textForCas.replace("´", "'");
		return textForCas.replace("’", "'").trim();
	}
	
	public boolean hasNext() throws IOException, CollectionException {
		return !items.isEmpty();
	}

	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		QueueItem item = items.poll();
	//	getLogger().debug(item);
		
		try {
			
			jcas.setDocumentLanguage(language);
			jcas.setDocumentText(item.getText());
			
			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			//TODO: The name of the getters und setters must be meaningful
			dmd.setDocumentId(item.getId());
			dmd.setDocumentTitle(item.getText());			
		}

		catch (Exception e) {
			throw new CollectionException(e);
		}

		//TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit
		//unit.setSuffix(String.valueOf(item.getId()));
		//unit.addToIndexes();
		//TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		// TODO
		//outcome.addToIndexes();
		currentIndex++;
	}
	
	
	class QueueItem{
		private String id;
		private String text;
		
		public QueueItem(String id, String text) {
			super();
			this.id = id;
			this.text = text;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		
	}
	
}