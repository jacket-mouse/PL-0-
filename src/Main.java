import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileWriter;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            String input = Files.readString(Path.of("/Users/leeson/Documents/compiler/compiler/src/dragon0.txt"));
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}