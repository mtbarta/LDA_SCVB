package lda;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class DocumentConsumer implements Callable<Document>{
	//private final BlockingQueue<LinkedList<String>> queue;
	private LinkedList<String> doc;
    public DocumentConsumer(LinkedList<String> doc) {
        //this.queue = queue;
    	this.doc = doc;
    }

    public Document call() {
    	Document result = new Document();
        //LinkedList<String> doc = null;
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        int docNum;
        while(true) {
            // block if the queue is empty
			//doc = queue.take(); 
			// do things with doc
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
			            map.put(word, 1);
			            Cj++;
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
