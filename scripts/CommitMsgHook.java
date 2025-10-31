import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;

public class CommitMsgHook {

    private final class RegexPatterns {
    	private RegexPatterns() {}
    	
    	public static Pattern compilePattern(String pattern) {
    	    return Pattern.compile("^" + pattern + "$");
    	}
    	
    	public static String optional(String pattern) {
    	    return "(?:" + pattern + ")?";
    	}
    	
    	public static String alternation(ArrayList<String> patternList) {
    	    return "(" + String.join("|", patternList) + ")";
    	}
    	
    	public static final String Noun = "[a-zA-Z0-9\\-]+";
    	public static final String Sentence = "[a-zA-Z0-9\\-][a-zA-Z0-9\\-\\h+]+";
    }

    public static void main(String[] args) {
        String commitMessage = args[0];

        var commitTypesList = loadAdditionalCommitTypes("commit-types.config");

        System.out.println("Added types: \n" + String.join("\n", commitTypesList) + "\n");

        // default types from the specification
        commitTypesList.add("feat");
        commitTypesList.add("fix");

        if (commitMessage.isEmpty()) {
            System.out.println("Commit message is invalid.");
            System.exit(1);
        }
        
        var blankLineSeperatedCommitMsg = commitMessage.split("\\n\\n");
        
        if (!isHeaderValid(blankLineSeperatedCommitMsg[0], commitTypesList)) {
            System.out.println("Header invalid.");
            System.exit(1);
        }

        System.out.println("Commit message is valid.");
        System.exit(0);
    }

    private static ArrayList<String> loadAdditionalCommitTypes(String filepath) {
        var commitTypes = new ArrayList<String>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            var nounPattern = RegexPatterns.compilePattern(RegexPatterns.Noun);

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (!nounPattern.matcher(line).matches()) {
                    System.out.println("Commit Type: \"" + line + "\" not added due to being invalid. Must be a noun (letters, numbers and hyphens only)\n");
                    continue;
                }
                
                line = line.toLowerCase();
                commitTypes.add(line);
            }
        }
        catch (Exception e) {
            System.out.println("Additional commit types will not recognized due to file reading error: ");
            e.printStackTrace(System.out);
        }

        return commitTypes;
    }
    
    private static boolean isHeaderValid(String header, ArrayList<String> validCommitTypesList) {
        header = header.toLowerCase();
    	
    	String headerRegex = RegexPatterns.alternation(validCommitTypesList) +
    	RegexPatterns.optional("\\(" + RegexPatterns.Noun + "\\)") +
    	RegexPatterns.optional("!") +
    	": " +
    	RegexPatterns.Sentence;
    	
    	var pattern = RegexPatterns.compilePattern(headerRegex);
    	
    	return pattern.matcher(header).matches();
    }
}
