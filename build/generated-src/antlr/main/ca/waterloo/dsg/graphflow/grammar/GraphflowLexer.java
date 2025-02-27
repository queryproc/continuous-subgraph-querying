// Generated from ca/waterloo/dsg/graphflow/grammar/Graphflow.g4 by ANTLR 4.7
package ca.waterloo.dsg.graphflow.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GraphflowLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LIMIT=1, QuotedCharacter=2, QuotedString=3, Comment=4, SPACE=5, TAB=6, 
		LINE_FEED=7, FORM_FEED=8, BACKSPACE=9, VERTICAL_TAB=10, CARRIAGE_RETURN=11, 
		DASH=12, UNDERSCORE=13, FORWARD_SLASH=14, BACKWARD_SLASH=15, SEMICOLON=16, 
		COMMA=17, COLON=18, SINGLE_QUOTE=19, DOUBLE_QUOTE=20, OPEN_ROUND_BRACKET=21, 
		CLOSE_ROUND_BRACKET=22, OPEN_SQUARE_BRACKET=23, CLOSE_SQUARE_BRACKET=24, 
		GREATER_THAN=25, Digits=26, Characters=27;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"LIMIT", "EscapedChar", "QuotedCharacter", "QuotedString", "Comment", 
		"SPACE", "TAB", "LINE_FEED", "FORM_FEED", "BACKSPACE", "VERTICAL_TAB", 
		"CARRIAGE_RETURN", "DASH", "UNDERSCORE", "FORWARD_SLASH", "BACKWARD_SLASH", 
		"SEMICOLON", "COMMA", "COLON", "SINGLE_QUOTE", "DOUBLE_QUOTE", "OPEN_ROUND_BRACKET", 
		"CLOSE_ROUND_BRACKET", "OPEN_SQUARE_BRACKET", "CLOSE_SQUARE_BRACKET", 
		"GREATER_THAN", "Digits", "Characters", "Digit", "Character", "A", "B", 
		"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", 
		"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		"'-'", "'_'", "'/'", "'\\'", "';'", "','", "':'", "'''", "'\"'", "'('", 
		"')'", "'['", "']'", "'>'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LIMIT", "QuotedCharacter", "QuotedString", "Comment", "SPACE", 
		"TAB", "LINE_FEED", "FORM_FEED", "BACKSPACE", "VERTICAL_TAB", "CARRIAGE_RETURN", 
		"DASH", "UNDERSCORE", "FORWARD_SLASH", "BACKWARD_SLASH", "SEMICOLON", 
		"COMMA", "COLON", "SINGLE_QUOTE", "DOUBLE_QUOTE", "OPEN_ROUND_BRACKET", 
		"CLOSE_ROUND_BRACKET", "OPEN_SQUARE_BRACKET", "CLOSE_SQUARE_BRACKET", 
		"GREATER_THAN", "Digits", "Characters"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public GraphflowLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Graphflow.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\35\u0141\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\u0081\n\3\3\4\3\4\3\4\5\4\u0086\n\4"+
		"\3\4\3\4\3\5\3\5\3\5\7\5\u008d\n\5\f\5\16\5\u0090\13\5\3\5\3\5\3\5\3\5"+
		"\3\5\7\5\u0097\n\5\f\5\16\5\u009a\13\5\3\5\3\5\5\5\u009e\n\5\3\6\3\6\3"+
		"\6\3\6\7\6\u00a4\n\6\f\6\16\6\u00a7\13\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u00af"+
		"\n\6\f\6\16\6\u00b2\13\6\3\6\5\6\u00b5\n\6\3\6\5\6\u00b8\n\6\5\6\u00ba"+
		"\n\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3"+
		"\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3"+
		"\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\6"+
		"\34\u00e7\n\34\r\34\16\34\u00e8\3\35\6\35\u00ec\n\35\r\35\16\35\u00ed"+
		"\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\5\37\u010c\n\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3"+
		"(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62"+
		"\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38\39\39\3"+
		"\u00a5\2:\3\3\5\2\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f\31\r\33\16"+
		"\35\17\37\20!\21#\22%\23\'\24)\25+\26-\27/\30\61\31\63\32\65\33\67\34"+
		"9\35;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2"+
		"g\2i\2k\2m\2o\2q\2\3\2)\5\2$$))^^\4\2))^^\3\2$$\3\2))\4\2\f\f\17\17\3"+
		"\3\f\f\3\2\"\"\3\2\13\13\3\2\f\f\3\2\16\16\3\2\n\n\3\2\r\r\3\2\17\17\4"+
		"\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKk"+
		"k\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2"+
		"TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\|"+
		"|\2\u014d\2\3\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2"+
		"\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2"+
		"\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\3s\3\2\2"+
		"\2\5\u0080\3\2\2\2\7\u0082\3\2\2\2\t\u009d\3\2\2\2\13\u00b9\3\2\2\2\r"+
		"\u00bb\3\2\2\2\17\u00bd\3\2\2\2\21\u00bf\3\2\2\2\23\u00c1\3\2\2\2\25\u00c3"+
		"\3\2\2\2\27\u00c5\3\2\2\2\31\u00c7\3\2\2\2\33\u00c9\3\2\2\2\35\u00cb\3"+
		"\2\2\2\37\u00cd\3\2\2\2!\u00cf\3\2\2\2#\u00d1\3\2\2\2%\u00d3\3\2\2\2\'"+
		"\u00d5\3\2\2\2)\u00d7\3\2\2\2+\u00d9\3\2\2\2-\u00db\3\2\2\2/\u00dd\3\2"+
		"\2\2\61\u00df\3\2\2\2\63\u00e1\3\2\2\2\65\u00e3\3\2\2\2\67\u00e6\3\2\2"+
		"\29\u00eb\3\2\2\2;\u00ef\3\2\2\2=\u010b\3\2\2\2?\u010d\3\2\2\2A\u010f"+
		"\3\2\2\2C\u0111\3\2\2\2E\u0113\3\2\2\2G\u0115\3\2\2\2I\u0117\3\2\2\2K"+
		"\u0119\3\2\2\2M\u011b\3\2\2\2O\u011d\3\2\2\2Q\u011f\3\2\2\2S\u0121\3\2"+
		"\2\2U\u0123\3\2\2\2W\u0125\3\2\2\2Y\u0127\3\2\2\2[\u0129\3\2\2\2]\u012b"+
		"\3\2\2\2_\u012d\3\2\2\2a\u012f\3\2\2\2c\u0131\3\2\2\2e\u0133\3\2\2\2g"+
		"\u0135\3\2\2\2i\u0137\3\2\2\2k\u0139\3\2\2\2m\u013b\3\2\2\2o\u013d\3\2"+
		"\2\2q\u013f\3\2\2\2st\5U+\2tu\5O(\2uv\5W,\2vw\5O(\2wx\5e\63\2x\4\3\2\2"+
		"\2y\u0081\5\17\b\2z\u0081\5\31\r\2{\u0081\5\21\t\2|\u0081\5\25\13\2}\u0081"+
		"\5\23\n\2~\177\7^\2\2\177\u0081\t\2\2\2\u0080y\3\2\2\2\u0080z\3\2\2\2"+
		"\u0080{\3\2\2\2\u0080|\3\2\2\2\u0080}\3\2\2\2\u0080~\3\2\2\2\u0081\6\3"+
		"\2\2\2\u0082\u0085\5)\25\2\u0083\u0086\5\5\3\2\u0084\u0086\n\3\2\2\u0085"+
		"\u0083\3\2\2\2\u0085\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088\5)"+
		"\25\2\u0088\b\3\2\2\2\u0089\u008e\5+\26\2\u008a\u008d\5\5\3\2\u008b\u008d"+
		"\n\4\2\2\u008c\u008a\3\2\2\2\u008c\u008b\3\2\2\2\u008d\u0090\3\2\2\2\u008e"+
		"\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0091\3\2\2\2\u0090\u008e\3\2"+
		"\2\2\u0091\u0092\5+\26\2\u0092\u009e\3\2\2\2\u0093\u0098\5)\25\2\u0094"+
		"\u0097\5\5\3\2\u0095\u0097\n\5\2\2\u0096\u0094\3\2\2\2\u0096\u0095\3\2"+
		"\2\2\u0097\u009a\3\2\2\2\u0098\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099"+
		"\u009b\3\2\2\2\u009a\u0098\3\2\2\2\u009b\u009c\5)\25\2\u009c\u009e\3\2"+
		"\2\2\u009d\u0089\3\2\2\2\u009d\u0093\3\2\2\2\u009e\n\3\2\2\2\u009f\u00a0"+
		"\7\61\2\2\u00a0\u00a1\7,\2\2\u00a1\u00a5\3\2\2\2\u00a2\u00a4\13\2\2\2"+
		"\u00a3\u00a2\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a5\u00a3"+
		"\3\2\2\2\u00a6\u00a8\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8\u00a9\7,\2\2\u00a9"+
		"\u00ba\7\61\2\2\u00aa\u00ab\7\61\2\2\u00ab\u00ac\7\61\2\2\u00ac\u00b0"+
		"\3\2\2\2\u00ad\u00af\n\6\2\2\u00ae\u00ad\3\2\2\2\u00af\u00b2\3\2\2\2\u00b0"+
		"\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1\u00b4\3\2\2\2\u00b2\u00b0\3\2"+
		"\2\2\u00b3\u00b5\7\17\2\2\u00b4\u00b3\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5"+
		"\u00b7\3\2\2\2\u00b6\u00b8\t\7\2\2\u00b7\u00b6\3\2\2\2\u00b8\u00ba\3\2"+
		"\2\2\u00b9\u009f\3\2\2\2\u00b9\u00aa\3\2\2\2\u00ba\f\3\2\2\2\u00bb\u00bc"+
		"\t\b\2\2\u00bc\16\3\2\2\2\u00bd\u00be\t\t\2\2\u00be\20\3\2\2\2\u00bf\u00c0"+
		"\t\n\2\2\u00c0\22\3\2\2\2\u00c1\u00c2\t\13\2\2\u00c2\24\3\2\2\2\u00c3"+
		"\u00c4\t\f\2\2\u00c4\26\3\2\2\2\u00c5\u00c6\t\r\2\2\u00c6\30\3\2\2\2\u00c7"+
		"\u00c8\t\16\2\2\u00c8\32\3\2\2\2\u00c9\u00ca\7/\2\2\u00ca\34\3\2\2\2\u00cb"+
		"\u00cc\7a\2\2\u00cc\36\3\2\2\2\u00cd\u00ce\7\61\2\2\u00ce \3\2\2\2\u00cf"+
		"\u00d0\7^\2\2\u00d0\"\3\2\2\2\u00d1\u00d2\7=\2\2\u00d2$\3\2\2\2\u00d3"+
		"\u00d4\7.\2\2\u00d4&\3\2\2\2\u00d5\u00d6\7<\2\2\u00d6(\3\2\2\2\u00d7\u00d8"+
		"\7)\2\2\u00d8*\3\2\2\2\u00d9\u00da\7$\2\2\u00da,\3\2\2\2\u00db\u00dc\7"+
		"*\2\2\u00dc.\3\2\2\2\u00dd\u00de\7+\2\2\u00de\60\3\2\2\2\u00df\u00e0\7"+
		"]\2\2\u00e0\62\3\2\2\2\u00e1\u00e2\7_\2\2\u00e2\64\3\2\2\2\u00e3\u00e4"+
		"\7@\2\2\u00e4\66\3\2\2\2\u00e5\u00e7\5;\36\2\u00e6\u00e5\3\2\2\2\u00e7"+
		"\u00e8\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e98\3\2\2\2"+
		"\u00ea\u00ec\5=\37\2\u00eb\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00eb"+
		"\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee:\3\2\2\2\u00ef\u00f0\4\62;\2\u00f0"+
		"<\3\2\2\2\u00f1\u010c\5? \2\u00f2\u010c\5A!\2\u00f3\u010c\5C\"\2\u00f4"+
		"\u010c\5E#\2\u00f5\u010c\5G$\2\u00f6\u010c\5I%\2\u00f7\u010c\5K&\2\u00f8"+
		"\u010c\5M\'\2\u00f9\u010c\5O(\2\u00fa\u010c\5Q)\2\u00fb\u010c\5S*\2\u00fc"+
		"\u010c\5U+\2\u00fd\u010c\5W,\2\u00fe\u010c\5Y-\2\u00ff\u010c\5[.\2\u0100"+
		"\u010c\5]/\2\u0101\u010c\5_\60\2\u0102\u010c\5a\61\2\u0103\u010c\5c\62"+
		"\2\u0104\u010c\5e\63\2\u0105\u010c\5g\64\2\u0106\u010c\5i\65\2\u0107\u010c"+
		"\5k\66\2\u0108\u010c\5m\67\2\u0109\u010c\5o8\2\u010a\u010c\5q9\2\u010b"+
		"\u00f1\3\2\2\2\u010b\u00f2\3\2\2\2\u010b\u00f3\3\2\2\2\u010b\u00f4\3\2"+
		"\2\2\u010b\u00f5\3\2\2\2\u010b\u00f6\3\2\2\2\u010b\u00f7\3\2\2\2\u010b"+
		"\u00f8\3\2\2\2\u010b\u00f9\3\2\2\2\u010b\u00fa\3\2\2\2\u010b\u00fb\3\2"+
		"\2\2\u010b\u00fc\3\2\2\2\u010b\u00fd\3\2\2\2\u010b\u00fe\3\2\2\2\u010b"+
		"\u00ff\3\2\2\2\u010b\u0100\3\2\2\2\u010b\u0101\3\2\2\2\u010b\u0102\3\2"+
		"\2\2\u010b\u0103\3\2\2\2\u010b\u0104\3\2\2\2\u010b\u0105\3\2\2\2\u010b"+
		"\u0106\3\2\2\2\u010b\u0107\3\2\2\2\u010b\u0108\3\2\2\2\u010b\u0109\3\2"+
		"\2\2\u010b\u010a\3\2\2\2\u010c>\3\2\2\2\u010d\u010e\t\17\2\2\u010e@\3"+
		"\2\2\2\u010f\u0110\t\20\2\2\u0110B\3\2\2\2\u0111\u0112\t\21\2\2\u0112"+
		"D\3\2\2\2\u0113\u0114\t\22\2\2\u0114F\3\2\2\2\u0115\u0116\t\23\2\2\u0116"+
		"H\3\2\2\2\u0117\u0118\t\24\2\2\u0118J\3\2\2\2\u0119\u011a\t\25\2\2\u011a"+
		"L\3\2\2\2\u011b\u011c\t\26\2\2\u011cN\3\2\2\2\u011d\u011e\t\27\2\2\u011e"+
		"P\3\2\2\2\u011f\u0120\t\30\2\2\u0120R\3\2\2\2\u0121\u0122\t\31\2\2\u0122"+
		"T\3\2\2\2\u0123\u0124\t\32\2\2\u0124V\3\2\2\2\u0125\u0126\t\33\2\2\u0126"+
		"X\3\2\2\2\u0127\u0128\t\34\2\2\u0128Z\3\2\2\2\u0129\u012a\t\35\2\2\u012a"+
		"\\\3\2\2\2\u012b\u012c\t\36\2\2\u012c^\3\2\2\2\u012d\u012e\t\37\2\2\u012e"+
		"`\3\2\2\2\u012f\u0130\t \2\2\u0130b\3\2\2\2\u0131\u0132\t!\2\2\u0132d"+
		"\3\2\2\2\u0133\u0134\t\"\2\2\u0134f\3\2\2\2\u0135\u0136\t#\2\2\u0136h"+
		"\3\2\2\2\u0137\u0138\t$\2\2\u0138j\3\2\2\2\u0139\u013a\t%\2\2\u013al\3"+
		"\2\2\2\u013b\u013c\t&\2\2\u013cn\3\2\2\2\u013d\u013e\t\'\2\2\u013ep\3"+
		"\2\2\2\u013f\u0140\t(\2\2\u0140r\3\2\2\2\22\2\u0080\u0085\u008c\u008e"+
		"\u0096\u0098\u009d\u00a5\u00b0\u00b4\u00b7\u00b9\u00e8\u00ed\u010b\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}