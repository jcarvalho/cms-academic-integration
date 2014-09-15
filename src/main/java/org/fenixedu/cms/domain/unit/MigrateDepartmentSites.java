package org.fenixedu.cms.domain.unit;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fenixedu.cms.domain.CMSFolder;
import org.fenixedu.cms.domain.Category;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.Component;
import org.fenixedu.cms.domain.component.ListCategoryPosts;
import org.fenixedu.cms.domain.component.ViewPost;
import org.fenixedu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.commons.i18n.LocalizedString;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.fenixedu.cms.domain.component.StrategyBasedComponent.forType;
import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.MigrationUtil.*;

public class MigrateDepartmentSites extends CustomTask {
    private static final Map<String, MigrationUtil.PageTemplate> migrationTemplates = new HashMap<String, MigrationUtil.PageTemplate>();
    private static final LocalizedString TITLE_HOMEPAGE = getLocalizedString(BUNDLE, "researchUnit.homepage");
    private static final String PATH_HOMEPAGE = "/publico/department/departmentSite.do?method=presentation";

    private static final LocalizedString TITLE_EVENTS = getLocalizedString(BUNDLE, "label.events");
    private static final String PATH_EVENTS = "/publico/department/events.do?method=viewAnnouncements";

    private static final LocalizedString TITLE_ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcements");
    private static final String PATH_ANNOUNCEMENTS = "/publico/department/announcements.do?method=viewAnnouncements";

    private static final LocalizedString TITLE_ORGANIZATION = getLocalizedString(BUNDLE, "researchUnit.organization");
    private static final String PATH_ORGANIZATION = "/publico/department/departmentSite.do?method=organization";

    private static final LocalizedString TITLE_SUBUNITS = getLocalizedString(BUNDLE, "researchUnit.subunits");
    private static final String PATH_SUBUNITS = "/publico/department/departmentSite.do?method=subunits";

    public static final LocalizedString TITLE_VIEW_POST= getLocalizedString(BUNDLE, "label.viewPost");

    public static final LocalizedString TITLE_THESES = getLocalizedString(BUNDLE, "department.theses");
    private static final String PATH_THESES = "/publico/department/theses.do?method=showTheses";

    public static final LocalizedString TITLE_THESIS = getLocalizedString(BUNDLE, "department.thesis");

    public static final LocalizedString TITLE_PUBLICATIONS = getLocalizedString(BUNDLE, "department.publications");
    private static final String PATH_PUBLICATIONS = "/publico/department/departmentSiteResearch.do?method=showPublications";

    public static final LocalizedString TITLE_DEGREES = getLocalizedString(BUNDLE, "department.degrees");
    private static final String PATH_DEGREES = "/publico/department/degrees.do";

    public static final LocalizedString TITLE_COURES = getLocalizedString(BUNDLE, "department.courses");
    private static final String PATH_COURSES = "/publico/department/showDepartmentCompetenceCourses.faces";

    public static final LocalizedString TITLE_COURSE = getLocalizedString(BUNDLE, "department.course");

    public static final LocalizedString TITLE_EMPLOYEES = getLocalizedString(BUNDLE, "department.staff");
    private static final String PATH_EMPLOYEES = "/publico/department/departmentEmployees.do";

    public static final LocalizedString TITLE_TEACHERS = getLocalizedString(BUNDLE, "department.faculty");
    private static final String PATH_TEACHERS = "/publico/department/teachers.do";
    private static final LocalizedString TITLE_DEPARTMENT = getLocalizedString(BUNDLE, "department");

    @Override public void runTask() throws Exception {
        deleteSiteClass(UnitSite.class);
        sitesForClass(net.sourceforge.fenixedu.domain.DepartmentSite.class).forEach(oldSite -> create(oldSite));
    }

    private void create(net.sourceforge.fenixedu.domain.DepartmentSite oldSite) {
        getLogger().info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
        UnitSite newSite = new UnitSite(oldSite.getUnit());
        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setSlug(createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(THEME);
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));
        Page.create(newSite, null, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", Authenticate.getUser(),
                forType(ViewPost.class));

