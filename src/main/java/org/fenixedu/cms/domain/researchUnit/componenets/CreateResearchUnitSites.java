package org.fenixedu.cms.domain.researchUnit.componenets;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;

import net.sourceforge.fenixedu.domain.ResearchUnitSite;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.executionCourse.CreateExecutionCourseSite;
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
                Lists.newArrayList(Iterables.filter(Bennu.getInstance().getSiteSet(), ResearchUnitSite.class));

        log.info(" [ creating research unit sites (existing " + researchUnitSites.size() + ") ]");

        for (ResearchUnitSite oldSite : researchUnitSites) {
            log.info("[ old site: " + oldSite.getExternalId() + " ]");

            Site newSite = new Site();

            newSite.setPublished(true);

            newSite.setDescription(CreateExecutionCourseSite.localized(oldSite.getUnit().getNameI18n()));

            newSite.setName(CreateExecutionCourseSite.localized(oldSite.getName()));

            newSite.setSlug(createSlug(oldSite));

            newSite.setBennu(Bennu.getInstance());

            newSite.setTheme(CMSTheme.forType(THEME));

            Menu menu = new Menu(newSite, getLocalizedString(BUNDLE, "label.menu"));
            Page.create(newSite, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

            CreateExecutionCourseSite.createStaticPages(newSite, menu, null, oldSite.getOrderedSections());
        }
    }

    private String createSlug(ResearchUnitSite oldSite) {
        return Site.slugify(oldSite.getReversePath().replace('/', '-'));
    }
}
