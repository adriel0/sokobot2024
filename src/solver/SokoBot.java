package solver;
import java.util.*;
import java.util.stream.*;

class State {
  public int[] playerLocation = new int[2];
  public List<int[]> boxLocations = new ArrayList<>();
  public char[][] boxmap; 
  public String actions = "";
  public int cost = 0;
  public int heuristic;
  public String generatekey(){
    String temp = "";
    for (int[] b : boxLocations) {
      temp += Integer.toString(b[0]) + Integer.toString(b[1]);
    }
    temp += Integer.toString(playerLocation[0]) + Integer.toString(playerLocation[1]);
    return temp;
  }
  public String generateboxkey(){
    String temp = "";
    for (int[] b : boxLocations) {
      temp += Integer.toString(b[0]) + Integer.toString(b[1]);
    }
    return temp;
  }
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
  //public List<State> explored = new ArrayList<>();
  public Set<String> explored = new HashSet<>();
  public Set<String> exploredbox = new HashSet<>();
  public Set<String> exploredboxbad = new HashSet<>();
  public char[] actions = {'l', 'r', 'u', 'd'};
  public State startState = new State();
  public char[][] mapData;
  public char[][] itemsData;
  
  public long endexecutionTime = 0;
  public long pruneexecutionTime = 0;
  public long dupeexecutionTime = 0;
  public int prunecount = 0;
  
  public void set(List<int[]> source, int[] elementToReplace, int[] newValue){
    for (int[] e : source) {
      if(e[0] == elementToReplace[0] && e[1] == elementToReplace[1]){
        source.set(source.indexOf(e),newValue);
        return;
      }
    }
    return;
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
    return boxLocations.stream().allMatch(bl->(mapData[bl[0]][bl[1]] == '.'));
  }



public int heuristicFunction(List<int[]> boxLocations, int[] playerLocation){
  int estimatedDistance = 0;
 
  for (int[] b : boxLocations) {
    int temp = Integer.MAX_VALUE;
    if(mapData[b[0]][b[1]] != '.'){
      for (int[] g : goals) {
        int dist = Math.abs(b[0] - g[0]) +
                  Math.abs(b[1] - g[1]);
        temp = Math.min(temp, dist);
      }
      estimatedDistance += temp;
      estimatedDistance += ((Math.abs(b[0] - playerLocation[0]) +
      Math.abs(b[1] - playerLocation[1]))-1)*((Math.abs(b[0] - playerLocation[0]) +
                     Math.abs(b[1] - playerLocation[1]))-1);
    }
  }
  int temp = Integer.MAX_VALUE;
  for (int[] g : goals) {
    int dist = Math.abs(playerLocation[0] - g[0]) +
                Math.abs(playerLocation[1] - g[1]);
    temp = Math.min(temp, dist);
  }
  estimatedDistance += temp*temp;
  

  return estimatedDistance;
}

