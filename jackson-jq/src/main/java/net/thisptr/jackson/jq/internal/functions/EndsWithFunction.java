package net.thisptr.jackson.jq.internal.functions;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import net.thisptr.jackson.jq.Expression;
import net.thisptr.jackson.jq.Function;
import net.thisptr.jackson.jq.Output;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.internal.BuiltinFunction;
import net.thisptr.jackson.jq.internal.misc.Preconditions;

@BuiltinFunction("endswith/1")
public class EndsWithFunction implements Function {
	@Override
	public void apply(final Scope scope, final List<Expression> args, final JsonNode in, final Output output) throws JsonQueryException {
		Preconditions.checkInputType("endswith", in, JsonNodeType.STRING);

		final String text = in.asText();

		args.get(0).apply(scope, in, (suffix) -> {
			if (!suffix.isTextual())
				throw new JsonQueryException("1st argument of endswith() must evaluate to string");
			output.emit(BooleanNode.valueOf(text.endsWith(suffix.asText())));
		});
	}
}
