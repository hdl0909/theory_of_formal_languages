import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.*;

public class SyntaxAnalysis {
    private Queue<Token> tokens;
    private Token currentToken;
    private int numLine;
    private Lexer lexer;
    private Map<String, String> declaredVariables; // Хранилище объявленных переменных и их типов

    public SyntaxAnalysis(LexemeTable lexemeTableHead, Lexer lexer) {
        tokens = new LinkedList<>();
        declaredVariables = new HashMap<>();
        LexemeTable current = lexemeTableHead;
        while (current != null) {
            tokens.add(current.token);
            current = current.next;
        }
        this.lexer = lexer;
        currentToken = tokens.poll();
    }

    private void nextToken() { // функция для получения следующего токена
        if (currentToken != null) {
            numLine = currentToken.numLine;
        }
        currentToken = tokens.poll();
    }

    public void parseIdAssign() { // обработка описания данных
        List<String> variables = new ArrayList<>();
        variables.add(currentToken.tokenValue);

        nextToken();

        while (currentToken != null && currentToken.tokenValue.equals(",")) {
            nextToken();
            if (currentToken == null || currentToken.tokenType != TokenType.IDENT) { // если объявляем не идентификатор
                throw new RuntimeException("Ошибка в строке " + numLine);
            }
            variables.add(currentToken.tokenValue);
            nextToken();
        }

        if (currentToken == null || !currentToken.tokenValue.equals(":")) { // нету : после идентификаторов
            throw new RuntimeException("Ошибка в строке " + numLine);
        } else if (variables.get(variables.size() - 1).equals(",")) { // после запятой нету идентификатора
            throw new RuntimeException("Ошибка в строке " + numLine);
        }

        nextToken();
        if (currentToken == null || (!currentToken.tokenValue.equals("%") && !currentToken.tokenValue.equals("!") && !currentToken.tokenValue.equals("$"))) {
            throw new RuntimeException("Ошибка в строке " + numLine); // не написан тип данных
        }

        String type = currentToken.tokenValue;

        for (String variable : variables) {
            if (declaredVariables.containsKey(variable)) { // если уже объявляли такую переменную
                throw new RuntimeException("Ошибка в строке " + numLine);
            }
            declaredVariables.put(variable, type);
        }

        nextToken();
        if (currentToken == null || !currentToken.tokenValue.equals(";")) { // в конце строки должна стоять ;
            throw new RuntimeException("Ошибка в строке " + numLine);
        }
        nextToken();
    }


    public void parseExpression() {
        if (currentToken.tokenValue.equals(":=")) { // если вначале идет присваивание без идентфиикатора
            throw new RuntimeException("Ошибка в строке " + numLine);
        } else if (currentToken.tokenValue.equals("writeln") || currentToken.tokenValue.equals("readln")) {
            String functionName = currentToken.tokenValue;
            nextToken();
            if (currentToken == null || !currentToken.tokenValue.equals("(")) { // после readln и writeln не идет скобка
                throw new RuntimeException("Ошибка в строке " + numLine);
            }
            nextToken();

            boolean expectingArgument = true;
            while (currentToken != null && !currentToken.tokenValue.equals(")")) {
                if (expectingArgument) {
                    if (currentToken.tokenType != TokenType.IDENT && currentToken.tokenType != TokenType.NUM) {
                        throw new RuntimeException("Ошибка в строке " + numLine); // в скобках readln и writeln указан ни идентфикатор, ни число
                    }
                    nextToken();
                    expectingArgument = false;
                } else {
                    if (currentToken.tokenValue.equals(",")) {
                        nextToken();
                        expectingArgument = true;
                    } else { // между идентификаторами и числами нету запятой
                        throw new RuntimeException("Ошибка в строке " + numLine);
                    }
                }
            }

            if (currentToken == null || !currentToken.tokenValue.equals(")")) { // если readln и writeln не заканчивается закрывающейся скобкой
                throw new RuntimeException("Ошибка в строке " + numLine);
            }
            nextToken();
            return;
        }
        else if (currentToken == null || (currentToken.tokenType != TokenType.IDENT && currentToken.tokenType != TokenType.NUM
        && !currentToken.tokenValue.equals("("))) { // если выражение или условие пустое или не содержит идентификатор, число или открывающую скобку
            throw new RuntimeException("Ошибка в строке " + numLine);
        }

        int openBrackets = 0;
        while (currentToken != null && (currentToken.tokenType == TokenType.OPER || currentToken.tokenType == TokenType.NUM || currentToken.tokenType == TokenType.IDENT
        || currentToken.tokenValue.equals("(") || currentToken.tokenValue.equals(")"))) {
            if (currentToken.tokenType == TokenType.IDENT && !declaredVariables.containsKey(currentToken.tokenValue)) {
                throw new RuntimeException("Ошибка в строке " + numLine); // переменная, которая есть в выражении не объявлена
            }
            if (currentToken.tokenValue.equals("(")) {
                openBrackets++;
                nextToken();
                if (currentToken.tokenValue.equals(")")) { // после открывающей скобки сразу следует закрывающаяся
                    throw new RuntimeException("Ошибка в строке: " + numLine);
                }
            } else if (currentToken.tokenValue.equals(")")) {
                openBrackets--;
                nextToken();
                if (currentToken.tokenValue.equals("(")) { // после закрывающейся сразу идет открывающаяся
                    throw new RuntimeException("Ошибка в строке: " + numLine);
                }
            }
            else nextToken();
        }
    }

