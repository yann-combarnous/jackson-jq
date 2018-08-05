package net.thisptr.jackson.jq.internal.functions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import net.thisptr.jackson.jq.Expression;
import net.thisptr.jackson.jq.Function;
import net.thisptr.jackson.jq.Output;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.internal.BuiltinFunction;
import net.thisptr.jackson.jq.internal.misc.JsonNodeComparator;
import net.thisptr.jackson.jq.internal.misc.Preconditions;

@BuiltinFunction("indices/1")
public class IndicesFunction implements Function {
	private static final JsonNodeComparator comparator = JsonNodeComparator.getInstance();

	@Override
	public void apply(final Scope scope, final List<Expression> args, final JsonNode in, final Output output) throws JsonQueryException {
		Preconditions.checkInputType("indices", in, JsonNodeType.STRING, JsonNodeType.ARRAY);

		args.get(0).apply(scope, in, (needle) -> {
			final ArrayNode indices = scope.getObjectMapper().createArrayNode();
			for (final int index : indices(needle, in))
				indices.add(index);
			output.emit(indices);
		});
	}

	public static List<Integer> indices(final JsonNode needle, final JsonNode haystack) throws JsonQueryException {
		final List<Integer> result = new ArrayList<>();
		if (needle.isTextual() && haystack.isTextual()) {
			final String haystackText = haystack.asText();
			final String needleText = needle.asText();
			for (int index = haystackText.indexOf(needleText); index >= 0; index = haystackText.indexOf(needleText, index + 1))
				result.add(index);
		} else if (needle.isArray() && haystack.isArray()) {
			if (needle.size() != 0) {
				for (int i = 0; i < haystack.size(); ++i) {
					boolean match = true;
					for (int j = 0; j < needle.size(); ++j) {
						if (i + j >= haystack.size() || comparator.compare(haystack.get(i + j), needle.get(j)) != 0) {
							match = false;
							break;
						}
					}
					if (match)
						result.add(i);
				}
			}
		} else if (haystack.isArray()) {
			for (int i = 0; i < haystack.size(); ++i)
				if (comparator.compare(haystack.get(i), needle) == 0)
					result.add(i);
		} else {
			throw new JsonQueryException("indices() is not applicable to " + haystack.getNodeType());
		}
		return result;
	}
}
