import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Parser {
    private final Lexer lexer; // 词法分析器
    private final List<RecordTable> recordTable;
    private final List<MidCode> midcodeTable;
    private Token look; // 向前看的词法单元
    private int cx; // 代码分配指针，代码生成模块总是在cx所指的位置生成新代码
    private final int size = 1000;
    // the following variables for block
    private int dx; // 数据分配指针
    private int level; // 当前的块深度
    private int tx; // 当前的符号表指针

    public List<MidCode> getMidcodeTable() {
        return midcodeTable;
    }


    public Parser(Lexer lexer) {
        this.lexer = lexer;
        recordTable = new ArrayList<>(size);
        midcodeTable = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            recordTable.add(null);
            midcodeTable.add(null);
        }
        level = 1;
        cx = 0;
        tx = -1;
        move();
        prog();
    }

    // 更新Token
    public void move() {
        Token token = lexer.nextToken();
        if (token == Token.EOF) System.exit(0);
        if (token == Token.UNKNOWN) {
            System.out.println(token);
            System.exit(1);
        }
        while (token == Token.NULL) {
            token = lexer.nextToken();
        }
//        System.out.println(token);
        this.look = token;
    }

    // 报错信息
    public void error(String message) {
        throw new Error("near line " + lexer.line + " " + message);
    }

    // 生成中间代码
    public void genMidCode(InstrucType f, int l, int a) {
        if (cx > size) error("代码长度超限");
        MidCode mc = new MidCode(f, l, a);
        midcodeTable.set(cx, mc);
        cx++;
    }

    // 输出中间代码表
    public void listcode() {
        int index = 0;
//        int int_index = 0; // 记录最后一个INT
        for (MidCode midCode : midcodeTable) {
            if (midCode != null && midCode.table != null) {
//                if (midCode.table.get("F") == InstrucType.INT) int_index = index;
                System.out.println(index + " F: " + midCode.table.get("F") + " L: " + midCode.table.get("L") + " A: " + midCode.table.get("A"));
                index++;
            }
        }
//        // 将最后一个INT指令的A+3
//        MidCode code = midcodeTable.get(int_index);
//        code.table.replace("A", (int) code.table.get("A") + 3);
//        midcodeTable.set(int_index, code);
    }

    // 符号表记录
    public void enter(TokenType type, String name) {
        tx++;
        RecordTable rt;
        switch (type) {
            case TokenType.SYM_CONST -> {
                Integer value = Integer.parseInt(look.getText());
                rt = new RecordTable(name, value, type);
            }
            case TokenType.SYM_VAR -> {
                rt = new RecordTable(name, type, level, dx);
                dx++;
            }
            case TokenType.SYM_PROCEDURE -> {
                rt = new RecordTable(name, type, level, cx);
            }
            default -> rt = null;
        }
        recordTable.set(tx, rt);
    }

    // 查找变量位置
    public int position(String name) {
        // 可能会有找到相同层次但作用域不同的变量？？
        int index = tx;
        while (index > 0 && !recordTable.get(index).table.get("name").equals(name)) {
            index--;
        }
        if (index < 0) error("变量未声明");
        return index;
    }

    public void sym_const() {
        boolean canBreak = false;
        while (!canBreak) {
            if (look.getType() == TokenType.SYM_IDENTIFIER) {
                String ident = look.getText();
                move();
                if (look.getType() == TokenType.SYM_EQU) move();
                else error("缺少=");
                if (look.getType() == TokenType.SYM_NUMBER) {
                    // 将常量写入符号表
                    enter(TokenType.SYM_CONST, ident);
                    move();
                } else error("常量赋值缺少数值");
                if (look.getType() == TokenType.SYM_SEMICOLON) {
                    move();
                    canBreak = true;
                }
                if (look.getType() == TokenType.SYM_COMMA) {
                    move();
                } else if (!canBreak) error("缺少;");
            } else error("缺少标识符");
        }
    }

    public void sym_var() {
        boolean canBreak = false;
        while (!canBreak) {
            if (look.getType() == TokenType.SYM_IDENTIFIER) {
                String ident = look.getText();
                // 将变量写入符号表
                enter(TokenType.SYM_VAR, ident);
                move();
                if (look.getType() == TokenType.SYM_SEMICOLON) {
                    move();
                    canBreak = true;
                }
                if (look.getType() == TokenType.SYM_COMMA) {
                    move();
                } else if (!canBreak) error("缺少;");
            } else error("缺少标识符");
        }
    }

    public void sym_procedure() {
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            enter(TokenType.SYM_PROCEDURE, look.getText());
            level++;
            move();
        } else error("函数名必须为标识符");
        if (look.getType() == TokenType.SYM_SEMICOLON) move();
        else error("缺少;");
        block();
        level--;
        if (look.getType() == TokenType.SYM_SEMICOLON) {
            move();
        } else error("缺少;");
    }

    public void expression() {
        // 处理正负号
        String op = "";
        if (look.getType() == TokenType.SYM_PLUS || look.getType() == TokenType.SYM_MINUS) {
            op = look.getText();
            move();
        }
        term();
        if (op.equals("-")) genMidCode(InstrucType.OPR, 0, 1); // 负号运算，取反
        while (look.getType() == TokenType.SYM_PLUS || look.getType() == TokenType.SYM_MINUS) {
            op = look.getText();
            move();
            term();
            if (op.equals("+")) genMidCode(InstrucType.OPR, 0, 2); // 加
            else if (op.equals("-")) genMidCode(InstrucType.OPR, 0, 3); // 减
        }
    }

    public void factor() {
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            String ident = look.getText();
            int i = position(ident); // 查找当前变量在符号表中的位置
            Map<String, Object> table = recordTable.get(i).table;
            switch (table.get("kind")) {
                case TokenType.SYM_CONST -> genMidCode(InstrucType.LIT, 0, (Integer) table.get("value"));
                case TokenType.SYM_VAR ->
                        genMidCode(InstrucType.LOD, level - (Integer) table.get("level"), (Integer) table.get("address"));
                case TokenType.SYM_PROCEDURE -> error("procedure不能出现在表达式中");
                default -> throw new IllegalStateException("Unexpected value: " + recordTable.get(i).table.get("kind"));
            }
            move();
        } else if (look.getType() == TokenType.SYM_NUMBER) {
            int value = Integer.parseInt(look.getText());
            genMidCode(InstrucType.LIT, 0, value);
            move();
        } else if (look.getType() == TokenType.SYM_LPAREN) {
            move();
            expression();
            if (look.getType() == TokenType.SYM_RPAREN) move();
        } else error("无效因子");
    }

    public void term() {
        factor();
        while (look.getType() == TokenType.SYM_SLASH || look.getType() == TokenType.SYM_TIMES) {
            String operator = look.getText();
            move();
            factor();
            if (operator.equals("*")) {
                genMidCode(InstrucType.OPR, 0, 4); // 乘法
            } else if (operator.equals("/")) {
                genMidCode(InstrucType.OPR, 0, 5); // 除法
            }
        }
    }

    public void condition() {
        String op = "";
        if (look.getType() == TokenType.SYM_ODD) {
            move();
            expression();
            genMidCode(InstrucType.OPR, 0, 6);
        } else {
            expression();
            if (look.getType() == TokenType.SYM_EQU ||
                    look.getType() == TokenType.SYM_LES ||
                    look.getType() == TokenType.SYM_GTR ||
                    look.getType() == TokenType.SYM_NEQ ||
                    look.getType() == TokenType.SYM_LEQ ||
                    look.getType() == TokenType.SYM_GEQ) {
                op = look.getText();
                move();
                expression();
                switch (op) {
                    case "==" -> genMidCode(InstrucType.OPR, 0, 8);
                    case "<>" -> genMidCode(InstrucType.OPR, 0, 9);
                    case "<" -> genMidCode(InstrucType.OPR, 0, 10);
                    case ">=" -> genMidCode(InstrucType.OPR, 0, 11);
                    case ">" -> genMidCode(InstrucType.OPR, 0, 12);
                    case "<=" -> genMidCode(InstrucType.OPR, 0, 13);
                }
            } else error("缺少比较符号");
        }
    }

    public void statement() {
        int cx1, cx2; // 存储中间代码的临时位置
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            String ident = look.getText();
            int i = position(ident);
            // 只有变量能够被赋值
            if (recordTable.get(i).table.get("kind") != TokenType.SYM_VAR) error("不是变量");
            move();
            if (look.getType() == TokenType.SYM_BECOMES) {
                move();
                expression();
                genMidCode(InstrucType.STO, level - (Integer) recordTable.get(i).table.get("level"), (Integer) recordTable.get(i).table.get("address"));
            }
            if (look.getType() == TokenType.SYM_SEMICOLON) move();
            else error("缺少;");
        } else if (look.getType() == TokenType.SYM_CALL) {
            move();
            if (look.getType() == TokenType.SYM_IDENTIFIER) {
                String ident = look.getText();
                int i = position(ident);
                RecordTable rt = recordTable.get(i);
                if (rt.table.get("kind") == TokenType.SYM_PROCEDURE) {
                    genMidCode(InstrucType.CAL, level - (Integer) rt.table.get("level"), (Integer) rt.table.get("address")); // 过程名的地址为中间代码表中的地址
                }
                move();
            } else error("call缺少标识符");
            if (look.getType() == TokenType.SYM_SEMICOLON) move();
            else error("缺少;");
        } else if (look.getType() == TokenType.SYM_BEGIN) {
            move();
            while (look.getType() != TokenType.SYM_END) {
                statement();
            }
            move();
        } else if (look.getType() == TokenType.SYM_IF) {
            move();
            condition();
            if (look.getType() == TokenType.SYM_THEN) move();
            else error("缺少then");
            cx1 = cx;
            genMidCode(InstrucType.JPC, 0, 0);
            statement();
            midcodeTable.get(cx1).table.replace("A", cx);
        } else if (look.getType() == TokenType.SYM_WHILE) {
            move();
            cx1 = cx; // 记录条件判断指令的位置
            condition();
            cx2 = cx;
            genMidCode(InstrucType.JPC, 0, 0);
            if (look.getType() == TokenType.SYM_DO) move();
            else error("缺少do");
            statement();
            genMidCode(InstrucType.JMP, 0, cx1);
            midcodeTable.get(cx2).table.replace("A", cx);
        } else error("无效语句");

    }

    public void block() {
        // 初始化
        dx = 3; // 变量的初始地址
        int tx_0 = tx; // 该层次局部变量分配到的在符号表中的相对位置，到这里了，下一位是要存新的变量的位置
        int cx_0 = cx; // 存储当前层代码的开始位置
        int dx_0; // 存储当前层的dx，因为如果有嵌套层的话，dx会变化。

        genMidCode(InstrucType.JMP, 0, 0);


        // 判断嵌套层次
        if (level > 4) error("超过三层嵌套");

        if (look.getType() == TokenType.SYM_CONST) {
            move();
            sym_const();
        }
        if (look.getType() == TokenType.SYM_VAR) {
            move(); // 前置move
            sym_var();
        }
        dx_0 = dx; // 利用了dx_0是局部变量和dx是全局变量的特性
        while (look.getType() == TokenType.SYM_PROCEDURE) {
            move();
            sym_procedure();
            while (look.getType() == TokenType.SYM_PROCEDURE) {
                move();
                sym_procedure();
            }
        }
        dx = dx_0;
        midcodeTable.get(cx_0).table.replace("A", cx); // 将跳转位置改到这里
        genMidCode(InstrucType.INT, 0, dx); // 分配栈空间
        statement();
        genMidCode(InstrucType.OPR, 0, 0); // RET
    }

    public void prog() {
        block();
        if (look.getType() == TokenType.SYM_DOT) {
            // 打印符号表
            for (RecordTable rd : recordTable) {
                if (rd != null) System.out.println(rd.table);
            }
            // 打印中间代码
            listcode();
//            System.exit(0);
        }

    }

}