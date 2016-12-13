import performance.*;
import performance.RunThreadNew;

public class TestRBTree 
{
	static int numOfThreads = 6;
	static String TREE_TYPE = "SeqRBTree";
	static int numOfProcesses = 1000000;
	
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		if(args.length != 3)
		{
			System.out.println("wrong arguments...");
			System.out.println("Use: java TestRBTree <Name of the tree> <Number of Threads> <number of tasks>");
			return;
		}

		TREE_TYPE = args[0];
		numOfThreads = Integer.parseInt(args[1]);
		numOfProcesses = Integer.parseInt(args[2]);
		RunThreadNew[] threads = new RunThreadNew[numOfThreads];
		RBTree rbTreeObj = (RBTree)Class.forName("performance." + TREE_TYPE).newInstance();
		
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i] = new RunThreadNew(rbTreeObj, numOfThreads, numOfProcesses);
		}
		long startTime = System.currentTimeMillis();
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i].start();
		}
		
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i].join();
		}
		
		
		long endTime = System.currentTimeMillis();
		long totalTime = 0;
		for(int i=0; i<numOfThreads; i++)
		{
			totalTime += threads[i].endTime;
		}
		
		rbTreeObj.printME();
		
		System.out.println(rbTreeObj.getClass().getName());
		System.out.println("Total tasks:" + RunThreadNew.work + "  Total time " + (endTime - startTime) + " ms");
	}
}
