package de.ingrid.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

public class Utils {

    @SuppressWarnings("unchecked")
    public static List<Partner> getPartners(final IBus bus) throws Exception {
        final List<Partner> list = new ArrayList<Partner>();
        if (bus != null) {
            // get partners from bus
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off")); 

            final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final ArrayList partners = hits.getHits()[0].getArrayList("partner");
                for (final Object object : partners) {
                    final Map<String, Object> map = (Map<String, Object>) object;
                    final String partnerName = (String) map.get("name");
                    final String partnerId = (String) map.get("partnerid");
                    list.add(new Partner(partnerId, partnerName));
                }
            }
        } else {
            // get partners from file
            final Properties partners = new Properties();
            partners.load(Utils.class.getResourceAsStream("/partners.properties"));
            for (final Object key : partners.keySet()) {
                final String shortName = (String) key;
                final String displayName = partners.getProperty(shortName);
                list.add(new Partner(shortName, displayName));
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Provider> getProviders(final IBus bus) throws Exception {
        final List<Provider> list = new ArrayList<Provider>();
        if (bus != null) {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off")); 

            final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final ArrayList providers = hits.getHits()[0].getArrayList("provider");
                for (final Object object : providers) {
                    final Map<String, Object> map = (Map<String, Object>) object;
                    final String providerName = (String) map.get("name");
                    final String providerId = (String) map.get("providerid");
                    list.add(new Provider(providerId, providerName));
                }
            }
        }

        return list;
    }
}
