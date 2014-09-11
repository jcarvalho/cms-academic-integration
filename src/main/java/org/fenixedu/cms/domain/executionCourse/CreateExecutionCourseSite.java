package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.cms.domain.component.StrategyBasedComponent.forType;
import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.MigrationUtil.ANNOUNCEMENTS_SLUG;
import static org.fenixedu.cms.domain.MigrationUtil.ANNOUNCEMENTS_TITLE;
import static org.fenixedu.cms.domain.MigrationUtil.BIBLIOGRAPHIC_REFERENCES_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.BUNDLE;
import static org.fenixedu.cms.domain.MigrationUtil.CATEGORY_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.EVALUATIONS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.EVALUATION_METHOD_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.GROUPS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.INITIAL_PAGE_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.INQUIRIES_RESULTS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.LESSON_PLAN_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.MARKS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.OBJECTIVES_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.PROGRAM_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.SCHEDULE_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.SHIFTS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.VIEW_POST_TITLE;
import static org.fenixedu.cms.domain.MigrationUtil.VIEW_TEMPLATE;
import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.CMSFolder;
import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.component.ViewPost;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.MigrationUtil.PageTemplate;
import org.fenixedu.cms.domain.executionCourse.components.BibliographicReferencesComponent;
import org.fenixedu.cms.domain.executionCourse.components.EvaluationsComponent;
import org.fenixedu.cms.domain.executionCourse.components.ExecutionCourseComponent;
import org.fenixedu.cms.domain.executionCourse.components.GroupsComponent;
import org.fenixedu.cms.domain.executionCourse.components.InitialPageComponent;
import org.fenixedu.cms.domain.executionCourse.components.InquiriesResultsComponent;
import org.fenixedu.cms.domain.executionCourse.components.LessonPlanComponent;
import org.fenixedu.cms.domain.executionCourse.components.MarksComponent;
import org.fenixedu.cms.domain.executionCourse.components.ObjectivesComponent;
import org.fenixedu.cms.domain.executionCourse.components.ScheduleComponent;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Iterables;

public class CreateExecutionCourseSite extends CustomTask {

    private static final String EXECUTION_COURSE_SITE_FOLDER = "execution-course";
    private static final LocalizedString EXECUTION_COURSE_FOLDER_DESCRIPTION = getLocalizedString(BUNDLE,
            "executionCourse.folder.description");

    private static final String INITIAL_PAGE_PATH = "/publico/executionCourse.do?method=firstPage";
    private static final String GROUPS_PATH = "/publico/executionCourse.do?method=groupings";
    private static final String ANNOUNCEMENTS_PATH = "/publico/announcementManagement.do?method=start";
    private static final String EVALUATIONS_PATH = "/publico/executionCourse.do?method=evaluations";
    private static final String BIBLIOGRAPHIC_REFERENCES_PATH = "/publico/executionCourse.do?method=bibliographicReference";
    private static final String SCHEDULE_PATH = "/publico/executionCourse.do?method=schedule";
    private static final String EVALUATION_METHOD_PATH = "/publico/executionCourse.do?method=evaluationMethod";
    private static final String OBJECTIVES_PATH = "/publico/executionCourse.do?method=objectives";
    private static final String CONTENT_SEARCH_PATH = "/publico/searchFileContent.do?method=prepareSearchForExecutionCourse";
    private static final String LESSON_PLAN_PATH = "/publico/executionCourse.do?method=lessonPlannings";
    private static final String PROGRAM_PATH = "/publico/executionCourse.do?method=program";
    private static final String INQUIRIES_RESULTS_PATH = "/publico/executionCourse.do?method=studentInquiriesResults";
    private static final String SUMMARIES_PATH = "/publico/executionCourse.do?method=summaries";
    private static final String SHIFTS_PATH = "/publico/executionCourse.do?method=shifts";

