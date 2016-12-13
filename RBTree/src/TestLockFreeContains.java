import LockFreeContains.*;
public class TestLockFreeContains 
{
	static int numOfThreads = 6;
	static int numOfTasks = 1000;
	static String TREE_TYPE = "LockFreeRBTree";
	
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		if(args.length != 3)
		{
			System.out.println("wrong arguments...");
			System.out.println("Use: java TestLockFreeContains <Name of the tree> <number of threads> <number of tasks>");
			return;
		}

		TREE_TYPE = args[0];
		numOfThreads = Integer.parseInt(args[1]);
		numOfTasks = Integer.parseInt(args[2]);

		RunThreadNew[] threads = new RunThreadNew[numOfThreads];
		RBTree rbTreeObj = (RBTree)Class.forName("LockFreeContains." + TREE_TYPE).newInstance();
		
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i] = new RunThreadNew(rbTreeObj, numOfThreads, numOfTasks);
		}
		long startTime = System.nanoTime();
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i].start();
		}
		
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i].join();
		}
		
		
		long endTime = System.nanoTime();
		long totalTime = 0;
		for(int i=0; i<numOfThreads; i++)
		{
			totalTime += threads[i].endTime;
		}
		
		rbTreeObj.printME();
		
		System.out.println("Total time " + (endTime - startTime) + " ms Count: " + LockFreeRBTree.count );
		System.out.println("Total number of contains is " + RunThreadNew.success.get());
	}
}
