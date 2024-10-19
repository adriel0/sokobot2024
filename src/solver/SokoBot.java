package solver;
import java.util.*;

class State {
  public int[] playerLocation = new int[2];
  public List<int[]> boxLocations = new ArrayList<>();
  public String actions = "";
  public int cost = 0;
  public int heuristic;
}

class StateComparator implements Comparator<State>{
             
  public int compare(State s1, State s2) {
      if (s1.cost+s1.heuristic > s2.cost+s2.heuristic)
          return 1;
      else if (s1.cost+s1.heuristic < s2.cost+s2.heuristic)
          return -1;
      return 0;
      }
}


public class SokoBot {
  public List<int[]> walls = new ArrayList<>();
  public List<int[]> boxes = new ArrayList<>();
  public List<int[]> goals = new ArrayList<>();
  public int[] player;
  PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator());
  public List<State> explored = new ArrayList<>();
  public char[] actions = {'l', 'r', 'u', 'd'};
  public State startState;

  
  public int[] performAction(int[] location, char action){
    switch(action){
      case 'l':
        location[0] = location[0] - 1;
        break;
      case 'r':
        location[0] = location[0] + 1;
        break;
      case 'u':
        location[1] = location[1] + 1;
        break;
      case 'd':
        location[1] = location[1] - 1;
        break;
    }

    return location;
  }


  public boolean isEndState(List<int[]> boxLocations){
    for (int i = 0; i < boxLocations.size(); i++){
      if (!goals.contains(boxLocations.get(i))){
        return false;
      }
    }

    return true;
  }



public int heuristicFunction(List<int[]> boxLocations){
  int estimatedDistance = 0;
  List<int[]> unfinishedBoxes = new ArrayList<>();
  List<int[]> unfinishedGoals = new ArrayList<>();

  for (int[] goalLocations: goals) {
    unfinishedGoals.add(goalLocations.clone());
  }

  //for each box
  for (int i = 0; i < boxLocations.size(); i++){

    //if a goal position already has a box, remove it from unfinishedGoals
    if (goals.contains(boxLocations.get(i))){
      unfinishedGoals.remove(unfinishedGoals.indexOf(boxLocations.get(i)));

    //else add the box into unfinishedBoxes
    } else {
      unfinishedBoxes.add(boxLocations.get(i));
    }

  }
  //================================================================
  // maybe optimize this by sorting the boxes to their closest goals

  for (int j = 0; j < unfinishedBoxes.size(); j++){
    estimatedDistance += Math.abs(unfinishedBoxes.get(j)[0] - unfinishedGoals.get(j)[0]) +
                         Math.abs(unfinishedBoxes.get(j)[1] - unfinishedGoals.get(j)[1]);
  }

  return estimatedDistance;
}

  public boolean isExplored(State state){
    for (State exploredState : explored) {
      //if the state's player location AND box locations are exactly the same with one of the states in the explored list, then return true
      if (exploredState.playerLocation.equals(state.playerLocation) && exploredState.boxLocations.equals(state.boxLocations)){
        return true;
      }
    }
    return false;
  }

  
  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    /*
     * YOU NEED TO REWRITE THE IMPLEMENTATION OF THIS METHOD TO MAKE THE BOT SMARTER
     */
    /*
     * Default stupid behavior: Think (sleep) for 3 seconds, and then return a
     * sequence
     * that just moves left and right repeatedly.
     */
    

    // notes the location of each object in the map and saves them into their respective lists
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (mapData[i][j] == '#')
          walls.add(new int[]{i,j});
        else {
          if (mapData[i][j] == '.')
            goals.add(new int[]{i,j});
          if (itemsData[i][j] == '$')
            boxes.add(new int[]{i,j});
          else if (itemsData[i][j] == '@')
            player = new int[]{i,j};
          }
      }
    }

    //start state will save the initial location of the player and the boxes
    startState.playerLocation = player.clone(); 
    for (int[] locations: boxes) {
      startState.boxLocations.add(locations.clone());
    }
    
    frontier.add(startState);

    
    
    while (!frontier.isEmpty()){

      //extract one state
      State node = frontier.poll();

      //if all goals have boxes, game done
      if (isEndState(node.boxLocations)){
        return node.actions;
      }

      //attempt to perform all actions and see which ones are valid or invalid
      for (int i = 0; i < 4; i++){
        State nextNode = new State();
        char actionAttempted = actions[i];

        int[] nextPlayerLocation = performAction(node.playerLocation, actionAttempted);

        //if the player moved into a box location, it means they attempted to push it
        if (node.boxLocations.contains(nextPlayerLocation)){
            //check if the box can be pushed in the direction
            int[] nextBoxLocation = performAction(nextPlayerLocation, actionAttempted);
            //if box was not moved into a wall or another box, valid move
            if (!node.boxLocations.contains(nextBoxLocation) && !walls.contains(nextBoxLocation)){

              for (int[] box: node.boxLocations) {
                nextNode.boxLocations.add(box.clone());
              }
              nextNode.boxLocations.set(nextNode.boxLocations.indexOf(nextPlayerLocation), nextBoxLocation);
              nextNode.playerLocation = nextPlayerLocation.clone();
              nextNode.actions = node.actions;
              nextNode.actions += actionAttempted;
              nextNode.cost = node.cost; //if player did move a box, we don't add a cost (TO INCENTIVIZE THE MOVES THAT MOVE A BOX)
              nextNode.heuristic = heuristicFunction(nextNode.boxLocations);
              //if not explored yet, we add the next state to frontier and explored list
              if (!isExplored(nextNode)){
                frontier.add(nextNode);
                explored.add(nextNode);
              }

            }

        //this part means that the player did not move into a location of a box or a wall, so he just moved into an empty space
        } else if (!walls.contains(nextPlayerLocation)) {
          for (int[] box: node.boxLocations) {
            nextNode.boxLocations.add(box.clone());
          }
          nextNode.playerLocation = nextPlayerLocation.clone();
          nextNode.actions = node.actions;
          nextNode.actions += actionAttempted;
          nextNode.cost = node.cost + 1; //if player did not move a box, add 1 to cost
          nextNode.heuristic = heuristicFunction(nextNode.boxLocations);
          if (!isExplored(nextNode)){
            frontier.add(nextNode);
            explored.add(nextNode);
          }
        }


      }
    }

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}
