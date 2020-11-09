/*
 * Copyright 2018-2020 VMware, Inc. All Rights Reserved.
 * SPDX-License-Identifier: BSD-2
 */
package ddlog;

import com.vmware.ddlog.DDlogJooqProvider;
import com.vmware.ddlog.ir.DDlogProgram;
import com.vmware.ddlog.translator.Translator;
import ddlogapi.DDlogAPI;
import ddlogapi.DDlogException;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.jooq.impl.DSL.field;

public class JooqProviderTest {

    /*
     * We can only have one DDlog program loaded in memory at a time. We
     * therefore conduct a series of tests within a single method.
     */
    @Test
    public void testInsertAndSelect() throws IOException, DDlogException {
        String s1 = "create table hosts (id varchar(36), capacity integer, up boolean)";
        String v2 = "create view hostsv as select distinct * from hosts";
        String v1 = "create view good_hosts as select distinct * from hosts where capacity < 10";
        List<String> ddl = new ArrayList<>();
        ddl.add(s1);
        ddl.add(v2);
        ddl.add(v1);
        compileAndLoad("testInserts", ddl);
        final DDlogAPI dDlogAPI = new DDlogAPI(1, null, true);

        // Initialise the data provider
        MockDataProvider provider = new DDlogJooqProvider(dDlogAPI, ddl);
        MockConnection connection = new MockConnection(provider);

        // Pass the mock connection to a jOOQ DSLContext:
        DSLContext create = DSL.using(connection);

        // Test 1: insert statements
        create.execute("insert into hosts values ('n1', 10, true)");
        create.batch("insert into hosts values ('n54', 18, false)",
                     "insert into hosts values ('n9', 2, true)");
        final Field<String> field1 = field("id", String.class);
        final Field<Integer> field2 = field("capacity", Integer.class);
        final Field<Boolean> field3 = field("up", Boolean.class);

        final Record test1 = create.newRecord(field1, field2, field3);
        final Record test2 = create.newRecord(field1, field2, field3);
        final Record test3 = create.newRecord(field1, field2, field3);

        test1.setValue(field1, "n1");
        test1.setValue(field2, 10);
        test1.setValue(field3, true);

        test2.setValue(field1, "n54");
        test2.setValue(field2, 18);
        test2.setValue(field3, false);

        test3.setValue(field1, "n9");
        test3.setValue(field2, 2);
        test3.setValue(field3, true);

        // Test 2: make sure selects read out the same content inserted above
        final Result<Record> hostsvResults = create.fetch("select * from hostsv");
        assertTrue(hostsvResults.contains(test1));
        assertTrue(hostsvResults.contains(test2));
        assertTrue(hostsvResults.contains(test3));

        final Result<Record> goodHostsResults = create.fetch("select * from good_hosts");
        assertFalse(goodHostsResults.contains(test1));
        assertFalse(goodHostsResults.contains(test2));
        assertTrue(goodHostsResults.contains(test3));
    }

    public static void compileAndLoad(final String name, final List<String> ddl) throws IOException, DDlogException {
        final Translator t = new Translator(null);
        ddl.forEach(t::translateSqlStatement);
        final DDlogProgram dDlogProgram = t.getDDlogProgram();
        final String fileName = String.format("/tmp/%s.dl", name);
        File tmp = new File(fileName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
        bw.write(dDlogProgram.toString());
        bw.close();
        if (!DDlogAPI.compileDDlogProgram(fileName, true, "../lib", "./lib")) {
            throw new RuntimeException("Failed to compile ddlog program");
        }
        DDlogAPI.loadDDlog();
    }
}
