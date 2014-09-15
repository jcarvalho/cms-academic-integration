package org.fenixedu.cms.domain.unit;

import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.I18N;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentType(name = "departmentPublications", description = "Publications information for a Department")
public class UnitPublicationsComponent extends UnitSiteComponent {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext global) {
        global.put("researchers", researchersIds(unit(page)));
        global.put("sotisUrl", "https://sotis.tecnico.ulisboa.pt"); //FIXME get real configuration property when available
        global.put("language", I18N.getLocale().toLanguageTag());
    }

    private String researchersIds(Unit unit) {
        Set<Unit> units = Sets.newHashSet(unit);
        units.addAll(unit.getAllSubUnits());
        return units.stream().flatMap(u -> parties(u)).map(Person::getNickname).collect(Collectors.joining(","));
    }

    private Stream<Person> parties(Unit unit) {
        return ((Collection<Person>) unit.getChildParties(Person.class)).stream();
    }

}