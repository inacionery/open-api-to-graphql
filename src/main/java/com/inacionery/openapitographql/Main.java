package com.inacionery.openapitographql;

import com.inacionery.openapitographql.model.Input;
import com.inacionery.openapitographql.model.Mutation;
import com.inacionery.openapitographql.model.Property;
import com.inacionery.openapitographql.model.Query;
import com.inacionery.openapitographql.model.Type;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.yaml.snakeyaml.Yaml;

/**
 * @author Inácio Nery
 */
public class Main {

	public static void main(String[] args) throws IOException {
		Map<String, Object> root = _readFile("rest-openapi.yaml");

		List<Type> types = _createtypes(root);

		List<Query> queries = _createQueries(types, root);

		List<Mutation> mutations = _createMutations(types, root);

		List<Input> inputs = _createInputs(types, mutations);

		String graphQLContent =
			_getGraphQLContent(types, inputs, queries, mutations);

		_writeFile(graphQLContent, "rest-openapi.graphql");
	}

	private static String _capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private static void _createInput(
		List<Input> inputs, List<Type> types, Set<Type> inputTypes) {

		for (Type type : inputTypes) {
			Input input = new Input();

			input.setName(type.getName());

			for (Property property : type.getProperties()) {
				input.addProperty(
					new Property(
						property.getName(), property.getType(),
						property.isRequired(), property.isReference()));
			}

			if (!inputs.contains(input)) {
				inputs.add(input);

				_createInput(
					inputs, types, _getInputType(types, type.getProperties()));
			}
		}
	}

	private static List<Input> _createInputs(
		List<Type> types, List<Mutation> mutations) {

		List<Input> inputs = new ArrayList<>();

		Set<Type> inputTypes = new HashSet<>();

		for (Mutation mutation : mutations) {
			inputTypes.addAll(_getInputType(types, mutation.getProperties()));

			mutation.getProperties().forEach(
				property -> {
					if (property.isReference()) {
						property.setType(property.getType() + "Input");
					}
				});
		}

		_createInput(inputs, types, inputTypes);

		for (Input input : inputs) {
			input.getProperties().forEach(
				property -> {
					if (property.isReference()) {
						property.setType(property.getType() + "Input");
					}
				});
		}

		return inputs;
	}

	private static List<Mutation> _createMutations(
		List<Type> types, Map<String, Object> root) {

		List<Mutation> mutations = new ArrayList<>();

		Map<String, Object> paths =
			(Map<String, Object>)root.get("paths");

		paths.forEach(
			(queryName, path) -> {
				Map<String, Object> post =
					(Map<String, Object>)((Map<String, Object>)path).get(
						"post");

				if (post != null) {
					Mutation mutation = new Mutation(queryName);

					List<Map<String, Object>> parameters =
						(List<Map<String, Object>>)post.get("parameters");

					parameters.forEach(
						paramenter -> {
							Map<String, Object> schema =
								(Map<String, Object>)paramenter.get("schema");

							mutation.addProperty(
								new Property(
									(String)paramenter.get("name"),
									_getTypeValue(
										types,
										(String)paramenter.get("name"), schema),
									(Boolean)paramenter.get("required"),
									_isReference(
										(Map<String, Object>)schema)));
						});

					Map<String, Object> requestBody =
						(Map<String, Object>)post.get("requestBody");

					Map<String, Object> contents =
						(Map<String, Object>)requestBody.get("content");

					Map<String, Object> content =
						(Map<String, Object>)contents.get("*/*");

					Map<String, Object> schema =
						(Map<String, Object>)content.get("schema");

					mutation.addProperty(
						new Property(
							"requestBody",
							_getTypeValue(types, "requestBody", schema), true,
							_isReference((Map<String, Object>)schema)));

					mutation.setReturnType(_getReturnType(types, post));

					mutations.add(mutation);
				}
			});

		return mutations;
	}

	private static List<Query> _createQueries(
		List<Type> types, Map<String, Object> root) {

		List<Query> queries = new ArrayList<>();

		Map<String, Object> paths = (Map<String, Object>)root.get("paths");

		paths.forEach(
			(queryName, path) -> {
				Map<String, Object> get = (Map<String, Object>)
					((Map<String, Object>)path).get("get");

				if (get != null) {
					Query query = new Query(queryName);

					List<Map<String, Object>> parameters =
						(List<Map<String, Object>>)get.get("parameters");

					parameters.forEach(
						paramenter -> {
							Map<String, Object> schema =
								(Map<String, Object>)paramenter.get("schema");

							query.addProperty(
								new Property(
									(String)paramenter.get("name"),
									_getTypeValue(
										types, (String)paramenter.get("name"),
										schema),
									(Boolean)paramenter.get("required"),
									_isReference((Map<String, Object>)schema)));
						});

					query.setReturnType(_getReturnType(types, get));

					queries.add(query);
				}
			});

		return queries;
	}

