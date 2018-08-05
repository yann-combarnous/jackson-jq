package net.thisptr.jackson.jq.internal.tree.binaryop.assignment;

import com.fasterxml.jackson.databind.JsonNode;

import net.thisptr.jackson.jq.Expression;
import net.thisptr.jackson.jq.Output;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.exception.IllegalJsonArgumentException;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.internal.misc.JsonNodeUtils;
import net.thisptr.jackson.jq.internal.misc.JsonNodeUtils.Mutation;
import net.thisptr.jackson.jq.internal.operators.BinaryOperator;
import net.thisptr.jackson.jq.internal.tree.ThisObject;
import net.thisptr.jackson.jq.internal.tree.binaryop.BinaryOperatorExpression;
import net.thisptr.jackson.jq.internal.tree.fieldaccess.FieldAccess;
import net.thisptr.jackson.jq.internal.tree.fieldaccess.FieldAccess.ResolvedPath;

public class ComplexAssignment extends BinaryOperatorExpression {
	private BinaryOperator operator;

	public ComplexAssignment(final Expression lhs, final Expression rhs, final BinaryOperator operator) {
		super(lhs, rhs, operator.image() + "=");
		this.operator = operator;
	}

	@Override
	public void apply(final Scope scope, final JsonNode in, final Output output) throws JsonQueryException {
		if (!(lhs instanceof FieldAccess))
			throw new IllegalJsonArgumentException("left hand side must be FieldAccess");

		final ResolvedPath resolvedPath = ((FieldAccess) lhs).resolvePath(scope, in);
		if (!(resolvedPath.target instanceof ThisObject))
			throw new IllegalJsonArgumentException("cannot update value");

		rhs.apply(scope, in, (rvalue) -> {
			output.emit(JsonNodeUtils.mutate(scope.getObjectMapper(), in, resolvedPath.path, new Mutation() {
				@Override
				public JsonNode apply(final JsonNode node) throws JsonQueryException {
					return operator.apply(scope.getObjectMapper(), node, rvalue);
				}
			}, true));
		});
	}
}
