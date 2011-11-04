import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RecursiveDescent {


	public static String[] RE_CHAR = {"\\ ", "!", "\"", "#", "$", "%", "&", "'", "\\(", "\\)", "\\*", "\\+", ",", "-", ".", 
		"/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\?", "@", "A", "B",
		"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
		"X", "Y", "Z", "\\[", "\\\\", "\\]", "^", "_", "`", "a", "b","c", "d", "e", "f", "g", "h", "i", "j", "k", "l", 
		"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "\\|", "}", "~"}; 
	
	
	public static String[] CLS_CHAR = {" ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "\\-", ".", 
		"/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B",
		"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
		"X", "Y", "Z", "\\[", "\\\\", "\\]", "^", "_", "`", "a", "b","c", "d", "e", "f", "g", "h", "i", "j", "k", "l", 
		"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~"}; 
	
	//public static String[] OPERATORS = {"|", "*", "+", ".", "^"};
	List<Token> ReCharTokens;
	//List<Token> OperatroList;
	List<String> reCharList;
	List<String> clsCharList;
	List<String> definedClasses;
	
	public RecursiveDescent(String fileName){
		//TODO JW initialize the scanner;
		ReCharTokens = new ArrayList<Token>();
		for(int i=0; i<RE_CHAR.length; i++){
			ReCharTokens.add(new Token(RE_CHAR[i], false));
		}
		
		reCharList = Arrays.asList(RE_CHAR);
		clsCharList = Arrays.asList(CLS_CHAR);
		
		
		definedClasses = new ArrayList<String>();
		
//		OperatroList = new ArrayList<Token>();
//		for(int i=0; i< OPERATORS.length; i++){
//			OperatroList.add(new Token(OPERATORS[i], false));
//			
//		}
		
		
	}
	
	private RecursiveDescentInterState regex(){
		rexp();
	}
	
	private RecursiveDescentInterState rexp(){
		rexp1();
		rexpPrime();
	}
	
	private RecursiveDescentInterState rexpPrime(){
		Token token = scanner.peek(); 
		if(token.equals(new Token("|", false))){
			scanner.matchToken(token);
			rexp1();
			rexpPrime();
		}
		else{
			return;
		}
	}
	
	private RecursiveDescentInterState rexp1(){
		rexp2();
		rexp1Prime();
	}
	

	
	private RecursiveDescentInterState rexp1Prime(){
		rexp2();
		rexp1Prime();
		return;
	}
	
	private RecursiveDescentInterState rexp2(){
		Token token= scanner.peek();
		if(token.equals(new Token("(", false))){
			scanner.matchToken(token);
			rexp();
			scanner.matchToken(new Token(")", false));
			rexp2Tail();
		}
		else if(reCharList.contains(token.getValue())){
			scanner.matchToken(token);
			rexp2Tail();
		}
		else{
			rexp3();
		}	
			
	}
	
	private RecursiveDescentInterState rexp2Tail(){
		Token token = scanner.peek();
		if(token.equals(new Token("*", false))){
			scanner.matchToken(token);
		}
		else if(token.equals(new Token("+", false))){
			scanner.matchToken(token);
		}
		else{
			return;
		}
	}
	
	private RecursiveDescentInterState rexp3(){
		charClass();
		return;
	}

	private RecursiveDescentInterState charClass(){
		Token token = scanner.peek();
		if(token.equals(new Token(".", false))){
			scanner.matchToken(token);
		}
		else if(token.equals(new Token("[", false))){
			scanner.matchToken(token);
			charClass1();
		}
		else if(definedClasses.contains(token.getValue())){
			scanner.matchToken(token);
		}
		else{
			throw new SyntaxErrorException();
		}
	}
	
	private RecursiveDescentInterState charClass1(){
		charSetList();
		excludeSet();
	}
	
	private RecursiveDescentInterState charSetList(){
		Token token = scanner.peek();
		if(token.equals(new Token("]", false))){
			scanner.matchToken(token);
		}
		else{
			charSet();
			charSetList();
		}
	}
	
	private RecursiveDescentInterState charSet(){
		Token token = scanner.peek();
		if(clsCharList.contains(token.getValue())){
			scanner.matchToken(token);
			charSetTail();
		}
		else{
			throw new SyntaxErrorException();
		}
	}
	
	private RecursiveDescentInterState charSetTail(){
		Token token = scanner.peek();
		if(token.equals(new Token("-", false))){
			scanner.matchToken(token);
			Token token1 = scanner.peek();
			if(clsCharList.contains(token.getValue())){
				scanner.matchToken(token1);
			}
			else{
				throw new SyntaxErrorException();
			}
		}
		else{
			return null;
		}
	}
	private RecursiveDescentInterState excludeSet(){
		scanner.matchToken(new Token("^", false));
		charSet();
		scanner.matchToken(new Token("IN", false));
		excludetSetTail();
	}
	private RecursiveDescentInterState excludetSetTail(){
		Token token = scanner.peek();
		if(token.equals(new Token("["))){
			scanner.matchToken(token);
			charSet();
			scanner.matchToken(new Token("]", false));
		}
		else if(definedClasses.contains(token.getValue())) {
			scanner.matchToken(token);
		}
		else{
			
			throw new SyntaxErrorException();
		}
		
	}
	
}
