package cfs;
import java.util.Scanner;
public class SeqRBTree implements RBTree
{
	@Override
	public void print(){}
	private final int RED = 0;
	private final int BLACK = 1;
	
	private Node root = new Node(-1);

	public SeqRBTree()
	{
	}
	public void printME()
	{
		
	}
	public void printTree(Node node) 
	{
		if (node.key == -1) 
		{
			return;
		}

		System.out.print(((node.color==RED)?"Color: Red ":"Color: Black ")+"Key: "+node.key+" Parent: "+node.parent.key+"\n");
		printTree(node.left);

		printTree(node.right);
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

	@Override
	public void insert(Node node) 
	{
		Node temp = root;
		if (root.key == -1) 
		{
			root = node;
			node.color = BLACK;
			node.parent = new Node(-1);
		} 
		else
		{
			node.color = RED;
			while (true) 
			{
				if (node.key < temp.key) 
				{
					if (temp.left.key == -1) 
					{
						temp.left = node;
						node.parent = temp;
						break;
					}
					else
					{
						temp = temp.left;
					}
				}
				else if (node.key >= temp.key)
				{
					if (temp.right.key == -1)
					{
						temp.right = node;
						node.parent = temp;
						break;
					}
					else
					{
						temp = temp.right;
					}
				}
			}
			fixTree(node);
		}
	}

	//Takes as argument the newly inserted node
	private void fixTree(Node node) 
	{
		while (node.parent.color == RED) 
		{
			Node uncle = new Node(-1);
			if (node.parent == node.parent.parent.left)
			{
				uncle = node.parent.parent.right;
				if (uncle.key != -1 && uncle.color == RED) //case1 violation
				{
					node.parent.color = BLACK;
					uncle.color = BLACK;
					node.parent.parent.color = RED;
					node = node.parent.parent;
					continue;
				}

				if (node == node.parent.right) 
				{
					//Double rotation needed
					node = node.parent;
					rotateLeft(node);
				} 
				node.parent.color = BLACK;
				node.parent.parent.color = RED;
				//if the "else if" code hasn't executed, this
				//is a case where we only need a single rotation 
				rotateRight(node.parent.parent);
			} 
			else 
			{
				uncle = node.parent.parent.left;
				if (uncle.key != -1 && uncle.color == RED)	//case 1 violation
				{
					node.parent.color = BLACK;
					uncle.color = BLACK;
					node.parent.parent.color = RED;
					node = node.parent.parent;
					continue;
				}

				if (node == node.parent.left)
				{
					//Double rotation needed
					node = node.parent;
					rotateRight(node);
				}
				node.parent.color = BLACK;
				node.parent.parent.color = RED;
				//if the "else if" code hasn't executed, this
				//is a case where we only need a single rotation
				rotateLeft(node.parent.parent);
			}
		}
		root.color = BLACK;
	}

	void rotateLeft(Node node)
	{
		if (node.parent.key != -1)
		{
			if (node == node.parent.left)
			{
				node.parent.left = node.right;
			}
			else
			{
				node.parent.right = node.right;
			}
			node.right.parent = node.parent;
			node.parent = node.right;

			if (node.right.left.key != -1)
			{
				node.right.left.parent = node;
			}
			node.right = node.right.left;
			node.parent.left = node;
		} 
		else
		{//Need to rotate root
			Node right = root.right;
			root.right = right.left;
			right.left.parent = root;
			root.parent = right;
			right.left = root;
			right.parent = new Node(-1);
			root = right;
		}
	}

	void rotateRight(Node node) 
	{
		if (node.parent.key != -1)
		{
			if (node == node.parent.left)
			{
				node.parent.left = node.left;
			}
			else
			{
				node.parent.right = node.left;
			}

			node.left.parent = node.parent;
			node.parent = node.left;

			if (node.left.right.key != -1)
			{
				node.left.right.parent = node;
			}
			node.left = node.left.right;
			node.parent.right = node;

		} 
		else
		{//Need to rotate root
			Node left = root.left;
			root.left = root.left.right;
			left.right.parent = root;
			root.parent = left;
			left.right = root;
			left.parent = new Node(-1);
			root = left;
		}
	}

	//Deletes whole tree
	void deleteTree()
	{
		root = new Node(-1);
	}

	//Deletion Code .

	//This operation doesn't care about the new Node's connections
	//with previous node's left and right. The caller has to take care
	//of that.
	void transplant(Node target, Node with)
	{ 
		if(target.parent.key == -1)
		{
			root = with;
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
		if(y_original_color==BLACK)
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
		if(y_original_color==BLACK)
			deleteFixup(x);  
		return z;
	}

	void deleteFixup(Node x)
	{
		while(x!=root && x.color == BLACK)
		{ 
			if(x == x.parent.left)
			{
				Node w = x.parent.right;

				if(w.color == RED)
				{
					w.color = BLACK;
					x.parent.color = RED;
					rotateLeft(x.parent);
					w = x.parent.right;
				}

				if(w.left.color == BLACK && w.right.color == BLACK)
				{
					w.color = RED;
					x = x.parent;
					continue;
				}
				else if(w.right.color == BLACK)
				{
					w.left.color = BLACK;
					w.color = RED;
					rotateRight(w);
					w = x.parent.right;
				}

				if(w.right.color == RED)
				{
					w.color = x.parent.color;
					x.parent.color = BLACK;
					w.right.color = BLACK;
					rotateLeft(x.parent);
					x = root;
				}
			}
			else
			{
				Node w = x.parent.left;
				if(w.color == RED)
				{
					w.color = BLACK;
					x.parent.color = RED;
					rotateRight(x.parent);
					w = x.parent.left;
				}

				if(w.right.color == BLACK && w.left.color == BLACK)
				{
					w.color = RED;
					x = x.parent;
					continue;
				}
				else if(w.left.color == BLACK)
				{
					w.right.color = BLACK;
					w.color = RED;
					rotateLeft(w);
					w = x.parent.left;
				}

				if(w.left.color == RED)
				{
					w.color = x.parent.color;
					x.parent.color = BLACK;
					w.left.color = BLACK;
					rotateRight(x.parent);
					x = root;
				}
			}
		}
		x.color = BLACK; 
	}

	Node treeMinimum(Node subTreeRoot){
		while(subTreeRoot.left.key != -1)
		{
			subTreeRoot = subTreeRoot.left;
		}
		return subTreeRoot;
	}

	public void consoleUI() 
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);

		while (true) 
		{
			System.out.println("\n1.- Add items\n"
					+ "2.- Delete items\n"
					+ "3.- Check items\n"
					+ "4.- Print tree\n"
					+ "5.- Delete tree\n");
			int choice = scan.nextInt();
			int item;
			Node node;
			switch (choice) 
			{
			case 1:
				item = scan.nextInt();
				while (item != -999) //To let the code know when to stop adding 
				{
					node = new Node(item);
					insert(node);
					System.out.println("waiting for next item. Enter -999 to stop adding");
					item = scan.nextInt();
				}
				System.out.println("printing");
				printTree(root);
				break;
			case 2:
				item = scan.nextInt();
				while (item != -999) {
					node = new Node(item);
					System.out.print("\nDeleting item " + item);
					if (delete(node) != null) {
						System.out.print(": deleted!");
					} else {
						System.out.print(": does not exist!");
					}
					item = scan.nextInt();
				}
				System.out.println();
				printTree(root);
				break;
			case 3:
				item = scan.nextInt();
				while (item != -999) {
					node = new Node(item);
					System.out.println((findNode(node, root) != null) ? "found" : "not found");
					item = scan.nextInt();
				}
				break;
			case 4:
				printTree(root);
				break;
			case 5:
				deleteTree();
				System.out.println("Tree deleted!");
				break;
			}
		}
	}
}