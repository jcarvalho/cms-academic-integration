package org.fenixedu.cms.domain;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.UnitSite;
import net.sourceforge.fenixedu.domain.cms.CmsContent;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import net.sourceforge.fenixedu.domain.messaging.AnnouncementBoard;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.domain.component.*;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import java.util.*;
import java.util.function.Predicate;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public abstract class MigrationTask extends CustomTask {

    protected static final Set<String> siteSlugs = Sets.newHashSet();

    protected static final String THEME = "fenixedu-default-theme";
    protected static final String BUNDLE = "resources.FenixEduCMSResources";

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    public static final LocalizedString MENU = getLocalizedString("resources.FenixEduCMSResources", "label.menu");

    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();

    protected static final MultiLanguageString ANNOUNCEMENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt,
            "Anúncios");
    protected static final MultiLanguageString EVENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt, "Eventos");
    protected static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");
    protected static final LocalizedString EVENTS = getLocalizedString(BUNDLE, "label.event");

    public Menu topMenu;
    public Menu sideMenu;

    public void createStaticPages(Site newSite, MenuItem menuItemParent, net.sourceforge.fenixedu.domain.Site oldSite) {
            List<Section> topMenuSections = topMenuSections(oldSite.getOrderedSections());
        List<Section> sideMenuSections = sideMenuSections(oldSite.getOrderedSections());

        topMenu = new Menu(newSite, TOP_MENU);
        sideMenu = new Menu(newSite, MENU);

        topMenuSections.forEach(s -> createStaticPage(newSite, topMenu, menuItemParent, s));
        sideMenuSections.forEach(s -> createStaticPage(newSite, sideMenu, menuItemParent, s));
    }

    public void createMenuComponents(Site newSite) {
        //assign top and side menu components to all pages
        for (Page page : newSite.getPagesSet()) {
            new TopMenuComponent(topMenu);
            new SideMenuComponent(sideMenu);
        }

        //remove unused menus
        if (topMenu.getComponentSet().isEmpty()) {
            topMenu.delete();
        }
        if (sideMenu.getComponentSet().isEmpty()) {
            sideMenu.delete();
        }
    }

    private List<Section> sideMenuSections(List<Section> sections) {
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

    private List<Section> topMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (isTopSection(section)) {
                sideMenuSections.addAll(section.getChildrenSections());
            }
        }
        return sideMenuSections;
    }

    private boolean isTopSection(Section section) {
        Predicate<String> predicate = name -> "top".equalsIgnoreCase(name) || "topo".equalsIgnoreCase(name);
        return section.getName().getAllContents().stream().anyMatch(predicate);
    }

    private boolean isSideSection(Section section) {
        Predicate<String> predicate = name -> "side".equalsIgnoreCase(name) || "lateral".equalsIgnoreCase(name);
        return section.getName().getAllContents().stream().anyMatch(predicate);
    }

    private boolean equalContent(LocalizedString str1, LocalizedString str2) {
        return str1.getContent().equalsIgnoreCase(str2.getContent());
    }

    private boolean isIgnoredSection(CmsContent cmsContent) {
        LocalizedString sectionName = cmsContent.getName().toLocalizedString();
        return cmsContent instanceof TemplatedSection || isTopSection((Section) cmsContent)
                || isSideSection((Section) cmsContent);
    }

    public void createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Section section) {
        List<Section> subsections = section.getOrderedSubSections();
        LocalizedString name = localized(section.getName());
        if (!isIgnoredSection(section)) {
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
                final Page page = Page.create(site, menu, menuItemParent, name, isPublished, "category", Authenticate.getUser(),pageCategory);
                page.setCreationDate(site.getCreationDate());

                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
                    Post.create(site, page, localized(item.getName()), localized(item.getBody()), category, isEnabled, Authenticate.getUser());
                });
            }
        }
        subsections.forEach(subsection -> createStaticPage(site, menu, menuItemParent, subsection));
    }

    public void deleteAllSites() {
        Set<org.fenixedu.bennu.cms.domain.Site> allSites = Bennu.getInstance().getSitesSet();
        Iterable<List<org.fenixedu.bennu.cms.domain.Site>> sitesChunks = Iterables.partition(allSites, 100);
        getLogger().info("removing all sites..");
        for(List<org.fenixedu.bennu.cms.domain.Site> siteChunk : sitesChunks) {
            getLogger().info("removing sites " + siteChunk.size());
            FenixFramework.atomic(() -> siteChunk.stream().forEach(s -> s.delete()));
        }
    }

    public LocalizedString localizedStr(String str) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(str)) {
            for (Locale locale : CoreConfiguration.supportedLocales()) {
                result = result.with(locale, str);
            }
        }
        return result;
    }

    public LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }

    public <T> Set<T> sitesForClass(Class<T> clazz) {
        return Sets.newHashSet(Iterables.filter(Bennu.getInstance().getSiteSet(), clazz));
    }


    public void migrateAnnouncements(UnitSite oldSite, Site newSite) {
        Unit researchUnit = oldSite.getUnit();
        for (AnnouncementBoard board : researchUnit.getBoardsSet()) {

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
                        post.addCategories(newSite.categoryForSlug("announcement", ANNOUNCEMENTS));
                    }
                    if (isEventsBoard) {
                        post.addCategories(newSite.categoryForSlug("event", EVENTS));
                    }

                    announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                            .map(name -> newSite.categoryForSlug(name.getContent(), name))
                            .forEach(category -> post.addCategories((Category) category));

                    Space campus = announcement.getCampus();
                    if (campus != null) {
                        String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                        if (!Strings.isNullOrEmpty(campusName)) {
                            post.addCategories(newSite
                                    .categoryForSlug("campus-" + campus.getExternalId(), localizedStr(campusName)));
                        }
                    }
                }
            }
        }
    }


    protected String createSlug(net.sourceforge.fenixedu.domain.Site oldSite) {
        String newSlug = oldSite.getReversePath().substring(1).replace("/", "-");
        while (siteSlugs.contains(newSlug)) {
            String randomSlug = UUID.randomUUID().toString().substring(0, 3);
            newSlug = Joiner.on("-").join(newSlug, randomSlug);
        }
        siteSlugs.add(newSlug);
        return newSlug;
    }
}
