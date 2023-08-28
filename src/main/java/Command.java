import java.util.HashMap;
import java.util.Map;

/**
 * An abstraction for an instruction command.
 *
 * <p>
 *     This class represents a simple command structure with name, data, parameters.
 *     It also provides convenience parser methods to automatically parse an instruction String into a command,
 *     and enum lookups for known command operation types.
 * </p>
 */
public class Command {

    /**
     * A command operation, representing an identified operation for a command.
     */
    public enum Operation {
        AddTodo,
        AddDeadline,
        AddEvent,
        Delete,

        MarkComplete,
        UnmarkComplete,

        List,
        Exit,

        Unknown
    }

    private final String name;
    private Operation type = null;
    private final String data;
    private final Map<String, String> params = new HashMap<>();

    /**
     * Constructs a command with the given properties.
     *
     * @param name The name of the command. Cannot be null.
     * @param data The data of the command. Cannot be null; supply empty string if no data.
     * @param params The parameters of the command. May be null.
     */
    public Command(String name, String data, Map<String, String> params) {
        this.name = name;
        this.data = data;

        if (params != null) {
            this.params.putAll(params);
        }

        if (name == null || data == null) {
            throw new NullPointerException("Name and data of a command cannot be null.");
        }
    }

    /**
     * Parses a string instruction as a command.
     *
     * <p>
     *     This expects the format: "commandName Some long data text, /key1 value1 /key2 value2".
     * </p>
     *
     * @param instruction The string instruction to parse.
     * @return The resulting command.
     */
    public static Command parse(String instruction) {
        String[] parts = instruction.trim().split(" ");

        // 1. The first space delimited component is our name
        String name = parts[0].trim();

        // 2. Let's build our data component.
        StringBuilder data = new StringBuilder();

        // Search up till next part with starting "/".
        // That's our data. We mark the index onwards where
        // the components are no longer data (so it is excluded).
        int dataEndIndex = 1;
        while (dataEndIndex < parts.length) {
            String part = parts[dataEndIndex];
            if (part.startsWith("/")) {
                break;
            }
            dataEndIndex++;
        }

        // Merge the data components into a string.
        for (int i = 1; i < dataEndIndex; i++) {
            if (i > 1) {
                data.append(' ');
            }
            data.append(parts[i]);
        }

        // 3. Now, we have our parameters. Let's do some fancy processing.
        Map<String, String> params = new HashMap<>();

        // We scan through each word to iteratively add the key-value pairs.
        String currParamKey = "";
        StringBuilder currParamValue = new StringBuilder();
        for (int i = dataEndIndex; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.startsWith("/")) {
                // New key value pair.
                // Check if old key exists, then add it if it does.
                if (!currParamKey.isEmpty()) {
                    params.put(currParamKey, currParamValue.toString().trim());
                }
                // Set the new key value pair.
                currParamKey = part;
                currParamValue = new StringBuilder();
            } else {
                currParamValue.append(parts[i]);
                currParamValue.append(' ');
            }
        }
        if (!currParamKey.isEmpty()) {
            params.put(currParamKey, currParamValue.toString().trim());
        }

        // 4. Now we are done! Construct and return the result.
        return new Command(name, data.toString(), params);
    }

    /**
     * Parses a string instruction's command name.
     *
     * <p>
     *     This expects the format: "commandName Some long data text, /key1 value1 /key2 value2".
     *     It only returns the commandName component, and does not attempt to read anything beyond that.
     * </p>
     *
     * @param instruction The string instruction to parse.
     * @return The command name retrieved as a non-null string.
     */
    public static String parseCommandName(String instruction) {
        String[] parts = instruction.split(" ", 2);
        return parts[0];
    }

    /**
     * Obtains the name of the given command.
     *
     * @return The name of the given command as a non-null string.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtains the operation of the given command by comparing the name against one of the standard operations.
     *
     * @return The operation of the given command.
     */
    public Operation getOperation() {
        if (this.type != null) {
            return this.type;
        }

        switch (this.name) {
            case "todo": return this.type = Operation.AddTodo;
            case "event": return this.type = Operation.AddEvent;
            case "deadline": return this.type = Operation.AddDeadline;

            case "delete": return this.type = Operation.Delete;

            case "mark": return this.type = Operation.MarkComplete;
            case "unmark": return this.type = Operation.UnmarkComplete;

            case "list": return this.type = Operation.List;

            case "bye":
            case "exit":
                return this.type = Operation.Exit;
        }

        return this.type = Operation.Unknown;
    }

    /**
     * Obtains the data of the given command.
     *
     * @return The data of the given command as a non-null string.
     */
    public String getData() {
        return this.data;
    }

    /**
     * Obtains the value of the given parameter in the command.
     *
     * @param key the parameter, also known as the key.
     * @return The value of this param, or null if unset.
     */
    public String getParam(String key) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        return this.params.get(key);
    }

    /**
     * Checks whether the given parameter was initialised.
     *
     * @param key the parameter, also known as the key.
     * @return `true` if it was initialised, `false` otherwise.
     */
    public boolean hasParam(String key) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        return this.params.containsKey(key);
    }


    /**
     * Checks whether the given parameter has any <i>useful</i> value.
     *
     * @param key the parameter, also known as the key.
     * @return `true` if it has any non-empty, non-whitespace value, `false` otherwise.
     */
    public boolean hasParamWithUsefulValue(String key) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        String value = this.params.get(key);
        if (value == null) {
            value = "";
        }
        return !value.isBlank();
    }
}
