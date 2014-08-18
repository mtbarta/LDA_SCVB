package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Print {
	public static void printDocTopics(double[][] array){
		System.out.println();
		System.out.println(Arrays.deepToString(array));
	}
	public static void printTopicWords(ArrayList<HashMap<String,Double>> TopicWords){
		for (int topic=0; topic < TopicWords.size(); topic++){
			System.out.printf("Topic: %d", topic);
			System.out.println();
			for(Map.Entry<String,Double> entry : TopicWords.get(topic).entrySet()){
				System.out.println("    " + entry.getKey() + " : " + entry.getValue());
			}
		}
	}
}
