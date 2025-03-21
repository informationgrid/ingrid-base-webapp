/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.elasticsearch;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import de.ingrid.elasticsearch.IndexManager;

public class ElasticSearchUtilsTest {

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {}

    @Test
    void test() {
        String newName = IndexManager.getNextIndexName("abc", "", "");
        assertThat(newName, startsWith("abc_"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_201504141213084", "", "");
        assertThat(newName, startsWith("abc_"));
        assertThat(newName.length(), greaterThan(15));
        assertThat(newName, not(startsWith("abc@_201504141213084")));

        newName = IndexManager.getNextIndexName("abc_def", "", "");
        assertThat(newName, startsWith("abc_def_"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_def_143", "", "");
        assertThat(newName, startsWith("abc_def_"));
        assertThat(newName, startsWith("abc_def_143"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_def_201504141213084", "", "");
        assertThat(newName, startsWith("abc_def_"));
        assertThat(newName.length(), greaterThan(15));
        assertThat(newName, not(startsWith("abc_def_201504141213084")));
    }

    @Test
    void testWithUuid() {
        String newName = IndexManager.getNextIndexName("abc", "xyz", "plug-name");
        assertThat(newName, startsWith("abc@plug-name-xyz_"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_201504141213084", "xyz", "plug-name");
        assertThat(newName, startsWith("abc@plug-name-xyz_"));
        assertThat(newName.length(), greaterThan(15));
        assertThat(newName, not(startsWith("abc@_201504141213084")));

        newName = IndexManager.getNextIndexName("abc_def", "xyz", "plug-name");
        assertThat(newName, startsWith("abc_def@plug-name-xyz_"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_def_143", "xyz", "plug-name");
        assertThat(newName, startsWith("abc_def_"));
        assertThat(newName, startsWith("abc_def_143@plug-name-xyz"));
        assertThat(newName.length(), greaterThan(15));

        newName = IndexManager.getNextIndexName("abc_def_201504141213084", "xyz", "plug-name");
        assertThat(newName, startsWith("abc_def@plug-name-xyz_"));
        assertThat(newName.length(), greaterThan(15));
        assertThat(newName, not(startsWith("abc_def_201504141213084")));
    }

}
