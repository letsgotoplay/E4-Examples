package org.annotation.ast;

import static lombok.javac.Javac.CTC_INT;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;
import static lombok.javac.handlers.JavacHandlerUtil.deleteImportFromCompilationUnit;

import org.kohsuke.MetaInfServices;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.core.AnnotationValues;
import lombok.javac.Javac8BasedLombokOptions;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

@MetaInfServices(JavacAnnotationHandler.class)
public class SingletonJavacHandler extends JavacAnnotationHandler<Singleton> {

	@Override
	public void handle(AnnotationValues<Singleton> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {

		Context context = annotationNode.getContext();

		Javac8BasedLombokOptions options = Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
		options.deleteLombokAnnotations();

		// remove annotation
		deleteAnnotationIfNeccessary(annotationNode, Singleton.class);
		// remove import
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

		// private constructor
		JavacNode singletonClass = annotationNode.up();
		JavacTreeMaker singletonClassTreeMaker = singletonClass.getTreeMaker();

		addPrivateConstructor(singletonClass, singletonClassTreeMaker);

		// singleton holder
		JavacNode holderInnerClass = addInnerClass(singletonClass, singletonClassTreeMaker);

		// inject static field to this
		addInstanceVar(singletonClass, singletonClassTreeMaker, holderInnerClass);

		// add factory method
		addFactoryMethod(singletonClass, singletonClassTreeMaker, holderInnerClass);
		// add void method
		addVoidEmptyMethod(singletonClass, singletonClassTreeMaker);
	}

	private void addVoidEmptyMethod(JavacNode singletonClass, JavacTreeMaker singletonClassTreeMaker) {
		JCExpression returnType = singletonClassTreeMaker.TypeIdent(CTC_INT);
		JCTree.JCModifiers modifier = singletonClassTreeMaker.Modifiers(Flags.PUBLIC);
		JCTree.JCBlock block = addEmptyReturnBlock(singletonClass, singletonClassTreeMaker);
		JCTree.JCMethodDecl voidMethod = singletonClassTreeMaker.MethodDef(modifier,
				singletonClass.toName("__methodHook__"), returnType, List.nil(), List.nil(), List.nil(), block, null);
		JavacHandlerUtil.injectMethod(singletonClass, voidMethod);

	}

	private JCTree.JCBlock addEmptyReturnBlock(JavacNode typeNode, JavacTreeMaker maker) {
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		Name resultName = typeNode.toName("result");
		/* int result = 1; */ {
			statements.append(maker.VarDef(maker.Modifiers(0), resultName, maker.TypeIdent(CTC_INT), maker.Literal(1)));
		}
		statements.append(maker.Return(maker.Ident(resultName)));
		JCBlock body = maker.Block(0, statements.toList());
		return body;
	}

	private void addFactoryMethod(JavacNode singletonClass, JavacTreeMaker singletonClassTreeMaker,
			JavacNode holderInnerClass) {
		JCTree.JCModifiers modifiers = singletonClassTreeMaker.Modifiers(Flags.PUBLIC | Flags.STATIC);

		JCTree.JCClassDecl singletonClassDecl = (JCTree.JCClassDecl) singletonClass.get();
		JCTree.JCIdent singletonClassType = singletonClassTreeMaker.Ident(singletonClassDecl.name);

		JCTree.JCBlock block = addReturnBlock(singletonClassTreeMaker, holderInnerClass);

		JCTree.JCMethodDecl factoryMethod = singletonClassTreeMaker.MethodDef(modifiers,
				singletonClass.toName("getInstance"), singletonClassType, List.nil(), List.nil(), List.nil(), block,
				null);
		JavacHandlerUtil.injectMethod(singletonClass, factoryMethod);
	}

	private JCTree.JCBlock addReturnBlock(JavacTreeMaker singletonClassTreeMaker, JavacNode holderInnerClass) {

		JCTree.JCClassDecl holderInnerClassDecl = (JCTree.JCClassDecl) holderInnerClass.get();
		JavacTreeMaker holderInnerClassTreeMaker = holderInnerClass.getTreeMaker();
		JCTree.JCIdent holderInnerClassType = holderInnerClassTreeMaker.Ident(holderInnerClassDecl.name);

		JCTree.JCFieldAccess instanceVarAccess = holderInnerClassTreeMaker.Select(holderInnerClassType,
				holderInnerClass.toName("INSTANCE"));
		JCTree.JCReturn returnValue = singletonClassTreeMaker.Return(instanceVarAccess);

		ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
		statements.append(returnValue);

		return singletonClassTreeMaker.Block(0L, statements.toList());
	}

	private void addInstanceVar(JavacNode singletonClass, JavacTreeMaker singletonClassTM, JavacNode holderClass) {
		JCTree.JCModifiers fieldMod = singletonClassTM.Modifiers(Flags.PRIVATE | Flags.STATIC | Flags.FINAL);

		JCTree.JCClassDecl singletonClassDecl = (JCTree.JCClassDecl) singletonClass.get();
		JCTree.JCIdent singletonClassType = singletonClassTM.Ident(singletonClassDecl.name);

		JCTree.JCNewClass newKeyword = singletonClassTM.NewClass(null, List.nil(), singletonClassType, List.nil(),
				null);

		JCTree.JCVariableDecl instanceVar = singletonClassTM.VarDef(fieldMod, singletonClass.toName("INSTANCE"),
				singletonClassType, newKeyword);
		JavacHandlerUtil.injectField(holderClass, instanceVar);
	}

	private JavacNode addInnerClass(JavacNode singletonClass, JavacTreeMaker singletonTM) {
		JCTree.JCModifiers modifiers = singletonTM.Modifiers(Flags.PRIVATE | Flags.STATIC);
		String innerClassName = singletonClass.getName() + "Holder";
		JCTree.JCClassDecl innerClassDecl = singletonTM.ClassDef(modifiers, singletonClass.toName(innerClassName),
				List.nil(), null, List.nil(), List.nil());
		return JavacHandlerUtil.injectType(singletonClass, innerClassDecl);
	}

	private void addPrivateConstructor(JavacNode singletonClass, JavacTreeMaker singletonTM) {
		JCTree.JCModifiers modifiers = singletonTM.Modifiers(Flags.PRIVATE);
		JCTree.JCBlock block = singletonTM.Block(0L, List.nil());
		JCTree.JCMethodDecl constructor = singletonTM.MethodDef(modifiers, singletonClass.toName("<init>"), null,
				List.nil(), List.nil(), List.nil(), block, null);

		JavacHandlerUtil.injectMethod(singletonClass, constructor);
	}

}
