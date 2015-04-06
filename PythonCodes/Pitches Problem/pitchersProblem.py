# 4,7,10 Pitchers problem using BFS and DFS
class Stack:
    "A container with a last-in-first-out (LIFO) queuing policy."
    def __init__(self):
        self.list = []

    def push(self,item):
        "Push 'item' onto the stack"
        self.list.append(item)

    def pop(self):
        "Pop the most recently pushed item from the stack"
        return self.list.pop()

    def isEmpty(self):
        "Returns true if the stack is empty"
        return len(self.list) == 0

class Queue:
    "A container with a first-in-first-out (FIFO) queuing policy."
    def __init__(self):
        self.list = []

    def push(self,item):
        "Enqueue the 'item' into the queue"
        self.list.insert(0,item)

    def pop(self):
        """
          Dequeue the earliest enqueued item still in the queue. This
          operation removes the item from the queue.
        """
        return self.list.pop()

    def isEmpty(self):
        "Returns true if the queue is empty"
        return len(self.list) == 0
        
        
class Moves:
    ONE = (1,2)
    TWO = (1,3)
    THREE = (2,1)
    FOUR = (2,3)
    FIVE = (3,1)
    SIX = (3,2)
    """
    def getLegalMoves(container1, container2, container3):
        legalMoves = []
        if (container1 < container2) and (container1 != 0) and (container2 != 7):
            legalMoves.append(ONE)
        if (container1 < container3) and (container1 != 0) and (container3 != 10):
            legalMoves.append(TWO)
        if (container2 < container1) and (container2 != 0) and (container1 != 4):
            legalMoves.append(THREE)
        if (container2 < container3) and (container2 != 0) and (container3 != 10):
            legalMoves.append(FOUR)
        if (container3 < container1) and (container3 != 0) and (container1 != 4):
            legalMoves.append(FIVE)
        if (container3 < container2) and (container3 != 0) and (container2 != 7):
            legalMoves.append(SIX)
    
        return legalMoves
    """
def getLegalMoves(container1, container2, container3):
        print "Input: ", container1, container2, container3
        legalMoves = []
        if (container1[1] != 0) and (container2[1] != container2[0]):
            legalMoves.append(Moves.ONE)
        if (container1[1] != 0) and (container3[1] != container3[0]):
            legalMoves.append(Moves.TWO)
        if (container2[1] != 0) and (container1[1] != container1[0]):
            legalMoves.append(Moves.THREE)
        if (container2[1] != 0) and (container3[1] != container3[0]):
            legalMoves.append(Moves.FOUR)
        if (container3[1] != 0) and (container1[1] != container1[0]):
            legalMoves.append(Moves.FIVE)
        if (container3[1] != 0) and (container2[1] != container2[0]):
            legalMoves.append(Moves.SIX)
    
        return legalMoves
        
def transferContents(source, destination):
    source_capacity = source[0]
    destination_capacity = destination[0]
    source_level = source[1]
    destination_level = destination[1]
    destination_remaining_capacity = destination_capacity - destination_level
    if destination_remaining_capacity <= source_level:
        destination_level = destination_level + destination_remaining_capacity
        source_level = source_level - destination_remaining_capacity
    elif destination_remaining_capacity >= source_level:
        destination_level = destination_level + source_level
        source_level = 0
    source = (source_capacity, source_level)
    #print "source after move", source
    destination = (destination_capacity, destination_level)
    #print "destination after move", destination
    return source, destination
        
