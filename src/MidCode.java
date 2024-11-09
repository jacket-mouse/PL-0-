import java.util.*;

public class MidCode {
    Map<String, Object> table = new Hashtable<>();

    public MidCode(InstrucType f, int l, int a) {
        table.put("F", f); // 功能
        table.put("L", l); // 层次差
        table.put("A", a); // 数值/地址
    }
}
