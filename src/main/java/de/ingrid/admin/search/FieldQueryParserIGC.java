package de.ingrid.admin.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.udk.DoublePadding;

@Service
public class FieldQueryParserIGC extends AbstractParser {

    private static Logger log = Logger.getLogger(FieldQueryParserIGC.class);

    /** neither qx1 or qx2 are between x1 and x2 */
    private static final int FIRST_X_CASE = 0;

    /** qx1 and qx2 ar between x1 and x2 */
    private static final int SECOND_X_CASE = 1;

    /** qx1 and qx2 are not between x1 and x2 */
    private static final int THIRD_X_CASE = 2;


    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        FieldQuery[] fields = ingridQuery.getFields();
        
        processGeoAndTimeQueries(fields, booleanQuery);
    }

    private void processGeoAndTimeQueries(FieldQuery[] fields, BooleanQuery booleanQuery) {
        Map<String,Object> geoMap = new HashMap<String,Object>(fields.length);
        Map<String,Object> timeMap = new HashMap<String,Object>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            FieldQuery query = fields[i];
            String indexField = query.getFieldName();
            String value = query.getFieldValue().toLowerCase();
            if (indexField.equals("x1")) {
                geoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
            } else if (indexField.equals("x2")) {
                geoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
            } else if (indexField.equals("y1")) {
                geoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
            } else if (indexField.equals("y2")) {
                geoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
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
                booleanQuery.add(new org.apache.lucene.search.TermQuery(new Term(query.getFieldName(), query
                        .getFieldValue().toLowerCase())), Occur.SHOULD);
            } else {
                final String term = query.getFieldValue().toLowerCase();
                final String field = query.getFieldName();

                if (term.indexOf(' ') > -1) {
                    PhraseQuery phraseQuery = new PhraseQuery();
                    StringTokenizer tokenizer = new StringTokenizer(term);
                    while (tokenizer.hasMoreTokens()) {
                        phraseQuery.add(new Term(field, tokenizer.nextToken()));
                    }
                    booleanQuery.add(phraseQuery, transform(query.isRequred(), query.isProhibited()));
                } else {
                    booleanQuery.add(new org.apache.lucene.search.TermQuery(new Term(field, term)),
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
    }

    private static void prepareIncludeGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            Term x1Term1 = new Term("x1", x1);
            Term x2Term1 = new Term("x2", x2);
            Term y1Term1 = new Term("y1", y1);
            Term y2Term1 = new Term("y2", y2);

            Term x1TermMin = new Term("x1", DoublePadding.padding(5.3));
            Term x2TermMax = new Term("x2", DoublePadding.padding(14.77));
            Term y1TermMin = new Term("y1", DoublePadding.padding(46.76));
            Term y2TermMax = new Term("y2", DoublePadding.padding(54.73));

            Query xRangeQuery1 = new org.apache.lucene.search.RangeQuery(x1TermMin, x1Term1, true);
            Query xRangeQuery2 = new org.apache.lucene.search.RangeQuery(x2Term1, x2TermMax, true);
            Query yRangeQuery1 = new org.apache.lucene.search.RangeQuery(y1TermMin, y1Term1, true);
            Query yRangeQuery2 = new org.apache.lucene.search.RangeQuery(y2Term1, y2TermMax, true);

            booleanQuery.add(xRangeQuery1, Occur.MUST);
            booleanQuery.add(xRangeQuery2, Occur.MUST);
            booleanQuery.add(yRangeQuery1, Occur.MUST);
            booleanQuery.add(yRangeQuery2, Occur.MUST);
        }
    }

    private static void prepareExactGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            Term x1Term1 = new Term("x1", x1);
            Term x2Term1 = new Term("x2", x2);
            Term y1Term1 = new Term("y1", y1);
            Term y2Term1 = new Term("y2", y2);

            Query xTermQuery1 = new org.apache.lucene.search.TermQuery(x1Term1);
            Query xTermQuery2 = new org.apache.lucene.search.TermQuery(x2Term1);
            Query yTermQuery1 = new org.apache.lucene.search.TermQuery(y1Term1);
            Query yTermQuery2 = new org.apache.lucene.search.TermQuery(y2Term1);

            booleanQuery.add(xTermQuery1, Occur.MUST);
            booleanQuery.add(xTermQuery2, Occur.MUST);
            booleanQuery.add(yTermQuery1, Occur.MUST);
            booleanQuery.add(yTermQuery2, Occur.MUST);
        }
    }

    private static void prepareIntersectGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        BooleanQuery geoQuery = new BooleanQuery();
        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            BooleanQuery query1 = prepareIntersectGeoQuery(x1, x2, y1, y2, FIRST_X_CASE);
            BooleanQuery query2 = prepareIntersectGeoQuery(x1, x2, y1, y2, SECOND_X_CASE);
            BooleanQuery query3 = prepareIntersectGeoQuery(x1, x2, y1, y2, THIRD_X_CASE);

            geoQuery.add(query1, Occur.SHOULD);
            geoQuery.add(query2, Occur.SHOULD);
            geoQuery.add(query3, Occur.SHOULD);
            if (geoQuery.getClauses().length > 0) {
                booleanQuery.add(geoQuery, Occur.MUST);
            }
        }
    }

    private static BooleanQuery prepareIntersectGeoQuery(String x1, String x2, String y1, String y2, int x_case) {

        BooleanQuery booleanQuery = new BooleanQuery();
        Term x1Term1 = new Term("x1", x1);
        Term x1Term2 = new Term("x1", x2);
        Term x2Term1 = new Term("x2", x1);
        Term x2Term2 = new Term("x2", x2);
        Term y1Term1 = new Term("y1", y1);
        Term y1Term2 = new Term("y1", y2);
        Term y2Term1 = new Term("y2", y1);
        Term y2Term2 = new Term("y2", y2);
        Term y1TermMin = new Term("y1", DoublePadding.padding(46.76));
        Term y2TermMax = new Term("y2", DoublePadding.padding(54.73));

        switch (x_case) {
        case FIRST_X_CASE:
            BooleanQuery xQuery1FirstCase = new BooleanQuery();
            BooleanQuery xQuery2FirstCase = new BooleanQuery();
            BooleanQuery yQueryFistCase = new BooleanQuery();
            BooleanQuery yOutside = new BooleanQuery();

            org.apache.lucene.search.RangeQuery xRangeQuery1 = new org.apache.lucene.search.RangeQuery(x1Term1,
                    x1Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery2 = new org.apache.lucene.search.RangeQuery(x2Term1,
                    x2Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery3 = new org.apache.lucene.search.RangeQuery(x1Term1,
                    x1Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery4 = new org.apache.lucene.search.RangeQuery(x2Term1,
                    x2Term2, true);

            org.apache.lucene.search.RangeQuery yRangeQuery1 = new org.apache.lucene.search.RangeQuery(y1Term1,
                    y1Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery2 = new org.apache.lucene.search.RangeQuery(y2Term1,
                    y2Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery3 = new org.apache.lucene.search.RangeQuery(y1TermMin,
                    y1Term1, true);
            org.apache.lucene.search.RangeQuery yRangeQuery4 = new org.apache.lucene.search.RangeQuery(y2Term1,
                    y2TermMax, true);

            // must: true, false must_not: false, true should: false, false
            xQuery1FirstCase.add(xRangeQuery1, Occur.MUST);
            xQuery1FirstCase.add(xRangeQuery2, Occur.MUST_NOT);
            xQuery2FirstCase.add(xRangeQuery3, Occur.MUST_NOT);
            xQuery2FirstCase.add(xRangeQuery4, Occur.MUST);

            yOutside.add(yRangeQuery3, Occur.MUST);
            yOutside.add(yRangeQuery4, Occur.MUST);

            yQueryFistCase.add(yRangeQuery1, Occur.SHOULD);
            yQueryFistCase.add(yRangeQuery2, Occur.SHOULD);
            yQueryFistCase.add(yOutside, Occur.SHOULD);

            booleanQuery.add(xQuery1FirstCase, Occur.SHOULD);
            booleanQuery.add(xQuery2FirstCase, Occur.SHOULD);

            break;

        case SECOND_X_CASE:
            org.apache.lucene.search.RangeQuery xRangeQuery1SecondCase = new org.apache.lucene.search.RangeQuery(
                    x1Term1, x1Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery2SecondCase = new org.apache.lucene.search.RangeQuery(
                    x2Term1, x2Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery1SecondCase = new org.apache.lucene.search.RangeQuery(
                    y1Term1, y1Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery2SecondCase = new org.apache.lucene.search.RangeQuery(
                    y2Term1, y2Term2, true);
            booleanQuery.add(xRangeQuery1SecondCase, Occur.MUST);
            booleanQuery.add(xRangeQuery2SecondCase, Occur.MUST);
            booleanQuery.add(yRangeQuery1SecondCase, Occur.MUST_NOT);
            booleanQuery.add(yRangeQuery2SecondCase, Occur.MUST_NOT);
            break;

        case THIRD_X_CASE:
            BooleanQuery thirdCase = new BooleanQuery();
            org.apache.lucene.search.RangeQuery xRangeQuery1ThirdCase = new org.apache.lucene.search.RangeQuery(
                    x1Term1, x1Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery2ThirdCase = new org.apache.lucene.search.RangeQuery(
                    x2Term1, x2Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery1ThirdCase = new org.apache.lucene.search.RangeQuery(
                    y1Term1, y1Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery2ThirdCase = new org.apache.lucene.search.RangeQuery(
                    y2Term1, y2Term2, true);
            thirdCase.add(yRangeQuery1ThirdCase, Occur.SHOULD);
            thirdCase.add(yRangeQuery2ThirdCase, Occur.SHOULD);
            booleanQuery.add(xRangeQuery1ThirdCase, Occur.MUST_NOT);
            booleanQuery.add(xRangeQuery2ThirdCase, Occur.MUST_NOT);
            booleanQuery.add(thirdCase, Occur.MUST);
            break;

        default:
            break;
        }

        return booleanQuery;
    }

    private static void prepareInsideGeoQuery(BooleanQuery booleanQuery, Map<String,Object> geoMap) {
        String x1 = (String) geoMap.get("x1");
        String x2 = (String) geoMap.get("x2");
        String y1 = (String) geoMap.get("y1");
        String y2 = (String) geoMap.get("y2");

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            Term x1Term1 = new Term("x1", x1);
            Term x1Term2 = new Term("x1", x2);

            Term y1Term1 = new Term("y1", y1);
            Term y1Term2 = new Term("y1", y2);

            Term x2Term1 = new Term("x2", x1);
            Term x2Term2 = new Term("x2", x2);

            Term y2Term1 = new Term("y2", y1);
            Term y2Term2 = new Term("y2", y2);

            org.apache.lucene.search.RangeQuery xRangeQuery1 = new org.apache.lucene.search.RangeQuery(x1Term1,
                    x1Term2, true);
            org.apache.lucene.search.RangeQuery xRangeQuery2 = new org.apache.lucene.search.RangeQuery(x2Term1,
                    x2Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery1 = new org.apache.lucene.search.RangeQuery(y1Term1,
                    y1Term2, true);
            org.apache.lucene.search.RangeQuery yRangeQuery2 = new org.apache.lucene.search.RangeQuery(y2Term1,
                    y2Term2, true);

            booleanQuery.add(xRangeQuery1, Occur.MUST);
            booleanQuery.add(xRangeQuery2, Occur.MUST);
            booleanQuery.add(yRangeQuery1, Occur.MUST);
            booleanQuery.add(yRangeQuery2, Occur.MUST);

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
            log.debug("resulting query after prepareTime:" + query.toString());
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
            Term termT1Min = new Term("t1", t1);
            Term termT1Max = new Term("t1", t2);

            Term termT2Min = new Term("t2", t1);
            Term termT2Max = new Term("t2", t2);

            // we must match also documents where t0 are in this range
            Term termT0Min = new Term("t0", t1);
            Term termT0Max = new Term("t0", t2);

            org.apache.lucene.search.RangeQuery rangeQueryt0 = new org.apache.lucene.search.RangeQuery(termT0Min,
                    termT0Max, true);
            org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(termT1Min,
                    termT1Max, true);
            org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(termT2Min,
                    termT2Max, true);

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
            query.add(new org.apache.lucene.search.TermQuery(termT0), Occur.MUST);
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
            Term termT1Min = new Term("t1", "00000000");
            Term termT2Max = new Term("t2", "99999999");
            Term termT1Max = new Term("t1", t1);
            Term termT2Min = new Term("t2", t2);

            org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(termT1Min,
                    termT1Max, true);
            org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(termT2Min,
                    termT2Max, true);

            query.add(rangeQuery11, Occur.MUST);
            query.add(rangeQuery12, Occur.MUST);
        } else if (null != t0) {
            t0 = t0.replaceAll("-", "");
            Term termT1 = new Term("t1", t0);
            Term termT2 = new Term("t2", t0);
            Term min = new Term("t1", "00000000");
            Term max = new Term("t2", "99999999");
            org.apache.lucene.search.RangeQuery rangeQueryT1 = new org.apache.lucene.search.RangeQuery(min, termT1,
                    false);
            org.apache.lucene.search.RangeQuery rangeQueryT2 = new org.apache.lucene.search.RangeQuery(termT2, max,
                    false);
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
            Term termT1Min = new Term("t1", "00000000");
            Term termT1Date1 = new Term("t1", t1);
            Term termT1Date2 = new Term("t1", t2);

            Term termT2Max = new Term("t2", "99999999");
            Term termT2Date1 = new Term("t2", t1);
            Term termT2Date2 = new Term("t2", t2);

            org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(termT1Min,
                    termT1Date1, true);
            org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(termT2Date1,
                    termT2Date2, true);

            org.apache.lucene.search.RangeQuery rangeQuery21 = new org.apache.lucene.search.RangeQuery(termT1Date1,
                    termT1Date2, true);
            org.apache.lucene.search.RangeQuery rangeQuery22 = new org.apache.lucene.search.RangeQuery(termT2Date2,
                    termT2Max, true);

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
            booleanQueryTime.add(new org.apache.lucene.search.TermQuery(termT0), Occur.SHOULD);
            booleanQueryTime.add(new org.apache.lucene.search.TermQuery(termT1), Occur.SHOULD);
            booleanQueryTime.add(new org.apache.lucene.search.TermQuery(termT2), Occur.SHOULD);
            query.add(booleanQueryTime, Occur.MUST);
        }
    }
}
