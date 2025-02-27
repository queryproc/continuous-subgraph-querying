package ca.waterloo.dsg.graphflow.storage;

import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores a mapping of {@code String} keys to {@code short} keys and vice versa. Each new
 * {@code String} key inserted gets a consecutively increasing short key starting from 0.
 * If more then {@link Short#MAX_VALUE} keys are inserted, an error is thrown.
 */
public class KeyStore {

    private static Logger logger = LogManager.getLogger(KeyStore.class);

    public static short ANY = -1;

    private short nextTypeKey = 0;
    private short nextLabelKey = 0;
    public Map<String, Short> stringToShortTypeKeyMap = new HashMap<>();
    public Map<String, Short> stringToShortLabelKeyMap = new HashMap<>();

    public short getNextLabelKey() {
        return nextLabelKey;
    }

    public short getNextTypeKey() {
        return nextTypeKey;
    }

    /**
     * Constructs a {@link KeyStore} object.
     */
    public KeyStore() {}

    /**
     * Constructs a {@link KeyStore} object.
     *
     * @param stringToShortTypeKeyMap is the user defined to internal type key map.
     * @param stringToShortLabelKeyMap is the user defined to internal label key map.
     * @param nextTypeKey is the next internal type key to assign.
     * @param nextLabelKey is the next internal label key to assign.
     */
    public KeyStore(Map<String, Short> stringToShortTypeKeyMap,
        Map<String, Short> stringToShortLabelKeyMap, short nextTypeKey, short nextLabelKey) {
        this.stringToShortTypeKeyMap = stringToShortTypeKeyMap;
        this.stringToShortLabelKeyMap = stringToShortLabelKeyMap;
        this.nextTypeKey = nextTypeKey;
        this.nextLabelKey = nextLabelKey;
    }

    /**
     * Get the type key as short.
     *
     * @param key is the {@code String} key to get a mapping of.
     */
    public short getTypeKeyAsShort(String key) {
        if (null == stringToShortTypeKeyMap.get(key)) {
            throw new IllegalArgumentException("Type " + key + " does not exist in the database.");
        }
        return stringToShortTypeKeyMap.get(key);
    }

    /**
     * Get the label key as short.
     *
     * @param key is the {@code String} key to get a mapping of.
     */
    public short getLabelKeyAsShort(String key) {
        if (null == stringToShortLabelKeyMap.get(key)) {
            throw new IllegalArgumentException("Label " + key + " does not exist in the database.");
        }
        return stringToShortLabelKeyMap.get(key);
    }

    /**
     * Insert the type key if it has not been inserted before.
     *
     * @param key is the {@code String} type key to insert.
     */
    short insertTypeKeyIfNeeded(String key) {
        return insertKey(key, nextTypeKey, stringToShortTypeKeyMap);
    }

    /**
     * Insert the label key if it has not been inserted before.
     *
     * @param key is the {@code String} label key to insert.
     */
    short insertLabelKeyIfNeeded(String key) {
        return insertKey(key, nextLabelKey, stringToShortLabelKeyMap);
    }

    private short insertKey(String key, short nextKey, Map<String, Short> stringToShortKeyMap) {
        if (stringToShortKeyMap.containsKey(key)) {
            // logger.info("The key " + key + " has already been inserted.");
            return stringToShortKeyMap.get(key);
        }
        if (nextKey < 0) {
            logger.error("Max number of keys inserted.");
            throw new IllegalArgumentException("Max number of keys inserted.");
        }
        stringToShortKeyMap.put(key, nextKey);
        if (stringToShortKeyMap.equals(stringToShortTypeKeyMap)) {
            return nextTypeKey++;
        } else { // .equals(stringToShortLabelKeyMap)
            return nextLabelKey++;
        }
    }

    /**
     * Constructs a {@link KeyStore} object from binary serialized data.
     *
     * @param directoryPath is the directory to deserialize binary data.
     * @return the constructed graph object.
     * @throws IOException if stream to file cannot be written to or closed.
     * @throws ClassNotFoundException if the object read is from input stream is not found.
     */
    @SuppressWarnings("unchecked") // casting.
    public static KeyStore deserialize(String directoryPath) throws IOException,
        ClassNotFoundException {
        var stringToShortTypeKeyMap = (HashMap<String, Short>) IOUtils.deserializeObj(
            directoryPath + "TypesKeyMap");
        var stringToShortLabelKeyMap = (HashMap<String, Short>) IOUtils.deserializeObj(
            directoryPath + "LabelsKeyMap");
        var nextTypeKey = (short) IOUtils.deserializeObj(directoryPath + "nextTypeKey");
        var nextLabelKey = (short) IOUtils.deserializeObj(directoryPath + "nextLabelKey");
        return new KeyStore(stringToShortTypeKeyMap, stringToShortLabelKeyMap,
            nextTypeKey, nextLabelKey);
    }

    /**
     * Serializes the store by persisting different fields into different files.
     *
     * @param directoryPath is the directory to which the store's serialized objects are persisted.
     * @throws IOException if stream to file cannot be written to or closed.
     */
    public void serialize(String directoryPath) throws IOException {
        logger.info("Serializing the types and labels key store.");
        IOUtils.serializeObjs(directoryPath, new Object[] {
            /* <filename , field to serialize> pair */
            "TypesKeyMap", stringToShortTypeKeyMap,
            "LabelsKeyMap", stringToShortLabelKeyMap,
            "nextTypeKey", nextTypeKey,
            "nextLabelKey", nextLabelKey
        });
    }
}
