package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scvb.Minibatch;
import util.Document;

/*
 * TODO: implement an iterator based on minibatch size.
 * TODO: implement a tokenizer class that can be adjusted.
 */
public class Reader {
	private int wordsInCorpus = 0;
	private HashMap<Integer,Integer> docSizes;
	
	public ArrayList<Document> readAll(String fileDir, int numDocs, int numThreads) 
			throws InterruptedException, ExecutionException, IOException{
		//read each file. When each file is vectorized, put it in a minibatch.
		//producer-consumer threading structure.
		/*
		URL file2 = getClass().getResource(fileDir);
		if (file2 == null){
			throw new FileNotFoundException("Directory location not found");
		}
		String filePath = file2.getPath();
        filePath = filePath.replaceFirst("/","");
        */
		File directory = new File(fileDir);
		
		//error checking!
		if (numDocs != directory.listFiles().length){
			throw new IOException("Documents specified is not the same as amount in Directory.");
		}
				
        ExecutorService service = Executors.newFixedThreadPool(numThreads);
        CompletionService<Document> completionService = 
        	       new ExecutorCompletionService<Document>(service);
        System.out.println("Submitting files to threads...");
        // Wait for ReadFile to complete
        service.submit(new ReadFile(completionService, directory)).get();
        System.out.println("    Thread Pool shut down...");
        service.shutdown();  // interrupt CPUTasks
        
		//do things with processed docs.
        ArrayList<Document> documents = new ArrayList<Document>(numDocs);
        while(!service.isTerminated()){
        	try{
	        	Document doc = completionService.take()
	        			.get();
	        	documents.add(doc);
        	} catch(ExecutionException e) {
        		e.getCause();
        		e.printStackTrace();
        	}
        }
        service.awaitTermination(365, TimeUnit.DAYS);
        return documents;
	}
	
	/*
	 * GETTERS
	 */
	public int getWordsInCorpus() {
		return wordsInCorpus;
	}

	public HashMap<Integer, Integer> getDocSizes() {
		return docSizes;
	}

}
