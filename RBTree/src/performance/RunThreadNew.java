package performance;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class RunThreadNew extends Thread
{
	RBTree rbTree;
	static volatile AtomicInteger addInt = new AtomicInteger(0);
	public static int work = 1000000;
	public long endTime;
	public long startTime;
	volatile static AtomicInteger count = new AtomicInteger(0);
	public static int numOfThreads = 8;

	public RunThreadNew(RBTree rbTree2, int _numOfThreads, int _work)
	{
		rbTree = rbTree2;
		this.numOfThreads = _numOfThreads;
		this.work = _work;
	}

	@Override
	public void run()
	{	
		count.getAndIncrement();
		while(count.get() != numOfThreads);
		startTime = System.currentTimeMillis();
		
		if(rbTree.getClass().getName() =="performance.SeqRBTree")
		{
			synchronized(rbTree)
			{	
					for (int i = 0; i < work/numOfThreads; i++)
					{
						Node node2 = new Node(ThreadLocalRandom.current().nextInt(0, 1000));
						rbTree.insert(node2);
					}
				endTime =  System.currentTimeMillis() - startTime;
			}
		}
		else
		{		
			for (int i = 0; i < work/numOfThreads; i++)
				{
					Node node2 = new Node(ThreadLocalRandom.current().nextInt(0, 1000));
					rbTree.insert(node2);
				}
			endTime =  System.currentTimeMillis() - startTime;
		}
	}
}