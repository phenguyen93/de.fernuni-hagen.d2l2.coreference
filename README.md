# de.fernuni-hagen.d2l2.coreference
Coreference Resolution finds references to the same entity in a text, such as in the sentence "President Barack Obama was born in Hawaii.  He was elected in 2008." where "President Barack Obama" and "he" refer to the same person. In this project, we will first use DKPro Core UIMA CoreNLPCoreferenceResolver-component to test some simple short texts.

There are also three different reference systems available in CoreNLP (CoreNLP is created by the Stanford NLP Group) which are:
- Deterministic: Fast rule-based coreference resolution for English and Chinese.
- Statistical: Machine-learning-based coreference resolution for English. Unlike the other systems, this one only requires dependency parses, which are faster to produce than constituency parses.
- Neural: Most accurate but slow neural-network-based coreference resolution for English and Chinese.

(see https://stanfordnlp.github.io/CoreNLP/coref.html#api for more informations). 
So, we will test all 3 systems on short examples to see the difference between them.
# Usage
## First Steps
- Import de.fernuni-hagen.d2l2.coreference as a maven project in eclipse
- To use DKPro's Coreference Resolver component: Adjust the paths to the input, output files and try running BaseExperiment.class
- Using the Stanford NLP Coreference Resolver component:
  + Run StanfordCorefNeural.class for neural system
  + Run StanfordCorefDeterministic.class for deterministic system
  + Run StanfordCorefStatistical.class for statistical system
