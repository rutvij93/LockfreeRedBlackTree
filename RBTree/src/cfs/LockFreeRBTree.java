package cfs;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeRBTree implements RBTree {
	public static volatile Node root;
	public Node rootw, root1w;
	Node[] rootp = new Node[6];
	public static volatile AtomicInteger moveUp;
	ThreadLocal<ArrayList<Node>> myList;
	public static volatile long count = 0;

	public LockFreeRBTree() {
		LockFreeRBTree.moveUp = new AtomicInteger(-1);
		myList = new ThreadLocal<ArrayList<Node>>() {
			protected ArrayList<Node> initialValue() {
				return new ArrayList<Node>();
			}
		};
		LockFreeRBTree.root = new Node(-1);
		this.rootw = new Node(-2); // Nil node is not to be modified at all
		this.root1w = new Node(-2);
		for (int i = 0; i < 6; i++) {
			this.rootp[i] = new Node(-2);
		}
		LockFreeRBTree.root.parent = rootp[0];
		this.rootw.parent = rootp[0];
		this.rootp[0].left = root;
		this.rootp[0].right = rootw;
		this.rootp[0].color = Node.BLACK;
		for (int i = 1; i < 6; i++) {
			this.rootp[i - 1].parent = rootp[i];
			this.rootp[i].left = rootp[i - 1];
			this.rootp[i].right = new Node(-1);
			// My change start
			this.rootp[i].right.left = new Node(-1);
			this.rootp[i].right.right = new Node(-1);
			// My change end
			this.rootp[i].color = Node.BLACK;
			this.rootp[i].flag.set(Node.NO_FLAG);
		}
		this.root1w.parent = rootp[1];
		this.rootp[1].right = root1w;
	}

	public void printREDBLACKTree(Node root, int space) {
		if (root == null) {
			return;
		}
		printREDBLACKTree(root.right, space + 5);
		for (int i = 0; i < space; i++) {
			System.out.print(" ");
		}
		System.out.println(root.key + " " + (root.color == Node.BLACK ? "B" : "R") + root.flag);
		if (root.key != -1)
			count++;
		printREDBLACKTree(root.left, space + 5);
	}

	public void printME() {
		printREDBLACKTree(root, 0);
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
//				System.out.println("");
			} else {
				if (curr == EMPTY_NODE){
					
//					System.out.print(" x ");
				}	
				else {
					if (curr != MARKER_NODE) {
						String print = "  " + curr.key + "->" + curr.flag.get();
						// String print = " " + curr.key + curr.flag + " ";
						if (curr.color == Node.RED)
							print = " [" + curr.key + "]->" + curr.flag.get();
//						System.out.print(print);
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
		restart: while (true) {
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
				continue restart;
			}
			if (!(y == z.left || y == z.right)) {
				while (myList.get().size() > 0)
					myList.get().remove(0).flag.set(Node.NO_FLAG);
				continue restart;
			}
			x.parent = z;
			x.flag.set(Node.LOCAL_AREA);
			myList.get().add(x);
			if (z == root.parent) {
				LockFreeRBTree.root = x;
				LockFreeRBTree.root.parent.left = x;
			} else if (x.key < z.key)
				z.left = x;
			else
				z.right = x;
			RB_Insert_Fixup(x);
			break;
		}
	while (myList.get().size() > 0)
		myList.get().remove(0).flag.set(Node.NO_FLAG);
	moveUp.compareAndSet((int) Thread.currentThread().getId(), -1); 
	}

	private boolean SetupLocalAreaForInsert(Node z) {
		try {
			if (!(z.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		Node zppp = zpp.parent;
		if (!(zppp.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);
			zpp.flag.set(Node.NO_FLAG);
			myList.get().remove(zpp);
			return false;
		}
		myList.get().add(zppp);
		if (zppp != zpp.parent) // parent has changed - abort
		{
			z.flag.set(Node.NO_FLAG);
			myList.get().remove(z);
			zp.flag.set(Node.NO_FLAG);
			myList.get().remove(zp);
			zpp.flag.set(Node.NO_FLAG);
			myList.get().remove(zpp);
			zppp.flag.set(Node.NO_FLAG);
			myList.get().remove(zppp);
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
					zppp.flag.set(Node.NO_FLAG);
					myList.get().remove(zppp);
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
			zppp.flag.set(Node.NO_FLAG);
			myList.get().remove(zppp);
			uncle.flag.set(Node.NO_FLAG);
			myList.get().remove(uncle);
			return false;
		}
		// System.out.println("2 root : " + root.key);
		return true;
	}

	public void RB_Insert_Fixup(Node x) {
		Node y;
		while (x.parent.color == Node.RED) {
			if (x.parent == x.parent.parent.left) {
				y = x.parent.parent.right;
				if (y.color == Node.RED) // case 1
				{
					x.parent.color = Node.BLACK;
					y.color = Node.BLACK;
					x.parent.parent.color = Node.RED;
					if (x.parent.parent.parent.color == Node.RED)
						x = moveInserterUp(x);
					else
						break;
				} else {
					if (x == x.parent.right) // case 2
					{
						x = x.parent;
						rotateLeft(x);
					}
					x.parent.color = Node.BLACK; // case 3
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
		// Fix up complete. Release all the acquiNode.RED flags
		while (!myList.get().isEmpty())
			myList.get().remove(0).flag.set(Node.NO_FLAG);
		LockFreeRBTree.root.color = Node.BLACK;
	}

	private Node moveInserterUp(Node oldx) {
		restart: while (true) {
			Node oldp = oldx.parent;
			Node oldgp = oldp.parent;
			Node oldDummy = oldgp.parent;
			Node oldDummyParent = oldDummy.parent;
			Node olduncle;
			Node newUncle;
			if (oldp == oldgp.left)
				olduncle = oldgp.right;
			else
				olduncle = oldgp.left;
			if (moveUp.get() != (int) Thread.currentThread().getId()) {
				if (!moveUp.compareAndSet(-1, (int) Thread.currentThread().getId())) {
					// release the flag of newDummy
					continue restart;
				}
			}
			// try to acquire flag on upper node
			if ((oldDummyParent.flag.get() != Node.DUMMY_FLAG)) {
				if (!(oldDummyParent.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
					if (moveUp.get() == (int) Thread.currentThread().getId()) {
						moveUp.set(-1);
					}
					continue restart;
				}
			}
			myList.get().add(oldDummyParent);
			Node newDummy = oldDummyParent.parent;
			if (!newDummy.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG)) {
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				continue restart;
			} // spin to get first new move up node
			myList.get().add(newDummy);
			// @@@
			if (oldDummyParent != oldDummy.parent) {
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				continue restart;
			}
			if (newDummy != oldDummyParent.parent) {
				// release flag of newDummy
				// we dont need to release flags on oldDummyParent and oldDummy
				// here.
				// Because flag on newDummy has made sure that the link is not
				// broken
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				continue restart;
			}
			Node newDummyParent = new Node(-1);
			newDummyParent = newDummy.parent;
			if (!newDummyParent.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))
			{
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				continue restart;
			}
			myList.get().add(newDummyParent);
			if (newDummy != oldDummyParent.parent) {
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				newDummyParent.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummyParent);
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				continue restart;
			}
			if (newDummyParent != newDummy.parent) {
				// release flag of newDummy
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				newDummyParent.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummyParent);
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				continue restart;
			}
			if (oldDummy == oldDummy.parent.left)
				newUncle = oldDummy.parent.right;
			else
				newUncle = oldDummy.parent.left;
			// check if newUncle is not someone else's local area
			if (newUncle.flag.get() == Node.LOCAL_AREA) {
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				{
					oldDummyParent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyParent);
				}
				// release the flag of newDummy
				newDummyParent.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummyParent);
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				continue restart;
			}
			// Get the global moveUpFlag
			// change the flag of oldDummy to Local Area
			oldDummy.flag.compareAndSet(Node.DUMMY_FLAG, Node.LOCAL_AREA);
			myList.get().add(oldDummy);
			oldDummyParent.flag.compareAndSet(Node.DUMMY_FLAG, Node.LOCAL_AREA);
			myList.get().add(oldDummyParent);
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
			LockFreeRBTree.root.right = right.left;
			right.left.parent = root;
			LockFreeRBTree.root.parent = right;
			right.left = root;
			right.parent = new Node(-1);
			LockFreeRBTree.root = right;
			LockFreeRBTree.root.parent = rootp[0];
			this.rootp[0].left = root;
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
			LockFreeRBTree.root.left = root.left.right;
			if (left.right != null)
				left.right.parent = root; // need to fix this
			LockFreeRBTree.root.parent = left;
			left.right = root;
			left.parent = new Node(-1);
			LockFreeRBTree.root = left;
			LockFreeRBTree.root.parent = rootp[0];
			this.rootp[0].left = root;
		}
	}

	public Node delete() 
	{
		restart: while(true){
			Node y = null;
			Node z = root;

			if (z.key == -1)
			{
				return z;
			}

			try {
				while (z.left.key != -1)
					z = z.left;	//find leftmost node
			} catch (Exception e) {
				continue restart;
			}

			if(!(z.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)))
			{
				continue restart;
			}
			myList.get().add(z);

			if (z.left.key == -1 || z.right.key == -1)
				y = z;
			else
				y = FindSuccessor(z); // key-order successor

			if(y != z )
			{	
				if(!(y.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)))
				{
					z.flag.set(Node.NO_FLAG);
					myList.get().remove(z);
					continue restart;
				}
			}
			myList.get().add(z);

			if (!SetupLocalAreaForDelete(y, z)) // release flags
			{
				z.flag.set(Node.NO_FLAG);
				myList.get().remove(z);

				if (y != z)
				{
					y.flag.set(Node.NO_FLAG);
					myList.get().remove(y);
				}
				continue restart;
			}

			Node x = new Node(-1);

			if (y.left.key != -1)
				x = y.left;
			else
				x = y.right;

			// unlink y from tree
			x.parent = y.parent;


			if (y.parent == root.parent){
				root = x;
				rootp[0].left = x;
				root.parent = rootp[0];
			}
			else 
			{
				if (y == y.parent.left)
					y.parent.left = x;
				else
					y.parent.right = x;
			} // end else

			if (y != z)
			{
				z.key = y.key;
				z.flag.set(Node.NO_FLAG);
				myList.get().remove(z);
			}

			if (y.color == Node.BLACK)
			{	
				RB_Delete_Fixup(x);
			}

			while (!myList.get().isEmpty())
				myList.get().remove(0).flag.set(Node.NO_FLAG);
			moveUp.compareAndSet((int) Thread.currentThread().getId(), -1);
			return z;
		}
	}

	void transplant(Node target, Node with) {
		if (target.parent.key == -2) {
			LockFreeRBTree.root = with;
			this.rootp[0].left = root;
			LockFreeRBTree.root.parent = rootp[0];
		} else if (target == target.parent.left) {
			target.parent.left = with;
		} else {
			target.parent.right = with;
		}
		with.parent = target.parent;
	}

	Node treeMinimum(Node subTreeRoot) {
		while (subTreeRoot.left.key != -1) {
			subTreeRoot = subTreeRoot.left;
		}
		return subTreeRoot;
	}

	Node FindSuccessor(Node subTreeRoot) {
		if (subTreeRoot.right.key != -1) {
			subTreeRoot = subTreeRoot.right;
			while (subTreeRoot.left.key != -1) {
				subTreeRoot = subTreeRoot.left;
			}
			return subTreeRoot;
		} else if (subTreeRoot.left.key != -1) {
			subTreeRoot = subTreeRoot.left;
			while (subTreeRoot.right.key != -1) {
				subTreeRoot = subTreeRoot.right;
			}
			return subTreeRoot;
		}
		return subTreeRoot;
	}

	private boolean SetupLocalAreaForDelete(Node y, Node z) {
		Node x = new Node(-1), w = new Node(-1), wlc = new Node(-1), wrc = new Node(-1);
		try {
			if (y.left.key != -1)
				x = y.left;
			else
				x = y.right;
		} catch (Exception e) {
			return false;
		}
		// try to get flags for the rest of the local area
		if (!(x.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA)))
			return false;
		myList.get().add(x);
		Node yp = y.parent; // keep a copy of our parent pointer
		if ((yp != z) && (!yp.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			return false;
		}
		myList.get().add(yp);
		if (yp != y.parent) // verify that parent is unchanged
		{
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			if (yp != z) {
				yp.flag.set(Node.NO_FLAG);
				myList.get().remove(yp);
			}
			return false;
		}
		if (y == y.parent.left)
			w = y.parent.right;
		else
			w = y.parent.left;
		if (w != null){
			if (!(w.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
				x.flag.set(Node.NO_FLAG);
				myList.get().remove(x);
				if (yp != z) {
					yp.flag.set(Node.NO_FLAG);
					myList.get().remove(yp);
				}
				return false;
			}
			myList.get().add(w);
			if (!(yp.left == w || yp.right == w)) // check if w is not sibling
			{
				x.flag.set(Node.NO_FLAG);
				myList.get().remove(x);
				w.flag.set(Node.NO_FLAG);
				myList.get().remove(w);
				if (yp != z) {
					yp.flag.set(Node.NO_FLAG);
					myList.get().remove(yp);
				}
				return false;
			}
		}
		Node ypp = yp.parent;
		if (!(ypp.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			w.flag.set(Node.NO_FLAG);
			myList.get().remove(w);
			if (yp != z) {
				yp.flag.set(Node.NO_FLAG);
				myList.get().remove(yp);
			}
			return false;
		}
		myList.get().add(ypp);
		if (ypp != yp.parent) // parent has changed - abort
		{
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			w.flag.set(Node.NO_FLAG);
			myList.get().remove(w);
			ypp.flag.set(Node.NO_FLAG);
			myList.get().remove(ypp);
			if (yp != z) {
				yp.flag.set(Node.NO_FLAG);
				myList.get().remove(yp);
			}
			return false;
		}
		Node yppp = ypp.parent;
		if (!(yppp.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			w.flag.set(Node.NO_FLAG);
			myList.get().remove(w);
			if (yp != z) {
				yp.flag.set(Node.NO_FLAG);
				myList.get().remove(yp);
			}
			ypp.flag.set(Node.NO_FLAG);
			myList.get().remove(ypp);
			return false;
		}
		myList.get().add(yppp);
		if (yppp != ypp.parent) // parent has changed - abort
		{
			x.flag.set(Node.NO_FLAG);
			myList.get().remove(x);
			w.flag.set(Node.NO_FLAG);
			myList.get().remove(w);
			if (yp != z) {
				yp.flag.set(Node.NO_FLAG);
				myList.get().remove(yp);
			}
			ypp.flag.set(Node.NO_FLAG);
			myList.get().remove(ypp);
			yppp.flag.set(Node.NO_FLAG);
			myList.get().remove(yppp);
			return false;
		}
		if (w.key != -1) {
			wlc = w.left;
			wrc = w.right;
			if (!(wlc.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
				x.flag.set(Node.NO_FLAG);
				myList.get().remove(x);
				w.flag.set(Node.NO_FLAG);
				myList.get().remove(w);
				if (yp != z) {
					yp.flag.set(Node.NO_FLAG);
					myList.get().remove(yp);
				}
				ypp.flag.set(Node.NO_FLAG);
				myList.get().remove(ypp);
				yppp.flag.set(Node.NO_FLAG);
				myList.get().remove(yppp);
				return false;
			}
			myList.get().add(wlc);
			if (!(wrc.flag.compareAndSet(Node.NO_FLAG, Node.LOCAL_AREA))) {
				x.flag.set(Node.NO_FLAG);
				myList.get().remove(x);
				w.flag.set(Node.NO_FLAG);
				myList.get().remove(w);
				wlc.flag.set(Node.NO_FLAG);
				myList.get().remove(wlc);
				if (yp != z) {
					yp.flag.set(Node.NO_FLAG);
					myList.get().remove(yp);
				}
				ypp.flag.set(Node.NO_FLAG);
				myList.get().remove(ypp);
				yppp.flag.set(Node.NO_FLAG);
				myList.get().remove(yppp);
				return false;
			}
			myList.get().add(wrc);
			if (wlc != w.left || wrc != w.right) {
				x.flag.set(Node.NO_FLAG);
				myList.get().remove(x);
				w.flag.set(Node.NO_FLAG);
				myList.get().remove(w);
				wlc.flag.set(Node.NO_FLAG);
				myList.get().remove(wlc);
				wrc.flag.set(Node.NO_FLAG);
				myList.get().remove(wrc);
				if (yp != z) {
					yp.flag.set(Node.NO_FLAG);
					myList.get().remove(yp);
				}
				ypp.flag.set(Node.NO_FLAG);
				myList.get().remove(ypp);
				yppp.flag.set(Node.NO_FLAG);
				myList.get().remove(yppp);
				return false;
			}
		}
		return true;
	}

	private void RB_Delete_Fixup(Node x) {
		Node w = new Node(-1);
		while (x != root && x.color == Node.BLACK) {
			if (x == x.parent.left) {
				w = x.parent.right;
				if (w.color == Node.RED) {
					w.color = Node.BLACK;
					x.parent.color = Node.RED;
					rotateLeft(x.parent);
					w = x.parent.right;
				}
				try {
					if (w.left.color == Node.BLACK && w.right.color == Node.BLACK) {
						w.color = Node.RED;
						x = MoveDeleterUp(x);
					} else {
						if (w.right.color == Node.BLACK) {
							w.left.color = Node.BLACK;
							w.color = Node.RED;
							rotateRight(w);
							w = x.parent.right;
						} // end if
						w.color = x.parent.color;
						x.parent.color = Node.BLACK;
						w.right.color = Node.BLACK;
						rotateLeft(x.parent);
					} // end else
				} catch (Exception e) {
					return;
				}
			} else {
				w = x.parent.left;
				if (w.color == Node.RED) {
					w.color = Node.BLACK;
					x.parent.color = Node.RED;
					rotateRight(x.parent);
					w = x.parent.left;
					// Deleted fixupcase1
				}
				try {
					if (w.right.color == Node.BLACK && w.left.color == Node.BLACK) {
						w.color = Node.RED;
						x = MoveDeleterUp(x);
					} else {
						if (w.left.color == Node.BLACK) {
							w.right.color = Node.BLACK;
							w.color = Node.RED;
							rotateLeft(w);
							// Deleted fixupcase3
							w = x.parent.left;
						} // end if
						w.color = x.parent.color;
						x.parent.color = Node.BLACK;
						w.left.color = Node.BLACK;
						rotateRight(x.parent);
					}
				} catch (Exception e) {
					return;
				}
			} // end if
		} // end while
		x.color = Node.BLACK;
	}

	private Node MoveDeleterUp(Node oldx) {
		restart: while (true) {
			Node oldp = oldx.parent;
			Node oldDummy = oldp.parent;
			Node oldDummyparent = oldDummy.parent;
			Node newDummy = oldDummyparent.parent;
			if (moveUp.get() != (int) Thread.currentThread().getId()) {
				if (!moveUp.compareAndSet(-1, (int) Thread.currentThread().getId())) {
					continue restart;
				}
			}
			oldDummy.flag.set(Node.DUMMY_FLAG);
			myList.get().add(oldDummy);
			// try to acquire flag on upper node
			if ((oldDummyparent.flag.get() != Node.DUMMY_FLAG)) {
				if (!(oldDummyparent.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
					if (moveUp.get() == (int) Thread.currentThread().getId()) {
						moveUp.set(-1);
					}
					continue restart;
				}
				myList.get().add(oldDummyparent);
			}
			if (oldDummy.parent != oldDummyparent) {
				oldDummyparent.flag.set(Node.NO_FLAG);
				myList.get().remove(oldDummyparent);
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				continue restart;
			}
			if (newDummy.flag.get() != Node.DUMMY_FLAG) {
				if (!(newDummy.flag.compareAndSet(Node.NO_FLAG, Node.DUMMY_FLAG))) {
					oldDummyparent.flag.set(Node.NO_FLAG);
					myList.get().remove(oldDummyparent);
					if (moveUp.get() == (int) Thread.currentThread().getId()) {
						moveUp.set(-1);
					}
					continue restart;
				}
			}
			myList.get().add(newDummy);
			if (oldDummy.parent != oldDummyparent) {
				oldDummyparent.flag.set(Node.NO_FLAG);
				myList.get().remove(oldDummyparent);
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				continue restart;
			}
			if (oldDummyparent.parent != newDummy) {
				oldDummyparent.flag.set(Node.NO_FLAG);
				myList.get().remove(oldDummyparent);
				newDummy.flag.set(Node.NO_FLAG);
				myList.get().remove(newDummy);
				if (moveUp.get() == (int) Thread.currentThread().getId()) {
					moveUp.set(-1);
				}
				continue restart;
			}
			// change the flag of oldDummy to Local Area
			oldDummy.flag.compareAndSet(Node.DUMMY_FLAG, Node.LOCAL_AREA);
			myList.get().add(oldDummy);
			// Release the flags on x and its uncle
			oldx.flag.set(Node.NO_FLAG);
			myList.get().remove(oldx);
			Node w = new Node(-1);
			if (oldx.parent.left == oldx)
				w = oldx.parent.right;
			else
				w = oldx.parent.left;
			if (w.key != -1) {
				if (w.flag.compareAndSet(Node.LOCAL_AREA, Node.NO_FLAG))
					myList.get().remove(w);
				if (w.left.flag.compareAndSet(Node.LOCAL_AREA, Node.NO_FLAG))
					myList.get().remove(w.left);
				if (w.right.flag.compareAndSet(Node.LOCAL_AREA, Node.NO_FLAG))
					myList.get().remove(w.right);
			}
			return oldp;
		}
	}
}