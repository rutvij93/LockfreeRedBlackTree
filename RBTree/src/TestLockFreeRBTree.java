import lockFree.*;

public class TestLockFreeRBTree 
{
	static int numOfThreads = 4;
	static int numOfTasks = 4;
	static final String TREE_TYPE = "LockFreeRBTree";
	
	public static void main(String[] args) throws InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		if(args.length != 2)
		{
			System.out.println("wrong arguments...");
			System.out.println("Use: java TestLockFreeRBTree <number of threads> <number of tasks>");
			return;
		}
		numOfThreads = Integer.parseInt(args[0]);
		numOfTasks = Integer.parseInt(args[1]);
		RunThreads[] threads = new RunThreads[numOfThreads];
		RBTree rbTreeObj = (RBTree)Class.forName("lockFree." + TREE_TYPE).newInstance();
		
		for(int i=0; i<numOfThreads; i++)
		{
			threads[i] = new RunThreads(rbTreeObj, numOfTasks);
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
		rbTreeObj.printME();
		
		System.out.println(rbTreeObj.getClass().toString());
		System.out.println("Nodes inserted: " + numOfTasks*numOfThreads
				+ "  Nodes deleted: " + LockFreeRBTree.count
				+ "  Total time " + (endTime - startTime) + " ms");
	}
}
