package org.fenixedu.cms.domain.department;

import net.sourceforge.fenixedu.domain.*;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.domain.component.Component;
import org.fenixedu.bennu.cms.domain.component.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.component.StrategyBasedComponent;
import org.fenixedu.bennu.cms.domain.component.ViewPost;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.cms.domain.researchUnit.componenets.HomeComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.Organization;
import org.fenixedu.cms.domain.researchUnit.componenets.SubUnits;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

/**
 * Created by borgez on 8/29/14.
 */
public class MigrateDepartmentSites extends MigrationTask {

    @Override public void runTask() throws Exception {
        deleteAllSites();
        sitesForClass(net.sourceforge.fenixedu.domain.DepartmentSite.class).forEach(oldSite -> create(oldSite));
    }

    private void create(net.sourceforge.fenixedu.domain.DepartmentSite oldSite) {
        getLogger().info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
        DepartmentSite newSite = new DepartmentSite(oldSite.getDepartment());
        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setName(oldSite.getUnit().getNameI18n().toLocalizedString());
        newSite.setSlug(oldSite.getReversePath().substring(1).replace("/", "-"));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));
        Page.create(newSite, null, null,  getLocalizedString(BUNDLE, "label.viewPost"), true, "view", Authenticate.getUser(),
                StrategyBasedComponent.forType(ViewPost.class));

        createStaticPages(newSite, null, oldSite);
        createDynamicPages(newSite, sideMenu);
        createMenuComponents(newSite);
        migrateAnnouncements(oldSite, newSite);
    }

    private void createDynamicPages(DepartmentSite site, Menu menu) {
        //TODO - pages names (resources) should refeer to unit or department instead of researchUnit

        User user = Authenticate.getUser();

        Component eventsCategory = new ListCategoryPosts(site.categoryForSlug("event", EVENTS));
        Component announcementsCategory = new ListCategoryPosts(site.categoryForSlug("announcement", ANNOUNCEMENTS));

        Page.create(site, null, null,  getLocalizedString(BUNDLE, "label.viewPost"), true, "view", user, StrategyBasedComponent
                .forType(ViewPost.class));

        site.setInitialPage(Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.homepage"), true,
                "unitHomepage", user, new HomeComponent()));

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.events"), true, "category", user, eventsCategory);

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.events"), true, "category", user,
                announcementsCategory);

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.organization"), true, "unitOrganization", user,
                new Organization());

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.subunits"), true, "subunits", user, new SubUnits());

    }

}
