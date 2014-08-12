package org.fenixedu.cms.domain.researchUnit;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.cms.domain.researchUnit.componenets.ResearchUnitComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.SubUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CreateResearchUnitSites extends MigrationTask {
    private static final Set<String> siteSlugs = Sets.newHashSet();

    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    Logger log = LoggerFactory.getLogger(CreateResearchUnitSites.class);

    @Override
    public void runTask() throws Exception {
        List<net.sourceforge.fenixedu.domain.ResearchUnitSite> researchUnitSites =
                Lists.newArrayList(Iterables.filter(Bennu.getInstance().getSiteSet(),
                        net.sourceforge.fenixedu.domain.ResearchUnitSite.class));

        deleteAllSites();

        log.info(" [ creating research unit sites (existing " + researchUnitSites.size() + ") ]");

        for (net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite : researchUnitSites) {
            if (oldSite.getName().toLocalizedString().getContent().equals("INESC-ID/ESW")) {
                log.info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
                create(oldSite);
            }
        }
    }

    private Site create(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        log.info("migrating old site '" + oldSite.getReversePath() + "'");
        ResearchUnitSite newSite = new ResearchUnitSite(oldSite.getUnit());

        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getUnit().getNameI18n()));
        newSite.setName(localized(oldSite.getName()));
        newSite.setSlug(createSlug(oldSite.getReversePath()));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));
        Page.create(newSite, null, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

        createStaticPages(newSite, null, oldSite);
        createDynamicPages(newSite, sideMenu);
        createMenuComponents(newSite);
        log.info("[ New Site: " + newSite.getName().getContent() + " at " + newSite.getInitialPage().getAddress());
        return newSite;
    }

    private void createDynamicPages(Site site, Menu menu) {
        log.info("creating dynamic pages for site " + site.getSlug());
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "label.researchers"), true, "members",
                new ResearchUnitComponent());
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.subunits"), true, "subunits", new SubUnits());
    }

    private String createSlug(String oldPath) {
        String newSlug = oldPath.substring(1).replace("/", "-");
        while (siteSlugs.contains(newSlug)) {
            String randomSlug = UUID.randomUUID().toString().substring(0, 3);
            newSlug = Joiner.on("-").join(newSlug, randomSlug);
        }
        siteSlugs.add(newSlug);
        return newSlug;
    }
}
