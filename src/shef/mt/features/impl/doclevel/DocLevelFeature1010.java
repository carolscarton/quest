/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shef.mt.features.impl.doclevel;

import java.util.ArrayList;
import shef.mt.features.impl.DocLevelFeature;
import shef.mt.features.util.Doc;
import shef.mt.features.util.Sentence;

/**
 *
 * @author carol
 */
public class DocLevelFeature1010 extends DocLevelFeature {
    
    public DocLevelFeature1010() {
        setIndex(1010);
        setDescription("source document perplexity");
        this.addResource("logprob");
        this.addResource("postagger");
    }

    @Override
    public void run(Doc source, Doc target) {
        ArrayList<Sentence> sentences = source.getSentences();
        double doc_log_prob = 0.0;
        for(int i=0; i<sentences.size();i++){
            
            doc_log_prob+=(float) sentences.get(i).getValue("ppl");
        }
        setValue((float) doc_log_prob/sentences.size());
    }

    @Override
    public void run(Sentence source, Sentence target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
