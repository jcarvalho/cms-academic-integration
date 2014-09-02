package org.fenixedu.cms.domain.researchUnit;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.researchUnit.componenets.ResearchUnitComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CreateResearchUnitSites extends CustomTask {
    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    Logger log = LoggerFactory.getLogger(CreateResearchUnitSites.class);

    @Override
    public void runTask() throws Exception {

        MigrationUtil.deleteAllSites();

        List<net.sourceforge.fenixedu.domain.ResearchUnitSite> researchUnitSites =
                Lists.newArrayList(Iterables.filter(Bennu.getInstance().getSiteSet(),
                        net.sourceforge.fenixedu.domain.ResearchUnitSite.class));

        log.info(" [ creating research unit sites (existing " + researchUnitSites.size() + ") ]");

        for (net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite : researchUnitSites) {
//            if (oldSite.getName().toLocalizedString().getContent().equals("INESC-ID/ESW")) {
            log.info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
            create(oldSite);
//            }
        }
    }

    private Site create(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        log.info("migrating old site '" + oldSite.getReversePath() + "'");
        ResearchUnitSite newSite = new ResearchUnitSite(oldSite.getUnit());

        newSite.setPublished(true);
        newSite.setDescription(MigrationUtil.localized(oldSite.getUnit().getNameI18n()));
        newSite.setName(MigrationUtil.localized(oldSite.getName()));
        newSite.setSlug(createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        Page.create(newSite, null, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", null, new ViewPost());
        createDynamicPages(newSite, newSite.getSideMenus().stream().findFirst().orElse(null));

        MigrationUtil.createStaticPages(newSite, null, oldSite);
        log.info("[ New Site: " + newSite.getName().getContent() + " at " + newSite.getInitialPage().getAddress());
        return newSite;
    }

    private void createDynamicPages(Site site, Menu menu) {
        log.info("creating dynamic pages for site " + site.getSlug());
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "label.researchers"), true, "members", null,
                new ResearchUnitComponent());
    }

    private String createSlug(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        return Site.slugify(oldSite.getReversePath().substring(1).replace('/', '-'));
    }
}
