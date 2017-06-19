package org.sonar.samples.java.checks;

import java.util.List;

import javax.persistence.EnumType;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.VariableTree;

@Rule(
		key = "PersistentEnumNotAnnotatedProperly",
		name = "OW - Persistent Enum not defined as EnumType.STRING",
		description = "Persistent Enums must be defined with @Enumerated(EnumType.STRING).",
		priority = Priority.BLOCKER,
		tags = {"enum"})
public class PersistentEnumNotAnnotatedProperlyRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;

	private String nameEntityClass;

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;

		scan(context.getTree());
		System.out.println(PrinterVisitor.print(context.getTree()));
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		if(tree.superClass() != null) {
			if (tree.superClass().is(Tree.Kind.PARAMETERIZED_TYPE)) {
				ParameterizedTypeTree param = (ParameterizedTypeTree) tree.superClass();
				if (param.type().toString().equals("GenericEntity")) {
					nameEntityClass = tree.symbol().toString();
				}
			}
		}
		List<AnnotationTree> annotations = tree.modifiers().annotations();
		for (AnnotationTree annotationTree : annotations) {
			if (annotationTree.annotationType().is(Tree.Kind.IDENTIFIER)) {
				IdentifierTree idf = (IdentifierTree) annotationTree.annotationType();
				if (idf.name().equals("Entity") || idf.name().equals("MappedSuperclass") || idf.name().equals("Embeddable")) {
					nameEntityClass = tree.symbol().toString();
				}
			}
		}
		
		super.visitClass(tree);
	}
	
	@Override
	public void visitVariable(VariableTree tree) {
		boolean isAnnotedEnumerated = Boolean.FALSE;
		
		if (tree.parent().is(Tree.Kind.CLASS) && tree.type().symbolType().symbol().isEnum() && !tree.symbol().isStatic()) {
			if (((ClassTree) tree.parent()).symbol().toString().equals(nameEntityClass)) {
				List<AnnotationTree> annotations = tree.modifiers().annotations();
				for (AnnotationTree annotationTree : annotations) {
					if (annotationTree.annotationType().is(Tree.Kind.IDENTIFIER)) {
						IdentifierTree idf = (IdentifierTree) annotationTree.annotationType();
						if (idf.name().equals("Transient")) {
							break;
						}
						if (idf.name().equals("Enumerated")) {
							isAnnotedEnumerated = Boolean.TRUE;
							if(annotationTree.arguments().size()>0) {
								if (annotationTree.arguments().get(0).is(Kind.MEMBER_SELECT)) {
									if (!((MemberSelectExpressionTree) annotationTree.arguments().get(0)).identifier().name().equals(EnumType.STRING.name())) {
										context.reportIssue(this, tree, String.format("Persistent Enum not defined as "
												+ "@Enumerated(EnumType.STRING) as it should be"));
									}
									else break;
								} else if (annotationTree.arguments().get(0).is(Kind.ASSIGNMENT)) {
									if (!((AssignmentExpressionTree) annotationTree.arguments().get(0)).lastToken().text().equals(EnumType.STRING.name())) {
										context.reportIssue(this, tree, String.format("Persistent Enum not defined as "
												+ "@Enumerated(EnumType.STRING) as it should be"));
									}
									else break;
								} 
							}
							else context.reportIssue(this, tree, String.format("Persistent Enum defined as "
									+ "EnumType.ORDINAL and not EnumType.STRING as it should be"));
						}
					}
				}
				if (!isAnnotedEnumerated) {
					context.reportIssue(this, tree, String.format("Persistent Enum should be annoted @Enumerated(EnumType.STRING)"));
				}
			}
		}
		super.visitVariable(tree);
	}
}
