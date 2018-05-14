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
public class EpicController {
    private static final Logger LOGGER = Logger.getLogger( TypeData.ClassName.class.getName() );
    private final String loggerMessage = "Unauthorized attempt to access: ";
    final String PATH_BASE = "/project/{projectId}/";
    ProjectService projectService;
    EpicService epicService;
    DocumentService documentService;
    CommonMethods commonMethods;
    UserService userService;
    TraceabilityService traceabilityService;
    CommentService commentService;

    @Autowired
    public EpicController(ProjectService projectService, EpicService epicService, DocumentService documentService,
                          CommonMethods commonMethods, UserService userService, TraceabilityService traceabilityService,
                          CommentService commentService) {
        this.projectService = projectService;
        this.epicService = epicService;
        this.documentService = documentService;
        this.commonMethods = commonMethods;
        this.userService = userService;
        this.traceabilityService = traceabilityService;
        this.commentService = commentService;
    }

    /**
     * This rest controller receives a request to get an epic list related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param modelAndView autogenerated model and view
     * @return model and view with epic list
     */
    @RequestMapping(value = PATH_BASE+"epics", method = RequestMethod.GET)
    public ModelAndView getEpicListView(@PathVariable int projectId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            List<EpicDom> epicDomList = epicService.getEpics(projectId);
            boolean isPm = commonMethods.isPM(user,principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("epicsDashboard");
            modelAndView.addObject("project", project);
            modelAndView.addObject("epicList", epicDomList);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a list of epics of project "
                +projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get an epic related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId {@link EpicDom} EpicId of epic requested
     * @param modelAndView autogenerated model and view
     * @return model and view with epic
     */
    @RequestMapping(value = PATH_BASE+"epic/{epicId}", method = RequestMethod.GET)
    public ModelAndView getEpicView(@PathVariable int projectId, @PathVariable int epicId, ModelAndView modelAndView,
                                    Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean isPm = commonMethods.isPM(user,principal.getName());
            boolean isStakeholder = commonMethods.isStakeholder(user, principal.getName(), project);
            List<Group_user> group = project.getGroup().getUsers();
            TraceDom traceability = traceabilityService.getTraceability(epicId);
            List<CommentDom> comments = commentService.getComments(epicId);
            CommentDom comment = new CommentDom();
            modelAndView.setViewName("epic");
            modelAndView.addObject("epic", epicService.getEpic(epicId));
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("fileList", documentService.getFileList(projectId, epicId));
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            modelAndView.addObject("traceability", traceability);
            modelAndView.addObject("traceObject", new TraceDom());
            modelAndView.addObject("reqTypes", project.getRequirementTypes());
            modelAndView.addObject("reqsNotTraced", traceabilityService.getNotTracedReqs(projectId,epicId));
            modelAndView.addObject("epicList", traceabilityService.getNotTracedEpics(projectId, epicId));
            modelAndView.addObject("userStoryList", traceabilityService.getNotTracedUserStories(projectId,epicId));
            modelAndView.addObject("comments", comments);
            modelAndView.addObject("comment", comment);
            modelAndView.addObject("isStakeholder", isStakeholder);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to obtain an epic from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to create an epic related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"epics/create", method = RequestMethod.GET)
    public ModelAndView getCreateEpicView(@PathVariable int projectId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean isPm = commonMethods.isPM(user,principal.getName());
            List<Group_user> group =project.getGroup().getUsers();
            EpicDom epicDom = new EpicDom();
            modelAndView.setViewName("createEpic");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("epic", epicDom);
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
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to create an epic in project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of an epic creation.
     * Then calls {@link EpicService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link EpicDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param epic {@link EpicDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link EpicDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"epics", method = RequestMethod.POST)
    public ModelAndView createEpic(@PathVariable int projectId, @ModelAttribute @Valid EpicDom epic, Principal principal,
                                   BindingResult result){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        epic.setAuthorId(user.getUserId());
        if (commonMethods.isAllowed(projectDomList, project)) {
            if(result.hasErrors()) {
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("createEpic");
                modelAndView.addObject("epic", epic);
                modelAndView.addObject("project", project);
                modelAndView.addObject("projectList", projectDomList);
                modelAndView.addObject("user", principal.getName());
                modelAndView.addObject("group", group);
                modelAndView.addObject("isPM", isPm);
                return modelAndView;
            }
            EpicDom persistedEpic = epicService.create(epic, projectId);
            String path = "/project/"+projectId+"/epic/"+persistedEpic.getIdEpic();
            return new ModelAndView("redirect:"+path);

        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to create an epic from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to update an epic related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId {@link EpicDom} epic Id to be updated
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"epic/update/{epicId}", method = RequestMethod.GET)
    public ModelAndView getUpdateEpicView(@PathVariable int projectId,@PathVariable int epicId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            EpicDom epicDom = epicService.getEpic(epicId);
            modelAndView.setViewName("updateEpic");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("epic", epicDom);
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
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update an epic from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of an epic update.
     * Then calls {@link EpicService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link EpicDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param epicId {@link EpicDom} epic Id to be updated
     * @param epic {@link EpicDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link EpicDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"epic/{epicId}/update", method = RequestMethod.POST)
    public ModelAndView updateEpic(@PathVariable int projectId, @PathVariable int epicId, @ModelAttribute @Valid EpicDom epic,
                                   Principal principal, BindingResult result){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            if (result.hasErrors()) {
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("updateEpic");
                modelAndView.addObject("epic", epic);
                modelAndView.addObject("project", project);
                modelAndView.addObject("projectList", projectDomList);
                modelAndView.addObject("user", principal.getName());
                modelAndView.addObject("group", group);
                modelAndView.addObject("isPM", isPm);
                return modelAndView;
            }
            EpicDom persistedEpic = epicService.update(epic, epicId, projectId);
            String path = "/project/"+projectId+"/epic/"+persistedEpic.getIdEpic();
            return new ModelAndView("redirect:"+path);
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update an epic from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * Delete epic receives an epic id as path variable and uses it to call {@link EpicService} delete method.
     * this method is called via JavaScript so returns a HttpStatus ok if deletion has been done and a HttpStatus
     * Internal Server Error if not.
     * @param projectId project id
     * @param epicId epic id to be deleted.
     * @return HttpStatus.ok if correct. HttpStatus.INTERNAL_SERVER_ERROR if not correct.
     */
    @RequestMapping(value = PATH_BASE+"/epic/{epicId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEpic(@PathVariable int projectId, @PathVariable int epicId, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean deleted = epicService.deleteEpic(epicId);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.OK).body("");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
            }
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to delete an epic from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }
}
