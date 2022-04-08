package de.fernunihagen.d2l2.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;


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
			
			FileInputStream file = new FileInputStream(new File(inputFileURL.getPath()));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);			
			XSSFSheet sheet = workbook.getSheetAt(0);
			int rowNum = sheet.getLastRowNum() + 1;
			String inputText = "";
			index = 1;
			for (int j = 0; j < rowNum; j++) {	
				inputText =    readCellData(j, 0, inputFileURL.getPath(),0);						
				items.add(new QueueItem(index, inputText));	
				index++;
			}
							
									            
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
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
		getLogger().debug(item);
		
		try {
			
			jcas.setDocumentLanguage(language);
			jcas.setDocumentText(item.getText());
			
			DocumentMetaData dmd = DocumentMetaData.create(jcas);
			//TODO: The name of the getters und setters must be meaningful
			dmd.setDocumentId(String.valueOf(item.getId()));
			dmd.setDocumentTitle(item.getText());			
		}

		catch (Exception e) {
			throw new CollectionException(e);
		}

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		// will add the token content as a suffix to the ID of this unit
		unit.setSuffix(String.valueOf(item.getId()));
		unit.addToIndexes();
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		// TODO
		outcome.addToIndexes();
		currentIndex++;
	}
	
	//read value of a cell in excel with given row and column values
	public static String readCellData(int row, int column, String path, int sheetNumber ) {
		String value = null;
		Workbook wb = null;
		try {
			// reading data from a file in the form of bytes
			FileInputStream fis=new FileInputStream(path);  
			// constructs an XSSFWorkbook object, by buffering the whole stream into the
			// memory
			wb = new XSSFWorkbook(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Sheet sheet = wb.getSheetAt(sheetNumber); // getting the XSSFSheet object at given index
		Row row1 = sheet.getRow(row); // returns the logical row
		Cell cell = row1.getCell(column); // getting the cell representing the given column
		if(cell == null||row1 == null) {
			value = ""; // getting cell value
		}else {
			value = cell.getStringCellValue();
		}
				return value; // returns the cell value
	}
	
	class QueueItem{
		private int id;
		private String text;
		
		public QueueItem(int id, String text) {
			super();
			this.id = id;
			this.text = text;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
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