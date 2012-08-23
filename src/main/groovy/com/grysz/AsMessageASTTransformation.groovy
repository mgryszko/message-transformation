package com.grysz
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
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

        MethodNode message = new MethodNode(
            method.name,
            method.modifiers,
            method.returnType,
            new Parameter(new ClassNode(Map), 'args') as Parameter[],
            method.exceptions,
            new BlockStatement(
                [
                    new ExpressionStatement(
                        new MethodCallExpression(
                            VariableExpression.THIS_EXPRESSION,
                            method.name,
                            new ArgumentListExpression(
                                new PropertyExpression(new VariableExpression('args'), method.parameters[0].name)
                            )
                        )
                    )
                ],
                new VariableScope()
            )
        )
        /*
        method(param1, param2)

        method(Map args) {
            method(args.param1, args.param2)
        }
         */

        method.declaringClass.addMethod(message)
    }

    private addError(msg, expr, source) {
        def syntaxEx = new SyntaxException("$msg\n", expr.getLineNumber(), expr.getColumnNumber())
        source.getErrorCollector().addError new SyntaxErrorMessage(syntaxEx, source)
    }
}
