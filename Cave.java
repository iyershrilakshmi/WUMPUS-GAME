import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class Cave {
    private HashMap<Integer, ArrayList<Integer>> roomConnections;

    public Cave() {
        roomConnections = new HashMap<>();
      
        // This initializes connections for all 30 rooms with empty sets
        for (int i = 1; i <= 30; i++) {
            roomConnections.put(i, new ArrayList<>());
        }
    }

    public void readCaveData(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int room = Integer.parseInt(parts[0]);
                ArrayList<Integer> connections = roomConnections.get(room);
                for (int i = 1; i < parts.length; i++) {
                    int connectedRoom = Integer.parseInt(parts[i]);
                    if (!connections.contains(connectedRoom)) {
                        connections.add(connectedRoom);
                    }
                    if (!roomConnections.get(connectedRoom).contains(room)) {
                        roomConnections.get(connectedRoom).add(room);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the cave data file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing room numbers: " + e.getMessage());
        }
    }

    public ArrayList<Integer> getAdjacentRooms(int room) {
        return roomConnections.getOrDefault(room, new ArrayList<>());
    }
}


