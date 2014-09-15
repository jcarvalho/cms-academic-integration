package org.fenixedu.cms.domain.unit;

import com.google.common.collect.Maps;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.Department;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.thesis.Thesis;
import net.sourceforge.fenixedu.domain.thesis.ThesisState;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static net.sourceforge.fenixedu.domain.thesis.ThesisState.*;

@ComponentType(name = "departmentTheses", description = "Theses information for a Department")
public class DepartmentDissertations extends UnitSiteComponent {

    public static final Map<ThesisState, String> states = Maps.newHashMap();

    {
        states.put(EVALUATED, "success");
        states.put(CONFIRMED, "primary");
        states.put(DRAFT, "default");
        states.put(APPROVED, "info");
        states.put(REVISION, "warning");
        states.put(SUBMITTED, "primary");
    }

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Department department = unit(page).getDepartment();
        globalContext.put("department", department);
        globalContext.put("unit", department.getDepartmentUnit());

        SortedMap<ExecutionYear, List<Thesis>> allThesesByYear = allThesesByYear(department);
        globalContext.put("thesesByYear", allThesesByYear);
        globalContext.put("years", allThesesByYear.keySet().stream().sorted(ExecutionYear.COMPARATOR_BY_YEAR));
        globalContext.put("degrees", department.getDegreesSet().stream().sorted(Degree.COMPARATOR_BY_DEGREE_TYPE_AND_NAME_AND_ID));
        globalContext.put("states", states);
    }

    private SortedMap<ExecutionYear, List<Thesis>> allThesesByYear(Department department) {
        TreeMap<ExecutionYear, List<Thesis>> thesesByYear = Maps.newTreeMap(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR);
        Stream<Thesis> allTheses = department.getDegreesSet().stream().flatMap(degree -> degree.getThesisSet().stream());
        thesesByYear.putAll(allTheses.collect(groupingBy(Thesis::getExecutionYear)));
        return thesesByYear;
    }

}