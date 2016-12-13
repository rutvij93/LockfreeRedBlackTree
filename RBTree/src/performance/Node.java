package performance;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node 
{
	public final static int RED = 0;
	public final static int BLACK = 1;
	public final static int NO_FLAG = 0;
	public final static int DUMMY_FLAG = 1;
	public final static int LOCAL_AREA = 2;

	AtomicInteger flag = new AtomicInteger(NO_FLAG);
	public int key = -1, color = BLACK;
	private int priority = 1;
	Node left, right, parent;
	AtomicInteger runTime, maxRunTime;

	Node(int key) 
	{
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
	
}