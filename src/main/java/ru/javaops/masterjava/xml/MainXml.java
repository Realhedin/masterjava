package ru.javaops.masterjava.xml;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.util.JaxbParser;
import src.main.resources.Group;
import src.main.resources.Payload;
import src.main.resources.Project;
import src.main.resources.User;

/**
 * @author dkorolev
 * Date: 7/8/2019
 * Time: 5:51 PM
 */
public class MainXml  {

    public void extractParticipantsListFromProject(String projectName, String fileName, JaxbParser jaxbParser) {
        try {
            Payload payload = jaxbParser.unmarshal(
                    Resources.getResource(fileName).openStream());
            List<Project> projects = payload.getProjects().getProject();
            List<Group> groups = new ArrayList<>();
            //find appropriate group
            for (Project project : projects) {
                if (projectName.equals(project.getName())) {
                     groups = project.getGroup();
                    if (!groups.isEmpty()) {
                        break;
                    } else {
                        return;
                    }
                }
            }
            //use groups
            List<User> users = payload.getUsers().getUser();
            List<String> usersInDesiredGroup = new ArrayList<>(users.size());
            for (User user : users) {
                List<Group> userGroups = user.getGroup();
                for (Object userGroup : userGroups) {
                    if (groups.contains(userGroup)) {
                        usersInDesiredGroup.add(user.getFullName());
                    }
                }
            }
            //print ascending order
            usersInDesiredGroup.stream().sorted().forEach(System.out::println);

        } catch (JAXBException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}