import java.util.*;
import java.io.*;

/* Takes a list of people and previous meals and randomly puts people into meal groups of various sizes. Prevents
 * two people from having a meal together more than once. List takes into account people who are willing to
 * initiate the meal, and assigns each group an initiator (denoted by *). */
public class MealMatcher {
     
     int size; // total number of people
     boolean[][] alreadyHadMeal; // keeps track of previous meals (true = already had meal)
     ArrayList<String> initiators; // people willing to initiate meal
     ArrayList<String> everyone; // list of all people
     Map<String, Integer> peopleVal; // stores index of each person in ArrayList everyone
     Random r; // random selection of people for groups
     
     /* Constructor */
     public MealMatcher(File file) throws FileNotFoundException {
          initiators = new ArrayList<String>();
          everyone = new ArrayList<String>();
          peopleVal = new HashMap<String, Integer>();
          r = new Random();
          
          Scanner sc = new Scanner(file);
          
          // Read list of all people (initiators marked with *)
          String name = sc.next();
          do {
               if (name.charAt(name.length() - 1) == '*') {
                    name = name.substring(0, name.length() - 1);
                    initiators.add(name);
               }
               everyone.add(name);
               peopleVal.put(name, size);
               size++;
               name = sc.next();
          } while (name.charAt(name.length() - 1) != ':');
          
          // Prevent people from being assigned meal with themselves
          alreadyHadMeal = new boolean[size][size];
          for (int i = 0; i < size; i++) {
               alreadyHadMeal[i][i] = true;
          }
          
          // Read list of previous meals had for each person
          String person1 = name.substring(0, name.length() - 1);
          for (int i = 0; i < size; i++) {
               String person2 = sc.next();
               while (sc.hasNext() && person2.charAt(person2.length() - 1) != ':') {
                    alreadyHadMeal[getVal(person1)][getVal(person2)] = true;
                    person2 = sc.next();
               }
               if (!sc.hasNext())
                    alreadyHadMeal[getVal(person1)][getVal(person2)] = true;
               person1 = person2.substring(0, person2.length() - 1);
          }
          sc.close();
     }
     
     /* Returns corresponding index of person's name */
     private int getVal(String name) {
          return peopleVal.get(name);
     }
     
     /* Returns ArrayList of random groups of people for meals of size at least groupSize. Each person must not
      * have had a meal previously. In each group, there must be at least one initiator. Recursively looks
      * for a possible solution. */
     public ArrayList<ArrayList<String>> createMeals(int groupSize) {
          int numOfGroups = size/groupSize;
          
          // Creates copy of existing instance variables
          ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>(numOfGroups);
          ArrayList<String> peopleNotPlaced = new ArrayList<String>(everyone);
          ArrayList<String> possibleInitiators = new ArrayList<String>(initiators);
          
          // Randomly assigns each group an initiator
          for (int i = 0; i < numOfGroups; i++) {
               ArrayList<String> group = new ArrayList<String>();
               String initiator = possibleInitiators.get(r.nextInt(possibleInitiators.size()));
               possibleInitiators.remove(initiator);
               peopleNotPlaced.remove(initiator);
               group.add(initiator);
               results.add(group);
          }
          
          // Adds people to groups one at a time by eliminating possibilities for randomly choosing people
          boolean success = true;
          for (int i = 0; i < groupSize; i++) {
               for (int j = 0; j < numOfGroups; j++) {
                    if (peopleNotPlaced.size() > 0) {
                         // Find all possible people to add to group
                         ArrayList<String> possible = new ArrayList<String>(peopleNotPlaced);
                         for (String k : results.get(j)) {
                              for (String l : peopleNotPlaced) {
                                   if (alreadyHadMeal[getVal(k)][getVal(l)])
                                        possible.remove(l);
                              }
                         }
                         
                         // Reached dead end: no people able to added to group
                         if (possible.size() == 0) {
                              success = false;
                              break;
                         }
                         
                         // Randomly add a person to the group
                         else {
                              String newPerson = possible.get(r.nextInt(possible.size()));
                              results.get(j).add(newPerson);
                              peopleNotPlaced.remove(newPerson);
                         }
                    }
               }
          }
          // Recursively repeat the process
          if (success == false)
               results = createMeals(groupSize);
          
          // After solution found, update alreadyHadMeal
          for (int i = 0; i < results.size(); i++) {
               for (int j = 0; j < results.get(i).size(); j++) {
                    for (int k = 0; k < results.get(i).size(); k++) {
                         alreadyHadMeal[getVal(results.get(i).get(j))][getVal(results.get(i).get(k))] = true;
                    }
               }
          }
          return results;
     }
     
     /* Test client.  */
     public static void main(String[] args) throws FileNotFoundException {
          
          // Input and output files
          File oldFile = new File("mealMatchersTest.txt");
          PrintWriter pw = new PrintWriter("mealMatchersTest-NEW.txt");
          PrintWriter pw2 = new PrintWriter("mealGroups.txt");
          
          MealMatcher mm = new MealMatcher(oldFile);
          ArrayList<ArrayList<String>> results = mm.createMeals(3);
          
          // Outputs groups to mealGroups.txt
          for (int i = 0; i < results.size(); i++) {
               for (int j = 0; j < results.get(i).size(); j++) {
                    if (j == 0)
                         pw2.println(results.get(i).get(j) + "*");
                    else 
                         pw2.println(results.get(i).get(j));
               }
               pw2.println();
          }
          
          // Outputs updated copy of input file to mealMatchersTest-NEW.txt
          for (int i = 0; i < mm.everyone.size(); i++) {
               if (mm.initiators.contains(mm.everyone.get(i)))
                    pw.println(mm.everyone.get(i) + "*");
               else
                    pw.println(mm.everyone.get(i));
          }
          for (int i = 0; i < mm.alreadyHadMeal.length; i++) {
               pw.print(mm.everyone.get(i) + ": ");
               for (int j = 0; j < mm.alreadyHadMeal[0].length; j++) {
                    if (mm.alreadyHadMeal[i][j] && i != j)
                         pw.print(mm.everyone.get(j) + " ");
               }
               pw.println();
          }
          
          pw.close();
          pw2.close();
     }
}