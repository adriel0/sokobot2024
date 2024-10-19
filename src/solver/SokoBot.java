package solver;
import java.util.*;
public class SokoBot {
  public List<int[]> walls = new ArrayList<>();
  public List<int[]> boxes = new ArrayList<>();
  public List<int[]> goals = new ArrayList<>();
  public int[] player;
  public List<int[]> frontier = new ArrayList<>();
  public List<int[]> explored = new ArrayList<>();
  public char[] actions = {'l', 'r', 'u', 'd'};

  
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


  public boolean isEndState(){
    for (int i = 0; i < boxes.size(); i++){
      if (!goals.contains(boxes.get(i))){
        return false;
      }
    }

    return true;
  }

public int heuristicFunction(int[] playerLocation, List<int[]> boxLocations){
  int estimatedDistance = 0;
  List<int[]> unfinishedBoxes = new ArrayList<>();
  List<int[]> unfinishedGoals = new ArrayList<>();

  Collections.copy(unfinishedGoals, goals);

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

  for (int j = 0; j < unfinishedBoxes.size(); j++){
    estimatedDistance += Math.abs(unfinishedBoxes.get(j)[0] - unfinishedGoals.get(j)[0]) +
                         Math.abs(unfinishedBoxes.get(j)[1] - unfinishedGoals.get(j)[1]);
  }

  return estimatedDistance;
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
    
    try {
      Thread.sleep(3000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

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

    // actual algorithm
    Collections.copy(frontier, boxes); //copy box locations into frontier
    while (!frontier.isEmpty()){
      int[] node = frontier.remove(0);


      for (int i = 0; i < 4; i++){
        char action = actions[i];



        int [] nextNode = performAction(node, action);
        if (walls.contains(nextNode) || boxes.contains(nextNode))){
          //invalid move so prune
        } else {
          //valid move
        }


      }
    }

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}
