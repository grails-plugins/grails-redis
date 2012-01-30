package grails.plugin.redis.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class MemoizeASTTransformation extends AbstractMemoizeASTTransformation {

    @Override
    protected void generateMemoizeProperties(ASTNode[] astNodes, SourceUnit sourceUnit, Map memoizeProperties) {
        def expire = astNodes[0]?.members?.expire?.text
        def keyString = astNodes[0]?.members?.key?.text
        def keyClosure = astNodes[0]?.members?.value

        if(keyClosure?.class != ClosureExpression && keyString.class != String) {
            addError("Internal Error: annotation doesn't contain key closure or key property", astNodes[0], sourceUnit)
            return
        }

        if(keyClosure && keyClosure.code?.statements[0]?.expression?.value?.class != String) {
            addError("Internal Error: annotation doesn't contain string key closure", astNodes[0], sourceUnit)
            return
        }

        if(expire && expire.class != String && !Integer.parseInt(expire)) {
            addError("Internal Error: provided expire is not an String (in millis)", astNodes[0], sourceUnit)
            return
        }
        //***************************************************************************

        memoizeProperties.put(KEY, (keyClosure) ? keyClosure?.code?.statements[0]?.expression?.value : keyString)
        if(expire) {
            memoizeProperties.put(EXPIRE, expire)
        }
    }

    @Override
    protected ConstantExpression createRedisServiceConstantExpression() {
        return new ConstantExpression("memoize")
    }

    @Override
    protected ArgumentListExpression createRedisServiceArgumentListExpression(Map memoizeProperties) {
        ArgumentListExpression argumentListExpression = new ArgumentListExpression()
        createRedisServiceMemoizeKeyExpression(memoizeProperties, argumentListExpression)
        if(memoizeProperties.containsKey(MEMBER)) {
            argumentListExpression.addExpression(createConstantExpression(memoizeProperties.get(MEMBER).toString()))
        }
        if(memoizeProperties.containsKey(EXPIRE)) {
            argumentListExpression.addExpression(createConstantExpression(Integer.parseInt(memoizeProperties.get(EXPIRE).toString())))
        }
        return argumentListExpression
    }
}