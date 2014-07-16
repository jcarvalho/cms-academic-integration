package org.fenixedu.cms.domain.executionCourse;

import java.util.Collection;
import java.util.List;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;

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

    @Override
    public void runTask() throws Exception {
        deleteAllSites();

        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612946319"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612917134"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612898443"));
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
        Site newSite = createSiteInstance(oldSite);

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
        createPages(newSite, menu, null, oldSite.getOrderedSections());
    }

    private String createSlug(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {
        String slug = oldSite.getReversePath();
        if (slug.startsWith("/")) {
            slug = StringUtils.right(oldSite.getReversePath(), slug.length() - 1);
        }
        slug = StringUtils.replace(slug, "/", "-");
        return slug;
    }

    private Site createSiteInstance(net.sourceforge.fenixedu.domain.Site oldSite) {
        if(oldSite instanceof net.sourceforge.fenixedu.domain.ExecutionCourseSite) {
            return createSite((net.sourceforge.fenixedu.domain.ExecutionCourseSite) oldSite);
        }
        return new Site();
    }

    private Site createSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {
        return new ExecutionCourseSite(oldSite.getExecutionCourse());
    }

    private Menu createMenu(Site site, List<Section> orderedSections) {
        Menu menu = new Menu();
        menu.setName(BundleUtil.getLocalizedString("resource.FenixEduCMSResources", "label.menu"));
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

    private void createPages(Site site, Menu menu, MenuItem menuItemParent, Collection<Section> sections) {
        for (Section section : sections) {
            Page page = createPage(site, menu, section);
            MenuItem menuItem = page != null ? createMenuItem(site, menu, page, section, menuItemParent) : null;
            if (!section.getChildrenSections().isEmpty()) {
                createPages(site, menu, menuItem, section.getChildrenSections());
            }
        }
    }

    private Page createPage(Site site, Menu menu, Section section) {
        if (section instanceof TemplatedSection) {
            return createDynamicPage(site, menu, (TemplatedSection) section);
        } else {
            return createStaticPage(site, menu, section);
        }
    }

    private Page createDynamicPage(Site site, Menu menu, TemplatedSection section) {
        switch (section.getCustomPath()) {
        case "/publico/executionCourse.do?method=summaries":
            createSummariesPage(site, menu, section);
            break;
        case "/publico/executionCourse.do?method=objectives":
            createPage(site, menu, section, new ObjectivesComponent(), "objectives");
            break;
        case "/publico/executionCourse.do?method=evaluationMethod":
            createPage(site, menu, section, new EvaluationMethodsComponent(), "evaluationMethods");
            break;
        case "/publico/executionCourse.do?method=bibliographicReference":
            createPage(site, menu, section, new BibliographicReferencesComponent(), "bibliographicReferences");
            break;
        case "/publico/executionCourse.do?method=evaluations":
            createPage(site, menu, section, new EvaluationsComponent(), "evaluations");
            createPage(site, menu, section, new MarksComponent(), "marks");
            break;
        case "/publico/executionCourse.do?method=program":
            createPage(site, menu, section, new ObjectivesComponent(), "program");
            break;
        case "/publico/executionCourse.do?method=lessonPlannings":
            createPage(site, menu, section, new LessonsPlanningComponent(), "lessonPlanings");
            break;
        case "/publico/executionCourse.do?method=groupings":
            createPage(site, menu, section, new GroupsComponent(), "groupings");
            break;
        case "/publico/executionCourse.do?method=shifts":
            createPage(site, menu, section, new ExecutionCourseComponent(), "shifts");
            break;
        case "/publico/executionCourse.do?method=studentInquiriesResults":
            createPage(site, menu, section, new InquiriesResultsComponent(), "inqueriesResults");
            break;
        case "/publico/executionCourse.do?method=firstPage":
            createPage(site, menu, section, new InitialPageComponent(), "firstPage");
            break;
        case "/publico/executionCourse.do?method=schedule":
            createPage(site, menu, section, new ScheduleComponent(), "schedule");
            break;
        default:
            break;
        }
        return null;
    }

    private void createPage(Site site, Menu menu, TemplatedSection section, Component component, String template) {
        Page page = new Page();
        page.setName(localized(section.getName()));
        page.setSite(site);
        page.addComponents(component);
        page.setTemplate(site.getTheme().templateForType(template));
        page.setPublished(section.getEnabled());

        createMenuItem(site, menu, page, section, null);
        createMenuComponenet(menu, page);
    }

    private void createViewPostPage(Site site) {
        Page page = new Page();
        page.setName(new LocalizedString(I18N.getLocale(), "View"));
        page.setSite(site);
        page.addComponents(new ViewPost());
        page.setTemplate(site.getTheme().templateForType("view"));
    }

    private void createSummariesPage(Site site, Menu menu, TemplatedSection section) {
        migrateSummaries((ExecutionCourseSite) site, menu);
        Page page = new Page();

        page.setCreationDate(site.getCreationDate());
        page.setName(localized(section.getName()));
        page.setPublished(section.getEnabled());
        page.setSite(site);

        ListCategoryPosts component = new ListCategoryPosts();
        component.setCategory(site.categoryForSlug("summary"));
        component.setPage(page);
        page.setTemplate(site.getTheme().templateForType("category"));

        createMenuItem(site, menu, page, section, null);
        createMenuComponenet(menu, page);
    }

    private void migrateSummaries(ExecutionCourseSite site, Menu menu) {
        site.categoryForSlug("summary", BundleUtil.getLocalizedString("resource.FenixEduCMSResources", "label.summaries"));
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
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

    private static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }

}