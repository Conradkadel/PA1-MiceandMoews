package csc460.searchstrategies;

import csc460.SearchNode;
import csc460.searchproblems.SearchProblem;

/**
 * A simple interface for search strategies.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public interface SearchStrategy {
    public void init(SearchProblem searchProblem);
    public SearchNode nextNode();
    public int getNumStatesExpanded();
    public int getMaxFringeSize();
}


