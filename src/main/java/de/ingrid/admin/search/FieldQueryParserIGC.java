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
package de.ingrid.admin.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * Maps FieldQuery(s) from IngridQuery to LuceneQuery and handles all IGC specials (time, coords ..).
 * <b>Notice: NEVER USE Occur.MUST_NOT when 1:n associations and it's not intended, that one value 
 * knocks out all the others (e.g. multiple x1 when multiple BBoxes) !!! Also MUST_NOT alone is NOT sufficient
 * for Lucene Query, also add MUST ...</b> 
 */
@Service
public class FieldQueryParserIGC extends AbstractParser {

    private static Logger log = Logger.getLogger(FieldQueryParserIGC.class);

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        if (log.isDebugEnabled()) {
            log.debug("incoming ingrid query:" + ingridQuery.toString());
            log.debug("incoming boolean query:" + booleanQuery.toString());
        }
        FieldQuery[] fields = ingridQuery.getFields();
        processGeoAndTimeQueries(fields, booleanQuery);
        if (log.isDebugEnabled()) {
            log.debug("resulting query:" + booleanQuery.toString());
        }
    }

    private void processGeoAndTimeQueries(FieldQuery[] fields, BooleanQuery booleanQuery) {
        Map<String,Object> geoMap = new HashMap<String,Object>(fields.length);
        Map<String,Object> timeMap = new HashMap<String,Object>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldQuery query = fields[i];
            String indexField = query.getFieldName();
            String value = query.getFieldValue().toLowerCase();
            if (indexField.equals("x1")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("x2")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("y1")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("y2")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("coord")) {
                List<String> list = (List<String>) geoMap.get(indexField);
                if (list == null) {
                    list = new LinkedList<String>();
                }
                list.add(value);
                geoMap.put(indexField, list);
            } else if ("t0".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("t1".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("t2".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("time".equals(indexField)) {
                List<String> list = (List<String>) timeMap.get(indexField);
                if (list == null) {
                    list = new LinkedList<String>();
                }
                list.add(value);
                timeMap.put(indexField, list);
            } else if ("incl_meta".equals(indexField) && "on".equals(value)) {
                booleanQuery.add(new TermQuery(new Term(query.getFieldName(), query
                        .getFieldValue().toLowerCase())), Occur.SHOULD);
            } else {
                final String term = filterTerm(query.getFieldValue().toLowerCase());
                final String field = query.getFieldName();

                if (term.indexOf(' ') > -1) {
                    PhraseQuery phraseQuery = new PhraseQuery();
                    StringTokenizer tokenizer = new StringTokenizer(term);
                    while (tokenizer.hasMoreTokens()) {
                        phraseQuery.add(new Term(field, tokenizer.nextToken()));
                    }
                    booleanQuery.add(phraseQuery, transform(query.isRequred(), query.isProhibited()));
                } else {
                    booleanQuery.add(new TermQuery(new Term(field, term)),
                    		transform(query.isRequred(), query.isProhibited()));
                }
            }
        }

        if (null == geoMap.get("coord")) {
            final List<String> list = new LinkedList<String>();
            list.add("exact");
            geoMap.put("coord", list);
        }
        prepareGeo(booleanQuery, geoMap);
        prepareTime(booleanQuery, timeMap);
    }

    private void prepareGeo(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        List<String> list = (List<String>) geoMap.get("coord");
        if (list != null) {
            BooleanQuery.setMaxClauseCount(10240);
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next();
                if ("inside".equals(value)) {
                    // innerhalb
                    prepareInsideGeoQuery(booleanQuery, geoMap);
                } else if ("intersect".equals(value)) {
                    // schneiden
                    prepareIntersectGeoQuery(booleanQuery, geoMap);
                } else if ("include".equals(value)) {
                    // enthalten
                    prepareIncludeGeoQuery(booleanQuery, geoMap);
                } else {
                    prepareExactGeoQuery(booleanQuery, geoMap);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("resulting query:" + booleanQuery.toString());
        }
    }

    /** Hits BBox INCLUDE the passed BBox */
    private static void prepareIncludeGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            // At least one x1 AND one y1 AND one x2 AND one y2 OUTSIDE passed BBox, borders are ok
            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
            		new Double(-360.0), new Double(x1), true, true);
            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x2), new Double(360.0), true, true);
            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
            		new Double(-360.0), new Double(y1), true, true);
            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y2), new Double(360.0), true, true);

            booleanQuery.add(x1Below, Occur.MUST);
            booleanQuery.add(x2Above, Occur.MUST);
            booleanQuery.add(y1Below, Occur.MUST);
            booleanQuery.add(y2Above, Occur.MUST);
        }
    }

    private static void prepareExactGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            Query x1EqualsX1 = NumericRangeQuery.newDoubleRange("x1",
            		new Double(x1), new Double(x1), true, true);
            Query x2EqualsX2 = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x2), new Double(x2), true, true);
            Query y1EqualsY1 = NumericRangeQuery.newDoubleRange("y1",
            		new Double(y1), new Double(y1), true, true);
            Query y2EqualsY2 = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y2), new Double(y2), true, true);

            booleanQuery.add(x1EqualsX1, Occur.MUST);
            booleanQuery.add(x2EqualsX2, Occur.MUST);
            booleanQuery.add(y1EqualsY1, Occur.MUST);
            booleanQuery.add(y2EqualsY2, Occur.MUST);
        }
    }

    /** Hits BBox INTERSECT the passed BBox */
    private static void prepareIntersectGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {

        	// NOT ALL OUTSIDE (this would be coord:include)
            // at least one x1 OR one y1 OR one x2 OR one y2 INSIDE passed BBox, borders are ok
            Query x1Inside = NumericRangeQuery.newDoubleRange("x1",
            		new Double(x1), new Double(x2), true, true);
            Query x2Inside = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x1), new Double(x2), true, true);
            Query y1Inside = NumericRangeQuery.newDoubleRange("y1",
            		new Double(y1), new Double(y2), true, true);
            Query y2Inside = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y1), new Double(y2), true, true);
            BooleanQuery isInside = new BooleanQuery();
            isInside.add(x1Inside, Occur.SHOULD);
            isInside.add(x2Inside, Occur.SHOULD);
            isInside.add(y1Inside, Occur.SHOULD);
            isInside.add(y2Inside, Occur.SHOULD);
            booleanQuery.add(isInside, Occur.MUST);

        	// NOT ALL INSIDE (this would be coord:inside)
            // at least one x1 OR one y1 OR one x2 OR one y2 OUTSIDE passed BBox, borders are ok
            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
            		new Double(-360.0), new Double(x1), true, true);
            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x2), new Double(360.0), true, true);
            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
            		new Double(-360.0), new Double(y1), true, true);
            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y2), new Double(360.0), true, true);
            BooleanQuery isOutside = new BooleanQuery();
            isOutside.add(x1Below, Occur.SHOULD);
            isOutside.add(x2Above, Occur.SHOULD);
            isOutside.add(y1Below, Occur.SHOULD);
            isOutside.add(y2Above, Occur.SHOULD);
            booleanQuery.add(isOutside, Occur.MUST);

            // guarantee that not all x are in area left or all x are in area right

            // at least one x1 is left of right border, border itself is ok
            Query x1LeftX2 = NumericRangeQuery.newDoubleRange("x1",
            		new Double(-360.0), new Double(x2), true, true);
            booleanQuery.add(x1LeftX2, Occur.MUST);
            // at least one x2 is right of left border, border itself is ok
            Query x2RightX1 = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x1), new Double(360.0), true, true);
            booleanQuery.add(x2RightX1, Occur.MUST);

            // guarantee that not all y are in area below or all y are in area above

            // at least one y1 is below upper border, border itself is ok
            Query y1BelowY2 = NumericRangeQuery.newDoubleRange("y1",
            		new Double(-360.0), new Double(y2), true, true);
            booleanQuery.add(y1BelowY2, Occur.MUST);
            // at least one y2 is above lower border, border itself is ok
            Query y2AboveY1 = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y1), new Double(360.0), true, true);
            booleanQuery.add(y2AboveY1, Occur.MUST);
        }
    }

    private static void prepareInsideGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            // NO x1 or y1 or x2 or y2 OUTSIDE passed BBox, borders are ok
            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
            		new Double(-360.0), new Double(x1), true, false);
            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
            		new Double(x2), new Double(360.0), false, true);
            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
            		new Double(-360.0), new Double(y1), true, false);
            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
            		new Double(y2), new Double(360.0), false, true);
            booleanQuery.add(x1Below, Occur.MUST_NOT);
            booleanQuery.add(x2Above, Occur.MUST_NOT);
            booleanQuery.add(y1Below, Occur.MUST_NOT);
            booleanQuery.add(y2Above, Occur.MUST_NOT);

            // NOTICE: WE NEED A MUST (or SHOULD) ! MUST_NOT ALONE IS NOT SUFFICIENT FOR Lucene Query !
            // http://lucene.apache.org/java/2_9_0/api/all/org/apache/lucene/search/BooleanClause.Occur.html#MUST_NOT
            // "Note that it is not possible to search for queries that only consist of a MUST_NOT clause."
        	booleanQuery.add(new MatchAllDocsQuery(), Occur.MUST);
        }
    }

    private static void prepareTime(BooleanQuery query, Map<String,Object> timeMap) {
        if (log.isDebugEnabled()) {
            log.debug("start prepareTime with t0=" + timeMap.get("t0") + ", t1:" + timeMap.get("t1") + ", t2:"
                    + timeMap.get("t2"));
        }

        List<String> list = (List<String>) timeMap.get("time");
        if (list == null) {
            // nothing selected -> default inside
            prepareInsideTime(query, timeMap);
        } else {
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next();
                if ("intersect".equals(value)) {
                    // innerhalb oder schneidet
                    prepareInsideOrIntersectTime(query, timeMap);
                } else if ("include".equals(value)) {
                    // innerhalb oder umschliesst
                    prepareInsideOrIncludeQuery(query, timeMap);
                } else {
                    prepareInsideTime(query, timeMap);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("resulting query:" + query.toString());
        }
    }

    private static void prepareInsideOrIncludeQuery(BooleanQuery query, Map<String,Object> timeMap) {
        BooleanQuery booleanQueryTime = new BooleanQuery();
        BooleanQuery inside = new BooleanQuery();
        BooleanQuery include = new BooleanQuery();
        prepareInsideTime(inside, timeMap);
        prepareIncludeTimeQuery(include, timeMap);
        if (include.getClauses().length > 0) {
            booleanQueryTime.add(include, Occur.SHOULD);
        }
        if (inside.getClauses().length > 0) {
            booleanQueryTime.add(inside, Occur.SHOULD);
        }

        if (booleanQueryTime.getClauses().length > 0) {
            query.add(booleanQueryTime, Occur.MUST);
        }
    }

    private static void prepareInsideOrIntersectTime(BooleanQuery query, Map<String,Object> timeMap) {
        BooleanQuery booleanQueryTime = new BooleanQuery();
        BooleanQuery inside = new BooleanQuery();
        BooleanQuery traverse = new BooleanQuery();
        prepareInsideTime(inside, timeMap);
        if (inside.getClauses().length > 0) {
            booleanQueryTime.add(inside, Occur.SHOULD);
        }
        prepareTraverseTime(traverse, timeMap);
        if (traverse.getClauses().length > 0) {
            booleanQueryTime.add(traverse, Occur.SHOULD);
        }
        if (booleanQueryTime.getClauses().length > 0) {
            query.add(booleanQueryTime, Occur.MUST);
        }
    }

    private static void prepareInsideTime(BooleanQuery query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            // we must match also documents where t0 are in this range
            Query rangeQueryt0 = new TermRangeQuery("t0", t1, t2, true, true);
            Query rangeQuery11 = new TermRangeQuery("t1", t1, t2, true, true);
            Query rangeQuery12 = new TermRangeQuery("t2", t1, t2, true, true);

            // connect with AND
            BooleanQuery booleanQueryT1T2 = new BooleanQuery();
            booleanQueryT1T2.add(rangeQuery11, Occur.MUST);
            booleanQueryT1T2.add(rangeQuery12, Occur.MUST);
            // connect with OR
            BooleanQuery booleanQueryTime = new BooleanQuery();
            booleanQueryTime.add(booleanQueryT1T2, Occur.SHOULD);
            booleanQueryTime.add(rangeQueryt0, Occur.SHOULD);
            // connect to whole query with AND
            query.add(booleanQueryTime, Occur.MUST);
        } else if (null != t0) {
            t0 = t0.replaceAll("-", "");
            Term termT0 = new Term("t0", t0);
            query.add(new TermQuery(termT0), Occur.MUST);
        }
    }

    private static void prepareIncludeTimeQuery(BooleanQuery query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            Query rangeQuery11 = new TermRangeQuery("t1", "00000000", t1, true, true);
            Query rangeQuery12 = new TermRangeQuery("t2", t2, "99999999", true, true);

            query.add(rangeQuery11, Occur.MUST);
            query.add(rangeQuery12, Occur.MUST);
        } else if (null != t0) {
            t0 = t0.replaceAll("-", "");

            Query rangeQueryT1 = new TermRangeQuery("t1", "00000000", t0, false, false);
            Query rangeQueryT2 = new TermRangeQuery("t2", t0, "99999999", false, false);

            query.add(rangeQueryT1, Occur.MUST);
            query.add(rangeQueryT2, Occur.MUST);
        }

    }

    private static void prepareTraverseTime(BooleanQuery query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            // (ti2:[tq1 TO tq2] && ti1:[00000000 TO tq1]) || (ti1:[tq1 TO tq2]
            // && ti2:[tq2 TO 99999999])

            Query rangeQuery11 = new TermRangeQuery("t1", "00000000", t1, true, true);
            Query rangeQuery12 = new TermRangeQuery("t2", t1, t2, true, true);

            Query rangeQuery21 = new TermRangeQuery("t1", t1, t2, true, true);
            Query rangeQuery22 = new TermRangeQuery("t2", t2, "99999999", true, true);


            BooleanQuery booleanQueryTime = new BooleanQuery();
            BooleanQuery first = new BooleanQuery();
            first.add(rangeQuery11, Occur.MUST);
            first.add(rangeQuery12, Occur.MUST);

            BooleanQuery second = new BooleanQuery();
            second.add(rangeQuery21, Occur.MUST);
            second.add(rangeQuery22, Occur.MUST);

            booleanQueryTime.add(first, Occur.SHOULD);
            booleanQueryTime.add(second, Occur.SHOULD);
            query.add(booleanQueryTime, Occur.MUST);
        } else if (null != t0) {
            t0 = t0.replaceAll("-", "");
            Term termT0 = new Term("t0", t0);
            Term termT1 = new Term("t1", t0);
            Term termT2 = new Term("t2", t0);

            BooleanQuery booleanQueryTime = new BooleanQuery();
            booleanQueryTime.add(new TermQuery(termT0), Occur.SHOULD);
            booleanQueryTime.add(new TermQuery(termT1), Occur.SHOULD);
            booleanQueryTime.add(new TermQuery(termT2), Occur.SHOULD);
            query.add(booleanQueryTime, Occur.MUST);
        }
    }
}
