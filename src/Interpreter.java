import java.util.List;

public class Interpreter {
    private int[] stack = new int[8000]; // 全部初始化为0
    private int B = 0; // 基址寄存器
    private int T = 0; // 栈顶寄存器
    private int P = 0; // 存放要执行的下一条指令的下标
    private MidCode code; // 存放要执行的代码
    private final List<MidCode> codeList;

    Interpreter(Parser parser) {
        codeList = parser.getMidcodeTable();
        interpreter();
    }

    private int get_sl(int B, int level) {
        int res_B = B;
        while (level > 0) {
            res_B = stack[res_B];
            level--;
        }
        return res_B;
    }


    // 打印栈的内容（可视化）
    public void printStack() {
        int top = T;
        if (top == -1) {
            System.out.println("Stack is empty.");
            return;
        }

        System.out.println("Stack contents (top to bottom):");
        for (int i = top; i >= 0; i--) {
            System.out.println("| " + stack[i] + " |"); // 模拟栈的每一层
        }
        System.out.println(" ----- "); // 模拟栈底
    }

    private void interpreter() {
        System.out.println("start PL/0");
        code = codeList.getFirst();
        P++;
        while (P != 0) {
            if (code.table.get("F") == InstrucType.JMP) {
                P = (int) code.table.get("A");
            } else if (code.table.get("F") == InstrucType.JPC) {
                if (stack[T] == 0) P = (int) code.table.get("A"); // 0 is false 1 is true 1 not jump 继续执行
                T -= 1;
            } else if (code.table.get("F") == InstrucType.INT) {
                T += (int) code.table.get("A") - 1; // 栈从0开始计数，0～A，实际上多了一个，要减去
            } else if (code.table.get("F") == InstrucType.LOD) {
                T++;
                stack[T] = stack[get_sl(B, (int) code.table.get("L")) + (int) code.table.get("A")];
            } else if (code.table.get("F") == InstrucType.LIT) {
                T++;
                stack[T] = (int) code.table.get("A");
            } else if (code.table.get("F") == InstrucType.CAL) {
                T++;
                stack[T] = get_sl(B, (int) code.table.get("L"));
                stack[T + 1] = B;
                stack[T + 2] = P;
                B = T;
                P = (int) code.table.get("A");
            } else if (code.table.get("F") == InstrucType.STO) {
                stack[get_sl(B, (int) code.table.get("L")) + (int) code.table.get("A")] = stack[T];
                T--;
            } else if (code.table.get("F") == InstrucType.OPR) {
                if ((int) code.table.get("A") == 0) {
                    T = B - 1; // 回到上一层栈顶
                    P = stack[T + 3]; // RA
                    B = stack[T + 2]; // DL
                } else if ((int) code.table.get("A") == 1) {
                    stack[T] = -stack[T];
                } else if ((int) code.table.get("A") == 2) {
                    T--;
                    stack[T] = stack[T] + stack[T + 1];
                } else if ((int) code.table.get("A") == 3) {
                    T--;
                    stack[T] = stack[T] - stack[T + 1];
                } else if ((int) code.table.get("A") == 4) {
                    T--;
                    stack[T] = stack[T] * stack[T + 1];
                } else if ((int) code.table.get("A") == 5) {
                    T--;
                    stack[T] = stack[T] / stack[T + 1];
                } else if ((int) code.table.get("A") == 6) {
                    stack[T] = stack[T] % 2;
                } else if ((int) code.table.get("A") == 8) {
                    T--;
                    if (stack[T] == stack[T + 1]) {
                        stack[T] = 0;
                    } else stack[T] = 1;
                } else if ((int) code.table.get("A") == 9) {
                    T--;
                    if (stack[T] == stack[T + 1]) {
                        stack[T] = 1;
                    } else stack[T] = 0;
                } else if ((int) code.table.get("A") == 10) {
                    T--;
                    if (stack[T] < stack[T + 1]) {
                        stack[T] = 1;
                    } else stack[T] = 0;
                } else if ((int) code.table.get("A") == 11) {
                    T--;
                    if (stack[T] >= stack[T + 1]) {
                        stack[T] = 1;
                    } else stack[T] = 0;
                } else if ((int) code.table.get("A") == 12) {
                    T--;
                    if (stack[T] > stack[T + 1]) {
                        stack[T] = 1;
                    } else stack[T] = 0;
                } else if ((int) code.table.get("A") == 13) {
                    T--;
                    if (stack[T] <= stack[T + 1]) {
                        stack[T] = 1;
                    } else stack[T] = 0;
                }
            }
            printStack();
            code = codeList.get(P);
            if (P == 0) break;
            P++;
        }
        System.out.println("end PL/0");
    }
}
