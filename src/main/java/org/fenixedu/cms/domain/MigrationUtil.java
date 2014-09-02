package org.fenixedu.cms.domain;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.CmsContent;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.cms.TemplatedSectionInstance;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Component;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuComponent;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.SideMenuComponent;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.TopMenuComponent;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MigrationUtil {
    private static final Logger log = LoggerFactory.getLogger(MigrationUtil.class);

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    private static final LocalizedString SIDE_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.sideMenu");
    private static final LocalizedString MENU = getLocalizedString("resources.FenixEduCMSResources", "label.menu");

    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();
    private static Predicate<String> matchesTop = name -> "top".equalsIgnoreCase(name) || "topo".equalsIgnoreCase(name);
    private static Predicate<String> matchesSide = name -> "side".equalsIgnoreCase(name) || "lateral".equalsIgnoreCase(name);

    private static Map<String, PageTemplate> pages;
    private static Site site;

    public static void deleteAllSites() {
        log.info("Removing all sites..");
        for (Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

    public static class PageTemplate {

        public static final PageTemplate staticPage = new PageTemplate();

        private final LocalizedString name;
        private final String slug;
        private final User creator;
        private final DateTime creationDate;
        private final Boolean published;
        private final String template;
        private final Component[] components;

        private String path;
        private Page page;

        public PageTemplate(LocalizedString name, String slug, User creator, DateTime creationDate, Boolean published,
                String template, Component... components) {
            this.creator = creator;
            this.name = name;
            this.creationDate = creationDate;
            this.published = published;
            this.template = template;
            this.slug = slug;
            this.components = components;
        }

        public PageTemplate(LocalizedString name, String slug, String template, Component... components) {
            this.name = name;
            this.template = template;
            this.slug = slug;
            this.components = components;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
        }

        public PageTemplate(String template, Component... components) {
            this.template = template;
            this.components = components;
            this.slug = null;
            this.name = null;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
        }

        private PageTemplate() {
            this.template = null;
            this.components = null;
            this.slug = null;
            this.name = null;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
        }

        public Page buildPage(Section section, Site site) {
            Preconditions.checkArgument(section != null, "Trying to build a page based of a null section.");

            String sectionPath = getSectionPath(section);
            if (page == null || path == null || !sectionPath.equals(path)) {
                Page newPage = new Page();
                newPage.setSite(site);
                newPage.setName(name != null ? name : localized(section.getName()));
                if (slug != null) {
                    newPage.setSlug(slug);
                };
                newPage.setCreatedBy(creator != null ? creator : site.getCreatedBy());
                newPage.setCreationDate(creationDate != null ? creationDate : section.getCreationDate() != null ? section
                        .getCreationDate() : site.getCreationDate());
                newPage.setPublished(published != null ? published : Optional.ofNullable(section.getEnabled()).orElse(true)
                        && section.getVisible()); //enabled is generally not used
                newPage.setTemplate(template != null ? site.getTheme().templateForType(template) : site.getTheme()
                        .templateForType("category"));

                Category category =
                        site.getCategoriesSet().stream().filter(c -> c.getName().equals(newPage.getName())).findAny()
                                .orElse(null);
                if (category == null && (components == null || !section.getOrderedChildItems().isEmpty())) {
                    category = new Category();
                    category.setCreatedBy(newPage.getCreatedBy());
                    category.setCreationDate(newPage.getCreationDate());
                    category.setName(newPage.getName());
                    category.setSite(site);
                }

                if (components == null) {
                    newPage.addComponents(new ListCategoryPosts(category));
                } else {
                    for (Component component : components) {
                        newPage.addComponents(component);
                    }
                }

                User pageCreator = newPage.getCreatedBy();
                final Category postCategory = category;
                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    Post post = new Post();
                    post.setSite(site);
                    post.setName(localized(item.getName()));
                    post.setBody(localized(item.getBody()));
                    post.setCreatedBy(pageCreator);
                    post.setCreationDate(section.getCreationDate() != null ? section.getCreationDate() : new DateTime());
                    post.addCategories(postCategory);
                    post.setActive(Optional.ofNullable(item.getEnabled()).orElse(true) && item.getVisible()); //enabled is generally not used
                    });

                path = sectionPath;
                this.page = newPage;

                log.info("Page { name: " + page.getName().getContent() + ", address: " + page.getAddress()
                        + " } created from section at " + sectionPath);
            }
            return page;
        }
    }

    public static void migrateSite(Site newSite, net.sourceforge.fenixedu.domain.Site oldSite,
            Map<String, PageTemplate> exceptionalPages) {
        Preconditions.checkArgument(oldSite != null, "Trying to migrate from a null site.");
        Preconditions.checkArgument(newSite != null, "Trying to migrate to a null site.");

        log.info("Creating pages for site " + newSite.getSlug());

        site = newSite;
        pages = exceptionalPages;

        Menu sideMenu = migrateMenuSections(MENU, selectSideMenuSections(oldSite));
        Menu topMenu = migrateMenuSections(TOP_MENU, selectTopMenuSections(oldSite));

        if (sideMenu != null) {
            for (Page page : newSite.getPagesSet()) {
                new SideMenuComponent(sideMenu, page);
            }
        }

        if (topMenu != null) {
            for (Page page : newSite.getPagesSet()) {
                new TopMenuComponent(topMenu, page);
            }
        }

        newSite.setCreatedBy(Authenticate.getUser()); //sites do not have a creator. This could have been set to another value before so that pages and posts inherit it though.
    }

    private static Menu migrateMenuSections(LocalizedString name, List<Section> sections) {
        if (sections.size() > 1) {
            Menu menu = new Menu(site, name);
            menu.setCreatedBy(site.getCreatedBy());
            menu.setCreationDate(sections.get(0) != null && sections.get(0).getCreationDate() != null ? sections.get(0)
                    .getCreationDate() : site.getCreationDate());
            sections.stream().skip(1).forEach(section -> migrateSection(section, menu, null));
            return menu;
        }
        return null;
    }

    private static void migrateSection(Section section, Menu menu, MenuItem parent) {
        boolean isTemplatedSection = isTemplatedSection(section);
        boolean isStaticFolderSection =
                !isTemplatedSection && section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
        Page page = null;
        final MenuItem newParent;
        if (!isStaticFolderSection) {
            PageTemplate pageTemplate = pages.get(getSectionPath(section));
            if (pageTemplate == null && isTemplatedSection) {
                //missing page, possibly to delete on migration
                log.warn("Couldn't find matching page for TemplatedSection " + section.getFullPath());
            } else if (pageTemplate == null) {
                page = PageTemplate.staticPage.buildPage(section, site);
            } else {
                page = pageTemplate.buildPage(section, site);
            }
        }
        if (menu != null) {
            newParent = MenuItem.create(menu, page, localized(section.getName()), parent);
        } else {
            newParent = parent;
        }
        section.getOrderedSubSections().forEach(subsection -> migrateSection(subsection, menu, newParent));

    }

    private static List<Section> selectSideMenuSections(net.sourceforge.fenixedu.domain.Site site) {
        //children of top level Side Menu sections and top level sections that are neither Top nor Side Menu sections. The original Side Menu section is in first place.
        List<Section> sideMenuSections = Lists.newArrayList();
        sideMenuSections.add(null);
        for (Section section : site.getOrderedSections()) {
            if (isSideSection(section)) {
                sideMenuSections.remove(0);
                sideMenuSections.add(0, section);
                sideMenuSections.addAll(section.getChildrenSections());
            } else if (!isTopSection(section)) {
                sideMenuSections.add(section);
            }
        }
        return sideMenuSections;
    }

    private static List<Section> selectTopMenuSections(net.sourceforge.fenixedu.domain.Site site) {
        //children of top level Top Menu sections. The original Top Menu section is in first place.
        List<Section> topMenuSections = Lists.newArrayList();
        topMenuSections.add(null);
        for (Section section : site.getOrderedSections()) {
            if (isTopSection(section)) {
                topMenuSections.remove(0);
                topMenuSections.add(0, section);
                topMenuSections.addAll(section.getChildrenSections());
            }
        }
        return topMenuSections;
    }

    private static boolean isTopSection(Section section) {
        return section.getName().getAllContents().stream().anyMatch(matchesTop);
    }

    private static boolean isSideSection(Section section) {
        return section.getName().getAllContents().stream().anyMatch(matchesSide);
    }

    private static boolean isTemplatedSection(Section section) {
        return section instanceof TemplatedSection || section instanceof TemplatedSectionInstance;
    }

    private static TemplatedSection getTemplatedSection(Section section) {
        if (section instanceof TemplatedSection) {
            return (TemplatedSection) section;
        } else if (section instanceof TemplatedSectionInstance) {
            return ((TemplatedSectionInstance) section).getSectionTemplate();
        }
        return null;
    }

    private static String getSectionPath(Section section) {
        if (isTemplatedSection(section)) {
            return getTemplatedSection(section).getCustomPath();
        }
        return section.getFullPath();
    }

    //TODO Check if the following methods can go somewhere else since they're not strictly related with migration
    public static LocalizedString localizedStr(String str) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(str)) {
            for (Locale locale : CoreConfiguration.supportedLocales()) {
                result = result.with(locale, str);
            }
        }
        return result;
    }

    public static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }

    //TODO replace all deprecated methods' uses and remove them.

    @Deprecated
    public static void createStaticPages(Site newSite, MenuItem menuItemParent, net.sourceforge.fenixedu.domain.Site oldSite) {
        log.info("creating static pages for site " + newSite.getSlug());
        List<Section> topMenuSections = topMenuSections(oldSite.getOrderedSections());
        List<Section> sideMenuSections = sideMenuSections(oldSite.getOrderedSections());
        Menu topMenu = new Menu(newSite, TOP_MENU);
        Menu sideMenu = new Menu(newSite, MENU);
        topMenuSections.forEach(s -> createStaticPage(newSite, topMenu, menuItemParent, s));
        sideMenuSections.forEach(s -> createStaticPage(newSite, sideMenu, menuItemParent, s));
        //assign top and side menu components to all pages5
        for (Page page : newSite.getPagesSet()) {
            if (!topMenuSections.isEmpty() && !hasMenu(page, topMenu)) {
                new TopMenuComponent(topMenu, page);
            }
            if (!sideMenuSections.isEmpty() && !hasMenu(page, sideMenu)) {
                new SideMenuComponent(sideMenu, page);
            }
        }
        if (topMenu.getComponentSet().isEmpty()) {
            topMenu.delete();
        }
        if (sideMenu.getComponentSet().isEmpty()) {
            sideMenu.delete();
        }
    }

    @Deprecated
    public static void createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Section section) {
        List<Section> subsections = section.getOrderedSubSections();
        if (!isIgnoredSection(section)) {
            LocalizedString name = localized(section.getName());
            log.info("migrating section " + name.getContent());
            //it means that the folder has no content and just acts like a folder on the menu
            boolean isFolderSection = section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
            if (isFolderSection) {
                MenuItem parent = MenuItem.create(menu, null, name, menuItemParent);
                subsections.forEach(subsection -> createStaticPage(site, menu, parent, subsection));
                return;
            } else {
                Category category = new Category();
                category.setName(name);
                ListCategoryPosts pageCategory = new ListCategoryPosts(category);

                boolean isPublished = Optional.ofNullable(section.getEnabled()).orElse(true);
                final Page page = Page.create(site, menu, menuItemParent, name, isPublished, "category", null, pageCategory);
                page.setCreationDate(site.getCreationDate());

                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
                    Post.create(site, page, localized(item.getName()), localized(item.getBody()), category, isEnabled, null);
                });
            }
        }
        subsections.forEach(subsection -> createStaticPage(site, menu, menuItemParent, subsection));
    }

    @Deprecated
    private static boolean hasMenu(Page page, Menu menu) {
        return menu.getComponentsOfClass(MenuComponent.class).stream().filter(m -> m.getPage().equals(page)).findAny()
                .isPresent();
    }

    @Deprecated
    private static List<Section> sideMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (isSideSection(section)) {
                sideMenuSections.addAll(section.getChildrenSections());
            } else if (!isTopSection(section)) {
                sideMenuSections.add(section);
            }
        }
        return sideMenuSections;
    }

    @Deprecated
    private static List<Section> topMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (isTopSection(section)) {
                sideMenuSections.addAll(section.getChildrenSections());
            }
        }
        return sideMenuSections;
    }

    @Deprecated
    private static boolean isIgnoredSection(CmsContent cmsContent) {
        return cmsContent instanceof TemplatedSection || isTopSection((Section) cmsContent)
                || isSideSection((Section) cmsContent);
    }

}
