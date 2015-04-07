import java.lang.*;
import java.util.*;

class node<K, V>
{
	K key;
	V value;
	node next;
	
	node()
	{
		key = 0;
		value = 0;
		next = null;		
	}
}
class linkedList
{
	node front;
	linkedList()
	{
		front = null;
	}
	
	void insert(int key, int data)
	{
		if (front == null)
		{
			front = new node();
			front.key = key;
			front.value = data;
		}
		else
		{
			node temp = front;
			while (temp.next != null)
			{
				temp = temp.next;				
			}
			node newNode = new node();
			newNode.key = key;
			newNode.value = data;
			temp.next = newNode;
		}
	}
	
	void delete(int key)
	{
		if (front == null)
		{
			System.out.println("Deletion not possible");
		}
		if (front.key == key)
		{
			if(front.next == null)
			{
				front = null;
			}
			else
			{
				node temp = front;
				front = front.next;
				temp = null;
			}
		}
		else
		{
			node temp = front;
			while (temp.next != null)
			{
				if (temp.next.key == key)
				{
					node delNode = temp.next;
					temp.next = temp.next.next;
					delNode = null;
					return;
				}
				temp = temp.next;
			}
		}
	}
	
	public void display()
	{
		node temp = front;
		System.out.print("(" + temp.key + " ," + temp.value + ") ->");
		while(temp.next != null)
		{
			temp = temp.next;
			System.out.print("(" + temp.key + " ," + temp.value + ") ->");
		}
	}
}


public class hashtable
{
	public static void main(String [] args)
	{
		testLinkedList();
	}
	public static void testLinkedList()
	{
		linkedList ll1 = new linkedList();
		Scanner sc = new Scanner(System.in);
		while (true)
		{
			System.out.println("\n 1. Add Element \n 2. Delete Element \n 3. View Linked List \n 4. Exit");
			System.out.println("Enter the choice");
			int choice = 0;
			choice = sc.nextInt();
			switch(choice)
			{
				case 1:
					System.out.println("Enter Key: ");
					int key = sc.nextInt();
					System.out.println("Enter Value: ");
					int value = sc.nextInt();
					ll1.insert(key, value);
					ll1.display();
				break;
				case 2:
					System.out.println("Enter Key to delete: ");
					key = sc.nextInt();
					ll1.delete(key);
					ll1.display();
				break;
				case 3:
					ll1.display();
				break;
				case 4:
					return;
			}
			
		}
	}
}