        newSite.setFolder(departmentFolder());
        migrateSite(newSite, oldSite, getMigrationTemplates(newSite));
        addPages(newSite, getAdditionalTemplates(newSite));

        migrateAnnouncements(newSite, oldSite.getUnit().getBoardsSet().iterator());
    }

    private String createSlug(net.sourceforge.fenixedu.domain.DepartmentSite oldSite) {
        return oldSite.getReversePath().replaceFirst("departamentos", "").replace("/", "");
    }

    private CMSFolder departmentFolder() {
        Supplier<CMSFolder> c = () -> new CMSFolder(PortalConfiguration.getInstance().getMenu(), "department", TITLE_DEPARTMENT);
        Stream<CMSFolder> folders = Bennu.getInstance().getCmsFolderSet().stream();
        return folders.filter(folder -> folder.getFunctionality().getPath().equals("department")).findFirst().orElseGet(c);
    }

    private Collection<MigrationUtil.PageTemplate> getAdditionalTemplates(UnitSite newSite) {
        List<PageTemplate> templates = Lists.newArrayList();
        templates.add(new PageTemplate(TITLE_VIEW_POST, null, "view", false, forType(ViewPost.class)));
        templates.add(new PageTemplate(TITLE_THESIS, null, "dissertation", false, forType(ThesisComponent.class)));
        templates.add(new PageTemplate(TITLE_COURSE, null, "competenceCourse", false, forType(CompetenceCourseComponent.class)));
        return templates;
    }

    private Map<String, MigrationUtil.PageTemplate> getMigrationTemplates(UnitSite site) {
        //TODO - pages names (resources) should refeer to unit or department instead of researchUnit
        Map<String, MigrationUtil.PageTemplate> templates = Maps.newHashMap();

        Category eventsCategory = site.categoryForSlug(EVENTS_SLUG, EVENTS_TITLE);
        Category announcementsCategory = site.categoryForSlug(ANNOUNCEMENTS_SLUG, ANNOUNCEMENTS_TITLE);

        Component eventsComponent = new ListCategoryPosts(eventsCategory);
        Component announcementsComponent = new ListCategoryPosts(announcementsCategory);

        templates.put(PATH_HOMEPAGE, new PageTemplate(TITLE_HOMEPAGE, null, "unitHomepage", forType(HomeComponent.class)));
        templates.put(PATH_EVENTS, new PageTemplate(TITLE_EVENTS, null, "category", eventsComponent));
        templates.put(PATH_ANNOUNCEMENTS, new PageTemplate(TITLE_ANNOUNCEMENTS, null, "category", announcementsComponent));
        templates.put(PATH_THESES, new PageTemplate(TITLE_THESES, null, "dissertations", forType(DepartmentDissertations.class)));
        templates.put(PATH_TEACHERS, new PageTemplate(TITLE_TEACHERS, null, "departmentFaculty", forType(UnitTeachersComponent.class)));
        templates.put(PATH_SUBUNITS, new PageTemplate(TITLE_SUBUNITS, null, "subunits", forType(SubUnits.class)));
        templates.put(PATH_ORGANIZATION, new PageTemplate(TITLE_ORGANIZATION, null, "unitOrganization", forType(Organization.class)));
        templates.put(PATH_EMPLOYEES, new PageTemplate(TITLE_EMPLOYEES, null, "employeesByArea", forType(EmployeesComponent.class)));
        templates.put(PATH_DEGREES, new PageTemplate(TITLE_DEGREES, null, "departmentDegrees", forType(DepartmentDegrees.class)));
        templates.put(PATH_COURSES, new PageTemplate(TITLE_COURES, null, "departmentCourses", forType(DepartmentCourses.class)));
        templates.put(PATH_PUBLICATIONS, new PageTemplate(TITLE_PUBLICATIONS, null, "researcherSection", forType(UnitReserachersComponent.class)));

        return templates;
    }

}
