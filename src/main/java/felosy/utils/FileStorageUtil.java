package felosy.utils;

import java.io.*;

/**
 * Utility class for storing and retrieving objects from persistent storage.
 */
public class FileStorageUtil {

    // Directory for storing application data
    private static final String DATA_DIR = "data";

    /**
     * Saves an object to persistent storage.
     *
     * @param object The object to save
     * @param filename The filename to save the object to
     * @throws IOException if an I/O error occurs
     */
    public static void saveObject(Object object, String filename) throws IOException {
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File file = new File(dataDir, filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(object);
        } catch (IOException e) {
            System.err.println("Error saving object to " + filename + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Loads an object from persistent storage.
     *
     * @param filename The filename to load the object from
     * @return The loaded object, or null if the file doesn't exist or an error occurs
     */
    public static Object loadObject(String filename) {
        File file = new File(DATA_DIR, filename);

        // Check if file exists
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading object from " + filename + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a file from persistent storage.
     *
     * @param filename The filename to delete
     * @return true if the file was deleted, false otherwise
     */
    public static boolean deleteFile(String filename) {
        File file = new File(DATA_DIR, filename);
        return file.delete();
    }

    /**
     * Checks if a file exists in persistent storage.
     *
     * @param filename The filename to check
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(String filename) {
        File file = new File(DATA_DIR, filename);
        return file.exists();
    }
}