package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.messaging.Announcement;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuComponent;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.Site;
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
    Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString("resources.FenixEduCMSResources",
            "label.announcement");

    @Override
    public void runTask() throws Exception {
        DateTime start = new DateTime();
        deleteAllSites();
        Set<ExecutionCourse> executionCourses = Bennu.getInstance().getExecutionCoursesSet();
        List<net.sourceforge.fenixedu.domain.ExecutionCourseSite> sites =
                executionCourses.stream().map(e -> e.getSite()).filter(s -> s != null).distinct().collect(Collectors.toList());
        int numSites = 1;
        for (net.sourceforge.fenixedu.domain.ExecutionCourseSite site : sites) {
            log.info("{ number: " + numSites++ + " of " + sites.size() + ", oldPath: " + site.getReversePath() + " }");
            createExecutionCourseSite(site);
        }
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

    private void deleteAllSites() {
        for(Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

    private void createExecutionCourseSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {

        ExecutionCourse executionCourse = oldSite.getExecutionCourse();
        ExecutionCourseSite newSite = ExecutionCourseListener.create(executionCourse);
        Menu menu = newSite.getMenusSet().stream().findFirst().orElse(null);

        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setStyle(oldSite.getStyle());
        newSite.setSlug(Site.slugify(oldSite.getReversePath().replace('/', '-')));

        dataMigration(newSite, menu);

        createStaticPages(newSite, menu, null, oldSite.getOrderedSections());
    }

    private void createStaticPages(Site site, Menu menu, MenuItem menuItemParent, Collection<Section> sections) {
        sections.stream().filter(section -> !(section instanceof TemplatedSection)).map(section -> section).forEach(section -> {
            Page page = createStaticPage(site, menu, section);
            MenuItem menuItem = null;
            if (page != null) {
                menuItem = MenuItem.create(site, menu, page, localized(section.getName()), menuItemParent);
                menuItem.setPosition(section.getOrder());
            }
            if (!section.getChildrenSections().isEmpty()) {
                createStaticPages(site, menu, menuItem, section.getChildrenSections());
            }
        });
    }

    private void dataMigration(ExecutionCourseSite site, Menu menu) {
        migrateSummaries(site, menu);
        migrateAnnouncements(site, menu);
    }

    private Page createStaticPage(Site site, Menu menu, Section section) {
        Page page = new Page();
        page.setCreationDate(site.getCreationDate());
        page.setName(localized(section.getName()));
        page.setPublished(section.getEnabled());
        page.setSite(site);
        page.setTemplate(site.getTheme().templateForType("category"));
        Category category = new Category();
        category.setName(page.getName());
        page.addComponents(new ListCategoryPosts(category));

        Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
        Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();
        section.getChildrenItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
            Post.create(site, page, localized(item.getName()), localized(item.getBody()), category);
        });

        MenuComponent.create(menu, page);

        return page;
    }


    private void migrateAnnouncements(ExecutionCourseSite site, Menu menu) {
        for(Announcement announcement : site.getExecutionCourse().getBoard().getAnnouncementSet()) {
            boolean hasSubject =announcement.getSubject() != null && !announcement.getSubject().isEmpty();
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
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
    }

    private static LocalizedString localizedStr(String str) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(str)) {
            for (Locale locale : CoreConfiguration.supportedLocales()) {
                result = result.with(locale, str);
            }
        }
        return result;
    }

    private static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }
    

}