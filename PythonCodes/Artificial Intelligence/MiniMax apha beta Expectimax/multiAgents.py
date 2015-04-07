# multiAgents.py
# --------------
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


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent


class ReflexAgent(Agent):
    """
      A reflex agent chooses an action at each choice point by examining
      its alternatives via a state evaluation function.

      The code below is provided as a guide.  You are welcome to change
      it in any way you see fit, so long as you don't touch our method
      headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {North, South, West, East, Stop}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = []
        for action in legalMoves:
            scores.append(self.evaluationFunction(gameState, action))
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices)  # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        # get game state that we want to evaluate
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        # print newPos
        newFood = successorGameState.getFood()
        # print newFood.asList()
        newGhostStates = successorGameState.getGhostStates()
        # print newGhostStates
        newScaredTimes = []
        for ghostState in newGhostStates:
            newScaredTimes.append(ghostState.scaredTimer)
            #print ghostState
        #print "newScaredTimes: ", newScaredTimes

        "*** YOUR CODE HERE ***"
        """
        Check if successor state is winning position.
        If it is a winning position, it should
        maximize store.

        If successor state is losing position,
        then its the minimum value
        """
        if successorGameState.isWin():
            return float("inf")
        if successorGameState.isLose():
            return -float("inf")

        score = successorGameState.getScore()
        """
        Check if successor state is power pellet place,
        If it is a power pellet place we can increase score.
        """
        powerplaces = currentGameState.getCapsules()

        if successorGameState.getPacmanPosition in powerplaces:
            score = score * 20

        # Incentive for eating power pallets
        # and scaring ghosts
        if newScaredTimes > 0:
            score = score + 300

        for plcae in powerplaces:
            if util.manhattanDistance(plcae, newPos) < 2:
                score = score + 100
        """
        If next position is very close to ghost position,
        we need to reduce the score
        """
        ghostpos = successorGameState.getGhostPosition(1)
        if manhattanDistance(newPos, ghostpos) < 2:
            score = score - 50


        foodlist = newFood.asList()
        closestfood = 100
        for foodpos in foodlist:
            distance = util.manhattanDistance(foodpos, newPos)
            if (distance < closestfood):
                closestfood = distance

        # Go for food only if you are safe from ghost
        if (newPos in foodlist) and (manhattanDistance(newPos, ghostpos) > 2):
            score = score + 100

        # Incentive to go for food position
        if not newPos in foodlist:
            score = score - 100

        # if newPos is in food list go for it
        if (newPos in foodlist):
            score = score + 200

        # If next state is the one in which less amount of food is left,
        # this means we are reaching to the goal of finishing all food.
        if (currentGameState.getNumFood() > successorGameState.getNumFood()):
            score += 500

        # stopping will waste time and reduce speed
        # hence we should discourage stopping
        if action == Directions.STOP:
            score -= 10

        # while going for finished food, pacman
        # should not avoid finishing nearby food
        # otherwise it has to revisit this part
        score = score - (5 * closestfood)
        #score -= closestfood
        return score
        #return successorGameState.getScore()


def scoreEvaluationFunction(currentGameState):
    """
      This default evaluation function just returns the score of the state.
      The score is the same one displayed in the Pacman GUI.

      This evaluation function is meant for use with adversarial search agents
      (not reflex agents).
    """
    return currentGameState.getScore()


class MultiAgentSearchAgent(Agent):
    """
      This class provides some common elements to all of your
      multi-agent searchers.  Any methods defined here will be available
      to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

      You *do not* need to make any changes here, but you can if you want to
      add functionality to all your adversarial search agents.  Please do not
      remove anything, however.

      Note: this is an abstract class: one that should not be instantiated.  It's
      only partially specified, and designed to be extended.  Agent (game.py)
      is another abstract class.
    """

    def __init__(self, evalFn='scoreEvaluationFunction', depth='2'):
        self.index = 0  # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)


class MinimaxAgent(MultiAgentSearchAgent):
    """
      Your minimax agent (question 2)
    """

    def getAction(self, gameState):
        """
          Returns the minimax action from the current gameState using self.depth
          and self.evaluationFunction.

          Here are some method calls that might be useful when implementing minimax.

          gameState.getLegalActions(agentIndex):
            Returns a list of legal actions for an agent
            agentIndex=0 means Pacman, ghosts are >= 1

          gameState.generateSuccessor(agentIndex, action):
            Returns the successor game state after an agent takes an action

          gameState.getNumAgents():
            Returns the total number of agents in the game
        """
        """
        Basic Algorithm:

        def MINIMAX-DECISION(state)
            return action corresponding to result of min-value(RESULT(state,a))
        def max-value(state):
            if Teminal-state return Utility(state)
            initialize v = -infinity
            for each legal action on currentState:
                successor-state = getSuccessor(currentState, action)
                v = max(v, min-value(successor-state))
            return v

        def min-value(state):
            if Teminal-state return Utility(state)
            initialize v = infinity
            for each legal action on currentState:
                successor-state = getSuccessor(currentState, action)
                v = min(v, max-value(successor-state))
            return v

        We will slightly change this algorithm so
        that multiple agents will be able to explore
        tree from same depth level.

        We introduce here a function called
        """

        # first move will be maximizer's move,
        # Hence starting Index is 0
        # 0 is the index of Pacman agent
        agentIndex = 0

        #start with current level of tree
        depth = self.depth

        # First call will go to maximizer
        # because we start with pacman,
        # which is a maximizer and agent index
        # for pacman is 0
        # This function will return a tuple containing
        # (value, action) pair.
        action = self.CalculateMinimaxDecision(gameState, agentIndex, depth)

        # This is the best action obtained after applying
        # minimax search on pacman game tree
        return action[1]


    def calculateMinValue(self, gameState, agentIndex, treedepth):
        # Minimum value and action associated
        # with it that should be taken to achieve
        # this value. For minimizer initialize value
        # to positive infinity, let us assume that
        # default direction is stop
        minValue = (float("inf"), Directions.STOP)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)
        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next agent index
            nextAgentIndex = agentIndex + 1
            # Get next state (to apply minimax decision)
            nextstate = gameState.generateSuccessor(agentIndex, action)
            returnvalue = self.CalculateMinimaxDecision(nextstate, nextAgentIndex, treedepth)

            # The above function can either return a taple
            # having score and action to be applied or
            # output of evaluation function in game state.
            # Hence we need to check before comparing
            if type(returnvalue) is tuple:
                returnvalue = returnvalue[0]
            newmin = min(minValue[0], returnvalue)

            # If new maximum value is found,
            # return this value and action
            # associated with this value
            if newmin != minValue[0]:
                minValue = (newmin, action)
        return minValue

    def calculateMaxValue(self, gameState, agentIndex, treedepth):
        # Maximum value and action associated
        # with it that should be taken to achieve
        # this value. For maximizer initialize value
        # to negative infinity, let us assume that
        # default direction is stop
        maxValue = (-float("inf"), Directions.STOP)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)

        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next agent index
            nextAgentIndex = agentIndex + 1

            # Get next state (to apply minimax decision)
            nextstate = gameState.generateSuccessor(agentIndex, action)
            returnvalue = self.CalculateMinimaxDecision(nextstate, nextAgentIndex, treedepth)

            # The above function can either return a taple
            # having score and action to be applied or
            # output of evaluation function in game state
            if type(returnvalue) is tuple:
                returnvalue = returnvalue[0]

            # If new maximum value is found,
            # return this value and action
            # associated with this value
            newMax = max(maxValue[0], returnvalue)
            if newMax != maxValue[0]:
                maxValue = (newMax, action)
        return maxValue

    def CalculateMinimaxDecision(self, gameState, agentIndex, treedepth):
        # Each time min - max calulating agent will increment
        # agent index, when agent index is greater than total
        # number of agents, that means, all agents have finished
        # working on this depth level. Hence, we should now work
        # next depth level. It is similar to exploring next
        # level of DFS by calling next level of recursion

        if agentIndex >= gameState.getNumAgents():
            treedepth = treedepth - 1
            agentIndex = 0


        # If we have already searched all levels or
        # if this state is winning state or
        # if this state is losing state
        # no need to search anymore, we can stop here
        if treedepth == 0 or gameState.isWin() or gameState.isLose():
            return self.evaluationFunction(gameState)

        # Pacman will always try to maximize,
        # as pacman agent is always 0 th index agent,
        # we will call maximizer on it
        # Hence, check If agent is pacman, then
        # apply maximizer because
        # pacman wants to win and
        # choose the best possible move
        # that will maximize its score
        # Ghosts are minimizers and want to choose
        # a move that will minimize score. Ghost
        # agents start at index greater than 0
        if agentIndex == 0:
            return self.calculateMaxValue(gameState, agentIndex, treedepth)
        else:
            return self.calculateMinValue(gameState, agentIndex, treedepth)


    """
    OLD APPROACH:
    Initially, I tried to implement the minimax functionality using
    two functions instead of three. The problem was that it was
    exploring wrong depth because each agent has to work on same level.

    Then I decided to take third function to control recursion level
    and maintaining which agent will process current level with what action

        curDepth = self.depth
        currentAgentIndex = 0
        numghosts = gameState.getNumAgents() - 1
        val = self.calculateMaxValue(gameState, numghosts, curDepth, 0)
        return val[0]
    def calculateMinValue(self, gameState, agents, depth, agentIndex):
        current_depth = depth
        if agentIndex >= agents:
            agentIndex = 0
            current_depth = depth - 1
        #print "Inside MIN depth = ",depth
        if gameState.isWin() or gameState.isLose() or depth == 0:
            #print "reached here to stop, MIN depth = ", depth
            return self.evaluationFunction(gameState)

        #v = float("inf")
        v = ("unknown", float("inf"))
        #current_depth = depth - 1
        r = range(1, agents+1)
        #for i in r:
        legalActions = gameState.getLegalActions(agentIndex)

        if agentIndex == 0:
            for action in legalActions:
                if action == "Stop":
                    continue
                nextState = gameState.generateSuccessor(agentIndex, action)
                #v = min(v, self.calculateMaxValue(nextState, agents, current_depth))
                retVal = self.calculateMaxValue(nextState, agents, current_depth, agentIndex)
                if type(retVal) is tuple:
                    retVal = retVal[1]

                vNew = max(v[1], retVal)
                #print "min, v = ", v
                if vNew is not v[1]:
                    v = (action, vNew)
        else:
            for action in legalActions:
                if action == "Stop":
                    continue
                nextState = gameState.generateSuccessor(agentIndex, action)
                #v = min(v, self.calculateMaxValue(nextState, agents, current_depth))
                retVal = self.calculateMinValue(nextState, agents, current_depth, agentIndex + 1)
                if type(retVal) is tuple:
                    retVal = retVal[1]

                vNew = min(v[1], retVal)
                    #print "min, v = ", v
                if vNew is not v[1]:
                    v = (action, vNew)
        return v


    def calculateMaxValue(self, gameState, agents, depth, agentIndex):
        #print "Inside MAX, depth = ", depth
        if gameState.isWin() or gameState.isLose() or depth == 0:
            #print "reached here to stop MAX, depth = ", depth
            return self.evaluationFunction(gameState)

        #v = -float("inf")
        v = ("unknown", -1*float("inf"))

        #r = range(1, agents+1)
        #for i in agents:
        #current_depth = depth - 1
        legalActions = gameState.getLegalActions(0)
        for action in legalActions:
            if action == "Stop":
                continue

            nextState = gameState.generateSuccessor(0, action)
            retVal = self.calculateMinValue(nextState, agents, depth, agentIndex)

            if type(retVal) is tuple:
                retVal = retVal[1]

            vNew = max(v[1], retVal)
            #print "max, v = ", v
            if vNew is not v[1]:
                v = (action, vNew)
        return v
    """

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
      Your minimax agent with alpha-beta pruning (question 3)
    """
    """
    Implemented Algorithm, as suggested in question
    def max-value(state, alpha, beta):
        initialize v = -infinity
        for each successor of state:
            v = max(v, value(successor, alpha, beta))
            if v > beta
                return v
            alpha = max(alpha, v)
        return v

    def min-value(state, alpha, beta):
        initialize v = +infinity
        for each successor of state:
            v = max(v, value(successor, alpha, beta))
            if v < alpha
                return v
            beta = min(beta, v)
        return v

    We will slightly modify this so that multiple ghost agents
    will call on a state on same depth level.

    """
    def getAction(self, gameState):
        """
          Returns the minimax action using self.depth and self.evaluationFunction
        """
        # alpha : MAX's best option on path to root
        # Initialize it to negative infinity
        # So that maximizer will use it to chose
        # the best him
        alpha = -float("inf")

        # beta : MIN's best option on path to root
        # Initialize it to positive infinity
        # So that minimizer will use it to chose
        # the best him
        beta = float("inf")

        # Start with current depth
        treeDepth = self.depth

        # Start with pacman,
        # agent insex in 0 for pacman
        agentIndex = 0

        # First call will go to maximizer
        # because we start with pacman,
        # which is a maximizer and agent index
        # for pacman is 0
        # This function will return a tuple containing
        # (value, action) pair.
        actionToReturn = self.CalculateMinimaxDecision(gameState, alpha, beta, agentIndex, treeDepth)

        # This is the best action obtained after applying
        # minimax search on pacman game tree
        return actionToReturn[1]

    def calculateMaxValue(self, gameState, alpha, beta, agentIndex, treeDepth):
        # Maximum value and action associated
        # with it that should be taken to achieve
        # this value. For maximizer initialize value
        # to negative infinity, let us assume that
        # default direction is stop
        maxValue = (-float("inf"), Directions.STOP)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)

        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next state
            nextState = gameState.generateSuccessor(agentIndex, action)
            # apply move for next agent
            nextAgentIndex = agentIndex + 1
            retVal = self.CalculateMinimaxDecision(nextState, alpha, beta, nextAgentIndex, treeDepth)

            # return value may be tuple or
            # value returned by evaluation function
            if type(retVal) is tuple:
                retVal = retVal[0]

            newMax = max(retVal, maxValue[0])

            if newMax != maxValue[0]:
                maxValue = (newMax, action)

            # If maximum value for this branch
            # is greater than beta value, prune tree
            # Don't need to expand this branch any more
            # because this branch is not going to rerun
            # any greater value
            if maxValue[0] > beta:
                return maxValue

            # check if new value is greater than alpha
            # If so we need to update this alpha value
            alpha = max(alpha, maxValue[0])
        return maxValue

    def calculateMinValue(self, gameState, alpha, beta, agentIndex, treeDepth):
        # Minimum value and action associated
        # with it that should be taken to achieve
        # this value. For minimizer initialize value
        # to positive infinity, let us assume that
        # default direction is stop
        minValue = (float("inf"), Directions.STOP)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)

        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next state
            nextState = gameState.generateSuccessor(agentIndex, action)
            # apply move for next agent
            nextAgentIndex = agentIndex + 1
            returnval = self.CalculateMinimaxDecision(nextState, alpha, beta, nextAgentIndex, treeDepth)

            # return value may be tuple or
            # value returned by evaluation function
            if type(returnval) is tuple:
                returnval = returnval[0]
            newMin = min(minValue[0], returnval)
            if newMin != minValue[0]:
                minValue = (newMin, action)

            # If minimum value for this branch
            # is less than alpha value, prune tree
            # Don't need to expand this branch any more
            # because this branch is not going to return any
            # lesser value
            if minValue[0] < alpha:
                return minValue

            # check if new value is less than beta
            # If so we need to update this beta value
            beta = min(beta, minValue[0])
        return minValue

    def CalculateMinimaxDecision(self, gameState, alpha, beta, agentIndex, treeDepth):
        # Each time min - max calulating agent will increment
        # agent index, when agent index is greater than total
        # number of agents, that means, all agents have finished
        # working on this depth level. Hence, we should now work
        # next depth level. It is similar to exploring next
        # level of DFS by calling next level of recursion
        if agentIndex >= gameState.getNumAgents():
            treeDepth = treeDepth - 1
            agentIndex = 0

        # Check if pacman has already reached to wiinning state
        # or losing state or leaf node, in this case program
        #  should end recursion and evaluate the current state
        # using utility funciton which in this case is
        # self.evaluataionFunction
        if gameState.isWin() or gameState.isLose() or treeDepth == 0:
            return self.evaluationFunction(gameState)

        # Pacman will always try to maximize,
        # as pacman agent is always 0 th index agent,
        # we will call maximizer on it
        # Hence, check If agent is pacman, then
        # apply maximizer because
        # pacman wants to win and
        # choose the best possible move
        # that will maximize its score
        # Ghosts are minimizers and want to choose
        # a move that will minimize score. Ghost
        # agents start at index greater than 0
        if agentIndex == 0:
            return self.calculateMaxValue(gameState, alpha, beta, agentIndex, treeDepth)
        else:
            return self.calculateMinValue(gameState, alpha, beta, agentIndex, treeDepth)


