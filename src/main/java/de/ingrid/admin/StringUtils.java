/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.admin;

import java.text.DecimalFormat;

public class StringUtils {

	private static final DecimalFormat FORMAT = new DecimalFormat(
			"0000000000.####");

	public static boolean isEmpty(final String s) {
		if (null == s || s.length() <= 0) {
			return true;
		}
		return false;
	}

	public static boolean isEmptyOrWhiteSpace(final String s) {
		if (!isEmpty(s)) {
			if (s.trim().length() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method fills a number with leading zeros. So we get a number with
	 * ten digits before the point a and four digits past the point
	 * 
	 * @param number
	 *            The number to pad.
	 * @return The padded number as a string.
	 */
	public static String padding(double number) {
		return FORMAT.format(number);
	}
}
