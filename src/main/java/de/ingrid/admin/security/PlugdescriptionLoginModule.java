package de.ingrid.admin.security;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.admin.IKeys;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

public class PlugdescriptionLoginModule extends AbstractLoginModule {

    private static final Log LOG = LogFactory.getLog(PlugdescriptionLoginModule.class);

    @Override
    protected IngridPrincipal authenticate(String userName, String password) {
        String pd = System.getProperty(IKeys.PLUG_DESCRIPTION);
        File file = new File(pd);
        IngridPrincipal ingridPrincipal = null;
        if (file.exists()) {
            PlugDescription plugDescription;
            try {
                plugDescription = new PlugdescriptionSerializer().deSerialize(file);
                String pwd = plugDescription.getIplugAdminPassword();
                if (userName.equals("admin") && password.equals(pwd)) {
                    Set<String> set = new HashSet<String>();
                    set.add("admin");
                    ingridPrincipal = new IngridPrincipal.KnownPrincipal("admin", pwd, set);
                } else {
                    ingridPrincipal = new IngridPrincipal.UnknownPrincipal();
                }
            } catch (IOException e) {
                LOG.error("can not verify login datas", e);
            }
        } else {
            ingridPrincipal = new IngridPrincipal.SuperAdmin("superadmin");
        }
        return ingridPrincipal;
    }

}
