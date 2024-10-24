import java.io.File;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        lexer.lexer("D:\\DAN\\proga\\java\\parser\\src\\input.txt");
        lexer.PrintTokens();
    }
}
