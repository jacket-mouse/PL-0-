# PL0-Compiler

Java实现的简易PL/0编译器，说明请见右侧web链接🔗。

- 主程序：

  - main.java

- 词法分析：

  - Lexer.java：词法分析器主程序
  - KeywordTable.java：关键字（保留字）表
  - TokenType.java：Token类型-enum
  - Token.java：Token

- 语法分析、语义分析与中间代码生成：

  - Parser.java：语法分析主程序
  - InstrucType.java：中间代码类型-enum

- 中间代码的解析程序：

  - Interpreter.java：解释程序

- 测试文件：

  - dargon2.txt：测试中间代码字段DL和SL的区别。

  - dragon1.txt：部分测试代码。

  - dragon0.txt：完整的测试代码。