class PouringWaterProblem:
    """
    This search problem finds paths through all four corners of a layout.

    You must select a suitable state space and successor function
    """
    """
    state:   The current search state
             The state here encodes:
             pacman position : current position of pacman (x,y)
             VisitedList: List of currently visited corners
    """
        
    def __init__(self):
        """
        Stores the walls, pacman's starting position and corners.
        """
        
    def getStartState(self):
        """
        Returns the start state (in your state space, not the full Pacman state
        space)
        """
        #"*** YOUR CODE HERE ***"
        
        state = [(4,4),(7,7),(10,0)]
        # return the starting position and list of
        # visited corners. Currently nothing is visited,
        # hence pass the empty list
        return state

    def isGoalState(self, state):
        """
        Returns whether this search state is a goal state of the problem.
        """
        "*** YOUR CODE HERE ***"
        """
        When either of the container 1 or container 2 contains 2 pints
        """
        # Get current state
        currentState = state[0]
        #print "current State: ", state
        if (state[0][1] == 2) or (state[1][1] == 2):
            return True
        
        return False     

    def getSuccessors(self, state):
        """
        Returns successor states, the actions they require, and a cost of 1.

         As noted in search.py:
            For a given state, this should return a list of triples, (successor,
            action, stepCost), where 'successor' is a successor to the current
            state, 'action' is the action required to get there, and 'stepCost'
            is the incremental cost of expanding to that successor
        """
        """
            state:   The current search state
                     The state here encodes:
                      pacman position : current position of pacman (x,y)
                      VisitedList: List of currently visited corners
        """
        successors = []
        # Get current state
        
        container1, container2, container3 = state
        #print "container1", container1
        #print "container2", container2
        #print "container3", container3
        
        
        legalMoves = getLegalMoves(container1, container2, container3)
        print "Legal Moves", legalMoves
        for move in legalMoves:
            #print "move: ",move
            #print "Moves.ONE : ",Moves.ONE
            #print "move == Moves.ONE ",move == Moves.ONE
            if move == Moves.ONE:
                """
                container1_capacity = container1[0]
                container2_capacity = container2[0]
                container1_level = container1[1]
                container2_level = container1[2]
                destination_remaining_capacity = container2_capacity - container2_level
                if destination_remaining_capacity <= container1_level
                     container2_level = container2_level + destination_remaining_capacity
                     container1_level = container1_level - destination_remaining_capacity
                else if destination_remaining_capacity >= container1_level
                     container2_level = container2_level + container1_level
                     container1_level = 0
                """
                new_container1, new_container2 = transferContents(container1, container2)
                new_container3 = container3
                #print "Applying move 1"
            if move == Moves.TWO:
                new_container1, new_container3 = transferContents(container1, container3)
                new_container2 = container2
                #print "Applying move 2"
            if move == Moves.THREE:
                new_container2, new_container1 = transferContents(container2, container1)
                new_container3 = container3
                #print "Applying move 3"
            if move == Moves.FOUR:
                new_container2, new_container3 = transferContents(container2, container3)
                new_container1 = container1
                #print "Applying move 4"
            if move == Moves.FIVE:
                new_container3, new_container1 = transferContents(container3, container1)
                new_container2 = container2
                #print "Applying move 5"
            if move == Moves.SIX:
                new_container3, new_container2 = transferContents(container3, container2)
                new_container1 = container1
                #print "Applying move 6"
            successor = ([new_container1, new_container2, new_container3], [move], 1)
            successors.append(successor)
        #print "successors: ", successors
        return successors
        
    def getCostOfActions(self, actions):
        """
        Returns the cost of a particular sequence of actions.  If those actions
        include an illegal move, return 999999.  This is implemented for you.
        """
        if actions == None: return 999999
        cost = 0
        for action in actions:
            cost = cost + 1
        return len(actions)     
        