  public char[][] getGrid(int[] box,char[][] boxmap){
    int x = box[0];
    int y = box[1];
    
    return new char[][]{
      new char[]{(mapData[x-1][y-1] == '#') ? 'w':boxmap[x-1][y-1], (mapData[x-1][y] == '#') ? 'w':boxmap[x-1][y], (mapData[x-1][y+1] == '#') ? 'w':boxmap[x-1][y+1]},
      new char[]{(mapData[x][y-1] == '#') ? 'w':boxmap[x][y-1], 'b', (mapData[x][y+1] == '#') ? 'w':boxmap[x][y+1]},
      new char[]{(mapData[x+1][y-1] == '#') ? 'w':boxmap[x][y-1], (mapData[x+1][y] == '#') ? 'w':boxmap[x+1][y], (mapData[x+1][y+1] == '#') ? 'w':boxmap[x+1][y+1]}};
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



  public boolean canPrune(List<int[]> boxes, char[][] boxmap){
    // List<int[]> unfinishedBoxes = new ArrayList<>();
    // for (int[] b : boxes) {
    //   if(mapData[b[0]][b[1]] != '.')
    //     unfinishedBoxes.add(b);
    // }
    // char[][] temp;
    return boxes.stream().filter(b ->mapData[b[0]][b[1]] != '.' )
    .anyMatch(b -> (mapData[b[0]-1][b[1]] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (mapData[b[0]-1][b[1]] == '#' && mapData[b[0]][b[1]+1] == '#') ||
    (mapData[b[0]+1][b[1]] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (mapData[b[0]+1][b[1]] == '#' && mapData[b[0]][b[1]+1] == '#')) ? true:
    boxes.stream().filter(b ->mapData[b[0]][b[1]] != '.' )
    .anyMatch(b -> (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && mapData[b[0]][b[1]+1] == '#') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && mapData[b[0]+1][b[1]] == '#') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && mapData[b[0]-1][b[1]] == '#') ||
    (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && mapData[b[0]+1][b[1]] == '#') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && mapData[b[0]][b[1]+1] == '#') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && mapData[b[0]-1][b[1]] == '#'))? true:
    boxes.stream().filter(b ->mapData[b[0]][b[1]] != '.' )
    .anyMatch(b -> (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && boxmap[b[0]][b[1]+1] == 'b') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && boxmap[b[0]+1][b[1]] == 'b') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && boxmap[b[0]][b[1]-1] == 'b') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && boxmap[b[0]-1][b[1]] == 'b') ||
    (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && boxmap[b[0]][b[1]-1] == 'b') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && boxmap[b[0]+1][b[1]] == 'b') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && boxmap[b[0]][b[1]+1] == 'b') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && boxmap[b[0]-1][b[1]] == 'b'))? true:
    boxes.stream().filter(b ->mapData[b[0]][b[1]] != '.' )
    .anyMatch(b -> (boxmap[b[0]-1][b[1]] == 'b' && boxmap[b[0]-1][b[1]+1] == 'b' && boxmap[b[0]][b[1]+1] == 'b') ||
    (boxmap[b[0]][b[1]+1] == 'b' && boxmap[b[0]+1][b[1]+1] == 'b' && boxmap[b[0]+1][b[1]] == 'b') ||
    (boxmap[b[0]+1][b[1]] == 'b' && boxmap[b[0]+1][b[1]-1] == 'b' && boxmap[b[0]][b[1]-1] == 'b') ||
    (boxmap[b[0]][b[1]-1] == 'b' && boxmap[b[0]-1][b[1]-1] == 'b' && boxmap[b[0]-1][b[1]] == 'b') ||
    (boxmap[b[0]-1][b[1]] == 'b' && boxmap[b[0]-1][b[1]-1] == 'b' && boxmap[b[0]][b[1]-1] == 'b') ||
    (boxmap[b[0]][b[1]-1] == 'b' && boxmap[b[0]+1][b[1]-1] == 'b' && boxmap[b[0]+1][b[1]] == 'b') ||
    (boxmap[b[0]+1][b[1]] == 'b' && boxmap[b[0]+1][b[1]+1] == 'b' && boxmap[b[0]][b[1]+1] == 'b') ||
    (boxmap[b[0]][b[1]+1] == 'b' && boxmap[b[0]-1][b[1]+1] == 'b' && boxmap[b[0]-1][b[1]] == 'b'))? true:
    boxes.stream().filter(b ->mapData[b[0]][b[1]] != '.' )
    .anyMatch(b -> (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && mapData[b[0]-1][b[1]] == '#') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && mapData[b[0]][b[1]+1] == '#') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && mapData[b[0]+1][b[1]] == '#') ||
    (boxmap[b[0]-1][b[1]] == 'b' && mapData[b[0]-1][b[1]-1] == '#' && mapData[b[0]][b[1]+1] == '#') ||
    (boxmap[b[0]][b[1]-1] == 'b' && mapData[b[0]+1][b[1]-1] == '#' && mapData[b[0]-1][b[1]] == '#') ||
    (boxmap[b[0]+1][b[1]] == 'b' && mapData[b[0]+1][b[1]+1] == '#' && mapData[b[0]][b[1]-1] == '#') ||
    (boxmap[b[0]][b[1]+1] == 'b' && mapData[b[0]-1][b[1]+1] == '#' && mapData[b[0]+1][b[1]] == '#'))? true:false;
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
    //start state will save the initial location of the player and the boxes
    startState.playerLocation = player.clone(); 
    startState.boxLocations = new ArrayList<>(boxes);
    startState.boxmap = new char[mapData.length][mapData[0].length];
    boxes.stream().forEach(b ->{startState.boxmap[b[0]][b[1]] = 'b';});
    frontier.add(startState);

    
    
    while (!frontier.isEmpty()){
      //extract one state
      State node = frontier.poll();

      //System.out.println(node.actions);
      //if all goals have boxes, game done
      //long startTime = System.nanoTime();
      if (isEndState(node.boxLocations)){

        //System.out.println("end "+ (double)endexecutionTime/1000000000.0 + "s");
        System.out.println("prune "+ (double)pruneexecutionTime/1000000000.0 + "s " + prunecount);
        //System.out.println("dupe "+ (double)dupeexecutionTime/1000000000.0 + "s");
                               
        System.out.println(node.actions);
        return node.actions;
      }
      //long endTime = System.nanoTime();
      //endexecutionTime += (endTime - startTime);

      for (char actionAttempted:actions){
        State nextNode = new State();

        int[] nextPlayerLocation = performAction(node.playerLocation, actionAttempted);

        //if the player moved into a box location, it means they attempted to push it
        if (node.boxmap[nextPlayerLocation[0]][nextPlayerLocation[1]] == 'b'){
            //check if the box can be pushed in the direction
            int[] nextBoxLocation = performAction(nextPlayerLocation, actionAttempted);
            //if box was not moved into a wall or another box, valid move
            if (node.boxmap[nextBoxLocation[0]][nextBoxLocation[1]] != 'b' && (mapData[nextBoxLocation[0]][nextBoxLocation[1]] != '#')){

            
              //startTime = System.nanoTime();
              nextNode.boxLocations = new ArrayList<>(node.boxLocations);

              set(nextNode.boxLocations, nextPlayerLocation, nextBoxLocation);
              nextNode.boxmap = new char[mapData.length][mapData[0].length];
              for (int i = 0; i < nextNode.boxmap.length; i++) {
                nextNode.boxmap[i] = node.boxmap[i].clone();
              }
              nextNode.boxmap[nextPlayerLocation[0]][nextPlayerLocation[1]] = ' ';
              nextNode.boxmap[nextBoxLocation[0]][nextBoxLocation[1]] = 'b';
              nextNode.playerLocation = nextPlayerLocation.clone();
              nextNode.actions = node.actions;
              nextNode.actions += actionAttempted;
              nextNode.cost = node.cost; //if player did move a box, we don't add a cost (TO INCENTIVIZE THE MOVES THAT MOVE A BOX)
              nextNode.heuristic = heuristicFunction(nextNode.boxLocations, nextNode.playerLocation);
              //endTime = System.nanoTime();
              //dupeexecutionTime += (endTime - startTime);

              long startTime = System.nanoTime();
              if (!explored.contains(nextNode.generatekey())){
                if(!exploredboxbad.contains(nextNode.generateboxkey())){
                  if(exploredbox.contains(nextNode.generateboxkey())){
                    frontier.add(nextNode);
                    explored.add(nextNode.generatekey());
                  }
                  else if(!canPrune(nextNode.boxLocations, nextNode.boxmap)){
                    frontier.add(nextNode);
                    explored.add(nextNode.generatekey());
                    exploredbox.add(nextNode.generateboxkey());
                  }
                  else{
                    exploredboxbad.add(nextNode.generateboxkey());
                  }
                }
              }
          // prunecount++;
          // if (!explored.contains(nextNode.generatekey()) && !canPrune(nextNode.boxLocations, nextNode.boxmap)){
          //   frontier.add(nextNode);
          //   explored.add(nextNode.generatekey());
          // }
              long endTime = System.nanoTime();
              pruneexecutionTime += (endTime - startTime);
            }

        //this part means that the player did not move into a location of a box or a wall, so he just moved into an empty space
        } else if ((mapData[nextPlayerLocation[0]][nextPlayerLocation[1]] != '#')) {
          //startTime = System.nanoTime();
          nextNode.boxLocations = node.boxLocations;
          //nextNode.boxLocations = new ArrayList<>(node.boxLocations);
          nextNode.boxmap = node.boxmap;
          //nextNode.boxmap = new char[mapData.length][mapData[0].length];
          //for (int i = 0; i < nextNode.boxmap.length; i++) {
            //nextNode.boxmap[i] = node.boxmap[i].clone();
          //}
          nextNode.playerLocation = nextPlayerLocation.clone();
          nextNode.actions = node.actions;
          nextNode.actions += actionAttempted;
          nextNode.cost = node.cost + 1; //if player did not move a box, add 1 to cost
          nextNode.heuristic = heuristicFunction(nextNode.boxLocations, nextNode.playerLocation);
          //endTime = System.nanoTime();
          //dupeexecutionTime += (endTime - startTime);
          long startTime = System.nanoTime();
          if (!explored.contains(nextNode.generatekey())){
            if(!exploredboxbad.contains(nextNode.generateboxkey())){
              if(exploredbox.contains(nextNode.generateboxkey())){
                frontier.add(nextNode);
                explored.add(nextNode.generatekey());
              }
              else if(!canPrune(nextNode.boxLocations, nextNode.boxmap)){
                frontier.add(nextNode);
                explored.add(nextNode.generatekey());
                exploredbox.add(nextNode.generateboxkey());
              }
              else{
                exploredboxbad.add(nextNode.generateboxkey());
              }
            }
          }
          //prunecount++;
          // if (!explored.contains(nextNode.generatekey()) && !canPrune(nextNode.boxLocations, nextNode.boxmap)){
          //   frontier.add(nextNode);
          //   explored.add(nextNode.generatekey());
          // }
          long endTime = System.nanoTime();
          pruneexecutionTime += (endTime - startTime);
        }


      }
    }

    return "lrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlrlr";
  }

}
