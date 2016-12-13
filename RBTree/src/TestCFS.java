import cfs.RBTree;
import cfs.RunThread;
import cfs.*;

import java.util.Random;

public class TestCFS
{
	static int numOfThreads = 2;
	static int numOfTasks = 10;
	static String TREE_TYPE = "SeqRBTree";
	Random random;
	
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		if(args.length != 3)
		{
			System.out.println("wrong arguments...");
			System.out.println("Use: java TestCFS <Name of the tree> <number of threads> <number of tasks>");
			return;
		}

		TREE_TYPE = args[0];
		numOfThreads = Integer.parseInt(args[1]);
		numOfTasks = Integer.parseInt(args[2]);
		
		long globalTime = System.currentTimeMillis();
		
		RunThread[] threads = new RunThread[numOfThreads];
		RBTree rbTreeObj = (RBTree)Class.forName("cfs." + TREE_TYPE).newInstance();
		
		Random random = new Random();
		
		for(int i=0; i<numOfTasks; i++)
		{
			Node x = new Node(0);
			x.setID(i);
			x.setPriority(random.nextInt(5) + 1);
			x.setMaxRunTime(10000);
			x.sleepDuraion = 1000;
			rbTreeObj.insert(x);
		}
		
		for(int i=0; i<numOfThreads; i++)
			threads[i] = new RunThread(rbTreeObj, numOfThreads, globalTime);

		for(int i=0; i<numOfThreads; i++)
			threads[i].start();
		
		for(int i=0; i<numOfThreads; i++)
			threads[i].join();
	}
}
