import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Parser {
    private final Lexer lexer; // 词法分析器
    private final List<RecordTable> recordTable;
    private final List<MidCode> midcodeTable;
    private int level; // 嵌套层次
    private Token look; // 向前看的词法单元
    private int cx; // 存储下一条指令的地址
    private int dx;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        level = 1;
        recordTable = new ArrayList<>();
        midcodeTable = new ArrayList<>();
        cx = 0;
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
        MidCode mc = new MidCode(f, l, a);
        midcodeTable.add(mc);
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
                    Integer value = Integer.parseInt(look.getText());
                    // 将常量写入符号表
                    RecordTable rt = new RecordTable(ident, TokenType.SYM_CONST, value);
                    recordTable.add(rt);
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
                RecordTable rd = new RecordTable(ident, TokenType.SYM_VAR, level, ++dx);
                recordTable.add(rd);
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
            RecordTable rt = new RecordTable(look.getText(), TokenType.SYM_PROCEDURE, level, address);
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
            move();
        } else if (look.getType() == TokenType.SYM_NUMBER) {
            Integer value = Integer.parseInt(look.getText());
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
            move();
            factor();
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
        dx = 3;
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