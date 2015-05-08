/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shef.mt.features.impl.doclevel;

import shef.mt.features.impl.DocLevelFeature;
import shef.mt.features.util.Sentence;
import shef.mt.features.util.Doc;

/**
 * percentage of punctuation marks in source
 *
 * @author Catalina Hallett
 *
 */
public class DocLevelFeature1074 extends DocLevelFeature {

    public DocLevelFeature1074() {
        setIndex(1074);
        setDescription("percentage of punctuation marks in source");
    }

    public void run(Sentence source, Sentence target) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void run(Doc source, Doc target) {
        int countS = 0;
        int countT = 0;
        float noTokensSource = 0;
        for(int i=0;i<source.getSentences().size();i++){
            if (source.getSentence(i).isSet("count_.")) {
                countS += (Integer) source.getSentence(i).getValue("count_.");
            } else {
                countS += source.getSentence(i).countChar('.');
            }
            if (source.getSentence(i).isSet("count_,")) {
                countS += (Integer) source.getSentence(i).getValue("count_,");
            } else {
                countS += source.getSentence(i).countChar(',');
            }
            if (source.getSentence(i).isSet("count_؟")) {
                countS += (Integer) source.getSentence(i).getValue("count_؟");
            } else {
                countS += source.getSentence(i).countChar('؟');
            }
            if (source.getSentence(i).isSet("count_¿")) {
                countS += (Integer) source.getSentence(i).getValue("count_¿");
            } else {
                countS += source.getSentence(i).countChar('¿');
            }
            if (source.getSentence(i).isSet("count_،")) {
                countS += (Integer) source.getSentence(i).getValue("count_،");
            } else {
                countS += source.getSentence(i).countChar('،');
            }
            if (source.getSentence(i).isSet("count_؛")) {
                countS += (Integer) source.getSentence(i).getValue("count_؛");
            } else {
                countS += source.getSentence(i).countChar('؛');
            }
            if (source.getSentence(i).isSet("count_¡")) {
                countS += (Integer) source.getSentence(i).getValue("count_¡");
            } else {
                countS += source.getSentence(i).countChar('¡');
            }
            if (source.getSentence(i).isSet("count_!")) {
                countS += (Integer) source.getSentence(i).getValue("count_!");
            } else {
                countS += source.getSentence(i).countChar('!');
            }
            if (source.getSentence(i).isSet("count_?")) {
                countS += (Integer) source.getSentence(i).getValue("count_?");
            } else {
                countS += source.getSentence(i).countChar('?');
            }
            if (source.getSentence(i).isSet("count_:")) {
                countS += (Integer) source.getSentence(i).getValue("count_:");
            } else {
                countS += source.getSentence(i).countChar(':');
            }
            if (source.getSentence(i).isSet("count_;")) {
                countS += (Integer) source.getSentence(i).getValue("count_;");
            } else {
                countS += source.getSentence(i).countChar(';');
            }

            if (source.getSentence(i).isSet("noTokens")) {
                noTokensSource += source.getSentence(i).getNoTokens();
            }
        }
        //setValue(countS / noTokensSource);
        setValue(countS);
        source.setValue("noPunct", countS);
        
    }
}