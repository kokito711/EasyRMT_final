/*
 * Copyright (c) $today.year.Sergio López Jiménez and Universidad de Valladolid
 *                             All rights reserved
 */

package com.Sergio.EasyRMT.Controller;

import com.Sergio.EasyRMT.Domain.*;
import com.Sergio.EasyRMT.Model.Group_user;
import com.Sergio.EasyRMT.Model.types.*;
import com.Sergio.EasyRMT.Service.*;
import javassist.bytecode.stackmap.TypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class RequirementController {
    private static final Logger LOGGER = Logger.getLogger( TypeData.ClassName.class.getName() );
    private final String loggerMessage = "Unauthorized attempt to access: ";
    final String PATH_BASE = "/project/{projectId}/";
    ProjectService projectService;
    RequirementService requirementService;
    DocumentService documentService;
    CommonMethods commonMethods;
    UserService userService;
    TraceabilityService traceabilityService;
    CommentService commentService;

    @Autowired
    public RequirementController(ProjectService projectService, RequirementService requirementService, DocumentService documentService,
                                 CommonMethods commonMethods, UserService userService, TraceabilityService traceabilityService,
                                 CommentService commentService) {
        this.projectService = projectService;
        this.requirementService = requirementService;
        this.documentService = documentService;
        this.commonMethods = commonMethods;
        this.userService = userService;
        this.traceabilityService = traceabilityService;
        this.commentService = commentService;
    }

    /**
     * This rest controller receives a request to get a requirement list related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param modelAndView autogenerated model and view
     * @return model and view with feature list
     */
    @RequestMapping(value = PATH_BASE+"requirements", method = RequestMethod.GET)
    public ModelAndView getRequirementListView(@PathVariable int projectId, ModelAndView modelAndView,
                                               Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            List<RequirementDom> requirementDomList = requirementService.getRequirements(projectId);
            List<RequirementTypeDom> reqTypes = projectService.getReqTypes();
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("requirementsDashboard");
            modelAndView.addObject("project", project);
            modelAndView.addObject("requirementList", requirementDomList);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("reqTypes", reqTypes);
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a list of requirements" +
                " from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a requirement related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the requirement
     * @param requirementId {@link RequirementDom} id of requirement requested
     * @param modelAndView autogenerated model and view
     * @return model and view with requirement
     */
    @RequestMapping(value = PATH_BASE+"requirement/{requirementId}", method = RequestMethod.GET)
    public ModelAndView getRequirementView(@PathVariable int projectId, @PathVariable int requirementId, ModelAndView modelAndView,
                                           Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            TraceDom traceability = traceabilityService.getTraceability(requirementId);
            List<CommentDom> comments = commentService.getComments(requirementId);
            modelAndView.setViewName("requirement");
            modelAndView.addObject("requirement", requirementService.getRequirement(requirementId));
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("reqTypes", projectService.getReqTypes());
            modelAndView.addObject("fileList", documentService.getFileList(projectId, requirementId));
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            modelAndView.addObject("traceability", traceability);
            modelAndView.addObject("traceObject", new TraceDom());
            modelAndView.addObject("reqTypes", project.getRequirementTypes());
            modelAndView.addObject("reqsNotTraced", traceabilityService.getNotTracedReqs(projectId,requirementId));
            if(project.getType().equals(ProjectType.AGILE)){
                modelAndView.addObject("epicList", traceabilityService.getNotTracedEpics(projectId, requirementId));
                modelAndView.addObject("userStoryList", traceabilityService.getNotTracedUserStories(projectId,requirementId));
            }
            else {
                modelAndView.addObject("featureList", traceabilityService.getNotTracedFeatures(projectId, requirementId));
                modelAndView.addObject("useCaseList", traceabilityService.getNotTracedUseCases(projectId,requirementId));
            }
            modelAndView.addObject("comments", comments);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a list of requirements" +
                " from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to create a requirement related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the requirement
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"requirements/create", method = RequestMethod.GET)
    public ModelAndView getCreateRequirementView(@PathVariable int projectId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            RequirementDom requirementDom = new RequirementDom();
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("createRequirement");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("requirement", requirementDom);
            modelAndView.addObject("priority", Priority.values());
            modelAndView.addObject("state", State.values());
            modelAndView.addObject("risk", Risk.values());
            modelAndView.addObject("complexity", Complexity.values());
            modelAndView.addObject("scope", Scope.values());
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to create a requirement" +
                " in project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of a requirement creation.
     * Then calls {@link RequirementService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link RequirementDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param requirementDom {@link RequirementDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link RequirementDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"requirements", method = RequestMethod.POST)
    public ModelAndView createRequirement(@PathVariable int projectId, @ModelAttribute @Valid RequirementDom requirementDom,
                                          BindingResult result, Principal principal) {
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        requirementDom.setAuthorId(user.getUserId());
        if (commonMethods.isAllowed(projectDomList, project)) {
            if (result.hasErrors()) {
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("requirement");
                modelAndView.addObject("requirement", requirementDom);
                modelAndView.addObject("project", project);
                modelAndView.addObject("projectList", projectDomList);
                modelAndView.addObject("reqTypes", projectService.getReqTypes());
                modelAndView.addObject("user", principal.getName());
                modelAndView.addObject("group", group);
                modelAndView.addObject("isPM", isPm);
                return modelAndView;
            }
            RequirementDom persistedRequirement = requirementService.create(requirementDom, projectId);
            String path = "/project/" + projectId + "/requirement/" + persistedRequirement.getIdRequirement();
            return new ModelAndView("redirect:" + path);
        }
        LOGGER.log(Level.INFO, loggerMessage + "User " + principal.getName() + " has tried to create a requirement" +
                " in project " + projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to update a requirement related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the feature
     * @param requirementId {@link RequirementDom}  Id to be updated
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"requirement/update/{requirementId}", method = RequestMethod.GET)
    public ModelAndView getUpdateRequirementView(@PathVariable int projectId,@PathVariable int requirementId,
                                                 ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            RequirementDom requirementDom = requirementService.getRequirement(requirementId);
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("updateRequirement");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("requirement", requirementDom);
            modelAndView.addObject("priority", Priority.values());
            modelAndView.addObject("state", State.values());
            modelAndView.addObject("risk", Risk.values());
            modelAndView.addObject("complexity", Complexity.values());
            modelAndView.addObject("scope", Scope.values());
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update a requirement" +
                " from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of a requirement update.
     * Then calls {@link RequirementService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link RequirementDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param requirementId {@link RequirementDom} Id to be updated
     * @param requirementDom {@link RequirementDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link RequirementDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"requirement/{requirementId}/update", method = RequestMethod.POST)
    public ModelAndView updateRequirement(@PathVariable int projectId, @PathVariable int requirementId,Principal principal,
                                   @ModelAttribute @Valid RequirementDom requirementDom, BindingResult result){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            if (result.hasErrors()) {
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                ModelAndView mav = new ModelAndView();
                mav.setViewName("requirement");
                mav.addObject("requirement", requirementDom);
                mav.addObject("project", project);
                mav.addObject("projectList", projectDomList);
                mav.addObject("reqTypes", projectService.getReqTypes());
                mav.addObject("user", principal.getName());
                return mav;
            }
            RequirementDom persistedRequirement = requirementService.update(requirementDom, requirementId, projectId);
            String path = "/project/" + projectId + "/requirement/" + persistedRequirement.getIdRequirement();
            return new ModelAndView("redirect:" + path);
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update a requirement" +
                " from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * Delete requirement receives a requirement id as path variable and uses it to call {@link RequirementService}
     * delete method.
     * This method is called via JavaScript so returns a HttpStatus ok if deletion has been done and a HttpStatus
     * Internal Server Error if not.
     * @param projectId project id
     * @param requirementId requirement id to be deleted.
     * @return HttpStatus.ok if correct. HttpStatus.INTERNAL_SERVER_ERROR if not correct.
     */
    @RequestMapping(value = PATH_BASE+"/requirement/{requirementId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteRequirement(@PathVariable int projectId, @PathVariable int requirementId,
                                            Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean deleted = requirementService.deleteRequirement(requirementId);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.OK).body("");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
            }
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to delete a requirement " +
                "from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }
}
