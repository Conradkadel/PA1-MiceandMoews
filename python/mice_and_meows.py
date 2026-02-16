# EXCLUDE FILE
'''
File:    mice_and_meows.py
Author:  Prof Feild
Purpose: Defines the Maze problem where an agent must navigate from a start
         position to an exit position in a grid-based maze.
'''

from math import sqrt
import sys
import problem

class MiceAndMeowState:
    '''Represents a state in the Mouse and Meows problem, which is the
    agent's position and the position of every mouse that has yet to be
    caught.
    '''
    def __init__(self, agentLocation, miceLocations):
        '''
        Parameters:
            agentLocation ((int,int)): The location of the agent in the maze.
            miceLocations (((int, int), ...)): A tuple of the locations of the mice in the maze.
        '''
        self.agentLocation = agentLocation
        self.miceLocations = miceLocations

    def __eq__(self, other):
        '''
        Returns (bool): Whether two states are equal; they are equal if the
            agent and mice are in the same locations in both states.
        '''
        return (self.agentLocation == other.agentLocation and 
            self.miceLocations == other.miceLocations)
    
    def __hash__(self):
        '''
        Returns (int): The hash of a state, which is the hash of the agent's
            location and the hash of the set of mice locations.
        '''
        return hash((self.agentLocation, self.miceLocations))
    

