/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shef.mt.features.impl.doclevel;

import java.util.HashSet;
import shef.mt.features.impl.DocLevelFeature;
import shef.mt.features.util.Doc;
import shef.mt.features.util.Sentence;

/**
 *
 * @author carol
 */
public class DocLevelFeature1015 extends DocLevelFeature{
    private int toks = 0;
    private int utoks = 0;
    
    public DocLevelFeature1015() {
        setIndex(1015);
        setDescription("number of occurrences of the target word within the target hypothesis (averaged for all words in the hypothesis - type/token ratio)");

    }

    /* (non-Javadoc)
     * @see wlv.mt.features.impl.Feature#run(wlv.mt.features.util.Sentence, wlv.mt.features.util.Sentence)
     */
    @Override
    public void run(Sentence source, Sentence target) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not supported yet.");
        

    }

    @Override
    public void run(Doc source, Doc target) {
        HashSet<String> uniqueTokens = new HashSet<String>();
        int noTokens=0;
        for(int i=0; i<target.getSentences().size(); i++){
            noTokens+= target.getSentence(i).getNoTokens();
            String[] tokens = target.getSentence(i).getTokens();
            for (String token : tokens) {
                uniqueTokens.add(token.toLowerCase());
            }
            
        }
        if (uniqueTokens.isEmpty()) {
            setValue(0);
        } else {
            setValue((float) noTokens / uniqueTokens.size()); 
        }
        
    }


}
