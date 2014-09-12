package org.fenixedu.cms.domain.homepage;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.cms.TemplatedSectionInstance;
import net.sourceforge.fenixedu.domain.homepage.Homepage;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.domain.component.Component;
import org.fenixedu.bennu.cms.domain.component.MenuComponent;
import org.fenixedu.bennu.cms.domain.component.StaticPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateHomepageSite extends CustomTask {

    @Override
    public void runTask() throws Exception {
        /*David Matos*/
        Homepage hp = FenixFramework.getDomainObject("910533118347");
        Site newSite;
        if (hp instanceof Homepage && hp.isHomepageActivated()) {
            newSite = new HomepageSite(hp);
        } else if (hp instanceof Homepage) {
            throw new Exception("Homepage is not activated");
        } else {
            throw new Exception("Not an Homepage domain object.");
        }

        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType("fenixedu-default-theme"));
        newSite.setDescription(localized(hp.getDescription()));
        newSite.setAlternativeSite(hp.getAlternativeSite());
        newSite.setName(localized(hp.getName())); // TODO set user friendly name
        newSite.setCreatedBy(hp.getPerson().getUser());
        newSite.setStyle(hp.getStyle());
        newSite.setPublished(true);

        Menu menu = createMenu(newSite, hp.getOrderedSections());
        createPages(newSite, menu, null, hp.getOrderedSections());
    }

    private Menu createMenu(Site site, List<Section> orderedSections) {
        Menu menu = new Menu();
        menu.setName(makeLocalized("Menu"));
        menu.setSite(site);
        return menu;
    }

    private MenuComponent createMenuComponent(Menu menu, Page page) {
        MenuComponent menuComponent = new MenuComponent();
        //TODO checkout the createdBy attributes of the site components
        menuComponent.setCreatedBy(Authenticate.getUser());
        menuComponent.setCreationDate(new DateTime());
        menuComponent.setMenu(menu);
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
        } else if (section instanceof TemplatedSectionInstance) {
            return createDynamicPage(site, menu, ((TemplatedSectionInstance) section).getSectionTemplate());
        } else {
            return createStaticPage(site, menu, section);
        }
    }

    private Page createDynamicPage(Site site, Menu menu, TemplatedSection section) {
        List<Component> components = new ArrayList<Component>();
        String templateType = "researcherSection";
        switch (section.getCustomPath()) {
        case "/publico/viewHomepage.do?method=show":
            templateType = "presentation";
            components.add(new HomepagePresentationComponent());
            break;
        case "/publico/viewHomepageResearch.do?method=showInterests":
            components.add(new HomepageResearcherComponent("researcher.interests.title.complete", "interests"));
            break;
        case "/publico/viewHomepageResearch.do?method=showPrizes":
            components.add(new HomepageResearcherComponent("researcher.PrizeAssociation.title.label", "prizes"));
            break;
        case "/publico/viewHomepageResearch.do?method=showParticipations":
            components.add(new HomepageResearcherComponent("link.activitiesManagement", "activities"));
            break;
        case "/publico/viewHomepageResearch.do?method=showPatents":
            components.add(new HomepageResearcherComponent("link.patentsManagement", "patents"));
            break;
        case "/publico/viewHomepageResearch.do?method=showPublications":
            components.add(new HomepageResearcherComponent("link.Publications", "publications"));
            break;
        default:
            break;
        }
        if (templateType != null && !components.isEmpty()) {
            Page page = new Page();
            page.setSite(site);
            page.setName(section.getName().toLocalizedString());
            for (Component component : components) {
                page.addComponents(component);
            }
            page.setTemplate(site.getTheme().templateForType(templateType));
            page.setCreatedBy(site.getCreatedBy());

            createMenuComponent(menu, page);
            return page;
        }
        return null;
    }

    private Page createStaticPage(Site site, Menu menu, Section section) {
        //create only if the page has static content
        Page page = new Page();
        page.setCreationDate(site.getCreationDate());
        page.setCreatedBy(site.getCreatedBy());
        page.setSite(site);
        page.setName(localized(section.getName()));
        page.setPublished(section.getEnabled());
        page.setTemplate(site.getTheme().templateForType("view"));
        for (Item item : section.getChildrenItems()) {
            createStaticPost(site, page, item);
        }
        createMenuComponent(menu, page);

        return page;
    }

    private void createStaticPost(Site site, Page page, Item item) {

        Post post = new Post();
        post.setCreatedBy(site.getCreatedBy());
        post.setSite(site);
        post.setName(localized(item.getName()));
        post.setBody(localized(item.getBody()));
        post.setCreationDate(new DateTime());

        StaticPost staticPostComponent = new StaticPost(post);
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