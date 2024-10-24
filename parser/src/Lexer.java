import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

enum States {H, ID, NM, ASGN, DLM, ERR}
enum TokenType {KWORD, IDENT, NUM, OPER, DELIM}

public class Lexer {
    private static final String[] KEYWORDS = {"for", "do", "to", "next", "readln", "writeln", "if",
            "else", "while", "begin", "end", "%", "!", "$"};
    private LexemeTable lexemeTableHead = null;
    private LexemeTable lexemeTable = null;

    private boolean isKeyword(String id) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void addToken(Token token) {
        LexemeTable newNode = new LexemeTable(token);
        if (lexemeTableHead == null) {
            lexemeTableHead = newNode;
            lexemeTable = newNode;
        }
        else {
            lexemeTable.next = newNode;
            lexemeTable = newNode;
        }
    }

    public void lexer(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            States currentState = States.H;
            int c = reader.read();
            String errSymbol = "";
            while (c != -1) {
                //  System.out.println(String.valueOf((char) c) + " " + currentState);
                switch (currentState) {
                    case H: {
                        while (Character.isWhitespace(c)) {
                            c = reader.read();
                        }
                        if (Character.isLetter(c) || c == '_') {
                            currentState = States.ID;
                        } else if (Character.isDigit(c) || c == '.') {
                            currentState = States.NM;
                            continue;
                        } else if (c == ':') {
                            currentState = States.ASGN;
                            continue;
                        } else {
                            currentState = States.DLM;
                            continue;
                        }
                        break;
                    }
                    case ASGN: {
                        int colon = c;
                        c = reader.read();

                        while (Character.isWhitespace(c)) {
                            c = reader.read();
                        }

                        if (c == '=') {
                            addToken(new Token(TokenType.OPER, ":="));
                            c = reader.read();
                            currentState = States.H;
                            break;
                        }
                        else if (c == '%' || c == '!' || c == '$') {
                            addToken(new Token(TokenType.DELIM, Character.toString((char) colon)));
                            addToken(new Token(TokenType.KWORD, Character.toString((char) c)));
                            c = reader.read();
                            currentState = States.H;
                            break;
                        } else {
                            errSymbol = String.valueOf((char) c);
                            currentState = States.ERR;
                            break;
                        }
                    }
                    case DLM:
                    {
                        if ((c == '(')  || (c == ')') || (c == ';') || (c == ',') || (c == '{') || (c == '}'))
                        {
                            addToken(new Token(TokenType.DELIM, Character.toString((char) c)));
                            c = reader.read();
                            currentState = States.H;
                            break;
                        }
                        else if (c == '<' || c == '>' || c == '=' || c == '!') {
                            int firstChar = c;
                            c = reader.read();
                            if (c == '=') {
                                addToken(new Token(TokenType.OPER, Character.toString((char) firstChar) + '='));
                                c = reader.read();
                            }
                            else if (firstChar == '=') {
                                errSymbol = String.valueOf((char) firstChar);
                                currentState = States.ERR;
                                break;
                            }
                            else {
                                addToken(new Token(TokenType.OPER, Character.toString((char) firstChar)));
                            }
                        }
                        else if (c == '+' || c == '-' || c == '*' || c == '|' || c == '&') {
                            int firstChar = c;
                            c = reader.read();
                            if (firstChar == '|' && c == '|') {
                                addToken(new Token(TokenType.OPER, "||"));
                                c = reader.read();
                            }
                            else if (firstChar == '&' && c == '&') {
                                addToken(new Token(TokenType.OPER, "&&"));
                                c = reader.read();
                            }
                            else if ((firstChar == '|') || (firstChar == '&')) {
                                errSymbol = String.valueOf((char) firstChar);
                                currentState = States.ERR;
                                break;
                            }
                            else {
                                addToken(new Token(TokenType.OPER, Character.toString((char) firstChar)));
                            }
                            currentState = States.H;
                            break;
                        }
                        else if (c == '/') {
                            int colon = c;
                            c = reader.read();
                            if (c == '*') {  // Начало многострочного комментария
                                c = reader.read();
                                while (c != -1) {  // Читаем до конца файла или до закрытия комментария
                                    if (c == '*') {
                                        c = reader.read();
                                        if (c == '/') {  // Найдено закрытие комментария
                                            c = reader.read();
                                            currentState = States.H;
                                            break;
                                        }
                                    } else {
                                        c = reader.read();  // Продолжаем читать комментарий
                                    }
                                }

                                if (c == -1) {  // Конец файла достигнут до закрытия комментария
                                    errSymbol = "Unterminated comment";
                                    currentState = States.ERR;
                                    break;
                                }
                            }
                            else {
                                addToken(new Token(TokenType.OPER, Character.toString((char) colon)));
                            }
                        }

                        else {
                            errSymbol = String.valueOf((char) c);
                            currentState = States.ERR;
                            c = reader.read();
                            break;
                        }
                        currentState = States.H;
                        break;
                    }
                    case ID: {
                        StringBuilder buf = new StringBuilder();
                        buf.append((char) c);
                        c = reader.read();
                        while (Character.isLetterOrDigit(c) || c == '_') {
                            buf.append((char) c);
                            c = reader.read();
                        }
                        String id = buf.toString();
                        if (isKeyword(id)) {
                            addToken(new Token(TokenType.KWORD, id));
                        } else {
                            addToken(new Token(TokenType.IDENT, id));
                        }
                        currentState = States.H;
                        break;
                    }
                    case NM:
                        StringBuilder buf = new StringBuilder();

                        boolean isBin = true;
                        boolean isDec = true;
                        boolean isOct = true;
                        boolean isHex = true;
                        boolean isOrd = false;

                        int B_count = 0;
                        int D_count = 0;
                        int O_count = 0;
                        int H_count = 0;
                        int T_count = 0;
                        int E_count = 0;

                        while (Character.isDigit(c) || Character.isAlphabetic(c) || c == '.' || c == '+' || c == '-' || c == 'e') {
                            char currentChar = (char) c;

                            if (currentChar != '0' && currentChar != '1' &&
                                    currentChar != 'B' && currentChar != 'b') {
                                isBin = false;
                            } else if (currentChar == 'b' || currentChar == 'B') {
                                B_count++;
                            }

                            if ((currentChar < '0' || currentChar > '7') &&
                                    (currentChar != 'o' && currentChar != 'O')) {
                                isOct = false;
                            } else if (currentChar == 'O' || currentChar == 'o') {
                                O_count++;
                            }

                            if ((currentChar < '0' || currentChar > '9') &&
                                    (currentChar != 'd' && currentChar != 'D')) {
                                isDec = false;
                            } else if (currentChar == 'd' || currentChar == 'D') {
                                D_count++;
                            }

                            if ((!"ABCDEFabcdef0123456789".contains(Character.toString(currentChar))) &&
                                    (currentChar != 'H' && currentChar != 'h')) {
                                isHex = false;
                            } else if (currentChar == 'H' || currentChar == 'h') {
                                H_count++;
                            }

                            if (currentChar == '.') {
                                isOrd = true;
                                T_count++;
                            }
                            if (currentChar == 'e') {
                                E_count++;
                            }
                            buf.append(currentChar);
                            c = reader.read();
                        }
                        String numStr = buf.toString();
                        if (isBin && (!numStr.startsWith("B") && !numStr.startsWith("b")) &&
                                (numStr.endsWith("B") || numStr.endsWith("b")) &&
                                (B_count == 1)) {
                            addToken(new Token(TokenType.NUM, numStr));
                        }
                        else if (isOct && (!numStr.startsWith("O") && !numStr.startsWith("o")) &&
                                (numStr.endsWith("O") || numStr.endsWith("o")) &&
                                (O_count == 1)) {
                            addToken(new Token(TokenType.NUM, numStr));
                        }
                        else if (isDec && (!numStr.startsWith("D") && !numStr.startsWith("d")) &&
                                (numStr.endsWith("D") || numStr.endsWith("d")) &&
                                (D_count == 1)) {
                            addToken(new Token(TokenType.NUM, numStr));
                        }
                        else if (isHex && (!numStr.startsWith("h") && !numStr.startsWith("H")) &&
                                (numStr.endsWith("H") || numStr.endsWith("h")) &&
                                (H_count == 1)) {
                            addToken(new Token(TokenType.NUM, numStr));
                        }
                        else {
                            boolean isValid = true;
                            for (char x : numStr.toCharArray()) {
                                if (!Character.isDigit(c) && x != 'e') {
                                    isValid = false;
                                    break;
                                }
                            }

                            boolean e = !numStr.endsWith("e") || !numStr.endsWith("-") || !numStr.endsWith("+");
                            if (isOrd && T_count == 1 || (numStr.contains("e+") || numStr.contains("e-")) && E_count == 1 && e && isValid) {
                                addToken(new Token(TokenType.NUM, numStr));
                            }
                            else {
                                errSymbol = numStr;
                                currentState = States.ERR;
                                break;
                            }
                        }
                        currentState = States.H;
                        break;
                    case ERR:
                    {
                        System.out.println("Unknown character: " + errSymbol);
                        currentState = States.H;
                        break;
                    }
                }
            }
            if (currentState == States.ERR) {
                System.out.println("Unknown character: " + errSymbol);
                currentState = States.H;
            }
        } catch (IOException e) {
            System.out.println("Error opening file: " + e.getMessage());
        }
    }

    public void PrintTokens() {
        LexemeTable current = lexemeTableHead;
        while (current != null) {
            System.out.println(current.token.toString());
            current = current.next;
        }
    }
}