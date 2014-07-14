package org.fenixedu.cms.domain.homepage;

import net.sourceforge.fenixedu.domain.homepage.Homepage;
import pt.ist.fenixframework.Atomic;

public class HomepageSite extends HomepageSite_Base {
    public HomepageSite(Homepage homepage) {
        setHomepage(homepage);
    }

    @Override
    @Atomic
    public void delete() {
        setHomepage(null);
        super.delete();
    }
}