    private static final LocalizedString INITIAL_PAGE_TITLE = getLocalizedString(BUNDLE, "label.initialPage");
    private static final LocalizedString GROUPS_TITLE = getLocalizedString(BUNDLE, "label.groups");
    private static final LocalizedString EVALUATIONS_TITLE = getLocalizedString(BUNDLE, "label.evaluations");
    private static final LocalizedString BIBLIOGRAPHIC_REFERENCES_TITLE = getLocalizedString(BUNDLE,
            "label.bibliographicReferences");
    private static final LocalizedString SCHEDULE_TITLE = getLocalizedString(BUNDLE, "label.schedule");
    private static final LocalizedString EVALUATION_METHOD_TITLE = getLocalizedString(BUNDLE, "label.evaluationMethods");
    private static final LocalizedString OBJECTIVES_TITLE = getLocalizedString(BUNDLE, "label.objectives");
    private static final LocalizedString MARKS_TITLE = getLocalizedString(BUNDLE, "label.marks");
    private static final LocalizedString LESSON_PLAN_TITLE = getLocalizedString(BUNDLE, "label.lessonsPlanings");
    private static final LocalizedString PROGRAM_TITLE = getLocalizedString(BUNDLE, "label.program");
    private static final LocalizedString INQUIRIES_RESULTS_TITLE = getLocalizedString(BUNDLE, "label.inquiriesResults");
    private static final LocalizedString SUMMARIES_TITLE = getLocalizedString(BUNDLE, "label.summaries");
    private static final LocalizedString SHIFTS_TITLE = getLocalizedString(BUNDLE, "label.shifts");

    private static final String SUMMARIES_SLUG = "summaries";

    private static final Map<String, PageTemplate> migrationTemplates = new HashMap<String, PageTemplate>();
    private static final List<PageTemplate> additionalTemplates = new ArrayList<PageTemplate>();
    private static CMSFolder ecFolder = null;

    /* XXX
     * BEFORE EXECUTING THIS SCRIPT PLEASE CORRECT SOME INVALID DATA ON DB BY RUNNING THE FOLLOWING COMMAND:
     * UPDATE SUMMARY SET SUMMARY_DATE_YEAR_MONTH_DAY = '2006-09-04' WHERE OID = 1498943591500;
     * */
    private static final int TRANSACTION_SIZE = 30;

    @Override
    public void runTask() throws Exception {
        MigrationUtil.deleteSiteClass(ExecutionCourseSite.class);
        MigrationUtil.deleteMatchingFolder(EXECUTION_COURSE_SITE_FOLDER);

        Set<net.sourceforge.fenixedu.domain.Site> sites = Bennu.getInstance().getSiteSet();
        getLogger().info("existing sites " + sites.size());

        Set<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites =
                MigrationUtil.sitesForClass(net.sourceforge.fenixedu.domain.ExecutionCourseSite.class);
        getLogger().info("starting migration of " + oldSites.size() + " execution course sites.");

        Iterable<List<net.sourceforge.fenixedu.domain.ExecutionCourseSite>> oldSitesChunks =
                Iterables.partition(oldSites, TRANSACTION_SIZE);
        getLogger().info("creating sites for " + +Iterables.size(oldSitesChunks) + " chunks.");

//        for (List<net.sourceforge.fenixedu.domain.ExecutionCourseSite> chunk : oldSitesChunks) {
//            atomic(() -> create(chunk));
//        }

        atomic(() -> create(oldSitesChunks.iterator().next()));
    }

    private static Map<String, PageTemplate> getMigrationTemplates() {
        if (migrationTemplates.isEmpty()) {
            migrationTemplates.put(GROUPS_PATH, new PageTemplate(GROUPS_TITLE, null, GROUPS_TEMPLATE,
                    forType(GroupsComponent.class)));
            migrationTemplates.put(EVALUATIONS_PATH, new PageTemplate(EVALUATIONS_TITLE, null, EVALUATIONS_TEMPLATE,
                    forType(EvaluationsComponent.class)));
            migrationTemplates.put(BIBLIOGRAPHIC_REFERENCES_PATH, new PageTemplate(BIBLIOGRAPHIC_REFERENCES_TITLE, null,
                    BIBLIOGRAPHIC_REFERENCES_TEMPLATE, forType(BibliographicReferencesComponent.class)));
            migrationTemplates.put(SCHEDULE_PATH, new PageTemplate(SCHEDULE_TITLE, null, SCHEDULE_TEMPLATE,
                    forType(ScheduleComponent.class)));
            migrationTemplates.put(EVALUATION_METHOD_PATH, new PageTemplate(EVALUATION_METHOD_TITLE, null,
                    EVALUATION_METHOD_TEMPLATE, forType(BibliographicReferencesComponent.class)));
            migrationTemplates.put(OBJECTIVES_PATH, new PageTemplate(OBJECTIVES_TITLE, null, OBJECTIVES_TEMPLATE,
                    forType(ObjectivesComponent.class)));
            //TODO content research
            //exceptionalPages.put(CONTENT_SEARCH_PATH, );
            migrationTemplates.put(LESSON_PLAN_PATH, new PageTemplate(LESSON_PLAN_TITLE, null, LESSON_PLAN_TEMPLATE,
                    forType(LessonPlanComponent.class)));
            migrationTemplates.put(PROGRAM_PATH, new PageTemplate(PROGRAM_TITLE, null, PROGRAM_TEMPLATE,
                    forType(ObjectivesComponent.class)));
            migrationTemplates.put(INQUIRIES_RESULTS_PATH, new PageTemplate(INQUIRIES_RESULTS_TITLE, null,
                    INQUIRIES_RESULTS_TEMPLATE, forType(InquiriesResultsComponent.class)));
            migrationTemplates.put(SHIFTS_PATH, new PageTemplate(SHIFTS_TITLE, null, SHIFTS_TEMPLATE,
                    forType(ExecutionCourseComponent.class)));
        }
        return migrationTemplates;
    }

