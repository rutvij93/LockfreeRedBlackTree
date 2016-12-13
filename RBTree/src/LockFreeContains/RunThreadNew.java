package LockFreeContains;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class RunThreadNew extends Thread
{
	RBTree rbTree;
	static volatile AtomicInteger addInt = new AtomicInteger(0);
	int work = 1000000;
	public int numOfThreads;
	public long endTime;
	public long startTime;
	public volatile static AtomicInteger success = new AtomicInteger(0);
	volatile static AtomicInteger count = new AtomicInteger(0);

	public RunThreadNew(RBTree rbTree2, int threads, int _work)
	{
		rbTree = rbTree2;
		numOfThreads = threads;
		work = _work;
	}

	@Override
	public void run()
	{
		if(rbTree.getClass().getName() == "LockFreeContains.LockFreeRBTree")
		{
			count.getAndIncrement();
			while(count.get() != numOfThreads);
			startTime = System.currentTimeMillis();
//			synchronized(rbTree)
			{	
				if(rbTree.getClass().toString() == "class lockfreeNew.LockFreeRBTree" || true)
				{

					for (int i = 0; i < work/numOfThreads; i++)
					{
						Node node2 = new Node(ThreadLocalRandom.current().nextInt(0, 1000));
						rbTree.insert(node2);
					}
					for (int i = 0; i < work/numOfThreads; i++)
					{
						if (rbTree.contains(ThreadLocalRandom.current().nextInt(0, 1000))){
							success.getAndIncrement();
						}
					}
				}
				endTime =  System.currentTimeMillis() - startTime;
			}
		}
		else
		{
			count.getAndIncrement();
			while(count.get() != numOfThreads);
			startTime = System.currentTimeMillis();
			synchronized(rbTree)
			{	
				if(rbTree.getClass().toString() == "class lockfreeNew.LockFreeRBTree" || true)
				{

					for (int i = 0; i < work/numOfThreads; i++)
					{
						Node node2 = new Node(ThreadLocalRandom.current().nextInt(0, 1000));
						rbTree.insert(node2);
					}
					for (int i = 0; i < work/numOfThreads; i++)
					{
						if (rbTree.contains(ThreadLocalRandom.current().nextInt(0, 1000))){
							success.getAndIncrement();
						}
					}
				}
				endTime =  System.currentTimeMillis() - startTime;
			}
		}
	}
}
