
package com.inacionery.openapitographql.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Inácio Nery
 */
public class Input {

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

		if (!(obj instanceof Input)) {
			return false;
		}

		Input other = (Input)obj;

		if (Objects.equals(_name, other._name) &&
			Objects.equals(_properties, other._properties)) {

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

	@Override
	public int hashCode() {
		return Objects.hash(_name, _properties);
	}

	public void setName(String name) {
		_name = name + "Input";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("input ");
		sb.append(_name);
		sb.append(" {\n");

		_properties.forEach(
			property -> {
				sb.append("\t");
				sb.append(property.toString());
				sb.append("\n");
			});

		sb.append("}");

		return sb.toString();
	}

	private String _name;
	private final List<Property> _properties = new ArrayList<>();

}