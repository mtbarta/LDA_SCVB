package Refactored;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Refactored.Document;

//TODO: overload for Hadoop processing.
//TODO: neither parameter does anything.
public class Vectorizer {
	public Vectorizer(int batchSize, int numDocs){
	}
	public List<Document> readAll(File fileDir) 
	throws InterruptedException, ExecutionException{
		//read each file. When each file is vectorized, put it in a minibatch.
		//producer-consumer threading structure.
		int NUM_THREADS = Runtime.getRuntime().availableProcessors();
        BlockingQueue<LinkedList<String>> queue = new ArrayBlockingQueue<>(50);
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        CompletionService<Document> completionService = 
        	       new ExecutorCompletionService<Document>(service);
        List<Future<Document>> docs = new ArrayList<Future<Document>>();
        for (int i = 0; i < (NUM_THREADS - 1); i++) {
            docs.add(completionService.submit(new DocumentConsumer(queue)));
        }
        // Wait for ReadFile to complete
        service.submit(new ReadFile(queue, fileDir)).get();
        service.shutdownNow();  // interrupt CPUTasks
        // Wait for DocumentConsumer to complete
        service.awaitTermination(365, TimeUnit.DAYS);
		
		//do things with processed docs.
        List<Document> Documents = new ArrayList<Document>();
        for(Future<Document> d : docs){
        	try{
        	Document doc = d.get();
        	Documents.add(doc);
        	System.out.println(Integer.toString(doc.Cj));
        	} catch(ExecutionException e) {
        		e.getCause();e.printStackTrace();
        	}
        }
        return Documents;
	}
	/*
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File("./text");
		
		Vectorizer vec = new Vectorizer(1,1);
		try {
			vec.readAll(file);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	public List<Minibatch> createMinibatches(ArrayList<Document> docs,
									int miniBatchSize, int numDocs){
		List<Minibatch> result = new ArrayList<Minibatch>();
		Minibatch mb = new Minibatch();
		for(int i=1; i<= numDocs; i++){
			mb.docs.add(docs.get(i));
			if (i % miniBatchSize == 0){
				result.add(mb);
				mb = new Minibatch();
			}
		}
		
		return result;
	}
}
