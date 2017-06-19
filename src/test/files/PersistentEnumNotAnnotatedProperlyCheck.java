//import org.sonar.plugins.java.api.tree.ClassTree;
//import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import javax.swing.ButtonGroup;
//import org.sonar.sslr.grammar.GrammarRuleKey;

import javax.persistence.EnumType;

@Entity
public class PersistentEnumNotAnnotatedProperlyCheck {
	
	@Enumerated // Noncompliant
	private Kind myField;
	
	@Enumerated(EnumType.STRING)
	private ButtonGroup button;
	
	@Enumerated(EnumType.ORDINAL)// Noncompliant
	private Kind myField2;
	
	@Enumerated(value = EnumType.STRING)
	private Kind myField3;
	
	private Kind myField4; // Noncompliant
	
	private static Kind myField5;
}