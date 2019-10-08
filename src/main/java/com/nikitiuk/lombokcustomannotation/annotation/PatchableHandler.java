package com.nikitiuk.lombokcustomannotation.annotation;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
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
@HandlerPriority(value = 1024)
public class PatchableHandler extends JavacAnnotationHandler<Patchable> {

    @Override
    public void handle(AnnotationValues<Patchable> annotation,
                       JCAnnotation ast,
                       JavacNode annotationNode) {

        Context context = annotationNode.getContext();
        Javac8BasedLombokOptions options = Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
        options.deleteLombokAnnotations();

        deleteAnnotationIfNeccessary(annotationNode, Patchable.class);
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        JavacNode patchableClass = annotationNode.up();
        JavacTreeMaker patchableClassTreeMaker = patchableClass.getTreeMaker();

        generateList(patchableClass, patchableClassTreeMaker);

        generateGetter(patchableClass, patchableClassTreeMaker);
    }

    private void generateList(JavacNode patchableClass, JavacTreeMaker patchableClassTM) {
        JCModifiers fieldMod = patchableClassTM.Modifiers(Flags.PUBLIC | Flags.STATIC);

        JCNewClass newHashMap = patchableClassTM
                .NewClass(null, nil(), chainDots(patchableClass, "java", "util", "HashSet"), nil(),   null);

        JCVariableDecl instanceVar = patchableClassTM.VarDef(
                fieldMod,
                patchableClass.toName("listOfSettedVars"),
                chainDots(patchableClass, "java", "util", "HashSet"),
                newHashMap
        );

        injectField(patchableClass, instanceVar);
    }

    private void generateGetter(JavacNode patchableClass, JavacTreeMaker patchableClassTM) {
        JCModifiers modifiers = patchableClassTM.Modifiers(Flags.PUBLIC);
        JCExpression methodType = chainDots(patchableClass, "java", "util", "HashSet");
        Name methodName = patchableClass.toName("getListOfSettedVars");
        JCBlock methodBlock = addReturnBlock(patchableClass, patchableClassTM);

        JCMethodDecl methodDecl = patchableClassTM.MethodDef(
                modifiers,
                methodName,
                methodType,
                nil(), nil(), nil(),
                methodBlock,
                null
        );

        injectMethod(patchableClass, methodDecl);
    }

    private JCBlock addReturnBlock(JavacNode patchableClass, JavacTreeMaker patchableClassTM) {
        JCClassDecl holderInnerClassDecl = (JCClassDecl) patchableClass.get();
        JCIdent holderInnerClassType = patchableClassTM.Ident(holderInnerClassDecl.name);

        JCFieldAccess mapVarAccess = patchableClassTM.Select(holderInnerClassType, patchableClass.toName("listOfSettedVars"));
        JCReturn returnValue = patchableClassTM.Return(mapVarAccess);

        ListBuffer<JCStatement> statements = new ListBuffer<>();
        statements.append(returnValue);

        return patchableClassTM.Block(0L, statements.toList());
    }
}