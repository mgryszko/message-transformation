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
    private static final String NAMED_PARAMS = '$_args'

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
            new Parameter(new ClassNode(Map), NAMED_PARAMS),
            new Parameter(params[0].type, params[0].name, params[0].initialExpression),
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
        def argsParam = new VariableExpression(NAMED_PARAMS)

        def args = [new VariableExpression(params[0].name)] + allButFirst(params).collect { param ->
            if (param.hasInitialExpression()) {
                new TernaryExpression(
                    new BooleanExpression(containsKey(argsParam, param.name)), // if
                    getAndCastToArray(argsParam, param), // then
                    param.initialExpression // else
                )
            } else {
                getAndCastToArray(argsParam, param)
            }
        }

        new ArgumentListExpression(args)
    }

    private allButFirst(c) {
        c[1..c.size() - 1]
    }

    private getAndCastToArray(argsParam, param) {
        def expr = get(argsParam, param.name)
        param.type.typeClass.array ? CastExpression.asExpression(param.type, expr) : expr
    }

    private get(map, key) {
        new PropertyExpression(map, key)
    }

    private containsKey(map, key) {
        new MethodCallExpression(map, 'containsKey', new ArgumentListExpression(new ConstantExpression(key)))
    }
}
