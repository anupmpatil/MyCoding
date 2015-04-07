# search.py
# ---------
# Licensing Information:  You are free to use or extend these projects for 
# educational purposes provided that (1) you do not distribute or publish 
# solutions, (2) you retain this notice, and (3) you provide clear 
# attribution to UC Berkeley, including a link to 
# http://inst.eecs.berkeley.edu/~cs188/pacman/pacman.html
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero 
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and 
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s, s, w, s, w, w, s, w]

def depthFirstSearch(problem):
    """
    Search the deepest nodes in the search tree first.

    Your search algorithm needs to return a list of actions that reaches the
    goal. Make sure to implement a graph search algorithm.

    To get started, you might want to try some of these simple commands to
    understand the search problem that is being passed in:

    print "Start:", problem.getStartState()
    print "Is the start a goal?", problem.isGoalState(problem.getStartState())
    print "Start's successors:", problem.getSuccessors(problem.getStartState())
    """
    """
    "*** YOUR CODE HERE ***"
    #util.raiseNotDefined()
    
    APPROACH 1: This commented code was my initial attempt, where I pushed
    only (x,y) position on stack.
    And maintained a directory which holds (state(x,y) position: (parent, action)),
    After reaching the goal, I backtracked from goal state to start state using
    parent stored in directory and created a list of actions while backtracking.
    Then inverted direction list to get list of actions from start to goal state.
    
    print "Start:", problem.getStartState()
    print "Is the start a goal?", problem.isGoalState(problem.getStartState())
    print "Start's successors:", problem.getSuccessors(problem.getStartState())
    
    state = problem.getStartState()
    print problem.isGoalState(state)
    s = util.Stack()
    s.push(state)
    visitedList = []
    directionList = []
    directory = dict()
    count = 0
    while ((problem.isGoalState(state) != True)):# or (s.isEmpty() == True)):
        state = s.pop()
        if visitedList.count(state) == 0:
            if count != 0:
                directionList.append(directory[state])
            visitedList.append(state)
            successorList = problem.getSuccessors(state)
            count = 1
            print "current state: ", state
            for adj in successorList:
                if visitedList.count(adj[0]) == 0:
                    s.push(adj[0])
                # add state, direction to state and parent of state
                    directory.update([(adj[0], (adj[1], state))])
                    print adj[0], "parent: ", state
        #print "inside while"
    print "IS THIS GOAL STATE?", problem.isGoalState(state)
    path_direction = []
    
    while (state != problem.getStartState()):
        path_direction.append(directory[state][0])
        state = directory[state][1]
        
    path_direction = path_direction[::-1]
    print "Total Path Length: ", len(path_direction)
    return  path_direction
    """
    #"*** YOUR CODE HERE ***"
    #util.raiseNotDefined()
    """
    APPROACH 2:
    The I worked on this approach, where I pushed entire state i.e.
    tuple ((x-y position), action, cost) on stack
    
    Similar is the case for all 4 questions, I have kept my both 
    approaches with APPROACH 1 commented.
    """
    
    # DFS uses stack as data structure to 
    # store states while exploring and searching 
    # for goal state.
    stack = util.Stack();
    visitedList = [];
    state = problem.getStartState()
    stack.push((state,[],0));

    while not stack.isEmpty():
        # Get the next element to process
        currentState, listOfMoves, cost = stack.pop();

        # Check if current state is already visited.
        # If it is already visited, do not explore it again,
        # get the next element on stack.
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
            stack.push((state, path, totalCost));

    return []


def breadthFirstSearch(problem):
    """Search the shallowest nodes in the search tree first.
    "*** YOUR CODE HERE ***"
    APPROACH 1:
    state = problem.getStartState()
    s = util.Queue()
    s.push(state)
    visitedList = []
    directionList = []
    directory = dict()
    count = 0
    while ((problem.isGoalState(state) != True)):# or (s.isEmpty() == True)):
        state = s.pop()
        if visitedList.count(state) == 0:
            if count != 0:
                directionList.append(directory[state])
            visitedList.append(state)
            successorList = problem.getSuccessors(state)
            count = 1
            #print "current state: ", state
            for adj in successorList:
                if visitedList.count(adj[0]) == 0:
                    s.push(adj[0])
                # add state, direction to state and parent of state
                    directory.update([(adj[0], (adj[1], state))])
                    #print adj[0], "parent: ", state
        #print "inside while"
    path_direction = []
    
    while (state != problem.getStartState()):
        path_direction.append(directory[state][0])
        state = directory[state][1]
    
    path_direction = path_direction[::-1]
    print path_direction
    return  path_direction
    """
    """Search the shallowest nodes in the search tree first."""
    #"*** YOUR CODE HERE ***"
    #util.raiseNotDefined()
    # APPROACH 2
    
    
    # BFS uses Queue as data structure to 
    # store states while exploring and searching 
    # for goal state.   
    state = problem.getStartState()
    queue = util.Queue();
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
    
