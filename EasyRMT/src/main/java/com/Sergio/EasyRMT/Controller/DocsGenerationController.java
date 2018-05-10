/*
 * Copyright (c) $today.year.Sergio López Jiménez and Universidad de Valladolid
 *                             All rights reserved
 */

package com.Sergio.EasyRMT.Controller;

import com.Sergio.EasyRMT.Domain.ProjectDom;
import com.Sergio.EasyRMT.Domain.TraceDom;
import com.Sergio.EasyRMT.Domain.UserDom;
import com.Sergio.EasyRMT.Model.Group_user;
import com.Sergio.EasyRMT.Model.types.ProjectType;
import com.Sergio.EasyRMT.Service.*;
import javassist.bytecode.stackmap.TypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class DocsGenerationController {

    private static final Logger LOGGER = Logger.getLogger( TypeData.ClassName.class.getName() );
    private final String loggerMessage = "Unauthorized attempt to access: ";
    final String PRINTER_PATH_BASE = "/print/{projectId}/";
    ProjectService projectService;
    FeatureService featureService;
    EpicService epicService;
    CommonMethods commonMethods;
    UserService userService;
    UseCaseService useCaseService;
    TraceabilityService traceabilityService;
    UserStoryService userStoryService;
    RequirementService requirementService;

    @Autowired
    public DocsGenerationController(ProjectService projectService, FeatureService featureService, EpicService epicService,
                                    CommonMethods commonMethods, UserService userService,
                                    UseCaseService useCaseService, TraceabilityService traceabilityService,
                                    UserStoryService userStoryService, RequirementService requirementService) {
        this.projectService = projectService;
        this.featureService = featureService;
        this.epicService = epicService;
        this.commonMethods = commonMethods;
        this.userService = userService;
        this.useCaseService = useCaseService;
        this.traceabilityService = traceabilityService;
        this.userStoryService = userStoryService;
        this.requirementService = requirementService;
    }

    /**
     * This method obtains a request to generate a page that will be printable
     * @param projectId id of project
     * @param type type of element
     * @param objectId id of object to be printed
     * @param principal element with login information
     * @return ModelAndView with printable page
     */
    @RequestMapping(value = PRINTER_PATH_BASE+"{type}/{objectId}", method = RequestMethod.GET)
    public ModelAndView getPrinterPage (@PathVariable int projectId,@PathVariable String type,
                                        @PathVariable int objectId,Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            ModelAndView modelAndView = new ModelAndView("printPage");
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            TraceDom extension = new TraceDom();
            TraceDom traceability= traceabilityService.getTraceability(objectId);
            populateModelAndView(type, objectId, modelAndView);
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            modelAndView.addObject("traceability", traceability);
            modelAndView.addObject("traceObject",extension );
            modelAndView.addObject("reqTypes", project.getRequirementTypes());
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a document to print from project "
                +projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method obtains a request to generate a page that will be printable
     * @param projectId id of project
     * @param type type of element
     * @param objectId id of object to be printed
     * @param principal element with login information
     * @return ModelAndView with printable page
     */
    @RequestMapping(value = PRINTER_PATH_BASE+"{type}/list/{objectId}", method = RequestMethod.GET)
    public ModelAndView getListPrinterPage (@PathVariable int projectId, @PathVariable String type,
                                            @PathVariable int objectId, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            ModelAndView modelAndView = new ModelAndView("printListPage");
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            populateListModelAndView(project, type, objectId, modelAndView);
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            modelAndView.addObject("type", type);
            modelAndView.addObject("reqTypes", project.getRequirementTypes());
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a document to print from project "
                +projectId);
        throw new AccessDeniedException("Not allowed");
    }

    private void populateListModelAndView(ProjectDom project, String type, int objectId, ModelAndView modelAndView) {
        switch (type){
            case "feature":
                if(objectId==0) {
                    modelAndView.addObject("object", featureService.getFeatures(project.getIdProject()));
                }
                else {
                    modelAndView.addObject("object", useCaseService.getUseCases(objectId));
                }
                break;
            case "epic":
                if(objectId==0) {
                    modelAndView.addObject("object", epicService.getEpics(objectId));
                }
                else {
                    modelAndView.addObject("object", userStoryService.getUserStory(objectId));
                }
                break;
            case "usecase":
                modelAndView.addObject("object",useCaseService.getByProjectID(project.getIdProject()));
                break;
            case "userstory":
                modelAndView.addObject("object",userStoryService.getByProjectID(project.getIdProject()));
                break;
            case "requirement":
                if(objectId==0) {
                    modelAndView.addObject("object", requirementService.getRequirements(project.getIdProject()));
                }
                else {
                    throw new AccessDeniedException("Not allowed");
                }
                break;
            default:
                throw new AccessDeniedException("Not allowed");
        }
    }


    private void populateModelAndView(String type, int objectId, ModelAndView modelAndView) {
        switch (type){
            case "feature":
                modelAndView.addObject("object",featureService.getFeature(objectId));
                break;
            case "epic":
                modelAndView.addObject("object",epicService.getEpic(objectId));
                break;
            case "usecase":
                modelAndView.addObject("object",useCaseService.getUseCase(objectId));
                break;
            case "userstory":
                modelAndView.addObject("object",userStoryService.getUserStory(objectId));
                break;
            case "requirement":
                modelAndView.addObject("object",requirementService.getRequirement(objectId));
            default:
                throw new AccessDeniedException("Not allowed");
        }
    }
}
