package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HashMapSort {
	public static HashMap<String, Double> sortByValue(Map<String, Double> unsortMap, int topWords)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {
            	return o2.getValue().compareTo(o1.getValue());
            }
        });
//        double sum = 0.0;
//        for(Entry<String,Double> e : list){
//        	sum += e.getValue();
//        }
//        System.out.println(sum);
        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (int i=0; i<topWords; i++)
        {
        	Entry<String,Double> entry = list.get(i);
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
