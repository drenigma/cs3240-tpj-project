package walker.expressions;

import ast.ExpressionNode;
import ast.Node;
import java.util.List;
import walker.ExpressionDelegate;
import walker.exceptions.ASTExecutionException;
import walker.exceptions.IncorrectNodeTypeException;

public class IDStatement1Expression implements ExpressionExpander {
    @Override
    public Object expand(ExpressionNode node, ExpressionDelegate delegate, Object param) throws ASTExecutionException {
        List<Node> subnodes = node.subnodes();

        if (subnodes.size() != 1 || !(subnodes.get(0) instanceof ExpressionNode)) {
            throw new IncorrectNodeTypeException(this.getClass().getSimpleName() + " Error: Requires 1 ExpressionNode", subnodes.get(0));
        }

        return delegate.expand((ExpressionNode) subnodes.get(0), null);
    }

    public static String type() {
        return "ID_STATEMENT1";
    }
}
