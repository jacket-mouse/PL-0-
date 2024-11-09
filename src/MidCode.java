import java.util.*;

public class MidCode {
    Map<String, Object> table = new Hashtable<>();

    public MidCode(InstrucType f, int l, int a) {
        table.put("F", f);
        table.put("L", l);
        table.put("A", a);
    }
}
