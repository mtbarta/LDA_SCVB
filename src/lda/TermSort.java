package lda;

import java.util.Comparator;
import java.util.HashMap;

public class TermSort implements Comparator<String> {

    HashMap<String, Double> base;
    public TermSort(HashMap<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) < base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}