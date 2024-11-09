import java.util.*;

public class RecordTable {
    Map<String, Object> table = new HashMap<>();

    // var和procedure
    public RecordTable(String name, TokenType kind, Integer level, Integer address) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("level", level);
        table.put("address", address);
    }
    // const
    public RecordTable(String name, TokenType kind, Object value) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("value", value);
    }

}
