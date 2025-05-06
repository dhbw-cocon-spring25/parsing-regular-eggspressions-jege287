package de.dhbw.mh.redeggs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static de.dhbw.mh.redeggs.Range.range;
import static de.dhbw.mh.redeggs.Range.single;

/**
 * A parser for regular expressions using recursive descent parsing.
 * This class is responsible for converting a regular expression string into a
 * tree representation of a {@link RegularEggspression}.
 */
public class RecursiveDescentRedeggsParser {

	private String input;
	private int cursor =1;

	private static final Character[] Literals = new Character[] { '(', ')', '[', ']', '$','|', '*', '^'};
	private static final Set<Character> LiteralsSet = new HashSet<>(Arrays.asList(Literals));

	/**
	 * The symbol factory used to create symbols for the regular expression.
	 */
	protected final SymbolFactory symbolFactory;

	/**
	 * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
	 * symbol factory.
	 *
	 * @param symbolFactory the factory used to create symbols for parsing
	 */
	public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {

		this.symbolFactory = symbolFactory;
	}

	private char peek() {
		if (input.isEmpty()) {
			return '$';
		}
		return input.charAt(0);
	}

	private char consume() {
		char r = this.peek();
		if (r != '$') {
			input = input.substring(1);
			cursor++;
		}
		System.out.println(r);
		return r;
	}

	public boolean isLiteral(char c) {
		return !LiteralsSet.contains(c);
	}


	/**
	 * Parses a regular expression string into an abstract syntax tree (AST).
	 *
	 *
	 * 
	 * This class uses recursive descent parsing to convert a given regular
	 * expression into a tree structure that can be processed or compiled further.
	 * The AST nodes represent different components of the regex such as literals,
	 * operators, and groups.
	 *
	 * @param regex the regular expression to parse
	 * @return the {@link RegularEggspression} representation of the parsed regex
	 * @throws RedeggsParseException if the parsing fails or the regex is invalid
	 */
	public RegularEggspression parse(String regex) throws RedeggsParseException {
		// TODO: Implement the recursive descent parsing to convert `regex` into an AST.
		// This is a placeholder implementation to demonstrate how to create a symbol.
		input = regex;
		if (input.length() == 1) {
			if (this.peek() == 'ε') {
				return new RegularEggspression.EmptyWord();
			} else if (this.peek() == '∅') {
				return new RegularEggspression.EmptySet();
			}
		}

		

		// Create a new symbol using the symbol factory
		VirtualSymbol symbol = symbolFactory.newSymbol()
				.include(single('_'), range('a', 'z'), range('A', 'Z'))
				.andNothingElse();

		// Return a dummy Literal RegularExpression for now
		return new RegularEggspression.Literal(symbol);
	}
	private RegularEggspression regex() throws RedeggsParseException {
		char character = peek();
		if (isLiteral(character) || character == '(' | character == '[') {
			RegularEggspression concat = concat();
			return union(concat);
		}

		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);
	}

	private RegularEggspression union(RegularEggspression left) throws RedeggsParseException{
		char character = peek();
		if (character == '|') {
			consume();
			RegularEggspression concat = concat();
			return new RegularEggspression.Alternation(left, union(concat));

		} else if (character == '$' || character == ')') {
			return left;
			
		}

		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);

	}

	private RegularEggspression concat() throws RedeggsParseException{
		char character = peek();

		if (isLiteral(character) || character == '(' || character == '[') {

			RegularEggspression kleene = kleene();
			return suffix(kleene);
		}
		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);
		
	}

	private RegularEggspression kleene() throws RedeggsParseException{
		char character = peek();

		if (isLiteral(character) || character == '(' || character == '[') {

			RegularEggspression base = base();
			return star(base);
			
		}
		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);
		
	}

	private RegularEggspression suffix(RegularEggspression left) throws RedeggsParseException{
		char character = peek();

		if (isLiteral(character) || character == '(' || character == '[') {

			RegularEggspression kleene = kleene();
			return new RegularEggspression.Concatenation(left, suffix(kleene));


		} else if (character == '$' || character == ')' || character == '|') {
			return left;
			
		}
		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);
	}
	private RegularEggspression star(RegularEggspression base) throws RedeggsParseException{
		char character = peek();

		if (character == '*') {
			consume();
			return new RegularEggspression.Star(base);

		} else if (isLiteral(character) || character == '(' || character == '[' || character == '$' || character == ')'
				|| character == '|') {

			return base;
			
		}
		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);


	}

	private RegularEggspression base() throws RedeggsParseException{
		char character = peek();

		if (isLiteral(character) ) {
			consume();
			// VirtualSymbol symbol = symbolFactory.newSymbol().include(range.single(character)).andNothingElse(); // TODO
			return new RegularEggspression.Literal(symbol);
		} else if (character == '(') {
			consume();
			RegularEggspression regex = regex();
			if (consume() != ')') {
				throw new RedeggsParseException("Unexpected symbol - Expected symbol ')'" + character + "' at position " + cursor + ".", +cursor);


			}
			return regex;
		} else if (character == '[') {
			consume();
			boolean negation = negation();
			SymbolFactory.Builder range = range(inhalt, negation);
			SymbolFactory.Builder inhalt = inhalt(symbolFactory.newSymbol(), negation);
			if (this.consume() != ']') {
				throw new RedeggsParseException("Unexpected symbol - Expected symbol ']'" + character + "' at position " + cursor + ".", +cursor);
			}
			return new RegularEggspression.Literal(range.andNothingElse());
		}
		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", +cursor);



			
		}

	private boolean negation() throws RedeggsParseException {
		char character = peek();
		if (character == '^') {
			this.consume();
			return true;
		} else if (isLiteral(character)) {
			return false;
		}

		throw new RedeggsParseException("Unexpected symbol '" + character + "' at position " + cursor + ".", cursor);
	}

	private  range() throws RedeggsParseException {




	}

	private inhalt() throws RedeggsParseException {

	}

	private  rest() throws RedeggsParseException {

	}


}
