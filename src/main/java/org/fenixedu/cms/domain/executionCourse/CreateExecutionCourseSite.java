package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.cms.CmsContent;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.messaging.Announcement;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Component;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.StaticPost;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Strings;

public class CreateExecutionCourseSite extends CustomTask {
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    private static final LocalizedString SIDE_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.sideMenu");
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString("resources.FenixEduCMSResources",
            "label.announcement");
    private static Integer numSites = 1;
    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();

    @Override
    public void runTask() throws Exception {
        DateTime start = new DateTime();
        deleteAllSites();
        Set<ExecutionCourse> executionCourses = Bennu.getInstance().getExecutionCoursesSet();
        executionCourses.stream().map(e -> e.getSite()).filter(Objects::nonNull).forEach(site -> {
            log.info("{ number: " + numSites++ + ", oldPath: " + site.getReversePath() + " }");
            createExecutionCourseSite(site);
        });

        DateTime end = new DateTime();

        log.info("[ duration: " + Hours.hoursBetween(start, end) + "hours, " + Minutes.minutesBetween(start, end) + "minutes, "
                + Seconds.secondsBetween(start, end) + " ]");
        /*
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612946319"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612917134"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612898443"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612875684"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612846760"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612818202"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612802249"));
        */

    }

    private net.sourceforge.fenixedu.domain.ExecutionCourseSite oldExecutionCourseSiteByExecutionCourse(String externalId) {
        ExecutionCourse e = FenixFramework.getDomainObject(externalId);
        return e.getSite();
    }

    public static void deleteAllSites() {
        log.info("removing all sites..");
        for (Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

    public void createExecutionCourseSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {

        ExecutionCourse executionCourse = oldSite.getExecutionCourse();
        ExecutionCourseSite newSite = ExecutionCourseListener.create(executionCourse);

        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setStyle(oldSite.getStyle());

        dataMigration(newSite, sideMenu(newSite));

        createStaticPages(newSite, null, oldSite);

        log.info("[ created at " + newSite.getSlug() + " ]");

    }

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
        return sections.stream().filter(s -> !equalContent(TOP_MENU, s.getName().toLocalizedString()))
                .collect(Collectors.toList());
    }

    private static List<Section> topMenuSections(List<Section> sections) {
        return sections.stream().filter(s -> equalContent(TOP_MENU, s.getName().toLocalizedString()))
                .collect(Collectors.toList());
    }

    private static boolean equalContent(LocalizedString str1, LocalizedString str2) {
        return str1.getContent().equalsIgnoreCase(str2.getContent());
    }

    private static boolean isIgnoredSection(CmsContent cmsContent) {
        LocalizedString sectionName = cmsContent.getName().toLocalizedString();
        return equalContent(TOP_MENU, sectionName) || equalContent(SIDE_MENU, sectionName);
    }

    public static Menu sideMenu(Site site) {
        return site.getMenusSet().stream().filter(m -> equalContent(m.getName(), ExecutionCourseListener.MENU)).findFirst()
                .orElseGet(() -> new Menu(site, ExecutionCourseListener.MENU));
    }

    public static Menu topMenu(Site site) {
        return site.getMenusSet().stream().filter(m -> equalContent(m.getName(), TOP_MENU)).findFirst()
                .orElseGet(() -> new Menu(site, TOP_MENU));
    }

    private void dataMigration(ExecutionCourseSite site, Menu menu) {
        migrateSummaries(site, menu);
        migrateAnnouncements(site, menu);
    }

    public static Page createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Item item) {
        log.info("migrating item " + item.getName().getContent());

        LocalizedString name = localized(item.getName());
        final Page page = Page.create(site, menu, menuItemParent, name, item.getEnabled(), "view", new Component[] {});
        page.setCreationDate(site.getCreationDate());

        Boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
        Post post = Post.create(site, page, name, localized(item.getBody()), null, isEnabled);

        StaticPost component = new StaticPost();
        component.setPost(post);
        page.addComponents(component);

        return page;
    }

    public static Page createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Section section) {
        if (section instanceof TemplatedSection || isIgnoredSection(section)) {
            section.getOrderedSubSections().forEach(subsection -> createStaticPage(site, menu, menuItemParent, subsection));
            section.getOrderedChildItems().forEach(item -> createStaticPage(site, menu, menuItemParent, item));
            return null;
        }
        LocalizedString name = localized(section.getName());

        log.info("migrating section " + name.getContent());

        Category category = new Category();
        category.setName(name);
        ListCategoryPosts pageCategory = new ListCategoryPosts(category);

        Boolean isPublished = Optional.ofNullable(section.getEnabled()).orElse(true);
        final Page page = Page.create(site, menu, menuItemParent, name, isPublished, "category", pageCategory);
        page.setCreationDate(site.getCreationDate());

        section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
            Boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
            Post.create(site, page, localized(item.getName()), localized(item.getBody()), category, isEnabled);
        });

        section.getOrderedSubSections().forEach(s -> createStaticPage(site, menu, menuItemParent, s));

        return page;
    }

    private void migrateAnnouncements(ExecutionCourseSite site, Menu menu) {
        log.info("migrating announcements for site " + site.getSlug());
        for (Announcement announcement : site.getExecutionCourse().getBoard().getAnnouncementSet()) {
            boolean hasSubject = announcement.getSubject() != null && !announcement.getSubject().isEmpty();
            boolean hasBody = announcement.getBody() != null && !announcement.getBody().isEmpty();
            if (hasSubject && hasBody) {
                Post post = new Post();
                post.setSite(site);

                post.setCreatedBy(announcement.getCreator() != null ? announcement.getCreator().getUser() : null);
                post.setCreationDate(announcement.getCreationDate());
                post.setBody(localized(announcement.getBody()));
                post.setName(localized(announcement.getSubject()));
                post.setActive(announcement.getVisible());
                post.setLocation(localizedStr(announcement.getPlace()));
                post.setPublicationBegin(announcement.getPublicationBegin());
                post.setPublicationEnd(announcement.getPublicationEnd());

                post.addCategories(site.categoryForSlug("announcement", ANNOUNCEMENTS));

                announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                        .map(name -> site.categoryForSlug(name.getContent(), name))
                        .forEach(category -> post.addCategories(category));

                Space campus = announcement.getCampus();
                if (campus != null) {
                    String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                    if (!Strings.isNullOrEmpty(campusName)) {
                        post.addCategories(site.categoryForSlug("campus-" + campus.getExternalId(), localizedStr(campusName)));
                    }
                }
            }
        }
    }

    private void migrateSummaries(ExecutionCourseSite site, Menu menu) {
        log.info("migrating summaries for site " + site.getSlug());
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
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