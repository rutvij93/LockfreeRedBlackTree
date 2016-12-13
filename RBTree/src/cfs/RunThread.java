package cfs;
public class RunThread extends Thread
{
	RBTree rbTree;
	static int numOfThreads = 0;
	static long globalTime;

	public RunThread(RBTree treeObj, int _threads, long _startTime)
	{
		numOfThreads = _threads;
		globalTime = _startTime;
		rbTree = treeObj;
	}

	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		while(true)
		{
			if(rbTree.getClass().getName() == "cfs.LockFreeRBTree")
			{
				Node deletedNode = new Node(-1);
//				synchronized (rbTree)
				{
					deletedNode = rbTree.delete();
				}
				if(deletedNode == null)
				{
					System.out.println("tree is empty");
				}
				if(deletedNode.key > -1)
				{
					//delete successful
					deletedNode.deleteCount++;
					if(deletedNode.isSleeping.get())
					{
						if((System.currentTimeMillis() - globalTime) >= deletedNode.sleepDuraion)
						{
							deletedNode.setSleeping(false);
							deletedNode = processTask(deletedNode);

							if(deletedNode.key * deletedNode.getPriority() <= deletedNode.maxRunTime.get())
							{
								deletedNode.reset();
//								synchronized (rbTree)
								{
									rbTree.insert(deletedNode);
								}
							}
							else
							{
								long endTime = System.currentTimeMillis();

								System.out.println("Process " + deletedNode.getID() + 
										" with priority " + deletedNode.getPriority() + 
										" completed in " + (endTime - startTime) + " ms");

							}
						}
						else
						{
							deletedNode.maxRunTime.set(deletedNode.maxRunTime.get() + 100);
							deletedNode.runTime.set(deletedNode.runTime.get() + 100);
							deletedNode.reset();
//							synchronized (rbTree)
							{
								rbTree.insert(deletedNode);
							}
						}
					}
					else
					{
						//give tasks to the processor
						deletedNode = processTask(deletedNode);

						if(deletedNode.key * deletedNode.getPriority() <= deletedNode.maxRunTime.get())
						{
							deletedNode.reset();
//							synchronized (rbTree)
							{
								rbTree.insert(deletedNode);
							}
						}
						else
						{
							long endTime = System.currentTimeMillis();

							System.out.println("Process " + deletedNode.getID() + 
									" with priority " + deletedNode.getPriority() + 
									" completed in " + (endTime - startTime) + " ms");

						}
					}
				}
				else
				{
					return; 
				}
			}
			else
			{
				Node deletedNode = new Node(-1);
				synchronized (rbTree)
				{
					deletedNode = rbTree.delete();
				}
				if(deletedNode == null)
				{
					System.out.println("tree is empty");
				}
				if(deletedNode.key > -1)
				{
					//delete successful
					deletedNode.deleteCount++;
					if(deletedNode.isSleeping.get())
					{
						if((System.currentTimeMillis() - globalTime) >= deletedNode.sleepDuraion)
						{
							deletedNode.setSleeping(false);
							deletedNode = processTask(deletedNode);

							if(deletedNode.key * deletedNode.getPriority() <= deletedNode.maxRunTime.get())
							{
								deletedNode.reset();
								synchronized (rbTree)
								{
									rbTree.insert(deletedNode);
								}
							}
							else
							{
								long endTime = System.currentTimeMillis();

								System.out.println("Process " + deletedNode.getID() + 
										" with priority " + deletedNode.getPriority() + 
										" completed in " + (endTime - startTime) + " ms");

							}
						}
						else
						{
							deletedNode.maxRunTime.set(deletedNode.maxRunTime.get() + 100);
							deletedNode.runTime.set(deletedNode.runTime.get() + 100);
							deletedNode.reset();
							synchronized (rbTree)
							{
								rbTree.insert(deletedNode);
							}
						}
					}
					else
					{
						//give tasks to the processor
						deletedNode = processTask(deletedNode);

						if(deletedNode.key * deletedNode.getPriority() <= deletedNode.maxRunTime.get())
						{
							deletedNode.reset();
							synchronized (rbTree)
							{
								rbTree.insert(deletedNode);
							}
						}
						else
						{
							long endTime = System.currentTimeMillis();

							System.out.println("Process " + deletedNode.getID() + 
									" with priority " + deletedNode.getPriority() + 
									" completed in " + (endTime - startTime) + " ms");

						}
					}
				}
				else
				{
					return; 
				}
			}
		}
	}

	private Node processTask(Node x)
	{
		long startTime = 0;
		long endTime = 100;

		while((startTime < endTime) && (((x.key * x.getPriority()) + startTime) < x.maxRunTime.get()))
		{
			startTime++;
			x.DoMyTask();
		}
		x.key += Math.round((float)startTime/x.getPriority() +0.5);
		return x;
	}
}