def uniformCostSearch(problem):
    """Search the node of least total cost first."""
    "*** YOUR CODE HERE ***"
    """
    APPROACH 1
    #util.raiseNotDefined()
    
    state = problem.getStartState()

    #print problem.isGoalState(state)
    s = util.PriorityQueue()
    s.push(state, 0)
    visitedList = []
    directory = dict()
    while ((problem.isGoalState(state) != True)):
        #if s.isEmpty():
            #return
        state = s.pop()
        #if (problem.isGoalState(state) == True):
            #return
        visitedList.append(state)
        successorList = problem.getSuccessors(state)
        for adj in successorList:
            if visitedList.count(adj[0]) == 0:
                # state: direction, parent
                directory.update([(adj[0], (adj[1], state))])
                s.push(adj[0],adj[2])
                print "state", adj[0]
                print "cost", adj[2]                

    path_direction = []
    
    while (state != problem.getStartState()):
        path_direction.append(directory[state][0])
        state = directory[state][1]
    path_direction = path_direction[::-1]
    return  path_direction
    """
    
    # APPROACH 2
    
    
    # UCS uses priority queue to
    # store state information and 
    # cost incurred while 
    # exploring path to destination
    pqueue = util.PriorityQueue()
    # List of already visited states
    visited = [] 
    state = problem.getStartState()
    pqueue.push((state, [], 0), 0) 
    while not pqueue.isEmpty():
        # Get the next element to process
        currentState, listOfMoves, cost = pqueue.pop()
        
        # If current state is goal state, return list
        # of moves needed to reach this state.
        if problem.isGoalState(currentState):
            return listOfMoves 

        # If current state is not visited,
        # mark it as visited and expand it i.e.
        # process its successors
        if currentState not in visited:
            visited.append(currentState)
            successorList = problem.getSuccessors(currentState)
			
            for state, direction, cost in successorList:
                if state not in visited:
                    # Update the path with moves required
                    # to reach this state
                    path = listOfMoves + [direction] 
                    # Update the total cost for this path
                    totalCost = problem.getCostOfActions(path)
                    pqueue.push((state, path, totalCost), totalCost)
    return []

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0
    
def aStarSearch(problem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""
    "*** YOUR CODE HERE ***"
    """
    #util.raiseNotDefined()
    startState = problem.getStartState()
    state = problem.getStartState()

    #print problem.isGoalState(state)
    print "PROBLEM = ", problem
    s = util.PriorityQueue()
    cost = heuristic(state, problem)
    #pushedItem = (state, startState)
    s.push(state, cost)
    visitedList = []
    directory = dict()
    while ((problem.isGoalState(state) != True)):

    	state = s.pop()
		
        visitedList.append(state)
        successorList = problem.getSuccessors(state)
        for adj in successorList:
            if visitedList.count(adj[0]) == 0:
                # state: direction, parent
                directory.update([(adj[0], (adj[1], state))])
                cost = adj[2] + heuristic(adj[0], problem)
                s.push(adj[0], cost)

    path_direction = []
    
    while (state != problem.getStartState()):
        path.append(directory[state][1])
        path_direction.append(directory[state][0])
        state = directory[state][1]
    
    path_direction = path_direction[::-1]
    print path_direction
    return  path_direction
    """
	
    # aStar uses UCS like approach and 
	# uses priority queue to
    # store state information and 
    # cost plus Heuristic function 
	# incurred while 
    # exploring path to destination
    state = problem.getStartState()
    cost = heuristic(state, problem)
    
    pqueue = util.PriorityQueue()
    visited = [] 
    # For initial state, path cost is 0
	# Hence just push heuristic value
    pqueue.push((state, [], 0), cost)
	
    while not pqueue.isEmpty():
        currentState, listOfMoves, cost = pqueue.pop()
        
		# If current state is goal state, return list
        # of moves needed to reach this state.
        if problem.isGoalState(currentState):
            #print " moves: ", listOfMoves
            return listOfMoves

		# If current state is not visited,
        # mark it as visited and expand it i.e.
        # process its successors
        if currentState not in visited:
            visited.append(currentState)
            successorList = problem.getSuccessors(currentState)
            
            for state, direction, cost in successorList:
                if state not in visited:
                    path = listOfMoves + [direction]
                    totalCost = problem.getCostOfActions(path) + heuristic(state, problem)
                    pqueue.push((state, path, totalCost), totalCost)
    return [] 
    
    
# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch
