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
                toMessageParams(method.parameters),
                method.exceptions,
                callTransformedMethodBody(method.name, method.parameters),
            )
        }
    }

    private toMessageParams(params) {
        [
            new Parameter(new ClassNode(Map), 'args'),
            new Parameter(params[0].type, params[0].name),
        ] as Parameter[]
    }

    private callTransformedMethodBody(name, params) {
        def args = new VariableExpression('args')

        def transformedMethodArgs = [new VariableExpression(params[0].name)] +
            params[1..params.size() - 1].collect { Parameter it ->
                if (it.hasInitialExpression()) {
                    new TernaryExpression(
                        new BooleanExpression(
                            new MethodCallExpression(
                                args,
                                'containsKey',
                                new ArgumentListExpression(new ConstantExpression(it.name))
                            )
                        ),
                        new PropertyExpression(args, it.name), // then
                        it.initialExpression // else
                    )
                } else {
                    new PropertyExpression(args, it.name)
                }
            }
        new BlockStatement([
            new ExpressionStatement(
                new MethodCallExpression(
                    VariableExpression.THIS_EXPRESSION,
                    name,
                    new ArgumentListExpression(transformedMethodArgs)
                )
            )], new VariableScope()
        )
    }

    private addError(msg, expr, source) {
        def syntaxEx = new SyntaxException("$msg\n", expr.getLineNumber(), expr.getColumnNumber())
        source.getErrorCollector().addError new SyntaxErrorMessage(syntaxEx, source)
    }
}