	private static List<Type> _createtypes(Map<String, Object> root) {
		List<Type> types = new ArrayList<>();

		Map<String, Object> components =
			(Map<String, Object>)root.get("components");

		Map<String, Object> schemas =
			((Map<String, Object>)components.get("schemas"));

		schemas.forEach(
			(schemaName, content) -> {
				_populateTypes(
					types, schemaName, (Map<String, Object>)content);
			});

		return types;
	}

	private static String _getGraphQLContent(
		List<Type> types, List<Input> inputs, List<Query> queries,
		List<Mutation> mutations) {

		StringBuilder sb = new StringBuilder();

		for (Type type : types) {
			sb.append(type.toString());
			sb.append("\n");
		}

		for (Input input : inputs) {
			sb.append(input.toString());
			sb.append("\n");
		}

		sb.append("type Query {\n");

		for (Query query : queries) {
			sb.append("\t" + query.toString());
			sb.append("\n");
		}

		sb.append("}\n");

		sb.append("type Mutation {\n");

		for (Mutation mutation : mutations) {
			sb.append("\t" + mutation.toString());
			sb.append("\n");
		}

		sb.append("}");

		return sb.toString();
	}

	private static Set<Type> _getInputType(
		List<Type> types, List<Property> properties) {

		Stream<Property> propertiesStream = properties.stream();

		return propertiesStream.flatMap(
			property -> {
				Stream<Type> typeStream = types.stream();

				return typeStream.filter(
					type -> Objects.equals(
						type.getName(), property.getType()));
			}
		).collect(
			Collectors.toSet()
		);
	}

	private static String _getReturnType(
		List<Type> types, Map<String, Object> operation) {

		Map<String, Object> responses =
			(Map<String, Object>)operation.get("responses");

		Map<String, Object> response =
			(Map<String, Object>)responses.get(200);

		Map<String, Object> contents =
			(Map<String, Object>)response.get("content");

		Map<String, Object> content =
			(Map<String, Object>)contents.get("*/*");

		Map<String, Object> schema =
			(Map<String, Object>)content.get("schema");

		return _getTypeValue(
			types, (String)content.get("name"), schema);
	}

	private static String _getTypeValue(
		List<Type> types, String propertyName,
		Map<String, Object> property) {

		String typeValue = (String)property.get("type");

		if (typeValue != null) {
			if (typeValue.equals("object")) {
				typeValue = propertyName;

				_populateTypes(types, _capitalize(propertyName), property);
			}
			else if (typeValue.equals("array")) {
				Map<String, Object> items =
					(Map<String, Object>)property.get("items");

				typeValue = "[" +
					_capitalize(_getTypeValue(types, propertyName, items)) +
						"]";
			}
			else if (typeValue.equals("integer")) {
				typeValue = "int";
			}
		}
		else {
			String ref = (String)property.get("$ref");

			String[] split = ref.substring(1).split("/");

			typeValue = split[split.length - 1];
		}

		return typeValue;
	}

	private static boolean _isReference(Map<String, Object> property) {
		String typeValue = (String)property.get("type");

		if (typeValue != null) {
			if (typeValue.equals("object")) {
				return true;
			}

			return false;
		}

		return true;
	}

	private static void _populateTypes(
		List<Type> types, String schemaName, Map<String, Object> content) {

		Type type = new Type();

		type.setName(schemaName);

		Map<String, Object> properties =
			(Map<String, Object>)content.get("properties");

		if (properties != null) {
			properties.forEach(
				(propertieName, property) -> {
					String typeValue = _getTypeValue(
						types, propertieName, (Map<String, Object>)property);

					type.addProperty(
						new Property(
							propertieName, typeValue, false,
							_isReference((Map<String, Object>)property)));
				});
		}
		else {
			type.addProperty(new Property("empty", "String", false, false));
		}

		types.add(type);
	}

	private static Map<String, Object> _readFile(String pathname)
		throws IOException {

		Yaml yaml = new Yaml();

		ClassLoader classLoader = Main.class.getClassLoader();

		InputStream inputStream = classLoader.getResourceAsStream(pathname);

		return yaml.load(inputStream);
	}

	private static void _writeFile(String content, String pathname)
		throws IOException {

		File file = new File(pathname);

		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
	}

}