    public static Map<String, PageTemplate> getMigrationTemplates(ExecutionCourseSite newSite) {
        Map<String, PageTemplate> siteIndependentMigrationTemplates = getMigrationTemplates();
        Map<String, PageTemplate> migrationTemplates = new HashMap<String, PageTemplate>(siteIndependentMigrationTemplates);

        Category summariesCategory = newSite.categoryForSlug(SUMMARIES_SLUG, ANNOUNCEMENTS_TITLE);
        ListCategoryPosts summariesComponent = new ListCategoryPosts(summariesCategory);

        Category announcementsCategory = newSite.categoryForSlug(ANNOUNCEMENTS_SLUG, ANNOUNCEMENTS_TITLE);
        ListCategoryPosts announcementsComponent = new ListCategoryPosts(announcementsCategory);

        migrationTemplates.put(INITIAL_PAGE_PATH, new PageTemplate(INITIAL_PAGE_TITLE, null, INITIAL_PAGE_TEMPLATE,
                forType(InitialPageComponent.class), announcementsComponent));
        migrationTemplates.put(ANNOUNCEMENTS_PATH, new PageTemplate(ANNOUNCEMENTS_TITLE, null, CATEGORY_TEMPLATE,
                announcementsComponent));
        migrationTemplates.put(SUMMARIES_PATH, new PageTemplate(SUMMARIES_TITLE, null, CATEGORY_TEMPLATE, summariesComponent));

        return migrationTemplates;
    }

    private static List<PageTemplate> getAdditionalTemplates() {
        if (additionalTemplates.isEmpty()) {
            additionalTemplates.add(new PageTemplate(MARKS_TITLE, null, MARKS_TEMPLATE, forType(MarksComponent.class)));
            additionalTemplates.add(new PageTemplate(VIEW_POST_TITLE, null, VIEW_TEMPLATE, false, forType(ViewPost.class)));
        }
        return additionalTemplates;
    }

    public static List<PageTemplate> getAdditionalTemplates(Site newSite) {
        return getAdditionalTemplates();
    }

    public static CMSFolder getFolder() {
        if (ecFolder == null) {
            ecFolder =
                    new CMSFolder(PortalConfiguration.getInstance().getMenu(), EXECUTION_COURSE_SITE_FOLDER,
                            EXECUTION_COURSE_FOLDER_DESCRIPTION);
        }
        return ecFolder;
    }

    private void create(List<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites) {
        getLogger().info("creating for sites " + oldSites.size());
        oldSites.stream().filter(oldSite -> oldSite.getSiteExecutionCourse() != null)
                .forEach(oldSite -> migrateExecutionCourseSite(oldSite));
    }

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    public void migrateExecutionCourseSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {

        ExecutionCourse executionCourse = oldSite.getExecutionCourse();

        ExecutionCourseSite newSite = new ExecutionCourseSite();
        newSite.setExecutionCourse(executionCourse);
        newSite.setDescription(MigrationUtil.localized(oldSite.getDescription()));
        newSite.setName(executionCourse.getNameI18N().toLocalizedString());
        newSite.setSlug(MigrationUtil.createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setStyle(oldSite.getStyle());
        newSite.setTheme(MigrationUtil.THEME);
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));

        getFolder().addSite(newSite);

        MigrationUtil.migrateSite(newSite, oldSite, getMigrationTemplates(newSite));
        MigrationUtil.addPages(newSite, getAdditionalTemplates(newSite));

        //TODO convert or integrate this into MigrationUtil functionality
        migrateSummaries(newSite);
        MigrationUtil.migrateAnnouncements(newSite, executionCourse.getBoard());

        newSite.setPublished(true);

    }

    private void migrateSummaries(ExecutionCourseSite site) {
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> SummaryListener.updatePost(new Post(), summary));
    }

}
