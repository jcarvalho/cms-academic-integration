package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.cms.domain.executionCourse.components.BibliographicReferencesComponent;
import org.fenixedu.cms.domain.executionCourse.components.EvaluationMethodsComponent;
import org.fenixedu.cms.domain.executionCourse.components.EvaluationsComponent;
import org.fenixedu.cms.domain.executionCourse.components.ExecutionCourseComponent;
import org.fenixedu.cms.domain.executionCourse.components.GroupsComponent;
import org.fenixedu.cms.domain.executionCourse.components.InitialPageComponent;
import org.fenixedu.cms.domain.executionCourse.components.InquiriesResultsComponent;
import org.fenixedu.cms.domain.executionCourse.components.LessonsPlanningComponent;
import org.fenixedu.cms.domain.executionCourse.components.MarksComponent;
import org.fenixedu.cms.domain.executionCourse.components.ObjectivesComponent;
import org.fenixedu.cms.domain.executionCourse.components.ScheduleComponent;
import org.fenixedu.commons.i18n.LocalizedString;

import com.google.common.eventbus.Subscribe;

public class ExecutionCourseListener {

    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final LocalizedString VIEW_POST = getLocalizedString(BUNDLE, "label.viewPost");

    private static final LocalizedString TITLE_BIBLIOGRAPHIC_REFS = getLocalizedString(BUNDLE, "label.bibliographicReferences");
    private static final LocalizedString TITLE_EVALUATION_METHODS = getLocalizedString(BUNDLE, "label.evaluationMethods");
    private static final LocalizedString TITLE_INQUIRIES_RESULTS = getLocalizedString(BUNDLE, "label.inquiriesResults");
    private static final LocalizedString TITLE_LESSONS_PLANINGS = getLocalizedString(BUNDLE, "label.lessonsPlanings");
    private static final LocalizedString TITLE_ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcements");
    private static final LocalizedString TITLE_INITIAL_PAGE = getLocalizedString(BUNDLE, "label.initialPage");
    private static final LocalizedString TITLE_EVALUATIONS = getLocalizedString(BUNDLE, "label.evaluations");
    private static final LocalizedString TITLE_OBJECTIVES = getLocalizedString(BUNDLE, "label.objectives");
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");
    private static final LocalizedString TITLE_SCHEDULE = getLocalizedString(BUNDLE, "label.schedule");
    private static final LocalizedString TITLE_PROGRAM = getLocalizedString(BUNDLE, "label.program");
    private static final LocalizedString TITLE_GROUPS = getLocalizedString(BUNDLE, "label.groups");
    private static final LocalizedString TITLE_SHIFTS = getLocalizedString(BUNDLE, "label.shifts");
    private static final LocalizedString TITLE_MARKS = getLocalizedString(BUNDLE, "label.marks");
    private static final LocalizedString SUMMARY = getLocalizedString(BUNDLE, "label.summaries");
    public static final LocalizedString MENU = getLocalizedString(BUNDLE, "label.menu");

    @Subscribe
    public void doIt(DomainObjectEvent<ExecutionCourse> event) {
        create(event.getInstance());
    }

    public static ExecutionCourseSite create(ExecutionCourse executionCourse) {
        ExecutionCourseSite newSite = new ExecutionCourseSite(executionCourse);

        newSite.setTheme(CMSTheme.forType(THEME));

        Menu menu = new Menu(newSite, MENU);
        Page.create(newSite, null, null, VIEW_POST, true, "view", new ViewPost());
        createDynamicPages(newSite, menu);
        return newSite;
    }

    private static void createDynamicPages(ExecutionCourseSite site, Menu menu) {
        ListCategoryPosts summaryCategory = new ListCategoryPosts(site.categoryForSlug("summary", SUMMARY));
        ListCategoryPosts announcementCategory = new ListCategoryPosts(site.categoryForSlug("announcement", ANNOUNCEMENTS));

        Page.create(site, menu, null, TITLE_INITIAL_PAGE, true, "firstPage", new InitialPageComponent(), announcementCategory);
        Page.create(site, menu, null, TITLE_BIBLIOGRAPHIC_REFS, true, "bibliographicReferences",
                new BibliographicReferencesComponent());
        Page.create(site, menu, null, TITLE_EVALUATION_METHODS, true, "evaluationMethods", new EvaluationMethodsComponent());
        Page.create(site, menu, null, TITLE_INQUIRIES_RESULTS, true, "inqueriesResults", new InquiriesResultsComponent());
        Page.create(site, menu, null, TITLE_LESSONS_PLANINGS, true, "lessonPlanings", new LessonsPlanningComponent());
        Page.create(site, menu, null, TITLE_EVALUATIONS, true, "evaluations", new EvaluationsComponent());
        Page.create(site, menu, null, TITLE_OBJECTIVES, true, "objectives", new ObjectivesComponent());
        Page.create(site, menu, null, TITLE_SHIFTS, true, "shifts", new ExecutionCourseComponent());
        Page.create(site, menu, null, TITLE_ANNOUNCEMENTS, true, "category", announcementCategory);
        Page.create(site, menu, null, TITLE_PROGRAM, true, "program", new ObjectivesComponent());
        Page.create(site, menu, null, TITLE_SCHEDULE, true, "schedule", new ScheduleComponent());
        Page.create(site, menu, null, TITLE_GROUPS, true, "groupings", new GroupsComponent());
        Page.create(site, menu, null, TITLE_MARKS, true, "marks", new MarksComponent());
        Page.create(site, menu, null, SUMMARY, true, "category", summaryCategory);
    }

}
