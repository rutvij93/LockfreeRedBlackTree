package cfs;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node 
{
	public final static int RED = 0;
	public final static int BLACK = 1;
	public final static int NO_FLAG = 0;
	public final static int DUMMY_FLAG = 1;
	public final static int LOCAL_AREA = 2;
	public int id = 0;
	public volatile AtomicBoolean isSleeping, priorityChanged;
	public long sleepDuraion = 0;
	public long sleepStarted = 0;
	public long deleteCount = 0, countOnPriorityChange = 0;

	AtomicInteger flag = new AtomicInteger(NO_FLAG);
	public int key = -1, color = BLACK;
	private int priority = 0, originalPriority = 0;
	Node left, right, parent;
	AtomicInteger runTime, maxRunTime;

	public Node(int key) 
	{
		isSleeping = new AtomicBoolean(false);
		priorityChanged = new AtomicBoolean(false);
		this.runTime = new AtomicInteger(0);
		this.maxRunTime = new AtomicInteger(10000);	//10 millisecond
		this.flag.set(NO_FLAG);
		this.key = key;
 		if (key != -1)
		{
			this.color = RED;
			this.left = new Node(-1);
			this.right = new Node(-1);
		}
		else
		{
			this.color = BLACK;
			this.left = null;
			this.right = null;
		}
	} 
	
	public void DoMyTask()
	{
		// write the task
//		System.out.println("printing " + this.key);
	}

	public void reset()
	{
		this.color = RED;
		this.flag.set(NO_FLAG);
		this.left = new Node(-1);
		this.right = new Node(-1);
		
		if(id%4 == 0)
		{	
			if(deleteCount == 10)
				isSleeping.set(true);

			if((deleteCount > (countOnPriorityChange + 10)))
			{
				this.priority = originalPriority;
			}
		}
	}
	
	public void setMaxRunTime(int time)
	{
		this.maxRunTime.set(time);
	}
	
	public void setPriority(int _priority)
	{
		this.priority = _priority;
	}
	
	public int getPriority()
	{
		return priority;
	}
	
	public void setID(int _id)
	{
		this.id = _id;
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public void setSleeping(boolean value)
	{
		if(!value)
		{
			originalPriority = priority;
			priorityChanged.set(true);
			countOnPriorityChange = deleteCount;
			priority = 5;
		}
		isSleeping.set(value);
	}
}