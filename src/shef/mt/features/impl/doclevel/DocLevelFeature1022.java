/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shef.mt.features.impl.doclevel;
import java.util.HashSet;
import java.util.StringTokenizer;

import shef.mt.features.impl.DocLevelFeature;
import shef.mt.features.util.Doc;
import shef.mt.features.util.Sentence;
import shef.mt.tools.Giza;
import shef.mt.tools.Giza2;

/**
 * average number of translations per source word in the sentence (threshold in
 * giza1: prob > 0.2)
 *
 * @author Catalina Hallett
 *
 *
 */
public class DocLevelFeature1022 extends DocLevelFeature {

    final static Float probThresh = 0.20f;

    public DocLevelFeature1022() {
        setIndex(1022);
        setDescription("average number of translations per source word in the sentence (threshold in giza1: prob > 0.2)");
        this.addResource("Giza");
    }

    /* (non-Javadoc)
     * @see wlv.mt.features.util.Feature#run(wlv.mt.features.util.Sentence, wlv.mt.features.util.Sentence)
     */
    @Override
    public void run(Sentence source, Sentence target) {


        //setValue(result);
    }

    @Override
    public void run(Doc source, Doc target) {
        float result = 0.0f;
        for (int i=0;i<source.getSentences().size();i++){
            // TODO Auto-generated method stub
            float noTokens = source.getSentence(i).getNoTokens();
            String[] tokens = source.getSentence(i).getTokens();
            float probSum = 0;
            float value;
            for (String word : tokens) {
                value = Giza.getWordProbabilityCount(word.toLowerCase(), probThresh);
                probSum += value;
            }

            result += probSum / noTokens;            
        }
        setValue(result/(float)source.getSentences().size());
    }
}
