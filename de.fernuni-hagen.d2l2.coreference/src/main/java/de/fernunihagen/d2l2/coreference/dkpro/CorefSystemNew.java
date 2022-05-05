package de.fernunihagen.d2l2.coreference.dkpro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.stanford.nlp.coref.CorefAlgorithm;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefDocumentProcessor;
import edu.stanford.nlp.coref.CorefPrinter;
import edu.stanford.nlp.coref.CorefProperties;
import edu.stanford.nlp.coref.CorefScorer;
import edu.stanford.nlp.coref.CorefUtils;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefCluster;
import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.coref.data.Document;
import edu.stanford.nlp.coref.data.DocumentMaker;
import edu.stanford.nlp.dcoref.Constants;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.dcoref.MentionExtractor;
import edu.stanford.nlp.dcoref.RuleBasedCorefMentionFinder;
import edu.stanford.nlp.dcoref.Semantics;
import edu.stanford.nlp.dcoref.sievepasses.DeterministicCorefSieve;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.semgraph.SemanticGraphFactory.Mode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalStructure.Extras;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.logging.NewlineLogFormatter;
import edu.stanford.nlp.util.logging.Redwood;

/**
 * Class for running coreference algorithms
 * @author Kevin Clark
 */
public class CorefSystemNew {
	/** A logger for this class */
  private static final Redwood.RedwoodChannels log = Redwood.channels(CorefSystemNew.class);
  private final DocumentMaker docMaker;
  private final CorefAlgorithm corefAlgorithm;
  private final boolean removeSingletonClusters;
  private final boolean verbose;
  
  private final boolean allowReparsing;
 

  public CorefSystemNew(Properties props) {
    try {
      dictionaries = new Dictionaries(props);
      docMaker = new DocumentMaker(props, dictionaries);
      corefAlgorithm = CorefAlgorithm.fromProps(props, dictionaries);
      allowReparsing = PropertiesUtils.getBool(props, Constants.ALLOW_REPARSING_PROP, Constants.ALLOW_REPARSING);
      removeSingletonClusters = CorefProperties.removeSingletonClusters(props);
      verbose = CorefProperties.verbose(props);
    } catch (Exception e) {
      throw new RuntimeException("Error initializing coref system", e);
    }
	this.semantics = new Semantics();
  }

  public CorefSystemNew(DocumentMaker docMaker, CorefAlgorithm corefAlgorithm,
      boolean removeSingletonClusters, boolean verbose,boolean allowReparsing,MentionExtractor mentionExtractor) throws ClassNotFoundException, IOException {
    this.docMaker = docMaker;
    this.corefAlgorithm = corefAlgorithm;
    this.removeSingletonClusters = removeSingletonClusters;
    this.verbose = verbose;
	this.dictionaries = new Dictionaries();
	this.semantics = new Semantics();
	this.allowReparsing=allowReparsing;
	
  }

