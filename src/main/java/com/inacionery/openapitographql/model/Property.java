
package com.inacionery.openapitographql.model;

import java.util.Objects;

/**
 * @author Inácio Nery
 */
public class Property {

	public Property(
		String name, String type, Boolean required, boolean reference) {

		name = _removeDash(name);

		name = _removeUnderline(name);

		_name = name;

		_type = type.substring(0, 1).toUpperCase() + type.substring(1);
		_required = required;
		_reference = reference;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Property)) {
			return false;
		}

		Property other = (Property)obj;

		if (Objects.equals(_name, other._name) &&
			Objects.equals(_reference, other._reference) &&
			Objects.equals(_required, other._required) &&
			Objects.equals(_type, other._type)) {

			return true;
		}

		return false;
	}

	public String getName() {
		return _name;
	}

	public String getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name, _required, _type);
	}

	public Boolean isReference() {
		return _reference;
	}

	public Boolean isRequired() {
		return _required;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String toString() {
		return _name + ": " + _type + ((_required != null) &&
			_required ? "!" : "");
	}

	private String _capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private String _removeDash(String name) {
		String[] split = name.split("-");

		name = split[0];

		for (int j = 1; j < split.length; j++) {
			name += _capitalize(split[j]);
		}

		return name;
	}

	private String _removeUnderline(String name) {
		String[] split = name.split("_");

		name = split[0];

		for (int j = 1; j < split.length; j++) {
			name += _capitalize(split[j]);
		}

		return name;
	}

	private final String _name;
	private final boolean _reference;
	private final Boolean _required;
	private String _type;

}