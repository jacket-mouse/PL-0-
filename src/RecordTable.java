import java.util.*;

public class RecordTable {
    public Map<String, Object> table = new HashMap<>();

    // var procedure
    public RecordTable(String name, TokenType kind, Integer level, Integer address) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("level", level); // 层次差/偏移地址
        table.put("address", address);
    }
    // const
    public RecordTable(String name, Integer value, TokenType kind) {
        table.put("name", name);
        table.put("kind", kind);
        table.put("value", value);
    }

}
