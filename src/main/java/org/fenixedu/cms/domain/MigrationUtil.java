package org.fenixedu.cms.domain;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.cms.TemplatedSectionInstance;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import net.sourceforge.fenixedu.domain.messaging.AnnouncementBoard;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.Component;
import org.fenixedu.bennu.cms.domain.component.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.component.SideMenuComponent;
import org.fenixedu.bennu.cms.domain.component.TopMenuComponent;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MigrationUtil {
    private static final Logger log = LoggerFactory.getLogger(MigrationUtil.class);

    private static final Set<String> siteSlugs = Sets.newHashSet();

    public static final String BUNDLE = "resources.FenixEduCMSResources";
    public static final CMSTheme THEME = CMSTheme.forType("fenixedu-default-theme");

    public static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    public static final LocalizedString MENU = getLocalizedString("resources.FenixEduCMSResources", "label.menu");

    public static final MultiLanguageString ANNOUNCEMENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt,
            "Anúncios");
    public static final LocalizedString ANNOUNCEMENTS_TITLE = getLocalizedString(BUNDLE, "label.announcements");
    public static final String ANNOUNCEMENTS_SLUG = "announcements";

    public static final MultiLanguageString EVENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt, "Eventos");
    public static final LocalizedString EVENTS_TITLE = getLocalizedString(BUNDLE, "label.events");
    public static final String EVENTS_SLUG = "events";

    public static final LocalizedString VIEW_POST_TITLE = getLocalizedString(BUNDLE, "label.viewPost");

    //Template Names
    //Homepage Site Templates
    public static final String RESEARCHER_SECTION_TEMPLATE = "researcherSection";
    public static final String PRESENTATION_TEMPLATE = "presentation";
    //Execution Course Site Templates
    public static final String OBJECTIVES_TEMPLATE = "objectives";
    public static final String EVALUATION_METHOD_TEMPLATE = "evaluationMethods";
    public static final String BIBLIOGRAPHIC_REFERENCES_TEMPLATE = "bibliographicReferences";
    public static final String EVALUATIONS_TEMPLATE = "evaluations";
    public static final String MARKS_TEMPLATE = "marks";
    public static final String PROGRAM_TEMPLATE = "program";
    public static final String LESSON_PLAN_TEMPLATE = "lessonPlan";
    public static final String GROUPS_TEMPLATE = "groupings";
    public static final String SHIFTS_TEMPLATE = "shifts";
    public static final String INQUIRIES_RESULTS_TEMPLATE = "inqueriesResults";
    public static final String INITIAL_PAGE_TEMPLATE = "firstPage";
    public static final String SCHEDULE_TEMPLATE = "schedule";
    //Research Unit Site Templates
    public static final String UNIT_MEMBERS_TEMPLATE = "members";
    public static final String SUBUNITS_TEMPLATE = "subunits";
    public static final String UNIT_HOMEPAGE_TEMPLATE = "unitHomepage";
    public static final String UNIT_ORGANIZATION_TEMPLATE = "unitOrganization";
    //Generic Templates
    public static final String POSTS_TEMPLATE = "posts";
    public static final String VIEW_TEMPLATE = "view";
    public static final String CATEGORIES_TEMPLATE = "categories";
    public static final String CATEGORY_TEMPLATE = "category";
    //Others
    public static final String INQUIRIES_RESULTS_NOT_AVAILABLE_TEMPLATE = "inquiriesResultsNotAvailable";
    public static final String INQUIRIES_CURRICULAR_RESULTS_TEMPLATE = "inquiriesCurricularResults";
    public static final String INQUIRIES_STUDENT_RESULTS_TEMPLATE = "inquiriesStudentResults";
    public static final String INQUIRIES_STUDENT_FULL_RESULTS_TEMPLATE = "inquiriesStudentFullResults";

    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();
    private static Predicate<String> matchesTop = name -> "top".equalsIgnoreCase(name) || "topo".equalsIgnoreCase(name);
    private static Predicate<String> matchesSide = name -> "side".equalsIgnoreCase(name) || "lateral".equalsIgnoreCase(name);

    private static Map<String, PageTemplate> pages;
    private static Site site;

    public static void deleteAllSites() {
        int size = Bennu.getInstance().getSitesSet().size() - 1;
        for (Site site : Bennu.getInstance().getSitesSet()) {
            log.info("Deleting site " + site.getFullUrl() + ". Remaining sites: " + size + ".");
            atomic(() -> site.delete());
            size = size - 1;
        }
    }

    public static void deleteSiteClass(Class<? extends Site> siteClass) {
        Site[] sites =
                Bennu.getInstance().getSitesSet().stream().filter(site -> siteClass.isInstance(site))
                        .toArray(size -> new Site[size]);
        int size = sites.length - 1;
        for (Site site : sites) {
            log.info("Deleting site " + site.getFullUrl() + ". Remaining sites: " + size + ".");
            atomic(() -> site.delete());
            size = size - 1;
        }
    }

    public static void deleteAllFolders() {
        Bennu.getInstance().getCmsFolderSet().stream().forEach(folder -> atomic(() -> folder.delete()));
    }

    public static void deleteMatchingFolder(String slug) {
        Bennu.getInstance().getCmsFolderSet().stream()
                .filter(folder -> folder.getFunctionality().getPath().matches("\\A" + slug + "\\z"))
                .forEach(folder -> atomic(() -> folder.delete()));
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
        private final boolean isOnMenu;

        private String path;
        private Page page;

        public PageTemplate(LocalizedString name, String slug, User creator, DateTime creationDate, Boolean published,
                String template, boolean isOnMenu, Component... components) {
            this.creator = creator;
            this.name = name;
            this.creationDate = creationDate;
            this.published = published;
            this.template = template;
            this.slug = slug;
            this.components = components;
            this.isOnMenu = isOnMenu;
        }

        public PageTemplate(LocalizedString name, String slug, String template, boolean isOnMenu, Component... components) {
            this.name = name;
            this.template = template;
            this.slug = slug;
            this.components = components;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
            this.isOnMenu = isOnMenu;
        }

        public PageTemplate(LocalizedString name, String slug, String template, Component... components) {
            this.name = name;
            this.template = template;
            this.slug = slug;
            this.components = components;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
            this.isOnMenu = true;
        }

        public PageTemplate(String template, Component... components) {
            this.template = template;
            this.components = components;
            this.slug = null;
            this.name = null;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
            this.isOnMenu = true;
        }

        private PageTemplate() {
            this.template = null;
            this.components = null;
            this.slug = null;
            this.name = null;
            this.creationDate = null;
            this.creator = null;
            this.published = null;
            this.isOnMenu = true;
        }

        public Page buildPage(Section section, Site site) {
            Preconditions.checkArgument(section != null, "Trying to build a page based of a null section.");

            String sectionPath = site.getBaseUrl() + MigrationUtil.getSectionPath(section);
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

        public Page buildPage(Site site) {
            if (page == null || path == null || !path.equals(site.getBaseUrl())) {
                Page newPage = new Page();
                newPage.setSite(site);
                newPage.setName(name != null ? name : localizedStr(""));
                if (slug != null) {
                    newPage.setSlug(slug);
                };
                newPage.setCreatedBy(creator != null ? creator : site.getCreatedBy());
                newPage.setCreationDate(creationDate != null ? creationDate : site.getCreationDate());
                newPage.setPublished(published != null ? published : true);
                newPage.setTemplate(template != null ? site.getTheme().templateForType(template) : site.getTheme()
                        .templateForType("category"));

                if (components == null) {
                    Category category = new Category();
                    category.setCreatedBy(newPage.getCreatedBy());
                    category.setCreationDate(newPage.getCreationDate());
                    category.setName(newPage.getName());
                    category.setSite(site);
                    newPage.addComponents(new ListCategoryPosts(category));
                } else {
                    for (Component component : components) {
                        newPage.addComponents(component);
                    }
                }

                path = site.getBaseUrl();
                this.page = newPage;

                log.info("Page { name: " + page.getName().getContent() + ", address: " + page.getAddress() + " } created");
            }
            return page;
        }

        public boolean isOnMenu() {
            return isOnMenu;
        }
    }

    public static void migrateSite(Site newSite, net.sourceforge.fenixedu.domain.Site oldSite) {
        migrateSite(newSite, oldSite, new HashMap<String, PageTemplate>());
    }

    public static void addPages(Site newSite, Collection<PageTemplate> pageTemplates, boolean topMenu) {
        Menu menu = newSite.getMenusSet().isEmpty() ? new Menu(newSite, MENU) : newSite.getMenusSet().iterator().next();

        Component menuComponent;
        if (topMenu) {
            menuComponent = new TopMenuComponent(menu);
        } else {
            menuComponent = new SideMenuComponent(menu);
        }
        for (PageTemplate pageTemplate : pageTemplates) {
            Page page = pageTemplate.buildPage(newSite);
            if (pageTemplate.isOnMenu) {
                MenuItem.create(menu, page, page.getName(), null);
            }
            page.addComponents(menuComponent);
        }
    }

    public static void addPages(Site newSite, Collection<PageTemplate> pageTemplates) {
        addPages(newSite, pageTemplates, false);
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
            Component menuComponent = new SideMenuComponent(sideMenu);
            newSite.getPagesSet().forEach(p -> p.addComponents(menuComponent));
        }

        if (topMenu != null) {
            Component menuComponent = new TopMenuComponent(topMenu);
            newSite.getPagesSet().forEach(p -> p.addComponents(menuComponent));
        }

        newSite.setCreatedBy(Authenticate.getUser()); //sites do not have a creator. This could have been set to another value before so that pages and posts inherit it though.

        Menu mainMenu = sideMenu == null || sideMenu.getChildrenSorted().isEmpty() ? topMenu : sideMenu;
        newSite.setInitialPage(mainMenu.getChildrenSorted().get(0).getPage()); //first page in the main site menu is the initial page by default

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
        log.info("Migrating section " + getSectionPath(section));
        boolean isTemplatedSection = isTemplatedSection(section);
        boolean isStaticFolderSection =
                !isTemplatedSection && section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
        Page page = null;
        final MenuItem newParent;
        boolean doMenuItem = true;
        if (!isStaticFolderSection) {
            PageTemplate pageTemplate = pages.get(getSectionPath(section));
            if (pageTemplate == null && isTemplatedSection) {
                //missing page, possibly to delete on migration
                log.warn("Couldn't find matching page for TemplatedSection " + getTemplatedSection(section).getCustomPath());
            } else if (pageTemplate == null) {
                page = PageTemplate.staticPage.buildPage(section, site);
            } else {
                page = pageTemplate.buildPage(section, site);
                doMenuItem = pageTemplate.isOnMenu;
            }
        }
        if (doMenuItem && menu != null) {
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

    public static <T> Set<T> sitesForClass(Class<T> siteClass) {
        return Sets.newHashSet(Iterables.filter(Bennu.getInstance().getSiteSet(), siteClass));
    }

    public static String createSlug(net.sourceforge.fenixedu.domain.Site oldSite) {
        String newSlug = oldSite.getReversePath().substring(1).replace("/", "-");
        while (siteSlugs.contains(newSlug)) {
            String randomSlug = UUID.randomUUID().toString().substring(0, 3);
            newSlug = Joiner.on("-").join(newSlug, randomSlug);
        }
        siteSlugs.add(newSlug);
        return newSlug;
    }

    public static <T extends AnnouncementBoard> void migrateAnnouncements(Site newSite, Iterator<T> boards) {
        boards.forEachRemaining(board -> migrateAnnouncements(newSite, board));
    }

    public static void migrateAnnouncements(Site newSite, AnnouncementBoard board) {
        boolean isAnnouncementsBoard = board.isPublicToRead() && board.getName().equalInAnyLanguage(ANNOUNCEMENTS_NAME);
        boolean isEventsBoard = board.isPublicToRead() && board.getName().equalInAnyLanguage(EVENTS_NAME);

        for (Announcement announcement : board.getAnnouncementSet()) {
            boolean hasSubject = announcement.getSubject() != null && !announcement.getSubject().isEmpty();
            boolean hasBody = announcement.getBody() != null && !announcement.getBody().isEmpty();
            if (hasSubject && hasBody) {
                Post post = new Post();
                post.setSite(newSite);

                post.setCreatedBy(announcement.getCreator() != null ? announcement.getCreator().getUser() : null);
                post.setCreationDate(announcement.getCreationDate());
                post.setBody(localized(announcement.getBody()));
                post.setName(localized(announcement.getSubject()));
                post.setActive(announcement.getVisible());
                post.setLocation(localizedStr(announcement.getPlace()));
                post.setPublicationBegin(announcement.getPublicationBegin());
                post.setPublicationEnd(announcement.getPublicationEnd());
                post.setReferedSubjectBegin(announcement.getReferedSubjectBegin());
                post.setReferedSubjectEnd(announcement.getReferedSubjectEnd());

                if (isAnnouncementsBoard) {
                    post.addCategories(newSite.categoryForSlug("announcement", ANNOUNCEMENTS_TITLE));
                }
                if (isEventsBoard) {
                    post.addCategories(newSite.categoryForSlug("event", EVENTS_TITLE));
                }

                announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                        .map(name -> newSite.categoryForSlug(name.getContent(), name))
                        .forEach(category -> post.addCategories(category));

                Space campus = announcement.getCampus();
                if (campus != null) {
                    String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                    if (!Strings.isNullOrEmpty(campusName)) {
                        post.addCategories(newSite.categoryForSlug("campus-" + campus.getExternalId(), localizedStr(campusName)));
                    }
                }
            }
        }
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

}