class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """
    def getAction(self, gameState):
        """
          Returns the expectimax action using self.depth and self.evaluationFunction

          All ghosts should be modeled as choosing uniformly at random from their
          legal moves.
        """
        #"*** YOUR CODE HERE ***"
        #util.raiseNotDefined()

        """
        Basic Algorithm:

        The basic algorithm is similar to that of minimax tree
        search algorithm.
        The only difference is while ghost agents play
        they may not choose optimal decision every time.

        Hence, while calculating minimizer, we take into consideration the
        probability that ghost agent may not choose optimal move

        This is calculated by:
        total score = evaluated min value for action * (probabilty of choosing optimal
                                                        action which will produce this min value)
        total score = evaluated min value * (1/number of possible actions)

        def MINIMAX-DECISION(state)
            return action corresponding to result of min-value(RESULT(state,a))
        def max-value(state):
            if Teminal-state return Utility(state)
            initialize v = -infinity
            for each legal action on currentState:
                successor-state = getSuccessor(currentState, action)
                v = max(v, min-value(successor-state))
            return v

        def min-value(state):
            if Teminal-state return Utility(state)
            Assume default probabilty of choosing
            optimal move to be 0
            initialize v = 0
            legalactions = getLegalactions(state)
            probability = 1/len(legalactions)
            for each legal action on currentState:
                successor-state = getSuccessor(currentState, action)
                v = min(v, max-value(successor-state))
                v = v + (v * probability)
            return v

        We will slightly change this algorithm so
        that multiple agents will be able to explore
        tree from same depth level.

        We introduce here a function called
        """

        # first move will be maximizer's move,
        # Hence starting Index is 0
        # 0 is the index of Pacman agent
        agentIndex = 0

        #start with current level of tree
        depth = self.depth

        # First call will go to maximizer
        # because we start with pacman,
        # which is a maximizer and agent index
        # for pacman is 0
        # This function will return a tuple containing
        # (value, action) pair.
        action = self.CalculateMinimaxDecision(gameState, agentIndex, depth)

        # This is the best action obtained after applying
        # minimax search on pacman game tree
        return action[1]

    def calculateMaxValue(self, gameState, agentIndex, treedepth):
        # Maximum value and action associated
        # with it that should be taken to achieve
        # this value. For maximizer initialize value
        # to negative infinity, let us assume that
        # default direction is stop
        maxValue = (-float("inf"), Directions.STOP)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)

        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next agent index
            nextAgentIndex = agentIndex + 1

            # Get next state (to apply minimax decision)
            nextstate = gameState.generateSuccessor(agentIndex, action)
            returnvalue = self.CalculateMinimaxDecision(nextstate, nextAgentIndex, treedepth)

            # The above function can either return a taple
            # having score and action to be applied or
            # output of evaluation function in game state
            if type(returnvalue) is tuple:
                returnvalue = returnvalue[0]

            # If new maximum value is found,
            # return this value and action
            # associated with this value
            newMax = max(maxValue[0], returnvalue)
            if newMax != maxValue[0]:
                maxValue = (newMax, action)
        return maxValue

    def probabilisticExpectedValue(self, gameState, agentIndex, treedepth):
        # Value returned by probabilistic minimizing function
        # and action associated
        # with it that should be taken to achieve
        # this value. For probabilistic calculation
        # initialize value to 0 considering probability
        # of choosing optimal move to be zero,
        # let us assume that
        # default direction is stop
        cost = [0, Directions.STOP]
        retval = tuple(cost)

        # Get the set of legal actions
        # for current game state for
        # current agent
        legalactions = gameState.getLegalActions(agentIndex)

        # calculate probability of selecting a optimal
        # move from list of all possible moves
        probability = 1.0/float(len(legalactions))

        for action in legalactions:
            # onserved that stopping might lead
            # pacman to be eaten by ghost
            # we can safely consider stopping
            # as not the best action and can eliminate it
            if action == Directions.STOP:
                continue
            # get next agent index
            nextAgentIndex = agentIndex + 1

            # Get next state (to apply minimax decision)
            nextstate = gameState.generateSuccessor(agentIndex, action)
            returnvalue = self.CalculateMinimaxDecision(nextstate, nextAgentIndex, treedepth)
            if type(returnvalue) is tuple:
                returnvalue = returnvalue[0]

            # Calculate probability action
            # associated with this value
            cost[0] += float(returnvalue) * probability
            cost[1] = action
            retval = tuple(cost)
        return retval

    def CalculateMinimaxDecision(self, gameState, agentIndex, treedepth):
        # Each time min - max calulating agent will increment
        # agent index, when agent index is greater than total
        # number of agents, that means, all agents have finished
        # working on this depth level. Hence, we should now work
        # next depth level. It is similar to exploring next
        # level of DFS by calling next level of recursion

        if agentIndex >= gameState.getNumAgents():
            treedepth = treedepth - 1
            agentIndex = 0

        # If we have already searched all levels or
        # if this state is winning state or
        # if this state is losing state
        # no need to search anymore, we can stop here
        if treedepth == 0 or gameState.isWin() or gameState.isLose():
            return self.evaluationFunction(gameState)

        # Pacman will always try to maximize,
        # as pacman agent is always 0 th index agent,
        # we will call maximizer on it
        # Hence, check If agent is pacman, then
        # apply maximizer because
        # pacman wants to win and
        # choose the best possible move
        # that will maximize its score
        # Ghosts are minimizers and want to choose
        # a move that will minimize score. Ghost
        # agents start at index greater than 0
        if agentIndex == 0:
            return self.calculateMaxValue(gameState, agentIndex, treedepth)
        else:
            return self.probabilisticExpectedValue(gameState, agentIndex, treedepth)



def betterEvaluationFunction(currentGameState):
    """
      Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
      evaluation function (question 5).

      DESCRIPTION:

      In general, goal is to try to maximize the score
      by getting closer to remaining food,
      eating power pallets, eating scared ghosts,
      staying away from closest ghost.

      I will evaluate current game state by following parameters:
      1. Remaining food
      2. Power-pallets
      3. Ghost factor

      1. 1. Incentive to chase nearest remaining food item
         2. Inventive to choose remaining amount of food so that pacman won't waste time in areas that
            don't have food, to avoid penalty pacman will keep on chasing remaining food
         3. Incentive to chase distant food item so that pacman won't
            avoid chasing distant food if very less amount of food is left
      2. Power pallets:
         Incentive to chase remaining capsules so that pacman chases capsules
         whenever available so that it can eat ghost, as eating ghost will increase
         pacman score.
      3. 1. If ghost is not scared ghost, keep away from it, because being alive is important
            because if pacman stays alive, there are better chances to win. So penalty for
            staying near ghost is greater than that of eating closest or remaining food.
         2. Incentive for eating scared ghosts. Pacman will try to reach out for nearest
            scared ghost.

    """
    "*** YOUR CODE HERE ***"
    #util.raiseNotDefined()

    # Check if current game state is winning state
    # If yes we do not need to evaluate further more,
    # just return maximum possible value
    if currentGameState.isWin():
        return float("inf")

    # Check if current game state is losing state
    # If yes we do not need to evaluate further more,
    # just return minimum possible value
    if currentGameState.isWin():
        return -float("inf")

    # Get current score and modify
    # it with reference to current game
    # parameters
    score = currentGameState.getScore()

    # Get remaining food on board
    food = currentGameState.getFood()
    remainingFood = food.asList()

    remainingFoodDistances = []

    currentPosition = currentGameState.getPacmanPosition()

    for item in remainingFood:
        distance = util.manhattanDistance(item, currentPosition)
        remainingFoodDistances.append(-1 * distance)
    # If no food is left code shouldn't crash
    if len(remainingFoodDistances) == 0:
        remainingFoodDistances.append(0)

    # Incentive to chase nearest remaining food item
    score = score + max(remainingFoodDistances)

    # Inventive to choose remaining amount of food
    # so that pacman won't waste time in areas that
    # don't have food, to avoid penalty pacman
    # will keep on chasing remaining food
    score = score - (5 * len(remainingFood))

    # Incentive to chase distant food item so
    # that pacman won't avoid chasing distant food
    # if very less amount of food is left
    score = score + (2.5 * min(remainingFoodDistances))


    # Get the list of remaining capsules
    capsuleList = currentGameState.getCapsules()

    # Incentive to chase remaining capsules
    # So that pacman chases capsules
    # whenever available so that it can
    # eat ghost, as eating ghost will increase
    # pacman score.
    score = score - (150 * len(capsuleList))

    # maintain the list of distances to ghosts
    ghostDistances = []
    # maintain list of distances to scared ghosts
    distanceToScared = []
    ghostStates = currentGameState.getGhostStates()

    scaredGhosts = 0

    for ghostState in ghostStates:
        ghostPosition = ghostState.getPosition()
        if ghostState.scaredTimer == 0:
            scaredGhosts += 1
            ghostDistances.append(0)
            continue

        else:
            # calculate distance to scared ghost to chase it
            distanceToScared.append(util.manhattanDistance(currentPosition, ghostPosition))
            distance = util.manhattanDistance(currentPosition, ghostPosition)
            if distance == 0:
                ghostDistances.append(0)
            else:
                ghostDistances.append(-1.0/float(distance))

    # If no ghost is scared, code shouldn't crash
    if len(distanceToScared) == 0:
        distanceToScared.append(0)

    # try to eat nearest scared ghost
    score = score - 3 * min(distanceToScared)
    # try to keep ghost list minimum by eating scared ghosts
    score = score - 25 * (len(ghostStates) - scaredGhosts)
    # try to stay away from nearest ghost
    score = score + 5 * min(ghostDistances)

    return score

# Abbreviation
better = betterEvaluationFunction

# References:
# http://en.wikipedia.org/wiki/Minimax
# Mainly developed using algorithm given on question page.
# Structurally Minimax, Alpha-beta and ExpectimaxAgent searches
# are similar.


