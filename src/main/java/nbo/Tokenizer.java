package nbo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

public class Tokenizer {

    public record Match(String string, Token token, int startIndex) {
    }

    private final List<Token> preserve;
    private final List<Token> skip;
    private final List<Token> tokens;

    public Tokenizer() {
        preserve = new ArrayList<>();
        skip = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    public void addPreserve(Token token) {
        preserve.add(token);
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void addSkip(Token token) {
        skip.add(token);
    }

    public List<Match> tokenize(String input) throws NBOParseException {

        List<Match> result = new ArrayList<>();
        int index = 0;
        int length = input.length();
        String subString;

        while (index < length) {

            subString = input.substring(index);
            int currentIndex = index;

            for (Token token : this.preserve) {
                MatchResult match = token.match(subString);
                if (match != null && match.start() == 0) {
                    result.add(new Match(subString.substring(match.start(), match.end()), token, index));
                    index += match.end();
                    subString = input.substring(index);
                }
            }

            for (Token skip : this.skip) {
                MatchResult match = skip.match(subString);
                if (match != null && match.start() == 0) {
                    index += match.end();
                    subString = input.substring(index);
                }
            }

            for (Token token : this.tokens) {
                MatchResult match = token.match(subString);
                if (match != null && match.start() == 0) {
                    result.add(new Match(subString.substring(match.start(), match.end()), token, index));
                    index += match.end();
                    subString = input.substring(index);
                }
            }
            if (currentIndex == index) {
                throw new NBOParseException("Unexpected symbol", input, index);
            }
        }
        return result;
    }
}
