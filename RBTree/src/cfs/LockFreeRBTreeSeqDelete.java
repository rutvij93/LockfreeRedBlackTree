package cfs;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeRBTreeSeqDelete implements RBTree {
	public volatile Node root;
	public Node rootw, root1w;
	Node[] rootp = new Node[6];
	public static volatile AtomicInteger moveUp;
	ThreadLocal<ArrayList<Node>> myList;
	public static volatile long count = 0;

	public LockFreeRBTreeSeqDelete() {
		moveUp = new AtomicInteger(-1);
		myList = new ThreadLocal<ArrayList<Node>>() {
			protected ArrayList<Node> initialValue() {
				return new ArrayList<Node>();
			}
		};
		root = new Node(-1);
		rootw = new Node(-1); // Nil node is not to be modified at all
		root1w = new Node(-1);

		for (int i = 0; i < 6; i++) {
			rootp[i] = new Node(-2);
		}
		root.parent = rootp[0];
		rootw.parent = rootp[0];
		rootp[0].left = root;
		rootp[0].right = rootw;
		rootp[0].color = Node.BLACK;

		for (int i = 1; i < 6; i++) {
			rootp[i - 1].parent = rootp[i];
			rootp[i].left = rootp[i - 1];
			rootp[i].right = new Node(-1);
			rootp[i].color = Node.BLACK;
			rootp[i].flag.set(Node.NO_FLAG);
		}
		root1w.parent = rootp[1];
		rootp[1].right = root1w;
	}

	public void printRedBlackTree(Node root, int space) {
		if (root == null) {
			return;
		}
		printRedBlackTree(root.right, space + 5);
		for (int i = 0; i < space; i++) {
			System.out.print(" ");
		}
		System.out.println(root.key + " " + (root.color == Node.BLACK ? "B" : "R") + root.flag);
		if (root.key != -1)
			count++;
		printRedBlackTree(root.left, space + 5);
	}

	public void printME() {
		printRedBlackTree(root, 0);
	}

	public void print() {
		Node MARKER_NODE = new Node(-1);
		Node EMPTY_NODE = new Node(-1);
		MARKER_NODE.color = Node.BLACK;
		EMPTY_NODE.color = Node.BLACK;

		Queue<Node> q = new ArrayBlockingQueue<Node>(100);
		q.offer(rootp[5]);
		q.offer(MARKER_NODE);
		while (!q.isEmpty()) {
			Node curr = q.poll();
			if (curr == MARKER_NODE && !q.isEmpty()) {
				q.offer(MARKER_NODE);
				System.out.println("");
			} else {
				if (curr == EMPTY_NODE)
					System.out.print(" x ");
				else {
					if (curr != MARKER_NODE) {
						String print = "  " + curr.key + "->" + curr.flag.get();
						// String print = " " + curr.key + curr.flag + " ";
						if (curr.color == Node.RED)
							print = " [" + curr.key + "]->" + curr.flag.get();
						System.out.print(print);
					}
					if (curr.left == null)
						q.offer(EMPTY_NODE);
					else
						q.offer(curr.left);

					if (curr.right == null)
						q.offer(EMPTY_NODE);
					else
						q.offer(curr.right);
				}
			}
		}
	}

	@Override
	public void insert(Node x) {
		restart:
			while (true) {
				Node z = root.parent;
				Node y = root;

				while (y.key != -1) // Find insert point z
				{
					z = y;
					if (x.key < y.key)
						y = y.left;
					else
						y = y.right;
				} // end while

						if (!SetupLocalAreaForInsert(z)) {
							//release the flags and restart
							continue restart;
						}
						if (!(y == z.left || y == z.right)) {
							// if y is changed later
							while (myList.get().size() > 0)
								myList.get().remove(0).flag.set(Node.NO_FLAG);
							continue restart;
						}

						// Place new node x as child of z
						x.parent = z;
						x.flag.set(Node.LOCAL_AREA);
						myList.get().add(x);
						//			System.out.println("4 root : " + root.key);

						if (z == root.parent) {
							//setting the first node
							this.root = x;
							this.root.parent.left = x;
						} else if (x.key < z.key)
							z.left = x;
						else
							z.right = x;

						RB_Insert_Fixup(x);
						moveUp.compareAndSet((int) Thread.currentThread().getId(), -1);    //release the flag on moveUp
						break;
			}
	}

	private boolean SetupLocalAreaForInsert(Node z) {
		if (!(z.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)))
			return false;
		myList.get().add(z);

		Node zp = z.parent; // take a copy of our parent pointer
		if (!(zp.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			return false;
		}
		myList.get().add(zp);

		if (zp != z.parent) // parent has changed - abort
		{
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);
			return false;
		}

		Node zpp = zp.parent;
		if (!(zpp.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);

			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);

			return false;
		}
		myList.get().add(zpp);

		if (zpp != zp.parent) // parent has changed - abort
		{
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);
			zpp.flag.set(Node.NO_FLAG);
			myList.get().remove(zpp);
			return false;
		}

		Node uncle;

		if (z == z.parent.left) // uncle is the right child
			uncle = z.parent.right;
		else // uncle is the left child
			uncle = z.parent.left;
		try {
			if (uncle != null) {
				if (!(uncle.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
					z.flag.set(Node.NO_FLAG);
					myList.get().remove(z);
					zp.flag.set(Node.NO_FLAG);
					myList.get().remove(zp);
					zpp.flag.set(Node.NO_FLAG);
					myList.get().remove(zpp);
					return false;
				}
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

		myList.get().add(uncle);

		if (!(uncle == zp.left || uncle == zp.right)) {
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);
			zpp.flag.set(Node.NO_FLAG);
			myList.get().remove(zpp);
			uncle.flag.set(Node.NO_FLAG);
			myList.get().remove(uncle);
			return false;
		}
		//		System.out.println("2 root : " + root.key);
		return true;
	}

	public void RB_Insert_Fixup(Node x) {
		Node y;
		while (x.parent.color == Node.RED) {
			if (x.parent == x.parent.parent.left) {
				y = x.parent.parent.right;
				if (y.color == Node.RED)    //case 1
				{
					x.parent.color = Node.BLACK;
					y.color = Node.BLACK;
					x.parent.parent.color = Node.RED;
					if (x.parent.parent.parent.color == Node.RED)
						x = moveInserterUp(x);
					else
						break;
				} else {
					if (x == x.parent.right)        //case 2
					{
						x = x.parent;
						rotateLeft(x);
					}
					x.parent.color = Node.BLACK;    //case 3
					x.parent.parent.color = Node.RED;
					rotateRight(x.parent.parent);
				}
			} else {
				if (x.parent == x.parent.parent.right) {
					y = x.parent.parent.left;
					if (y.color == Node.RED) {
						x.parent.color = Node.BLACK;
						y.color = Node.BLACK;
						x.parent.parent.color = Node.RED;
						if (x.parent.parent.parent.color == Node.RED)
							x = moveInserterUp(x);
						else
							break;
					} else {
						if (x == x.parent.left) {
							x = x.parent;
							rotateRight(x);
						}
						x.parent.color = Node.BLACK;
						x.parent.parent.color = Node.RED;
						rotateLeft(x.parent.parent);
					}
				}
			}
		}
		// Fix up complete. Release all the acquired flags

		while (myList.get().size() > 0)
			myList.get().remove(0).flag.set(Node.NO_FLAG);

		root.color = Node.BLACK;
	}

	private Node moveInserterUp(Node oldx) {
		// check for a moveUpStruct from another process (due to move-up rule
		// Get direct pointers
		restart:
			while (true) {
				Node oldp = oldx.parent;
				Node oldgp = oldp.parent;
				Node oldDummy = oldgp.parent;
				Node olduncle;
				Node newUncle;

				if (oldp == oldgp.left)
					olduncle = oldgp.right;
				else
					olduncle = oldgp.left;

				// try to acquire flag on upper node

				Node newDummy = oldDummy.parent;

				if (!newDummy.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)) {
					if (moveUp.get() == (int) Thread.currentThread().getId()) {
						moveUp.set(-1);
					}
					continue restart;
				}    //spin to get first new move up node
				myList.get().add(newDummy);

				if (newDummy != oldDummy.parent) {
					//release flag of newDummy

					newDummy.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummy);
					continue restart;
				}


				Node newDummyParent = new Node(-1);
				newDummyParent = newDummy.parent;
				if (!newDummyParent.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))    //spin to get second new move up node
				{
					if (moveUp.get() == (int) Thread.currentThread().getId()) {
						moveUp.set(-1);
					}
					newDummy.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummy);
					continue restart;
				}

				myList.get().add(newDummyParent);

				if (newDummyParent != newDummy.parent) {
					//release flag of newDummy

					newDummy.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummy);

					newDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummyParent);
					continue restart;
				}


				if (oldDummy == oldDummy.parent.left)
					newUncle = oldDummy.parent.right;
				else
					newUncle = oldDummy.parent.left;

				//check if newUncle is not someone else's local area

				if (newUncle.flag.get() == Node.LOCAL_AREA) {
					//release the flag of newDummy
					newDummy.parent.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummy.parent);

					newDummy.flag.set(Node.NO_FLAG);
					myList.get().remove(newDummy);
					continue restart;
				}

				//Get the global moveUpFlag

				if (moveUp.get() != (int) Thread.currentThread().getId()) {
					if (!moveUp.compareAndSet(-1, (int) Thread.currentThread().getId())) {
						//release the flag of newDummy
						newDummy.parent.flag.set(Node.NO_FLAG);
						myList.get().remove(newDummy.parent);

						newDummy.flag.set(Node.NO_FLAG);
						myList.get().remove(newDummy);
						continue restart;
					}
				}
				// change the flag of oldDummy to Local Area
				oldDummy.flag.set(Node.LOCAL_AREA);
				myList.get().add(oldDummy);

				// Release the flags on x and its uncle
				oldx.flag.set(Node.NO_FLAG);
				myList.get().remove(oldx);

				oldp.flag.set(Node.NO_FLAG);
				myList.get().remove(oldp);

				olduncle.flag.compareAndSet(Node.LOCAL_AREA, Node.NO_FLAG);
				myList.get().remove(olduncle);

				return oldgp;
			}
	}

	// From Delete to Apply Move Up rule

	void rotateLeft(Node node) {
		if (node.parent.key != -2) {
			if (node == node.parent.left) {
				node.parent.left = node.right;
			} else {
				node.parent.right = node.right;
			}
			node.right.parent = node.parent;
			node.parent = node.right;

			if (node.right.left.key != -1) {
				node.right.left.parent = node;
			}
			node.right = node.right.left;
			node.parent.left = node;
		} else {// Need to rotate root
			Node right = root.right;
			root.right = right.left;
			right.left.parent = root;
			root.parent = right;
			right.left = root;
			right.parent = new Node(-1);
			root = right;
			root.parent = rootp[0];
			rootp[0].left = root;
		}
	}

	void rotateRight(Node node) {
		if (node.parent.key != -2) {
			if (node == node.parent.left) {
				node.parent.left = node.left;
			} else {
				node.parent.right = node.left;
			}

			node.left.parent = node.parent;
			node.parent = node.left;

			if (node.left.right.key != -1) {
				node.left.right.parent = node;
			}
			node.left = node.left.right;
			node.parent.right = node;

		} else {// Need to rotate root
			Node left = root.left;
			root.left = root.left.right;
			if (left.right != null)
				left.right.parent = root;    //need to fix this
			root.parent = left;
			left.right = root;
			left.parent = new Node(-1);
			root = left;
			root.parent = rootp[0];
			rootp[0].left = root;
		}
	}

	void transplant(Node target, Node with)
	{
		if(target.parent.key == -2)
		{
			root = with;
			rootp[0].left = root;
			root.parent = rootp[0];
		}
		else if(target == target.parent.left)
		{
			target.parent.left = with;
		}
		else
		{
			target.parent.right = with;
		}
		with.parent = target.parent;
	}

	private Node findNode(Node findNode, Node node)
	{
		if (root.key == -1)
		{
			return null;
		}
		if (findNode.key < node.key)
		{
			if (node.left.key != -1)
			{
				return findNode(findNode, node.left);
			}
		}
		else if (findNode.key > node.key)
		{
			if (node.right.key != -1)
			{
				return findNode(findNode, node.right);
			}
		}
		else if (findNode.key == node.key)
		{
			return node;
		}
		return null;
	}

	public Node delete(Node z)
	{
		if((z = findNode(z, root))==null)return null;
		Node x;
		Node y = z; // temporary reference y
		int y_original_color = y.color;

		if(z.left.key == -1)
		{
			x = z.right;
			transplant(z, z.right);
		}
		else if(z.right.key == -1)
		{
			x = z.left;
			transplant(z, z.left);
		}
		else
		{
			y = treeMinimum(z.right);
			y_original_color = y.color;
			x = y.right;
			if(y.parent == z)
				x.parent = y;
			else
			{
				transplant(y, y.right);
				y.right = z.right;
				y.right.parent = y;
			}
			transplant(z, y);
			y.left = z.left;
			y.left.parent = y;
			y.color = z.color;
		}
		if(y_original_color == Node.BLACK)
			deleteFixup(x);
		return x;
	}


	public Node delete()
	{
		if(root.key == -1)
			return root;

		Node z = root;
		while(z.left.key != -1)
			z = z.left;

		Node x;
		Node y = z; // temporary reference y
		int y_original_color = y.color;

		if(z.left.key == -1)
		{
			x = z.right;
			transplant(z, z.right);
		}
		else if(z.right.key == -1)
		{
			x = z.left;
			transplant(z, z.left);
		}
		else
		{
			y = treeMinimum(z.right);
			y_original_color = y.color;
			x = y.right;
			if(y.parent == z)
				x.parent = y;
			else
			{
				transplant(y, y.right);
				y.right = z.right;
				y.right.parent = y;
			}
			transplant(z, y);
			y.left = z.left;
			y.left.parent = y;
			y.color = z.color;
		}
		if(y_original_color == Node.BLACK)
			deleteFixup(x);
		return z;
	}

	void deleteFixup(Node x)
	{
		while(x!=root && x.color == Node.BLACK)
		{
			if(x == x.parent.left)
			{
				Node w = x.parent.right;

				if(w.color == Node.RED)
				{
					w.color = Node.BLACK;
					x.parent.color = Node.RED;
					rotateLeft(x.parent);
					w = x.parent.right;
				}
				try{
					if(w.left.color == Node.BLACK && w.right.color == Node.BLACK)
					{
						w.color = Node.RED;
						x = x.parent;
						continue;
					}
					else if(w.right.color == Node.BLACK)
					{
						w.left.color = Node.BLACK;
						w.color = Node.RED;
						rotateRight(w);
						w = x.parent.right;
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				if(w.right.color == Node.RED)
				{
					w.color = x.parent.color;
					x.parent.color = Node.BLACK;
					w.right.color = Node.BLACK;
					rotateLeft(x.parent);
					x = root;
				}
			}
			else
			{
				Node w = x.parent.left;
				if(w.color == Node.RED)
				{
					w.color = Node.BLACK;
					x.parent.color = Node.RED;
					rotateRight(x.parent);
					w = x.parent.left;
				}

				if(w.right.color == Node.BLACK && w.left.color == Node.BLACK)
				{
					w.color = Node.RED;
					x = x.parent;
					continue;
				}
				else if(w.left.color == Node.BLACK)
				{
					w.right.color = Node.BLACK;
					w.color = Node.RED;
					rotateLeft(w);
					w = x.parent.left;
				}

				if(w.left.color == Node.RED)
				{
					w.color = x.parent.color;
					x.parent.color = Node.BLACK;
					w.left.color = Node.BLACK;
					rotateRight(x.parent);
					x = root;
				}
			}
		}
		x.color = Node.BLACK;
	}

	Node treeMinimum(Node subTreeRoot){
		while(subTreeRoot.left.key !=-1 ){
			subTreeRoot = subTreeRoot.left;
		}
		return subTreeRoot;
	}
}
