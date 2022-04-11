package de.fernunihagen.d2l2.coreference;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

/** app for testing if Maven distribution is working properly */

public class StanfordCorefDeterministic
{
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
    	for (int i = 1; i <= 12; i++) {
    		//name of txt file
        	String inputText = i+".txt";
        	System.out.println(inputText);
        	String[] englishArgs = new String[]{"-file", inputText, "-outputFormat", "text", "-props", "deterministic-english.properties"};
            StanfordCoreNLP.main(englishArgs);
		}
    	//Note: The output file will have the same name as the input text with the extension ".out"
     
    }
}