    public void parseBlock() {
        int openBrackets = 0;

        while (currentToken != null) {
            if (currentToken.tokenValue.equals("}") || currentToken.tokenValue.equals("end")) {
                if (openBrackets > 0) {
                    throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
                }
                return;
            }

            if (currentToken.tokenValue.equals("if")) {
                nextToken();
                if (!currentToken.tokenValue.equals("(")) { // после if нету открывающейся скобки
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                parseExpression();
                if (!currentToken.tokenValue.equals("{")) { // после условия нету {
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                openBrackets++;
                parseBlock();
                if (currentToken == null || !currentToken.tokenValue.equals("}")) { // в конце оператора нету }
                    throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
                }
                nextToken();
                openBrackets--;

                if (currentToken != null && currentToken.tokenValue.equals("else")) {
                    nextToken();
                    if (!currentToken.tokenValue.equals("{")) { // после else нету {
                        throw new RuntimeException("Ошибка в строке " + numLine);
                    }
                    nextToken();
                    openBrackets++;
                    parseBlock();
                    if (currentToken == null || !currentToken.tokenValue.equals("}")) {
                        throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
                    }
                    nextToken();
                    openBrackets--;
                }
            }

            else if (currentToken.tokenValue.equals("while")) {
                nextToken();
                if (!currentToken.tokenValue.equals("(")) { // после while нету ( для условия
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                parseExpression();
                if (!currentToken.tokenValue.equals("{")) { // нету { после условия
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                openBrackets++;
                parseBlock();
                if (currentToken == null || !currentToken.tokenValue.equals("}")) {
                    throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
                }
                nextToken();
                openBrackets--;
            }

            else if (currentToken.tokenValue.equals("for")) {
                nextToken();
                // проверка синтаксиса for
                if (currentToken.tokenType != TokenType.IDENT) {
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                if (!currentToken.tokenValue.equals(":=")) {
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                parseExpression();
                if (!currentToken.tokenValue.equals("to")) {
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                parseExpression();
                if (!currentToken.tokenValue.equals("{")) {
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
                openBrackets++;
                parseBlock();
                if (currentToken == null || !currentToken.tokenValue.equals("}")) {
                    throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
                }
                nextToken();
                openBrackets--;
            }

            else {
                parseExpression();
                if (currentToken == null || !currentToken.tokenValue.equals(";")) { // выражение должно заканчиваться на ;
                    throw new RuntimeException("Ошибка в строке " + numLine);
                }
                nextToken();
            }
        }

        if (openBrackets > 0) {
            throw new RuntimeException("Ошибка: отсутствует закрывающая фигурная скобка");
        }
    }


    public void parseSyntax() {
        if (!currentToken.tokenValue.equals("{")) {
            throw new RuntimeException("Не найдена точка входа в программу");
        }
        nextToken();
        while (currentToken != null && !currentToken.tokenValue.equals("}")) {
            if (currentToken.tokenType == TokenType.IDENT) {
                parseIdAssign();
            } else if (currentToken.tokenValue.equals("begin")) {
                nextToken();
                parseBlock();
                if (!currentToken.tokenValue.equals("end")) {
                    throw new RuntimeException("Не найден end");
                }
                nextToken();
            } else if (lexer.isKeyword(currentToken.tokenValue)) {
                throw new RuntimeException("Не найден begin");
            } else {
                nextToken();
            }
        }
        if (currentToken == null) {
            throw new RuntimeException("Не найдена точка выхода из программы");
        }
    }
}
