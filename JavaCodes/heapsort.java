import java.util.*;
import java.lang.*;


class heap
{
	// Maintain heap size at class level
	static int heapsize = 0;
	
	// Elements of heap
	private int[] input;
	
	// hold elements after sorting
	private int[] sortedHeap;
	
	heap(int [] input)
	{
		this.input = input;
		heapsize = this.input.length;
		buildHeap();
	}
	
	// Return left child index
	private int getLeftChild(int parent)
	{
		return ( 2 * parent + 1);
	}
	
	// Return right child index
	private int getRightChild(int parent)
	{
		return ( 2 * parent + 2);
	}
	
	private void maxHeapify(int index)
	{
		int left = getLeftChild(index);
		int right = getRightChild(index);
		int largest = 0;
		if ((left < heapsize) && (input[index] < input[left]))
		{
			largest = left;
		}
		else
		{
			largest = index;
		}
		if ((right < heapsize) && (input[largest] < input[right]))
		{
			largest = right;
		}
		if (index != largest)
		{
			int temp = input[index];
			input[index] = input[largest];
			input[largest] = temp;
			maxHeapify(largest);
		}
	}
	/*
	 * Build the Heap
	 */
	private void buildHeap()
	{
		for(int i = (heapsize/2); i >= 0; i--)
		{
			maxHeapify(i);
		}
	}
	
	private void sortHeap()
	{
		System.out.println("Before sorting: ");
		for (int i = 0; i < this.input.length; i++)
		{
			System.out.println("Input is : " + this.input[i]);
		}
		sortedHeap = new int[heapsize];
		int j = 0;
		for (int i = heapsize - 1; i > 0; i--)
		{
			sortedHeap[j] = input[0];
			input[0] = input[heapsize - 1];
			heapsize = heapsize - 1;
			j++;
			maxHeapify(0);			
		}
		sortedHeap[j] = input[0];		
	}
	
	public void printSortedHeap()
	{
		sortHeap();
		for(int i = 0; i < sortedHeap.length; i++)
		{
			System.out.print("\t" + sortedHeap[i]);
		}
	}
}

public class heapsort
{
	public static void main(String [] args)
	{
		int [] inputarr = {11, 15, 12, 18, 14, 19, 17, 13, 20, 16};
		heap h = new heap(inputarr);
		h.printSortedHeap();		
	}
}