package org.cobbzilla.wizard.server.config;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class BasicSupportInfo {

    @Getter @Setter private String email;
    public boolean getHasEmail () { return !empty(email); }

    @Getter @Setter private String site;
    public boolean getHasSite () { return !empty(site); }

    public boolean getHasInfo() { return !empty(email) || !empty(site); }
    public boolean getHasEmailAndSite() { return !empty(email) && !empty(site); }

}
