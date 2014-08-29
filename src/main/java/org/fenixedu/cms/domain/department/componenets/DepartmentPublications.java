package org.fenixedu.cms.domain.department.componenets;

import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;
import org.fenixedu.commons.i18n.I18N;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentType(name = "departmentPublications", description = "Publications information for a Department")
public class DepartmentPublications extends DepartmentPublications_Base {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext global) {
        global.put("researchers", researchersIds(((DepartmentSite) page.getSite()).getDepartment().getDepartmentUnit()));
        global.put("sotisUrl", "https://sotis.tecnico.ulisboa.pt"); //FIXME get real configuration property when available
        global.put("language", I18N.getLocale().toLanguageTag());
    }

    private String researchersIds(Unit departmentUnit) {
        Set<Unit> units = Sets.newHashSet(departmentUnit);
        units.addAll(departmentUnit.getAllSubUnits());
        return units.stream().flatMap(unit -> parties(unit)).map(Person::getNickname).collect(Collectors.joining(","));
    }

    private Stream<Person> parties(Unit unit) {
        return ((Collection<Person>) unit.getChildParties(Person.class)).stream();
    }

}