
package com.inacionery.openapitographql.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Inácio Nery
 */
public class Query {

	public Query(String name) {
		name = name.replace("{", "").replace("}", "");

		String[] splitName = name.substring(1).split("/");

		_removeDash(splitName);

		name = splitName[0];

		if (splitName.length > 1) {
			name += "By";
		}

		for (int i = 1; i < splitName.length; i++) {
			name += _capitalize(splitName[i]);

			if (i != splitName.length - 1) {
				name += "And";
			}
		}

		_name = name;
	}

	public void addProperty(Property property) {
		_properties.add(property);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Query)) {
			return false;
		}

		Query other = (Query)obj;

		if (Objects.equals(_name, other._name) &&
			Objects.equals(_properties, other._properties) &&
			Objects.equals(_returnType, other._returnType)) {

			return true;
		}

		return false;
	}

	public String getName() {
		return _name;
	}

	public List<Property> getProperties() {
		return _properties;
	}

	public String getReturnType() {
		return _returnType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name, _properties, _returnType);
	}

	public void setReturnType(String returnType) {
		_returnType = returnType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(_name);

		if (!_properties.isEmpty()) {
			sb.append("(");

			_properties.forEach(
				property -> {
					sb.append(property.toString());
					sb.append(", ");
				});
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		}

		sb.append(": ");
		sb.append(_returnType);

		return sb.toString();
	}

	private String _capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private void _removeDash(String[] splitName) {
		for (int i = 0; i < splitName.length; i++) {
			String[] split = splitName[i].split("-");

			splitName[i] = split[0];

			for (int j = 1; j < split.length; j++) {
				splitName[i] += _capitalize(split[j]);
			}
		}
	}

	private final String _name;
	private List<Property> _properties = new ArrayList<>();
	private String _returnType;

}