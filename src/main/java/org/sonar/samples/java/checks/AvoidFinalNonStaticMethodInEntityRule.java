package org.sonar.samples.java.checks;

import java.util.List;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(
		key = "AvoidFinalNonStaticMethodInEntity",
		name = "OW - Public method is final in persisted Entity",
		description = "It is not a good idea to have public methods final in persisted entities as you can have issues with the proxyfication process.",
		priority = Priority.BLOCKER,
		tags = {"entity"})
public class AvoidFinalNonStaticMethodInEntityRule extends BaseTreeVisitor implements JavaFileScanner {

	private JavaFileScannerContext context;

	private boolean isEntityClass = Boolean.FALSE;

	@Override
	public void scanFile(JavaFileScannerContext context) {
		this.context = context;

		scan(context.getTree());
	}
	
	@Override
	public void visitClass(ClassTree tree) {
		isEntityClass = false;
		//Les superclasses avec des templates comme GenericEntity sont castées en ParameterizedTypeTree 
		//pour permettre l'accès aux informations
		if(tree.superClass() != null) {
			if (tree.superClass().is(Tree.Kind.PARAMETERIZED_TYPE)) {
				ParameterizedTypeTree param = (ParameterizedTypeTree) tree.superClass();
				if (param.type().toString().equals("GenericEntity")) {
					isEntityClass = Boolean.TRUE;
				}
			}
		}
		List<AnnotationTree> annotations = tree.modifiers().annotations();
		for (AnnotationTree annotationTree : annotations) {
			if (annotationTree.annotationType().is(Tree.Kind.IDENTIFIER)) {
				IdentifierTree idf = (IdentifierTree) annotationTree.annotationType();
				
				if (idf.name().equals("Entity") || idf.name().equals("MappedSuperclass") || idf.name().equals("Embeddable")) {
					isEntityClass = Boolean.TRUE;
				}
			}
		}
		
		super.visitClass(tree);
	}

	@Override
	public void visitMethod(MethodTree tree) {
		 if (isEntityClass) {
			 if (tree.symbol().isFinal() && !tree.symbol().isStatic()) {
				 context.reportIssue(this, tree, String.format("Avoid static final methods in entities, "
				 		+ "it causes issues with the proxification process"));
			 }
		 }
		 
		super.visitMethod(tree);
	}
}
