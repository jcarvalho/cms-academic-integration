package org.fenixedu.cms.domain.researchUnit;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.UnitSite;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import net.sourceforge.fenixedu.domain.messaging.AnnouncementBoard;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.cms.domain.researchUnit.componenets.HomeComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.Organization;
import org.fenixedu.cms.domain.researchUnit.componenets.ResearchUnitComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.SubUnits;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public class CreateResearchUnitSites extends MigrationTask {


    @Override
    public void runTask() throws Exception {
        deleteAllSites();
        sitesForClass(net.sourceforge.fenixedu.domain.ResearchUnitSite.class).forEach(oldSite -> create(oldSite));
    }

    @Atomic
    private Site create(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        getLogger().info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");

        ResearchUnitSite newSite = new ResearchUnitSite(oldSite.getUnit());

        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setName(oldSite.getUnit().getNameI18n().toLocalizedString());
        newSite.setSlug(createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));
        Page.create(newSite, null, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

        createStaticPages(newSite, null, oldSite);
        createDynamicPages(newSite, sideMenu);
        createMenuComponents(newSite);
        migrateAnnouncements(oldSite, newSite);
        return newSite;
    }


    private void createDynamicPages(Site site, Menu menu) {
        Page.create(site, null, null,  getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "label.researchers"), true, "members",
                new ResearchUnitComponent());
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.subunits"), true, "subunits", new SubUnits());

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.organization"), true, "unitOrganization",
                new Organization());

        Page homepage = Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.homepage"), true, "unitHomepage",
                new HomeComponent());

        site.setInitialPage(homepage);

        Component eventsCategory = new ListCategoryPosts(site.categoryForSlug("event", EVENTS));
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.events"), true, "category", eventsCategory);

    }


}