def depthFirstSearch(problem):
    # DFS uses stack as data structure to 
    # store states while exploring and searching 
    # for goal state.
    #print "Inside dfs"
    stack = Stack();
    visitedList = [];
    state = problem.getStartState()
    #print "Start state", state
    stack.push((state,[],0));

    while not stack.isEmpty():
        # Get the next element to process
        poppedItem = stack.pop()
        currentState, listOfMoves, cost = poppedItem
        #print "current state after pop: ", currentState
        #print "listOfMoves after pop: ", listOfMoves

        # Check if current state is already visited.
        # If it is already visited, do not explore it again,
        # get the next element on stack.
        if(currentState in visitedList):
            continue;

        # If current state is not visited,
        # mark it as visited.
        visitedList.append(currentState);
        #print "visited list", visitedList

        # If current state is goal state, return list
        # of moves needed to reach this state.
        if problem.isGoalState(currentState):
            #print "TOtal moves: ", len(listOfMoves)
            #print " moves: ", listOfMoves
            return listOfMoves;

        # Get list of successors of current node
        for state, direction, cost in problem.getSuccessors(currentState):
            #print "getting successors" ,state, direction, cost
            # Update the list of moves to reach this successor
            path = listOfMoves + [direction]
            # Get the total cost if this path is taken
            totalCost = problem.getCostOfActions(path)
            stack.push((state, path, totalCost));

    return []
	
def breadthFirstSearch(problem):
    
    # BFS uses Queue as data structure to 
    # store states while exploring and searching 
    # for goal state.   
    state = problem.getStartState()
    queue = Queue();
    visitedList = [];
    queue.push((state,[],0));

    while not queue.isEmpty():
        # Get the next element to process
        currentState, listOfMoves, cost = queue.pop();
        
        # Check if current state is already visited.
        # If it is already visited, do not explore it again,
        # get the next element in queue.
        if(currentState in visitedList):
            continue;

        # If current state is not visited,
        # mark it as visited.
        visitedList.append(currentState);

        # If current state is goal state, return list
        # of moves needed to reach this state.
        if problem.isGoalState(currentState):
            #print "TOtal moves: ", len(listOfMoves)
            #print " moves: ", listOfMoves
            return listOfMoves;

        # Get list of successors of current node
        for state, direction, cost in problem.getSuccessors(currentState):
            # Update the list of moves to reach this successor
            path = listOfMoves + [direction]
            # Get the total cost if this path is taken
            totalCost = problem.getCostOfActions(path)
            queue.push((state, path, totalCost));

    return [];  
    
# Main Function
if __name__ == '__main__':        
    search_problem = PouringWaterProblem()
    listOfMoves = depthFirstSearch(search_problem)
    state = search_problem.getStartState()
    #print "initial state : ",state
    container1, container2, container3 = state
    print "list of moves: ", listOfMoves
    for moves in listOfMoves:
        move = moves[0]
        if move == Moves.ONE:
            container1, container2 = transferContents(container1, container2)
            container3 = container3
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.TWO:
            container1, container3 = transferContents(container1, container3)
            container2 = container2
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.THREE:
            container2, container1 = transferContents(container2, container1)
            container3 = container3
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.FOUR:
            container2, container3 = transferContents(container2, container3)
            container1 = container1
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.FIVE:
            container3, container1 = transferContents(container3, container1)
            container2 = container2
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.SIX:
            container3, container2 = transferContents(container3, container2)
            container1 = container1
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
			
	search_problem1 = PouringWaterProblem()
    listOfMoves1 = breadthFirstSearch(search_problem)
    state1 = search_problem1.getStartState()
    #print "initial state : ",state
    container1, container2, container3 = state1
    print "BFS list of moves: ", listOfMoves1
    for moves1 in listOfMoves1:
        move = moves1[0]
        if move == Moves.ONE:
            container1, container2 = transferContents(container1, container2)
            container3 = container3
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.TWO:
            container1, container3 = transferContents(container1, container3)
            container2 = container2
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.THREE:
            container2, container1 = transferContents(container2, container1)
            container3 = container3
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.FOUR:
            container2, container3 = transferContents(container2, container3)
            container1 = container1
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.FIVE:
            container3, container1 = transferContents(container3, container1)
            container2 = container2
            print "Applying move :", move, "result: [", container1, container2, container3, "]"
        if move == Moves.SIX:
            container3, container2 = transferContents(container3, container2)
            container1 = container1
            print "Applying move :", move, "result: [", container1, container2, container3, "]"