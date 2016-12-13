package LockFreeContains;

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
}
