package org.fenixedu.cms.domain;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.cms.TemplatedSectionInstance;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
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
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class MigrationTask extends CustomTask {
    private static final Logger log = LoggerFactory.getLogger(MigrationTask.class);

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    private static final LocalizedString SIDE_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.sideMenu");
    private static final LocalizedString MENU = getLocalizedString("resources.FenixEduCMSResources", "label.menu");

    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();

    private static Predicate<String> matchesTop = name -> "top".equalsIgnoreCase(name) || "topo".equalsIgnoreCase(name);
    private static Predicate<String> matchesSide = name -> "side".equalsIgnoreCase(name) || "lateral".equalsIgnoreCase(name);

    //Migrated Site Parameters
    private Menu topMenu;
    private Menu sideMenu;
    private Site newSite;
    private net.sourceforge.fenixedu.domain.Site oldSite;
    private Map<String, Page> exceptionalPages;
    private boolean duplicateExceptionalPages;

    public void deleteAllSites() {
        log.info("Removing all sites..");
        for (Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

    protected void migrateSite(Site newSite, net.sourceforge.fenixedu.domain.Site oldSite, Map<String, Page> exceptionalPages,
            boolean duplicateExceptionalPages) {
        Preconditions.checkArgument(oldSite != null, "Trying to migrate from a null site.");
        Preconditions.checkArgument(newSite != null, "Trying to migrate to a null site.");

        log.info("Creating pages for site " + newSite.getSlug());

        this.newSite = newSite;
        this.oldSite = oldSite;

        this.exceptionalPages = exceptionalPages;
        this.duplicateExceptionalPages = duplicateExceptionalPages;

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

        this.newSite = null;
        this.oldSite = null;
        this.exceptionalPages = null;
        this.duplicateExceptionalPages = false;
    }

    private Menu migrateMenuSections(LocalizedString name, List<Section> sections) {
        if (!sections.isEmpty()) {
            Menu menu = new Menu(newSite, name);
            menu.setCreatedBy(newSite.getCreatedBy());
            menu.setCreationDate(sections.get(0) != null ? sections.get(0).getCreationDate() : newSite.getCreationDate());
            sections.stream().skip(1).forEach(section -> migrateSection(section, menu, null));
            return menu;
        }
        return null;
    }

    private void migrateSection(Section section, Menu menu, MenuItem parent) {
        boolean isTemplatedSection = isTemplatedSection(section);
        boolean isStaticFolderSection =
                !isTemplatedSection && section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
        if (!isStaticFolderSection) {
            Page page = null;
            if (isTemplatedSection) {
                TemplatedSection templatedSection = getTemplatedSection(section);
                page = exceptionalPages.get(templatedSection.getCustomPath());
            } else {
                page = exceptionalPages.get(section.getFullPath());
            }

            if (page == null && isTemplatedSection) {
                //missing, possibly to delete, page
                log.warn("Couldn't find matching page for TemplatedSection " + section.getFullPath());
            } else {
                //exceptional or new static page
                Category category = new Category();
                if (page == null) {
                    //new static page
                    page = new Page();
                    page.setSite(newSite); //FIXME need this before setName or NPE occurs
                    page.setName(localized(section.getName()));
                    page.setCreatedBy(newSite.getCreatedBy());  //FIXME more proper behavior would be to set the creator to the real section creator but so far i don't know how to get that info.
                    page.setTemplate(newSite.getTheme().templateForType("category"));
                    ListCategoryPosts pageCategory = new ListCategoryPosts(category);
                    page.addComponents(pageCategory);
                } else {
                    //exceptional

//                    if (!page.getMenuItemsSet().isEmpty() && duplicateExceptionalPages) {
//                        Page copy = new Page();
//                        //TODO copy page
//                        page = copy;
//                    }

                    page.setSite(newSite); //FIXME need this before setName or NPE occurs

                    //attempt to preserve name, slug and createdBy for exceptional page cases
                    if (page.getName() == null) {
                        String slug = page.getSlug();
                        page.setName(localized(section.getName()));
                        if (slug != null && !slug.isEmpty()) { //preserve set slug
                            page.setSlug(slug);
                        }
                    }
                    //FIXME hmmmm this isn't very right... what if the migration is ran by the proper creator of the page that is different from the site creator?
                    //maybe it's safer to presume that if not null, then it is to be maintained?
                    //FIXME more proper behavior would be to set the creator to the real section creator but so far i don't know how to get that info.
                    if (page.getCreatedBy() == null
                            || (page.getCreatedBy() == Authenticate.getUser() && newSite.getCreatedBy() != Authenticate.getUser())) {
                        page.setCreatedBy(newSite.getCreatedBy());
                    }

                }

                //set category according to the page
                category.setCreatedBy(page.getCreatedBy());
                category.setCreationDate(page.getCreationDate());
                category.setName(page.getName());
                category.setSite(newSite);

                //override site, creationDate and published
                page.setSite(newSite);
                page.setCreationDate(section.getCreationDate());
                page.setPublished(Optional.ofNullable(section.getEnabled()).orElse(true) && section.isVisible()); //FIXME enabled is generally not used, remove?

                //translate any items the page has into posts:

                final User pageCreator = page.getCreatedBy();
                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    Post post = new Post();

                    post.setSite(newSite);
                    post.setName(localized(item.getName()));
                    post.setBody(localized(item.getBody()));
                    //FIXME could item.getGroup().getMembers(section.getCreationDate()).iterator().next() work in some cases? Should i use it with pages and sections?
                    post.setCreatedBy(pageCreator);
                    post.setCreationDate(section.getCreationDate() != null ? section.getCreationDate() : new DateTime()); //FIXME section.getCreationDate() may be null!
                    post.addCategories(category);
                    post.setActive(Optional.ofNullable(item.getEnabled()).orElse(true) && item.getVisible()); //FIXME enabled is generally not used, remove?
                });
                parent = MenuItem.create(menu, page, localized(section.getName()), parent);
            }
        } else {
            parent = MenuItem.create(menu, null, localized(section.getName()), parent);
        }
        final MenuItem newParent = parent;
        section.getOrderedSubSections().forEach(subsection -> migrateSection(subsection, menu, newParent));

        /* important section stuff to migrate
            section.getOrderedChildItems();
            section.getOrderedSubSections();            // check
            section.getName();                          // check
            section.getVisible();                       // check
            section.getCreationDate();                  // check
            // ????? //section.getEnabled();            // check - see fixme
            // later //section.getSortedFiles();
            // ????? //section.getModificationDate();
            // ????? //section.getGroup();
            // ????? //section.getPermittedGroup();
         */
    }

    private List<Section> selectSideMenuSections(net.sourceforge.fenixedu.domain.Site site) {
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

    private List<Section> selectTopMenuSections(net.sourceforge.fenixedu.domain.Site site) {
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

    private boolean isTopSection(Section section) {
        return section.getName().getAllContents().stream().anyMatch(matchesTop);
    }

    private boolean isSideSection(Section section) {
        return section.getName().getAllContents().stream().anyMatch(matchesSide);
    }

    private boolean isTemplatedSection(Section section) {
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

//TODO Check if the following methods can go somewhere else since they're not strictly related with migration
    protected LocalizedString localizedStr(String str) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(str)) {
            for (Locale locale : CoreConfiguration.supportedLocales()) {
                result = result.with(locale, str);
            }
        }
        return result;
    }

    protected LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }
}