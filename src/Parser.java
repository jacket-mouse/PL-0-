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


    public Parser(Lexer lexer) {
        this.lexer = lexer;
        recordTable = new ArrayList<>(size);
        midcodeTable = new ArrayList<>(size);
        level = 1;
        cx = 0;
        tx = 0;
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
        System.out.println(token);
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
        for (MidCode midCode : midcodeTable) {
            System.out.println(midCode);
        }
    }

    // 符号表记录
    public void enter(TokenType type, String name) {
        tx++;
        RecordTable rt;
        switch (type) {
            case TokenType.SYM_CONST -> {
                Integer value = Integer.parseInt(look.getText());
                rt = new RecordTable(name, type, value);
            }
            case TokenType.SYM_VAR -> {
                rt = new RecordTable(name, type, level, dx);
                dx++;
            }
            case TokenType.SYM_PROCEDURE -> {
                rt = new RecordTable(name, type, level);
            }
            default -> rt = null;
        }
        recordTable.set(tx, rt);
    }

    // 查找变量位置
    public int position(String name) {
        int index = tx;
        while (index > 0 && recordTable.get(index).table.get("name") != name) {
            index--;
        }
        if (index <= 0) error("变量未声明");
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
                    // 将变量写入符号表

                    move();
                } else if (!canBreak) error("缺少;");
            } else error("缺少标识符");
        }
    }

    public void sym_procedure() {
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            RecordTable rt = new RecordTable(look.getText(), TokenType.SYM_PROCEDURE, level);
            recordTable.add(rt);
            level++;
            move();
        } else error("函数名必须为标识符");
        if (look.getType() == TokenType.SYM_SEMICOLON) move();
        else error("缺少;");
        block();
        if (look.getType() == TokenType.SYM_SEMICOLON) {
            move();
            level--;
        } else error("缺少;");
    }

    public void expression() {
        if (look.getType() == TokenType.SYM_PLUS || look.getType() == TokenType.SYM_MINUS) {
            move();
        }
        term();
        while (look.getType() == TokenType.SYM_PLUS || look.getType() == TokenType.SYM_MINUS) {
            move();
            term();
        }
    }

    public void factor() {
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            String ident = look.getText();
            int i = position(ident);
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
                genMidCode(InstrucType.OPR, 9, 4); // 乘法
            } else if (operator.equals("/")) {
                genMidCode(InstrucType.OPR, 0, 5); // 除法
            }
        }
    }

    public void condition() {
        if (look.getType() == TokenType.SYM_ODD) {
            move();
            expression();
        } else {
            expression();
            if (look.getType() == TokenType.SYM_EQU ||
                    look.getType() == TokenType.SYM_LES ||
                    look.getType() == TokenType.SYM_GTR ||
                    look.getType() == TokenType.SYM_NEQ ||
                    look.getType() == TokenType.SYM_LEQ ||
                    look.getType() == TokenType.SYM_GEQ) {
                move();
                expression();

            } else error("缺少比较符号");
        }
    }

    public void statement() {
        if (look.getType() == TokenType.SYM_IDENTIFIER) {
            String ident = look.getText();
            move();
            if (look.getType() == TokenType.SYM_BECOMES) {
                move();
                expression();
            }
            if (look.getType() == TokenType.SYM_SEMICOLON) move();
            else error("缺少;");
        } else if (look.getType() == TokenType.SYM_CALL) {
            move();
            if (look.getType() == TokenType.SYM_IDENTIFIER) {
                String ident = look.getText();
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
            statement();
        } else if (look.getType() == TokenType.SYM_WHILE) {
            move();
            condition();
            if (look.getType() == TokenType.SYM_DO) move();
            else error("缺少do");
            statement();
        } else error("无效语句");

    }

    public void block() {
        // 初始化
        dx = 3; // 变量的初始地址
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
        while (look.getType() == TokenType.SYM_PROCEDURE) {
            move();
            sym_procedure();
        }
        statement();

    }

    public void prog() {
        block();
        if (look.getType() == TokenType.SYM_DOT) {
            System.out.println("Finished");
            System.exit(0);
        }
    }

}