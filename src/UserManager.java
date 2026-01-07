import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class UserManager {

    private Path baseDir = Paths.get("data");
    private Path usersFile = baseDir.resolve("users.txt");

    public UserManager() {
        try {
            Files.createDirectories(baseDir);
            if (!Files.exists(usersFile)) Files.writeString(usersFile, "", StandardCharsets.UTF_8);

            // Admin sicherstellen
            Map<String, String> map = readAll();
            if (!map.containsKey("Admin")) {
                map.put("Admin", "Admin1234!");
                writeAll(map);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean register(String user, String pass) {
        try {
            Map<String, String> map = readAll();
            if (map.containsKey(user)) return false;
            map.put(user, pass);
            writeAll(map);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean login(String user, String pass) {
        try {
            Map<String, String> map = readAll();
            if (!map.containsKey(user)) return false;
            return pass.equals(map.get(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listUsers() {
        try {
            Map<String, String> map = readAll();
            List<String> users = new ArrayList<String>(map.keySet());
            Collections.sort(users, String.CASE_INSENSITIVE_ORDER);
            return users;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteUser(String user) {
        if ("Admin".equals(user)) return false; // Admin nicht löschbar
        try {
            Map<String, String> map = readAll();
            if (!map.containsKey(user)) return false;
            map.remove(user);
            writeAll(map);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> readAll() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.length() == 0) continue;
            String[] p = line.split(":", 2);
            if (p.length == 2) map.put(p[0], p[1]);
        }
        return map;
    }

    private void writeAll(Map<String, String> map) throws IOException {
        List<String> users = new ArrayList<String>(map.keySet());
        Collections.sort(users, String.CASE_INSENSITIVE_ORDER);

        List<String> out = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            String u = users.get(i);
            out.add(u + ":" + map.get(u));
        }

        Files.write(usersFile, out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
