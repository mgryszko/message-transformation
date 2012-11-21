package com.grysz

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class AsMessageASTTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: ${nodes.toList()}", nodes[0], source);
        }
        MethodNode method = nodes[1]
        if (method.parameters.size() > 1) {
            method.declaringClass.addMethod new MethodNode(
                method.name,
                method.modifiers,
                method.returnType,
                messageMethodParametersFrom(method.parameters),
                method.exceptions,
                messageMethodBodyCalling(method),
            )
        }
    }

    private addError(msg, expr, source) {
        def syntaxEx = new SyntaxException("$msg\n", expr.getLineNumber(), expr.getColumnNumber())
        source.getErrorCollector().addError new SyntaxErrorMessage(syntaxEx, source)
    }

    private messageMethodParametersFrom(params) {
        [
            new Parameter(new ClassNode(Map), 'args'),
            new Parameter(params[0].type, params[0].name),
        ] as Parameter[]
    }

    private messageMethodBodyCalling(method) {
        new BlockStatement([
            new ExpressionStatement(
                new MethodCallExpression(THIS_EXPRESSION, method.name, argsToCallOriginalMethod(method.parameters))
            )], new VariableScope()
        )
    }

    private argsToCallOriginalMethod(params) {
        def argsParam = new VariableExpression('args')

        def args = [new VariableExpression(params[0].name)] + allButFirst(params).collect {
            if (it.hasInitialExpression()) {
                new TernaryExpression(
                    new BooleanExpression(containsKey(argsParam, it.name)), // if
                    valueFromMap(argsParam, it.name), // then
                    it.initialExpression // else
                )
            } else {
                valueFromMap(argsParam, it.name)
            }
        }

        new ArgumentListExpression(args)
    }

    private allButFirst(c) {
        c[1..c.size() - 1]
    }

    private valueFromMap(map, key) {
        new PropertyExpression(map, key)
    }

    private containsKey(map, key) {
        new MethodCallExpression(map, 'containsKey', new ArgumentListExpression(new ConstantExpression(key)))
    }
}
