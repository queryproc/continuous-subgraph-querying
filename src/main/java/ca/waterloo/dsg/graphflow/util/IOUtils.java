package ca.waterloo.dsg.graphflow.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utils for Input, Output (I/O) and time operations.
 */
public class IOUtils {

    /**
     * Calculates the time difference between now and the given {@code beginTime}.
     *
     * @param beginTime The being time in nanoseconds.
     * @return Time difference in milliseconds.
     */
    public static double getElapsedTimeInMillis(long beginTime) {
        return (System.nanoTime() - beginTime) / 1000000.0;
    }

    public static void log(String filename, String output) throws IOException {
        var output_split = filename.split("/");
        var strBuilder = new StringBuilder();
        for (int i = 0; i < output_split.length - 1; i++) {
            strBuilder.append(output_split[i]);
            strBuilder.append("/");
        }
        IOUtils.mkdirs(strBuilder.toString());
        IOUtils.createNewFile(filename);
        var writer = new FileWriter(filename, true /* append to the file */);
        writer.write(output);
        writer.flush();
    }

    /**
     * @see File#mkdir()
     */
    public static void mkdirs(String directoryPath) throws IOException {
        var file = new File(directoryPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("The directory " + directoryPath + " was not created.");
            }
        }
    }

    /**
     * @see File#createNewFile()
     */
    public static void createNewFile(String filePath) throws IOException {
        var file = new File(filePath);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("The file " + filePath + " was not created.");
            }
        }
    }

    public static void serializeObj(String file, Object object) throws IOException {
        var stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        stream.writeObject(object);
        stream.close();
    }

    public static void serializeObjs(String directory, Object[] filenameObjectPairs)
        throws IOException {
        for (var i = 0; i < filenameObjectPairs.length; i += 2) {
            serializeObj(directory + filenameObjectPairs[i], filenameObjectPairs[i + 1]);
        }
    }

    public static Object deserializeObj(String file) throws IOException, ClassNotFoundException {
        var stream =  new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        var object = stream.readObject();
        stream.close();
        return object;
    }
}
