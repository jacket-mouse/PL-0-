import java.util.*;

public class RecordTable {
    public Map<String, Object> table = new HashMap<>();

    // procedure
    public RecordTable(String name, TokenType kind, Integer level) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("level", level); // 层次差/偏移地址
    }
    // var
    public RecordTable(String name, TokenType kind, Integer level, Integer address) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("level", level); // 层次差/偏移地址
        table.put("address", address);
    }
    // const
    public RecordTable(String name, TokenType kind, Object value) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("value", value);
    }

}
