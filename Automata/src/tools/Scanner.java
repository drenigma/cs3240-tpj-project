package tools;

import automata.Token;
import automata.CharToken;
import automata.NFA;
import exceptions.SyntaxErrorException;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A lexer object which includes file operations, Character class parsing, and Token scanning. To be used from a parser wrapper.
 * @author Paul
 */
public class Scanner {

// <editor-fold defaultstate="collapsed" desc="Field Definitions">
    public static Map<String, CharToken> charClasses;
    public static Map<String, NFA> identifiers;
    //private Set<Token> tokenList = new HashSet<Token>();
    private String currentLine, buffer;
    private FileReader file;
    private BufferedReader reader;
    private Token currentToken;

    private class LLPlus extends LinkedList<Character> {

        public String traverse() {
            StringBuilder t = new StringBuilder();
            for (Character u : this) {
                t.append(u);
            }
            return t.reverse().toString();
        }
    }
    private LLPlus tokenBuffer = new LLPlus();
    private Deque<Character> lookAhead = new LinkedList<Character>();
    private boolean busy;
// </editor-fold>

    /**
     * Indicates the busy state of the Scanner object.
     * @return True, if busy.
     */
    public boolean isBusy() {
        return this.busy;
    }

    public Scanner(String filename) throws FileNotFoundException, IOException {
        this.busy = true;
        /* Open file */
        file = new FileReader(filename);
        reader = new BufferedReader(file);

        scanCharClasses();

        this.busy = false;
    }

    private void scanLine() throws IOException {
        /* Keep scanning while lines remain */
        if ((currentLine = reader.readLine()) == null) {
            // End of File
        }
    }

    private void scanCharClasses() throws IOException {
        /* Ignore line comments in beginning of file */
        while (currentLine.startsWith("%")) {
            scanLine();
        }

        charClasses = new HashMap<String, CharToken>();

        while ((currentLine = reader.readLine()).startsWith("$")) {
            char[] chars = currentLine.toCharArray();
            StringBuilder CNBuilder = new StringBuilder();
            int i = 0;

            while (chars[i] == ' ' || chars[i] == '\t' && i++ < chars.length) {
                CNBuilder.append(chars[i]);
            }

            String charClassName = CNBuilder.toString();

            try {
                while (chars[i++] != '[') {
                    if (i > chars.length) {
                        throw new SyntaxErrorException("Missing '[' in character class '" + charClassName + "'!");
                    }
                }
            } catch (SyntaxErrorException e) {
            }

            boolean opposite = false;

            Set<Character> charSet = new HashSet<Character>();
            for (i = i + 1; i < chars.length; i++) {

                if (chars[i] == ']') {
                    break;
                }
                if (chars[i] == '^') {
                    opposite = true;
                }
                if (chars[i] == '\\') {
                    int newIndex = i + 1;
                    if (Constants.specialCharsList.contains(chars[newIndex])) {
                        charSet.add(chars[newIndex]);
                        i++;
                    }
                }
                if (Constants.upperCaseList.contains(chars[i])) {
                    char startChar = chars[i];
                    charSet.add(startChar);
                    if (chars[i + 1] == '-') {
                        i++;
                        int startIndex = Constants.upperCaseList.indexOf(startChar);
                        i++;
                        int endIndex = Constants.upperCaseList.indexOf(chars[i]) + 1;
                        for (int index = startIndex; index < endIndex; index++) {
                            charSet.add(Constants.upperCaseList.get(index));
                        }

                    }
                } else if (Constants.lowerCaseList.contains(chars[i])) {
                    char startChar = chars[i];
                    charSet.add(startChar);
                    if (chars[i + 1] == '-') {
                        i++;
                        int startIndex = Constants.lowerCaseList.indexOf(startChar);
                        i++;
                        int endIndex = Constants.lowerCaseList.indexOf(chars[i]) + 1;
                        for (int index = startIndex; index < endIndex; index++) {
                            charSet.add(Constants.lowerCaseList.get(index));
                        }
                    }
                } else if (Constants.digitsList.contains(chars[i])) {
                    char startChar = chars[i];
                    charSet.add(startChar);
                    if (chars[i + 1] == '-') {
                        i++;
                        int startIndex = Constants.digitsList.indexOf(startChar);
                        i++;
                        int endIndex = Constants.digitsList.indexOf(chars[i]) + 1;
                        for (int index = startIndex; index < endIndex; index++) {
                            charSet.add(Constants.digitsList.get(index));
                        }
                    }

                }
            }
            if (opposite) {
                while (chars[i] != '$') {
                    i++;
                }
                StringBuilder builder = new StringBuilder();
                for (i = 1; i < chars.length; i++) {
                    if (chars[i] == ' ' || chars[i] == '\t') {
                        break;
                    }
                    builder.append(chars[i]);
                }
                String className = builder.toString().trim();
                Set<Character> newCharSet = new HashSet<Character>();
                for (Character c : charClasses.get(className).chars) {
                    newCharSet.add(c);
                }
                for (Character c : charSet) {
                    newCharSet.remove(c);
                }
                charSet = newCharSet;
            }
            CharToken token = new CharToken(charClassName, charSet);
            charClasses.put(charClassName, token);
        }

        try {
            if (!currentLine.startsWith("%")) {
                throw new SyntaxErrorException("Can't find Token definitions!");
            }
        } catch (SyntaxErrorException e) {
        }

    }

