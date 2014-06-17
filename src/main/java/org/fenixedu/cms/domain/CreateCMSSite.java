package org.fenixedu.cms.domain;

import java.util.List;
import java.util.Locale;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuComponent;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.StaticPost;
import org.fenixedu.bennu.cms.domain.ViewPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;

import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class CreateCMSSite extends CustomTask {

    @Override
    public void runTask() throws Exception {
        /*compiladores*/
        net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite = FenixFramework.getDomainObject("2293514188720");
        Site newSite = createSiteInstance(oldSite);

        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType("cms-default-theme"));
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setName(localized(oldSite.getExecutionCourse().getNameI18N()));
//        if (!oldSite.getName().getAllContents().isEmpty()) {
//            newSite.setSlug(Iterables.getFirst(oldSite.getName().getAllContents(), null));
//        }
        newSite.setStyle(oldSite.getStyle());
        newSite.setPublished(true);

        Menu menu = createMenu(newSite, oldSite.getOrderedSections());
        createViewPostPage(newSite);
        createPages(newSite, menu, null, oldSite.getOrderedSections());
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
        menu.setName(makeLocalized("Menu"));
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
        MenuItem menuItem = new MenuItem();
        menuItem.setMenu(menu);
        menuItem.setName(localized(section.getName()));
        menuItem.setPage(page);
        menuItem.setParent(parent);
        menuItem.setPosition(section.getOrder());
        if (parent != null) {
            parent.add(menuItem);
            menu.add(menuItem);
        } else {
            menu.addToplevelItems(menuItem);
        }
        return menuItem;
    }

    private void createPages(Site site, Menu menu, MenuItem menuItemParent, List<Section> sections) {
        for (Section section : sections) {
            Page page = createPage(site, menu, section);
            MenuItem menuItem = page != null ? createMenuItem(site, menu, page, section, menuItemParent) : null;
            if (!section.getSubSections().isEmpty()) {
                createPages(site, menu, menuItem, section.getSubSections());
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
            createObjectivesPage(site, menu, section);
            break;
        case "/publico/executionCourse.do?method=evaluationMethod":
            createEvaluationMethodPage(site, menu, section);
            break;
        case "/publico/executionCourse.do?method=bibliographicReference":
            createBibliographicReferencePage(site, menu, section);
            break;
        default:
            break;
        }
        return null;
    }

    private void createBibliographicReferencePage(Site site, Menu menu, TemplatedSection section) {
        Page page = new Page();
        page.setName(section.getName().toLocalizedString());
        page.setSite(site);
        page.addComponents(new ExecutionCourseBibliographicReferences());
        page.setTemplate(site.getTheme().templateForType("bibliographicReferences"));

        createMenuItem(site, menu, page, section, null);
        createMenuComponenet(menu, page);
    }

    private void createEvaluationMethodPage(Site site, Menu menu, TemplatedSection section) {
        Page page = new Page();
        page.setName(section.getName().toLocalizedString());
        page.setSite(site);
        page.addComponents(new ExecutionCourseEvaluationMethods());
        page.setTemplate(site.getTheme().templateForType("evaluationMethods"));

        createMenuItem(site, menu, page, section, null);
        createMenuComponenet(menu, page);
    }

    private void createObjectivesPage(Site site, Menu menu, TemplatedSection section) {
        Page page = new Page();
        page.setName(section.getName().toLocalizedString());
        page.setSite(site);
        page.addComponents(new ExecutionCourseObjectives());
        page.setTemplate(site.getTheme().templateForType("objectives"));

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
        page.setName(section.getName().toLocalizedString());
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
        site.categoryForSlug("summary", makeLocalized("Summary"));
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
        page.setTemplate(site.getTheme().templateForType("view"));
        for (Item item : section.getChildrenItems()) {
            createStaticPost(site, page, item);
        }
        createMenuComponenet(menu, page);

        return page;
    }

    private void createStaticPost(Site site, Page page, Item item) {

        Post post = new Post();
        post.setSite(site);
        post.setName(localized(item.getName()));
        post.setBody(localized(item.getBody()));
        post.setCreationDate(new DateTime());

        StaticPost staticPostComponent = new StaticPost();
        staticPostComponent.setPage(page);
        staticPostComponent.setPost(post);
    }

    private static LocalizedString makeLocalized(String value) {
        LocalizedString.Builder builder = new LocalizedString.Builder();
        for (Locale locale : CoreConfiguration.supportedLocales()) {
            builder.with(locale, value);
        }
        return builder.build();
    }

    private static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : makeLocalized("");
    }

}