class MiceAndMeows(problem.Problem):
    '''
    The search problem: Of Mice and Meows. The agent is a cat and the goal is to
    catch all of the mice and make it to the exit. The cost of moving is 1,
    except for moving into water, which costs 6.
    '''
    heuristics = set(('manhattan', 'euclidean', 'mice-remaining','totalDistance','closestMice'))
    visited = []
    def __init__(self, boardFilename=None, heuristic=None):
        '''
        Parameters:
            boardFilename (str): The filename of the board to load.
            heuristic (str): The heuristic to use for informed search.
        '''  
        if boardFilename == None:
            self.board = []
            self.start = MiceAndMeowState((-1,-1), set())
            self.exit = (-1,-1)
        else:
            self.loadFile(boardFilename)

        heuristics = {
            'manhattan': self.manhattanDistance,
            'euclidean': self.euclideanDistance,
            'mice-remaining': self.miceRemainingDistance,
            'totalDistance' : self.heuristicTotalDistance,
            'closestMice' : self.heuristicClosestMice
        }
        if heuristic is None:
            self.heuristic = self.defaultHeuristic
        elif heuristic in heuristics:
            self.heuristic = heuristics[heuristic]
        else:
            sys.exit('Invalid heuristic: ' + heuristic)

        print('Heuristic:', heuristic)

    def loadFile(self, boardFilename):
        '''
        Parses a Mouse and Meows board file. Should contain one character per spot. The cat and mice
        positions are encoded in a state, while the exit and water positions are encoded in
        the problem instance itself (since those are fixed).

        Here are the character codes:
            s -- the agent's starting position
            e -- exit (if reached and all mice are gone, the goal state has been reached)
            w -- water (cost: 6)
            m -- mouse (0 or more)
            (space) -- an open spot (cost: 1)

        Parameters:
            boardFilename (str): The path to a file containing one character per spot on the board.
        '''
        self.board = []
        agentLocation = (-1,-1)
        miceLocations = []
        for i,row in enumerate(open(boardFilename)):
            self.board.append([])
            for j,col in enumerate(row.rstrip('\n')):
                self.board[-1].append(col)
                if col == 's':
                    agentLocation = (i,j)
                elif col == 'e':
                    self.exit = (i,j)
                elif col == 'm':
                    miceLocations.append((i,j))

        self.start = MiceAndMeowState(agentLocation, tuple(miceLocations))
        print('Starting state:')
        for row in self.board:
            print(''.join(['_' if c == ' ' else c for c in row]))

    def successors(self, state):
        '''
        Produces a list of spots -- ()  -- that the agent can move into, along
        with the spot's cost and estimate of how far it is from the exit.
        Returned as a list of tuples:

            [
                (move, MiceAndMeowsState, cost, dist), ...
            ]

        Parameters:
            state (MiceAndMeowsState): The current state of the problem.

        Returns (list((str, MiceAndMeowsState, float, float))): A list of the
            states that can be reached from the given state. Each item is a
            4-tuple: (action, new state, cost of action, estimated cost to
            goal).
        '''
        (i,j) = state.agentLocation
        successors = []
        potentialSuccessorSpots = (
            ('left', i,j-1),
            ('up', i-1,j), 
            ('right', i,j+1), 
            ('down', i+1,j) 
        )

        ## some kind of a list to keep track of visited nodes and t
    
        for move,i,j in potentialSuccessorSpots:
            if( i >= 0 and i < len(self.board) and 
                j >= 0 and j <len(self.board[i])):

                cost = 6 if self.board[i][j] == 'w' else 1

                mice = state.miceLocations
                # If the agent is moving onto a mouse, remove it from the set of mice.,
                if (i,j) in mice:
                    mice = tuple([m for m in mice if m != (i,j)])

                successorState = MiceAndMeowState((i,j), mice)

                if successorState not in self.visited:
                    successors.append((move, successorState, cost, self.getDistance(successorState)))
                    self.visited.append(successors)

        return successors

    def getDistance(self, state):
        '''
        Estimates the distance from the state to a goal state using whatever the current heuristic is.

        Parameters:
            state (MiceAndMeowsState): The state to check.
            
        Returns (float): The estimated distance from the given state to a goal state. This should
            use whatever heuristic is currently set for the problem.
        '''
        return self.heuristic(state)

    def isGoal(self, state):
        '''
        The goal is reached when the agent has caught all the mice and is at the exit.

        Parameters:
            state (MiceAndMeowsState): The state to check.

        Returns (bool): True if the given state is a goal state, False otherwise.
        '''     
        return state.agentLocation == self.exit and len(state.miceLocations) == 0

    def defaultHeuristic(self, state):
        '''Uses 0 for all estimates.

        Parameters:
            state (MiceAndMeowsState): The state to check.

        Returns (float): The default heuristic value for the given state: 0.
        '''
        return 0

    def manhattanDistance(self, state):
        '''
        Parameters:
            state (MiceAndMeowsState): The state to check.

        Returns (float): The Manhattan distance from the given state to the exit
            location.
        '''
        return abs(self.exit[0]-state.agentLocation[0]) + abs(self.exit[1]-state.agentLocation[1])

    def euclideanDistance(self, state):
        '''
        Parameters:
            state (MiceAndMeowsState): The state to check.

        Returns (float): The Euclidean distance from the given state to the exit
            location.
        '''
        return float(sqrt(pow(self.exit[0]-state.agentLocation[0], 2) + pow(self.exit[1]-state.agentLocation[1], 2)))
    
    ## 
    def heuristicClosestMice(self,state):
        if len(state.miceLocations) == 0:
            return self.manhattanDistance(state)
        lowestDistance = float('inf')
        closestMouse = None
        for x,y in state.miceLocations:
            currentDistance = (abs(x - state.agentLocation[0])) + (abs(y - state.agentLocation[1]))
            if currentDistance < lowestDistance:
                lowestDistance = currentDistance
                closestMouse = (x, y)
        # h = distance to nearest mouse + Manhattan distance from that mouse to exit
        # Still admissible: agent must visit at least the closest mouse, then reach the exit
        distToExit = abs(self.exit[0] - closestMouse[0]) + abs(self.exit[1] - closestMouse[1])
        return lowestDistance + distToExit

    ##### second heuristic
    def heuristicTwo(self,state):
        return        
    
    ## This would work: Distance of all the mices together if there are mices, if not we caluclate the manhatten distance to the goal
    def heuristicTotalDistance(self,state):
        if len(state.miceLocations) == 0:
            return self.manhattanDistance(state)
        return(self.distanceBetweenMice(state))
    
    def distanceBetweenMice(self,state):
        totalDistance = 0
        index = 0
        for x,y in state.miceLocations:
            if index >= len(state.miceLocations) - 1:
                return totalDistance
            else:
                totalDistance += abs(x - state.miceLocations[index + 1][0]) + abs(y - state.miceLocations[index + 1][1])
                index += 1
    
    
    def miceRemainingDistance(self, state):
        '''
        Parameters:
            state (MiceAndMeowsState): The state to check.

        Returns (float): The number of mice remaining; if 0, uses the Manhattan
            distance from the given state to the exit location.
        '''
        if len(state.miceLocations) > 0:
            return len(state.miceLocations)
        return self.manhattanDistance(state)