    private Token scanToken() throws IOException {
        if (lookAhead.isEmpty()) {
            // Starting a new line
            scanLine();
            for (int i = 0; i < currentLine.length(); i++) {
                lookAhead.addLast(currentLine.toCharArray()[i]);
            }
            /* We are reading an identifier */
            return newIdentifier();
        } else {
            try {
                /* Starting at some random point */
                // If lookAhead is empty, either detect an epsilon token or a syntax error
                if (pop() == null) {
                    if (tokenBuffer.isEmpty()) {
                        return Constants.EPSILON;
                    } else {
                        throw new SyntaxErrorException("Unexpected end of line!");
                    }
                }
                switch (tokenBuffer.peek()) {
                    case '\\':
                        /* Escaped character token */
                        return new Token("\\".concat(pop().toString()));
                    case '$':
                        /* Character class */
                        while (!Constants.specialCharsList.contains(pop())) {
                        }
                        unpop();
                        return new Token(tokenBuffer.traverse());
                    default:
                        /* Single Character */
                        return new Token(tokenBuffer.peek().toString());
                }
            } catch (SyntaxErrorException e) {
            }
        }
        return null;
    }

    private Token newIdentifier() {
        try {
            if (pop() != '$') {
                throw new SyntaxErrorException("Missing '$' leading Identifier!");
            }
        } catch (SyntaxErrorException e) {
        }
        while (lookAhead.pollFirst() != ' ' && popVerbatim() != '\t') {
        }
        /* Space marks the end of an identifier */
        return new Token(tokenBuffer.traverse());
    }

    /**
     * Pops the first character off the lookAhead and pushes it onto the token buffer. Ignores whitespace. To preserve whitespace, use popVerbatim().
     * @return the popped character.
     */
    private Character pop() {
        Character t = popVerbatim();
        if (java.lang.Character.isWhitespace(t)) {
            tokenBuffer.poll();
            return pop();
        } else {
            return t;
        }
    }

    /**
     * Pops the first character off the lookAhead and pushes it onto the token buffer. Preserve whitespace.
     * @return the popped character.
     */
    private Character popVerbatim() {
        tokenBuffer.push(lookAhead.pollFirst());
        return tokenBuffer.peekFirst();
    }

    private void unpop() {
        lookAhead.push(tokenBuffer.pollFirst());
    }

    public Token peekToken() throws IOException {
        return (currentToken = scanToken());
    }

    /**
     * Clears the token buffer if token matches.
     * @param token The token to be compared against.
     * @return True if tokens matched, false otherwise.
     */
    public boolean matchToken(Token token) {
        if (currentToken.equals(token)) {
            currentToken = new Token("");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Clears the token buffer if token matches.
     * @param token The token to be compared against.
     */
    public void matchToken() throws IOException {
        currentToken = currentToken.equals(new Token("")) || currentToken == null ? scanToken() : currentToken;
    }
}