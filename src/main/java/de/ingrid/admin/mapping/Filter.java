/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.mapping;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class Filter implements Externalizable {

	public static enum FilterType {
        GREATER_THAN, LOWER_THAN, CONTAINS, NOT_CONTAINS, EQUAL, NOT_EQUAL, BEFORE, AFTER
	}

	private Comparable<? extends Serializable> _expression;

	private FilterType _filterType = FilterType.GREATER_THAN;

	public Filter(final Comparable<? extends Serializable> expression,
			final FilterType filterType) {
		_expression = expression;
		_filterType = filterType;
	}

	public Comparable<? extends Serializable> getExpression() {
		return _expression;
	}

	public FilterType getFilterType() {
		return _filterType;
	}

	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		_filterType = FilterType.valueOf(in.readUTF());
		_expression = (Comparable<? extends Serializable>) in.readObject();
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeUTF(_filterType.name());
		out.writeObject(_expression);
	}

}
