package org.fenixedu.cms.domain.homepage;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.MigrationUtil.BUNDLE;
import static org.fenixedu.cms.domain.MigrationUtil.localized;
import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;

public class HomepageSite extends HomepageSite_Base {
    public HomepageSite(Homepage homepage) {
        super();
        setHomepage(homepage);
        setBennu(Bennu.getInstance());
        setDescription(localized(homepage.getDescription()));
        setAlternativeSite(homepage.getAlternativeSite());
        setName(getLocalizedString(BUNDLE, "homepage.title", homepage.getOwnersName()));
        setSlug(homepage.getPerson().getUsername());
        setCreatedBy(homepage.getPerson().getUser());
        setStyle(homepage.getStyle());
    }

    @Override
    @Atomic
    public void delete() {
        setHomepage(null);
        super.delete();
    }
}
