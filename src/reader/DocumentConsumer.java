package reader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import util.Document;
import util.Vocabulary;

public class DocumentConsumer implements Callable<Document>{
	private LinkedList<String> doc;
	private int docCount;
	private Vocabulary vocab;
	
    public DocumentConsumer(LinkedList<String> doc) {
        //this.queue = queue;
    	this.doc = doc;
    	this.docCount = 0;
    }
    public DocumentConsumer(LinkedList<String> doc, Vocabulary vocab) {
        //this.queue = queue;
    	this.doc = doc;
    	this.docCount = 0;
    	this.vocab = vocab;
    }
    public Document call() {
    	Document result = new Document();

        HashMap<String,Integer> map;
        if (this.vocab != null){
        	map = new HashMap<String,Integer>(this.vocab);
        } else {
        	map = new HashMap<String,Integer>();
        }
        
        int docNum;
        while(true) {

			docNum = Integer.parseInt(doc.pop());
			int Cj = 0;
			String line;
			while (!doc.isEmpty()) {
				line = doc.pop();
			    for (String word : line.split(" ")){
			    	if (map.containsKey(word)) {
			            map.put(word, map.get(word) + 1);
			            Cj++;
			        } else {
			        	if (this.vocab != null){
			        		Cj++;
			        	} else {
				            map.put(word, 1);
				            Cj++;
			        	}
			        }
			    }
			}
			result = new Document(docNum,Cj, map);
			return result;
        }
        // poll() returns null if the queue is empty
		//return result;
    }
}
