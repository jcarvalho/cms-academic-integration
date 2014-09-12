package org.fenixedu.cms.domain.homepage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.contacts.PartyContact;
import net.sourceforge.fenixedu.domain.contacts.PartyContactType;
import net.sourceforge.fenixedu.domain.homepage.Homepage;
import net.sourceforge.fenixedu.domain.person.RoleType;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

@ComponentType(name = "Presentation Component", description = "Provides homepage owner's presentation data.")
public class HomepagePresentationComponent implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext local, TemplateContext global) {
        Homepage homepage = homepage(page.getSite());
        if (homepage == null || !homepage.isHomepageActivated()) {
            return; //TODO we might want 404 here
        }

        global.put("homepage", homepage);
        Person owner = homepage.getPerson();
        global.put("owner", owner);

        if (homepage.getShowCurrentAttendingExecutionCourses()) {
            SortedSet<Attends> attendedCoursesByName = new TreeSet<Attends>(Attends.ATTENDS_COMPARATOR_BY_EXECUTION_COURSE_NAME);
            attendedCoursesByName.addAll(homepage.getPerson().getCurrentAttends());
            global.put("attendingCourses", attendedCoursesByName);
        }

        List<? extends PartyContact> emails = owner.getEmailAddresses();
        global.put("visibleEmails", getSortedFilteredContacts(emails));

        List<? extends PartyContact> phones = owner.getPhones();
        global.put("visiblePersonalPhones", getSortedFilteredContacts(phones, PartyContactType.PERSONAL));
        global.put("visibleWorkPhones", getSortedFilteredContacts(phones, PartyContactType.WORK));

        List<? extends PartyContact> mobilePhones = owner.getMobilePhones();
        global.put("visibleMobilePhones", getSortedFilteredContacts(mobilePhones));

        List<? extends PartyContact> websites = owner.getWebAddresses();
        global.put("visibleWebsites", getSortedFilteredContacts(websites));
    }

    private Homepage homepage(Site site) {
        return (site instanceof HomepageSite) ? ((HomepageSite) site).getHomepage() : null;
    }

    private boolean isVisible(PartyContact contact) {
        boolean publicSpace = true; //because this is a homepage. When this logic is exported to a more proper place remember to pass this as an argument.
        if (!Authenticate.isLogged() && publicSpace && contact.getVisibleToPublic().booleanValue()) {
            return true;
        }
        if (Authenticate.isLogged()) {
            User user = Authenticate.getUser();
            Person reader = user.getPerson();
            if (reader.hasRole(RoleType.CONTACT_ADMIN).booleanValue() || reader.hasRole(RoleType.MANAGER).booleanValue()
                    || reader.hasRole(RoleType.DIRECTIVE_COUNCIL).booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.EMPLOYEE).booleanValue() && contact.getVisibleToEmployees().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.TEACHER).booleanValue() && contact.getVisibleToTeachers().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.STUDENT).booleanValue() && contact.getVisibleToStudents().booleanValue()) {
                return true;
            }
            if (reader.hasRole(RoleType.ALUMNI).booleanValue() && contact.getVisibleToAlumni().booleanValue()) {
                return true;
            }
            if (contact.getVisibleToPublic()) {
                return true;
            }
        }
        return false;
    }

    protected List<PartyContact> getSortedFilteredContacts(Collection<? extends PartyContact> unfiltered,
            PartyContactType... types) {
        List<PartyContactType> typeList;
        if (types.length == 0) {
            typeList = Arrays.asList(PartyContactType.values());
        } else {
            typeList = Arrays.asList(types);
        }

        List<PartyContact> contacts = new ArrayList<PartyContact>();
        for (PartyContact contact : unfiltered) {
            if (isVisible(contact) && typeList.contains(contact.getType())) {
                contacts.add(contact);
            }
        }

        Collections.sort(contacts, new Comparator<PartyContact>() {
            @Override
            public int compare(PartyContact contact1, PartyContact contact2) {
                if (contact1.getType().ordinal() > contact2.getType().ordinal()) {
                    return -1;
                } else if (contact1.getType().ordinal() < contact2.getType().ordinal()) {
                    return 1;
                } else if (contact1.getDefaultContact().booleanValue()) {
                    return -1;
                } else if (contact2.getDefaultContact().booleanValue()) {
                    return 1;
                } else {
                    return contact1.getPresentationValue().compareTo(contact2.getPresentationValue());
                }
            }
        });
        return contacts;
    }

}
