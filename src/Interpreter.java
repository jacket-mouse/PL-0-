import java.util.List;

public class Interpreter {
    private int[] stack = new int[8000];
    private Parser parser;
    private int B = 0; // 基址寄存器
    private int T = 0; // 栈顶寄存器
    private int P = 0; // 存放要执行的下一条指令的下标
    private MidCode code; // 存放要执行的代码
    private final List<MidCode> codeList;

    Interpreter(Parser parser) {
        this.parser = parser;
        codeList = parser.getMidcodeTable();
//        interpreter();
    }

    private int get_sl(int B, int lever) {
        return 0;
    }

    private void interpreter() {
        code = codeList.getFirst();
        P++;
        while (P != 0) {
            if (code.table.get("F") == InstrucType.JMP) {
                P = (int) code.table.get("A");
            }else if(code.table.get("F") == InstrucType.JPC) {
                if (stack[T] == 0) P = (int) code.table.get("A");
                T -= 1;
            }else if(code.table.get("F") == InstrucType.INT) {

            }
        }
    }


}