  public void annotate(Annotation annotation) {
    Document document;
    try {
      document = docMaker.makeDocument(annotation);
    } catch (Exception e) {
      throw new RuntimeException("Error making document", e);
    }

    CorefUtils.checkForInterrupt();
    corefAlgorithm.runCoref(document);
    if (removeSingletonClusters) {
      CorefUtils.removeSingletonClusters(document);
    }
    CorefUtils.checkForInterrupt();
    try {
        List<Tree> trees = new ArrayList<>();
        List<List<CoreLabel>> sentences = new ArrayList<>();

        // extract trees and sentence words
        // we are only supporting the new annotation standard for this Annotator!
        boolean hasSpeakerAnnotations = false;
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
          // int sentNum = 0;
          for (CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            sentences.add(tokens);
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            trees.add(tree);

            SemanticGraph dependencies = SemanticGraphFactory.makeFromTree(tree, Mode.COLLAPSED, Extras.NONE, null, true); // locking here is crucial for correct threading!
            sentence.set(SemanticGraphCoreAnnotations.AlternativeDependenciesAnnotation.class, dependencies);

            if (!hasSpeakerAnnotations) {
              // check for speaker annotations
              for (CoreLabel t:tokens) {
                if (t.get(CoreAnnotations.SpeakerAnnotation.class) != null) {
                  hasSpeakerAnnotations = true;
                  break;
                }
              }
            }
            MentionExtractor.mergeLabels(tree, tokens);
            MentionExtractor.initializeUtterance(tokens);
          }
        } else {
          log.error("this coreference resolution system requires SentencesAnnotation!");
          return;
        }
        if (hasSpeakerAnnotations) {
          annotation.set(CoreAnnotations.UseMarkedDiscourseAnnotation.class, true);
        }

        // extract all possible mentions
        // this is created for each new annotation because it is not threadsafe
        RuleBasedCorefMentionFinder finder = new RuleBasedCorefMentionFinder(allowReparsing);
        //List<List<Mention>> allUnprocessedMentions = finder.extractPredictedMentions(annotation, 0, dictionaries);

        // add the relevant info to mentions and order them for coref
       
        Map<Integer, CorefChain> result = Generics.newHashMap();
        for (CorefCluster c : document.corefClusters.values()) {
          result.put(c.clusterID, new CorefChain(c, document.positions));
        }
        annotation.set(CorefCoreAnnotations.CorefChainAnnotation.class, result);

        
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        // restore to the fine-grained
        setNamedEntityTagGranularity(annotation, "fine");
      }
   
  }

  public void initLogger(Logger logger, String logFileName) {
      try {
          FileHandler fh = new FileHandler(logFileName, false);
          logger.addHandler(fh);
          logger.setLevel(Level.FINE);
          fh.setFormatter(new NewlineLogFormatter());
      } catch (SecurityException | IOException e) {
          throw new RuntimeException("Cannot initialize logger!", e);
      }
  }

  public void runOnConll(Properties props) throws Exception {
    File f = new File(CorefProperties.conllOutputPath(props));
    if (! f.exists()) {
      f.mkdirs();
    }
    String timestamp = Calendar.getInstance().getTime().toString().replaceAll("\\s", "-").replaceAll(":", "-");
    String baseName = CorefProperties.conllOutputPath(props) + timestamp;
    String goldOutput = baseName + ".gold.txt";
    String beforeCorefOutput = baseName + ".predicted.txt";
    String afterCorefOutput = baseName + ".coref.predicted.txt";
    PrintWriter writerGold = new PrintWriter(new FileOutputStream(goldOutput));
    PrintWriter writerBeforeCoref = new PrintWriter(new FileOutputStream(beforeCorefOutput));
    PrintWriter writerAfterCoref = new PrintWriter(new FileOutputStream(afterCorefOutput));

    Logger logger = Logger.getLogger(CorefSystemNew.class.getName());
    initLogger(logger,baseName + ".log");
    logger.info(timestamp);
    logger.info(props.toString());

    (new CorefDocumentProcessor() {
      @Override
      public void process(int id, Document document) {
        writerGold.print(CorefPrinter.printConllOutput(document, true));
        writerBeforeCoref.print(CorefPrinter.printConllOutput(document, false));
        long time = System.currentTimeMillis();
        corefAlgorithm.runCoref(document);
        if (verbose) {
          Redwood.log(getName(), "Coref took "
              + (System.currentTimeMillis() - time) / 1000.0 + "s");
        }
        CorefUtils.removeSingletonClusters(document);
        if (verbose) {
          CorefUtils.printHumanReadableCoref(document);
        }
        if (document.filterMentionSet != null) {
          Map<Integer,CorefCluster> filteredClusters = document.corefClusters
                  .values().stream().filter(x -> CorefUtils.filterClustersWithMentionSpans(x, document.filterMentionSet) )
                  .collect(Collectors.toMap(x -> x.clusterID, x -> x));
          writerAfterCoref.print(CorefPrinter.printConllOutput(document, false, true, filteredClusters));
        } else {
          writerAfterCoref.print(CorefPrinter.printConllOutput(document, false, true));
        }
      }

      @Override
      public void finish() throws Exception {}

      @Override
      public String getName() {
        return corefAlgorithm.getClass().getName();
      }
    }).run(docMaker);

    String summary = CorefScorer.getEvalSummary(CorefProperties.getScorerPath(props),
        goldOutput, beforeCorefOutput);

    logger.info("Before Coref");
    CorefScorer.printScoreSummary(summary, logger, false);
    CorefScorer.printScoreSummary(summary, logger, true);
    CorefScorer.printFinalConllScore(summary, logger);

    summary = CorefScorer.getEvalSummary(CorefProperties.getScorerPath(props), goldOutput,
        afterCorefOutput);
    logger.info("After Coref");
    CorefScorer.printScoreSummary(summary, logger, false);
    CorefScorer.printScoreSummary(summary, logger, true);
    CorefScorer.printFinalConllScore(summary, logger);

    writerGold.close();
    writerBeforeCoref.close();
    writerAfterCoref.close();
  }
  
//flip which granularity of ner tag is primary
 public void setNamedEntityTagGranularity(Annotation annotation, String granularity) {
   List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
   Class<? extends CoreAnnotation<String>> sourceNERTagClass;
   if (granularity.equals("fine"))
     sourceNERTagClass = CoreAnnotations.FineGrainedNamedEntityTagAnnotation.class;
   else if (granularity.equals("coarse"))
     sourceNERTagClass = CoreAnnotations.CoarseNamedEntityTagAnnotation.class;
   else
     sourceNERTagClass = CoreAnnotations.NamedEntityTagAnnotation.class;
   // switch tags
   for (CoreLabel token : tokens) {
     if (!"".equals(token.get(sourceNERTagClass)) && token.get(sourceNERTagClass) != null)
       token.set(CoreAnnotations.NamedEntityTagAnnotation.class, token.get(sourceNERTagClass));
   }
 }

  public static void main(String[] args) throws Exception {
    Properties props = StringUtils.argsToProperties(args);
    CorefSystemNew coref = new CorefSystemNew(props);
    coref.runOnConll(props);
  }
  public final Dictionaries dictionaries;
  public Dictionaries dictionaries() { return dictionaries; }
  public final Semantics semantics;
  public Semantics semantics() { return semantics; }
 
}
