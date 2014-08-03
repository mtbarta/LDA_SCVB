package Refactored;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ReadFile implements Runnable {

    private final BlockingQueue<LinkedList<String>> queue;
    private final File fileDir;
    private int count = 1;

    public ReadFile(BlockingQueue<LinkedList<String>> queue, File fileDir) {
        this.queue = queue;
        this.fileDir = fileDir;
    }

    @Override
    public void run() {
        try {
            for (File file : this.fileDir.listFiles()) {
            	if(file == null) break;
            	LinkedList<String> tempFile = new LinkedList<String>();
            	tempFile.add(Integer.toString(this.count));
            	//TODO: add processing features
                Scanner s = new Scanner(file);
                String line;
                while (s.hasNext()) {
                	line = s.next();
                	tempFile.add(line);
                }
                // block if the queue is full
                queue.offer(tempFile, 365, TimeUnit.DAYS);
            } 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//s.close();
		}
    }
}