package lda;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import lda.Document;

//TODO: overload for Hadoop processing.
public class Vectorizer {
	private int numDocs;
	public Vectorizer(int numDocs){
		this.numDocs = numDocs;
	}
	public HashMap<Integer,Document> readAll(File fileDir) 
	throws InterruptedException, ExecutionException, IOException{
		//read each file. When each file is vectorized, put it in a minibatch.
		//producer-consumer threading structure.
		
		//error checking!
		if (this.numDocs != fileDir.listFiles().length){
			throw new IOException();
		}
		
		final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        CompletionService<Document> completionService = 
        	       new ExecutorCompletionService<Document>(service);
        System.out.println("Submitting files to threads...");
        // Wait for ReadFile to complete
        service.submit(new ReadFile(completionService, fileDir)).get();
        System.out.println("    Thread Pool shut down...");
        service.shutdown();  // interrupt CPUTasks
        
		//do things with processed docs.
        HashMap<Integer,Document> documents = new HashMap<Integer,Document>(this.numDocs);
        while(!service.isTerminated()){
        	try{
	        	Document doc = completionService.take().get();
	        	documents.put(doc.docId,doc);
        	} catch(ExecutionException e) {
        		e.getCause();
        		e.printStackTrace();
        	}
        }
        service.awaitTermination(365, TimeUnit.DAYS);
        return documents;
	}
	
//	public static void main(String[] args) throws IOException {
//		long startTime = System.currentTimeMillis();
//		File file = new File("./text");
//		
//		Vectorizer vec = new Vectorizer(5);
//		try {
//			vec.readAll(file);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		long endTime = System.currentTimeMillis();
//		long totalTime = endTime - startTime;
//		System.out.print("Runtime: ");
//		System.out.print(totalTime);
//	}
	
	public BlockingQueue<Minibatch> createMiniBatches(HashMap<Integer,Document> docs,
									int miniBatchSize, int numDocs){
		BlockingQueue<Minibatch> result = new LinkedBlockingQueue<Minibatch>();
		Minibatch mb = new Minibatch();
		for(int i=0; i< numDocs; i++){
			mb.docs.add(docs.get(i));
			if (i % miniBatchSize == 0){
				result.add(mb);
				mb = new Minibatch();
			}
		}
		
		return result;
	}
}
