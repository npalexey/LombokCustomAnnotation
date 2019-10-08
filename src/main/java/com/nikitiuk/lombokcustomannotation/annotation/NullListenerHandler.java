package com.nikitiuk.lombokcustomannotation.annotation;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.Javac8BasedLombokOptions;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kohsuke.MetaInfServices;

import static com.sun.tools.javac.util.List.nil;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(value = 2048)
public class NullListenerHandler extends JavacAnnotationHandler<NullListener> {

    @Override
    public void handle(AnnotationValues<NullListener> annotation,
                       JCAnnotation ast,
                       JavacNode annotationNode) {
        Context context = annotationNode.getContext();
        Javac8BasedLombokOptions options = Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
        options.deleteLombokAnnotations();

        deleteAnnotationIfNeccessary(annotationNode, NullListener.class);
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        JavacNode field = annotationNode.up();
        generateSetter(field);
    }

    private void generateSetter(JavacNode fieldNode) {
        JavacTreeMaker fieldTM = fieldNode.getTreeMaker();
        JCModifiers modifiers = fieldTM.Modifiers(Flags.PUBLIC);
        Name methodName = fieldNode.toName(toSetterName(fieldNode));

        JCTree.JCExpression paramType = chainDots(fieldNode, "java", "lang", "Object");
        JCTree.JCVariableDecl param = fieldTM.VarDef(fieldTM.Modifiers(Flags.PARAMETER), fieldNode.toName("val"), paramType, null);

        JCBlock methodBlock = addReturnBlock(fieldNode, fieldTM);

        JCTree.JCMethodDecl createdSetter = fieldTM.MethodDef(
                modifiers,
                methodName,
                chainDots(fieldNode, "java", "util", "HashSet"),
                nil(),
                List.of(param),
                nil(),
                methodBlock,
                null
        );
        injectMethod(fieldNode.up(), createdSetter);
    }


    private JCBlock addReturnBlock(JavacNode field, JavacTreeMaker fieldTM) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        JavacNode aClass = field.directUp();
        JavacTreeMaker aClassTM = aClass.getTreeMaker();
        JCTree.JCClassDecl holderInnerClassDecl = (JCTree.JCClassDecl) aClass.get();
        JCTree.JCIdent holderInnerClassType = aClass.getTreeMaker().Ident(holderInnerClassDecl.name);

        JCTree.JCExpression expression = chainDots(field, "this", "listOfSettedVars", "add");
        JCTree.JCNewClass newString = fieldTM
                .NewClass(null, nil(), chainDots(field, "java", "lang", "String"),
                        List.of(fieldTM.Literal(field.getName())),   null);

        JCTree.JCExpression invokeAdd  = fieldTM.Apply(List.nil(), expression, List.of(newString));
        statements.append(fieldTM.Exec(invokeAdd));

        JCTree.JCFieldAccess mapVarAccess = aClassTM.Select(holderInnerClassType, aClass.toName("listOfSettedVars"));
        JCTree.JCReturn returnValue = fieldTM.Return(mapVarAccess);
        statements.append(returnValue);

        return fieldTM.Block(0L, statements.toList());
    }
}
