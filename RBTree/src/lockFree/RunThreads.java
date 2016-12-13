package lockFree;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class RunThreads extends Thread
{
	RBTree rbTree;
	int numOfTasks = 4;
	static volatile AtomicInteger addInt = new AtomicInteger(0);

	public RunThreads(RBTree rbTree2, int tasks)
	{
		rbTree = rbTree2;
		numOfTasks = tasks;
	}

	@Override
	public void run()
	{	
		for (int i = 0; i < numOfTasks; i++)
		{
			Node node2 = new Node(ThreadLocalRandom.current().nextInt(0, 1000));
			{
				rbTree.insert(node2);
				if (i%2 == 0)
					rbTree.delete();
			}
		}
	}
}
