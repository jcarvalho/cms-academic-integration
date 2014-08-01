package org.fenixedu.cms.domain;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.CmsContent;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseListener;
import org.fenixedu.commons.i18n.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MigrationUtils {

    private static final Logger log = LoggerFactory.getLogger(MigrationUtils.class);

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    private static final LocalizedString SIDE_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.sideMenu");
    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();

    public static void createStaticPages(Site newSite, MenuItem menuItemParent, net.sourceforge.fenixedu.domain.Site oldSite) {
        log.info("creating static pages for site " + newSite.getSlug());

        List<Section> topMenuSections = topMenuSections(oldSite.getOrderedSections());
        if (!topMenuSections.isEmpty()) {
            topMenuSections.forEach(s -> createStaticPage(newSite, topMenu(newSite), menuItemParent, s));
        }

        List<Section> sideMenuSections = sideMenuSections(oldSite.getOrderedSections());
        if (!sideMenuSections.isEmpty()) {
            sideMenuSections.forEach(s -> createStaticPage(newSite, sideMenu(newSite), menuItemParent, s));
        }
    }

    private static List<Section> sideMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            boolean isTopMenu = equalContent(TOP_MENU, section.getName().toLocalizedString());
            boolean isSideMenu = equalContent(SIDE_MENU, section.getName().toLocalizedString());
            if (isSideMenu) {
                sideMenuSections.addAll(section.getChildrenSections());
            } else if (!isTopMenu) {
                sideMenuSections.add(section);
            }
        }
        return sideMenuSections;
    }

    private static List<Section> topMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (equalContent(TOP_MENU, section.getName().toLocalizedString())) {
                sideMenuSections.addAll(section.getChildrenSections());
            }
        }
        return sideMenuSections;
    }

    private static boolean equalContent(LocalizedString str1, LocalizedString str2) {
        return str1.getContent().equalsIgnoreCase(str2.getContent());
    }

    private static boolean isIgnoredSection(CmsContent cmsContent) {
        LocalizedString sectionName = cmsContent.getName().toLocalizedString();
        return cmsContent instanceof TemplatedSection || equalContent(TOP_MENU, sectionName)
                || equalContent(SIDE_MENU, sectionName);
    }

    public static Menu sideMenu(Site site) {
        return site.getMenusSet().stream().filter(m -> equalContent(m.getName(), ExecutionCourseListener.MENU)).findFirst()
                .orElseGet(() -> new Menu(site, ExecutionCourseListener.MENU));
    }

    public static Menu topMenu(Site site) {
        return site.getMenusSet().stream().filter(m -> equalContent(m.getName(), TOP_MENU)).findFirst()
                .orElseGet(() -> new Menu(site, TOP_MENU));
    }

    public static void createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Section section) {
        List<Section> subsections = section.getOrderedSubSections();
        LocalizedString name = localized(section.getName());
        log.info("migrating section " + name.getContent());
        if (!isIgnoredSection(section)) {
            //it means that the folder has no content and just acts like a folder on the menu
            boolean isFolderSection = section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
            if (isFolderSection) {
                MenuItem parent = MenuItem.create(site, menu, null, name, menuItemParent);
                subsections.forEach(subsection -> createStaticPage(site, menu, parent, subsection));
                return;
            } else {
                Category category = new Category();
                category.setName(name);
                ListCategoryPosts pageCategory = new ListCategoryPosts(category);

                boolean isPublished = Optional.ofNullable(section.getEnabled()).orElse(true);
                final Page page = Page.create(site, menu, menuItemParent, name, isPublished, "category", pageCategory);
                page.setCreationDate(site.getCreationDate());

                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
                    Post.create(site, page, localized(item.getName()), localized(item.getBody()), category, isEnabled);
                });
            }
        }
        subsections.forEach(subsection -> createStaticPage(site, menu, menuItemParent, subsection));
    }

    public static void deleteAllSites() {
        log.info("removing all sites..");
        for (Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

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
