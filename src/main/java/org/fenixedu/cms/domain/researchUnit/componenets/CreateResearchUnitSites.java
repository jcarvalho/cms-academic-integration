package org.fenixedu.cms.domain.researchUnit.componenets;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.executionCourse.CreateExecutionCourseSite;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CreateResearchUnitSites extends CustomTask {
    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    @Override
    public void runTask() throws Exception {

        CreateExecutionCourseSite.deleteAllSites();

        List<net.sourceforge.fenixedu.domain.ResearchUnitSite> researchUnitSites =
                Lists.newArrayList(Iterables.filter(Bennu.getInstance().getSiteSet(),
                        net.sourceforge.fenixedu.domain.ResearchUnitSite.class));

        log.info(" [ creating research unit sites (existing " + researchUnitSites.size() + ") ]");

        for (net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite : researchUnitSites) {
            log.info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
            create(oldSite);
        }
    }

    private Site create(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        ResearchUnitSite newSite = new ResearchUnitSite(oldSite.getUnit());

        newSite.setPublished(true);
        newSite.setDescription(CreateExecutionCourseSite.localized(oldSite.getUnit().getNameI18n()));
        newSite.setName(CreateExecutionCourseSite.localized(oldSite.getName()));
        newSite.setSlug(createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        Menu menu = new Menu(newSite, getLocalizedString(BUNDLE, "label.menu"));
        Page.create(newSite, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());
        CreateExecutionCourseSite.createStaticPages(newSite, menu, null, oldSite.getOrderedSections());
        createDynamicPages(newSite, menu);

        return newSite;
    }

    private void createDynamicPages(Site site, Menu menu) {
        Page.create(site, menu, getLocalizedString(BUNDLE, "label.researchers"), true, "members", new ResearchUnitComponent());
    }

    private String createSlug(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        return Site.slugify(oldSite.getReversePath().substring(1).replace('/', '-'));
    }
}
