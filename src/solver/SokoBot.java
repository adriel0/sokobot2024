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
  public State startState = new State();
  public char[][] mapData;
  public char[][] itemsData;

  public void set(List<int[]> source, int[] elementToReplace, int[] newValue){
    for (int[] e : source) {
      if(e[0] == elementToReplace[0] && e[1] == elementToReplace[1]){
        source.set(source.indexOf(e),newValue);
        return;
      }
    }
    return;
  }
  public void remove(List<int[]> source, int[] elementToRemove){
    for (int[] e : source) {
      if(e[0] == elementToRemove[0] && e[1] == elementToRemove[1]){
        source.remove(source.indexOf(e));
        return;
      }
    }
  }
  public boolean equals(int[] source, int[] elementToCheck){
      if(source[0] == elementToCheck[0] && source[1] == elementToCheck[1])
        return true;

    return false;
  }
  public boolean contains(List<int[]> source, int[] elementToCheck){
    for (int[] e : source) {
      if(e[0] == elementToCheck[0] && e[1] == elementToCheck[1])
        return true;
    }

    return false;
  }
  public boolean containsall(List<int[]> source, List<int[]> elementsToCheck){
    for (int[] ec : elementsToCheck) {
      if(!contains(source, ec))
        return false;
    }
    return true;
  }
  public int[] performAction(int[] location, char action){
    switch(action){
      case 'l':
        return new int[]{location[0],location[1] - 1};
      case 'r':
        return new int[]{location[0],location[1] + 1};
      case 'u':
        return new int[]{location[0] - 1,location[1]};
      default:
        return new int[]{location[0] + 1,location[1]};
    }
  }


  public boolean isEndState(List<int[]> boxLocations){

    for (int[] bl : boxLocations) {
      if (!contains(goals,bl)){
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
    if (contains(goals,boxLocations.get(i))){
      remove(unfinishedGoals,boxLocations.get(i));

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
      if (equals(exploredState.playerLocation,state.playerLocation) && containsall(exploredState.boxLocations,state.boxLocations)){
        // System.out.println(exploredState.playerLocation[0]+","+exploredState.playerLocation[1]);
        // for (int[] boxes: exploredState.boxLocations) {
        //   System.out.println(boxes[0]+","+boxes[1]);
        // }
        //   System.out.println(" ");
        //   System.out.println(state.playerLocation[0]+","+state.playerLocation[1]);
        // for (int[] boxes: state.boxLocations) {
        //   System.out.println(boxes[0]+","+boxes[1]);
        // }
        return true;
      }
    }
    return false;
  }

  public char[][] getGrid(int[] box,List<int[]> boxes){
    int x = box[0];
    int y = box[1];
    return new char[][]{
      new char[]{contains(walls, new int[]{x-1,y-1}) ? 'w':(contains(boxes, new int[]{x-1,y-1}) ? 'b':' '), contains(walls, new int[]{x-1,y}) ? 'w':(contains(boxes, new int[]{x-1,y}) ? 'b':' '),contains(walls, new int[]{x-1,y+1}) ? 'w':(contains(boxes, new int[]{x-1,y+1}) ? 'b':' ')},
      new char[]{contains(walls, new int[]{x,y-1}) ? 'w':(contains(boxes, new int[]{x,y-1}) ? 'b':' '), 'b', contains(walls, new int[]{x,y+1}) ? 'w':(contains(boxes, new int[]{x,y+1}) ? 'b':' ')},
      new char[]{contains(walls, new int[]{x+1,y-1}) ? 'w':(contains(boxes, new int[]{x+1,y-1}) ? 'b':' '), contains(walls, new int[]{x+1,y}) ? 'w':(contains(boxes, new int[]{x+1,y}) ? 'b':' '),contains(walls, new int[]{x+1,y+1}) ? 'w':(contains(boxes, new int[]{x+1,y+1}) ? 'b':' ')}};
  }
  public char[][] rotate(char[][] grid) {
      char[][] temp = new char[3][3];
      for (int r = 0; r < 3; r++) {
          for (int c = 0; c < 3; c++) {
            temp[r][c] = grid[c][2-r];
          }
      }
      return temp;
  }
  public char[][] flip(char[][] grid) {
    char[][] temp = new char[3][3];
    for (int r = 0; r < 3; r++) {
        for (int c = 0; c < 3; c++) {
          temp[r][c] = grid[r][2-c];
        }
    }
    return temp;
}



  public boolean canPrune(List<int[]> boxes){
    List<int[]> unfinishedBoxes = new ArrayList<>();
    for (int[] b : boxes) {
      if(!contains(goals, b))
        unfinishedBoxes.add(b);
    }
    char[][] temp;
    for (int[] b : unfinishedBoxes) {
      temp = getGrid(b, boxes);
      for (int index = 0; index < 4; index++) {
        if ((temp[0][1] == 'w' && temp[1][2] == 'w') ||
        (temp[0][1] == 'b' && temp[0][2] == 'w' && temp[1][2] == 'w') ||
        (temp[0][1] == 'b' && temp[0][2] == 'w' && temp[1][2] == 'b') ||
        (temp[0][1] == 'b' && temp[0][2] == 'b' && temp[1][2] == 'b') ||
        (temp[0][1] == 'b' && temp[0][2] == 'w' && temp[1][0] == 'w'))
          return true;
        char[][] temp2 = new char[3][3];
        temp2[0] = temp[0].clone();
        temp2[1] = temp[1].clone();
        temp2[2] = temp[2].clone();
        temp2 = flip(temp2);
        if ((temp2[0][1] == 'w' && temp2[1][2] == 'w') ||
        (temp2[0][1] == 'b' && temp2[0][2] == 'w' && temp2[1][2] == 'w') ||
        (temp2[0][1] == 'b' && temp2[0][2] == 'w' && temp2[1][2] == 'b') ||
        (temp2[0][1] == 'b' && temp2[0][2] == 'b' && temp2[1][2] == 'b') ||
        (temp2[0][1] == 'b' && temp2[0][2] == 'w' && temp2[1][0] == 'w'))
          return true;
        temp = rotate(temp);
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
    this.mapData = mapData;
    this.itemsData = itemsData;
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
    explored = new ArrayList<>();
    //start state will save the initial location of the player and the boxes
    startState.playerLocation = player.clone(); 
    for (int[] locations: boxes) {
      startState.boxLocations.add(locations.clone());
    }
    
    frontier.add(startState);

    
    
    while (!frontier.isEmpty()){
      //extract one state
      State node = frontier.poll();

      //System.out.println(node.actions);
      //if all goals have boxes, game done
      if (isEndState(node.boxLocations)){
        System.out.println(node.actions);
        return node.actions;
      }

      //attempt to perform all actions and see which ones are valid or invalid
      for (int i = 0; i < 4; i++){
        State nextNode = new State();
        char actionAttempted = actions[i];

        int[] nextPlayerLocation = performAction(node.playerLocation, actionAttempted);

        //if the player moved into a box location, it means they attempted to push it
        if (contains(node.boxLocations,nextPlayerLocation)){
            //check if the box can be pushed in the direction
            int[] nextBoxLocation = performAction(nextPlayerLocation, actionAttempted);
            //if box was not moved into a wall or another box, valid move
            if (!contains(node.boxLocations,nextBoxLocation) && !contains(walls,nextBoxLocation)){

              for (int[] box: node.boxLocations) {
                nextNode.boxLocations.add(box.clone());
              }
              set(nextNode.boxLocations, nextPlayerLocation, nextBoxLocation);
              nextNode.playerLocation = nextPlayerLocation.clone();
              nextNode.actions = node.actions;
              nextNode.actions += actionAttempted;
              nextNode.cost = node.cost; //if player did move a box, we don't add a cost (TO INCENTIVIZE THE MOVES THAT MOVE A BOX)
              nextNode.heuristic = heuristicFunction(nextNode.boxLocations);
              //if not explored yet, we add the next state to frontier and explored list
              //System.out.println("checking: "+actionAttempted);
              if (!isExplored(nextNode) && !canPrune(nextNode.boxLocations)){
                frontier.add(nextNode);
                explored.add(nextNode);
              }

            }

        //this part means that the player did not move into a location of a box or a wall, so he just moved into an empty space
        } else if (!contains(walls,nextPlayerLocation)) {
          for (int[] box: node.boxLocations) {
            nextNode.boxLocations.add(box.clone());
          }
          nextNode.playerLocation = nextPlayerLocation.clone();
          nextNode.actions = node.actions;
          nextNode.actions += actionAttempted;
          nextNode.cost = node.cost + 1; //if player did not move a box, add 1 to cost
          nextNode.heuristic = heuristicFunction(nextNode.boxLocations);
          //System.out.println("checking: "+actionAttempted);
          if (!isExplored(nextNode) && !canPrune(nextNode.boxLocations)){
            frontier.add(nextNode);
            explored.add(nextNode);
          }
        }


      }
    }

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}
