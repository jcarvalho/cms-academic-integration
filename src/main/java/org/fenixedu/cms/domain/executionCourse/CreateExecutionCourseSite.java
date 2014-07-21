package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.messaging.Announcement;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Component;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuComponent;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
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
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class CreateExecutionCourseSite extends CustomTask {
    Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");
    private static final LocalizedString SUMMARY = getLocalizedString(BUNDLE, "label.summaries");
    private static final LocalizedString TITLE_MARKS = getLocalizedString(BUNDLE, "label.marks");
    private static final LocalizedString TITLE_OBJECTIVES = getLocalizedString(BUNDLE, "label.objectives");
    private static final LocalizedString TITLE_EVALUATION_METHODS = getLocalizedString(BUNDLE, "label.evaluationMethods");
    private static final LocalizedString TITLE_BIBLIOGRAPHIC_REFS = getLocalizedString(BUNDLE, "label.bibliographicReferences");
    private static final LocalizedString TITLE_EVALUATIONS = getLocalizedString(BUNDLE, "label.evaluations");
    private static final LocalizedString TITLE_LESSONS_PLANINGS = getLocalizedString(BUNDLE, "label.lessonsPlanings");
    private static final LocalizedString TITLE_GROUPS = getLocalizedString(BUNDLE, "label.groups");
    private static final LocalizedString TITLE_PROGRAM = getLocalizedString(BUNDLE, "label.program");
    private static final LocalizedString TITLE_SHIFTS = getLocalizedString(BUNDLE, "label.shifts");
    private static final LocalizedString TITLE_INQUIRIES_RESULTS = getLocalizedString(BUNDLE, "label.inquiriesResults");
    private static final LocalizedString TITLE_SCHEDULE = getLocalizedString(BUNDLE, "label.schedule");
    private static final LocalizedString TITLE_ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcements");
    private static final LocalizedString TITLE_INITIAL_PAGE = getLocalizedString(BUNDLE, "label.initialPage");

    @Override
    public void runTask() throws Exception {
        deleteAllSites();
//        Bennu.getInstance().getExecutionCoursesSet().stream().forEach(e -> createExecutionCourseSite(e.getSite()));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612946319"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612917134"));
//        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612898443"));
//        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612875684"));
//        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612846760"));
//        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612818202"));
//        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612802249"));

    }

    private void deleteAllSites() {
        Bennu.getInstance().getSitesSet().forEach(site -> site.delete());
    }

    private net.sourceforge.fenixedu.domain.ExecutionCourseSite oldExecutionCourseSiteByExecutionCourse(String executionCourseOID) {
        ExecutionCourse executionCourse = FenixFramework.getDomainObject(executionCourseOID);
        return executionCourse.getSite();
    }

    private void createExecutionCourseSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {
        ExecutionCourseSite newSite = createSiteInstance(oldSite);

        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setName(localized(oldSite.getExecutionCourse().getNameI18N()));
        newSite.setSlug(createSlug(oldSite));
        newSite.setStyle(oldSite.getStyle());
        newSite.setPublished(true);

        Menu menu = createMenu(newSite, oldSite.getOrderedSections());
        createViewPostPage(newSite);
        createDynamicPages(newSite, menu);
        createStaticPages(newSite, menu, null, oldSite.getOrderedSections());
    }

    private String createSlug(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {
        String slug = oldSite.getReversePath();
        if (slug.startsWith("/")) {
            slug = StringUtils.right(oldSite.getReversePath(), slug.length() - 1);
        }
        slug = StringUtils.replace(slug, "/", "-");
        return slug;
    }

    private ExecutionCourseSite createSiteInstance(net.sourceforge.fenixedu.domain.Site oldSite) {
        if(oldSite instanceof net.sourceforge.fenixedu.domain.ExecutionCourseSite) {
            return new ExecutionCourseSite(((net.sourceforge.fenixedu.domain.ExecutionCourseSite) oldSite).getExecutionCourse());
        }
        return null;
    }

    private Menu createMenu(Site site, List<Section> orderedSections) {
        Menu menu = new Menu();
        menu.setName(BundleUtil.getLocalizedString(BUNDLE, "label.menu"));
        menu.setSite(site);
        return menu;
    }

    private MenuComponent createMenuComponenet(Menu menu, Page page) {
        MenuComponent menuComponent = new MenuComponent();
        menuComponent.setCreatedBy(Authenticate.getUser());
        menuComponent.setCreationDate(new DateTime());
        menuComponent.setMenu(menu);
        menuComponent.setPage(page);
        return menuComponent;
    }

    private MenuItem createMenuItem(Site site, Menu menu, Page page, Section section, MenuItem parent) {
        MenuItem menuItem = createMenuItem(site, menu, page, localized(section.getName()), parent);
        menuItem.setPosition(section.getOrder());
        return menuItem;
    }

    private MenuItem createMenuItem(Site site, Menu menu, Page page, LocalizedString name, MenuItem parent) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setPage(page);
        menuItem.setParent(parent);
        menuItem.setMenu(menu);
        if (parent == null) {
            menu.add(menuItem);
        }
        return menuItem;
    }

    private void createStaticPages(Site site, Menu menu, MenuItem menuItemParent, Collection<Section> sections) {
        sections.stream().filter(section -> !(section instanceof TemplatedSection)).map(section -> section)
                .forEach(section -> {
                    Page page = createStaticPage(site, menu, section);
                    MenuItem menuItem = page != null ? createMenuItem(site, menu, page, section, menuItemParent) : null;
                    if (!section.getChildrenSections().isEmpty()) {
                        createStaticPages(site, menu, menuItem, section.getChildrenSections());
                    }
                });
    }

    private void createDynamicPages(ExecutionCourseSite site, Menu menu) {
        migrateSummaries(site, menu);
        migrateAnnouncements(site, menu);

        Page initialPage = createPage(site, menu, TITLE_INITIAL_PAGE, true, new InitialPageComponent(), "firstPage");
        initialPage.addComponents(new ListCategoryPosts(site.categoryForSlug("announcement")));

        createPage(site, menu, SUMMARY, true, new ListCategoryPosts(site.categoryForSlug("summary")), "category");
        createPage(site, menu, TITLE_ANNOUNCEMENTS, true, new ListCategoryPosts(site.categoryForSlug("announcement")), "category");
        createPage(site, menu, TITLE_OBJECTIVES, true, new ObjectivesComponent(), "objectives");
        createPage(site, menu, TITLE_EVALUATION_METHODS, true, new EvaluationMethodsComponent(), "evaluationMethods");
        createPage(site, menu, TITLE_BIBLIOGRAPHIC_REFS, true, new BibliographicReferencesComponent(), "bibliographicReferences");
        createPage(site, menu, TITLE_EVALUATIONS, true, new EvaluationsComponent(), "evaluations");
        createPage(site, menu, TITLE_MARKS, true, new MarksComponent(), "marks");
        createPage(site, menu, TITLE_PROGRAM, true, new ObjectivesComponent(), "program");
        createPage(site, menu, TITLE_LESSONS_PLANINGS, true, new LessonsPlanningComponent(), "lessonPlanings");
        createPage(site, menu, TITLE_GROUPS, true, new GroupsComponent(), "groupings");
        createPage(site, menu, TITLE_SHIFTS, true, new ExecutionCourseComponent(), "shifts");
        createPage(site, menu, TITLE_INQUIRIES_RESULTS, true, new InquiriesResultsComponent(), "inqueriesResults");
        createPage(site, menu, TITLE_SCHEDULE, true, new ScheduleComponent(), "schedule");

    }

    private Page createPage(Site site, Menu menu, LocalizedString name, boolean published, Component component, String template) {
        Page page = new Page();
        page.setName(name);
        page.setSite(site);
        page.addComponents(component);
        page.setTemplate(site.getTheme().templateForType(template));
        page.setPublished(published);
        createMenuItem(site, menu, page, name, null);
        createMenuComponenet(menu, page);
        return page;
    }

    private Page createStaticPage(Site site, Menu menu, Section section) {
        //create only if the page has static content
        Page page = new Page();
        page.setCreationDate(site.getCreationDate());
        page.setName(localized(section.getName()));
        page.setPublished(section.getEnabled());
        page.setSite(site);
        page.setTemplate(site.getTheme().templateForType("category"));
        Category category = new Category();
        category.setName(page.getName());
        page.addComponents(new ListCategoryPosts(category));
        for (Item item : section.getChildrenItems()) {
            createStaticPost(site, page, item, category);
        }
        createMenuComponenet(menu, page);

        return page;
    }

    private void createStaticPost(Site site, Page page, Item item, Category category) {
        Post post = new Post();
        post.setSite(site);
        post.setName(localized(item.getName()));
        post.setBody(localized(item.getBody()));
        post.setCreationDate(new DateTime());
        post.addCategories(category);
    }

    private void createViewPostPage(Site site) {
        Page page = new Page();
        page.setName(new LocalizedString(I18N.getLocale(), "View"));
        page.setSite(site);
        page.addComponents(new ViewPost());
        page.setTemplate(site.getTheme().templateForType("view"));
    }

    private void migrateAnnouncements(ExecutionCourseSite site, Menu menu) {
        for(Announcement announcement : site.getExecutionCourse().getBoard().getAnnouncementSet()) {
            Post post = new Post();
            post.setSite(site);
            post.setCreatedBy(announcement.getCreator().getUser());
            post.setCreationDate(announcement.getCreationDate());
            post.setBody(localized(announcement.getBody()));
            post.setName(localized(announcement.getSubject()));
            post.setActive(announcement.getVisible());
            post.setLocation(localized(announcement.getPlace()));
            post.setPublicationBegin(announcement.getPublicationBegin());
            post.setPublicationEnd(announcement.getPublicationEnd());
            
            post.addCategories(site.categoryForSlug("announcement", ANNOUNCEMENTS));
            
            announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                    .map(name -> site.categoryForSlug(name.getContent(), name)).forEach(category -> post.addCategories(category));
            
            if (announcement.getCampus() != null) {
                post.addCategories(site.categoryForSlug("campus-" + announcement.getCampus().getExternalId(),
                        localized(announcement.getCampus().getPresentationName())));
            }
        }
    }

    private void migrateSummaries(ExecutionCourseSite site, Menu menu) {
        site.categoryForSlug("summary", SUMMARY);
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
    }

    private static LocalizedString localized(String str) {
        LocalizedString result = new LocalizedString();
        for (Locale locale : CoreConfiguration.supportedLocales()) {
            result.with(locale, str);
        }
        return result;
    }

    private static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }

}