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
public class UseCaseController {
    private static final Logger LOGGER = Logger.getLogger( TypeData.ClassName.class.getName() );
    private final String loggerMessage = "Unauthorized attempt to access: ";
    private final String PATH_BASE = "/project/{projectId}/feature/{featureId}/";
    private ProjectService projectService;
    private FeatureService featureService;
    private UseCaseService useCaseService;
    private DocumentService documentService;
    private CommonMethods commonMethods;
    private UserService userService;
    private TraceabilityService traceabilityService;
    private CommentService commentService;

    @Autowired
     public UseCaseController(ProjectService projectService, FeatureService featureService, UseCaseService useCaseService,
                             DocumentService documentService, CommonMethods commonMethods, UserService userService,
                             TraceabilityService traceabilityService, CommentService commentService) {
        this.projectService = projectService;
        this.featureService = featureService;
        this.useCaseService = useCaseService;
        this.documentService = documentService;
        this.commonMethods = commonMethods;
        this.userService = userService;
        this.traceabilityService = traceabilityService;
        this.commentService = commentService;
    }

    //TODO Create handlers

    /**
     * This rest controller receives a request to get an use cases list related with a feature
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param featureId {@link FeatureDom} id
     * @return model and view with use cases list
     */
    @RequestMapping(value = PATH_BASE+"usecases", method = RequestMethod.GET)
    public ModelAndView getUseCasesListView(@PathVariable int projectId, @PathVariable int featureId,
                                             ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            List<UseCaseDom> useCaseDomList = useCaseService.getUseCases(featureId);
            FeatureDom featureDom = featureService.getFeature(featureId);
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("useCasesDashboard");
            modelAndView.addObject("project", project);
            modelAndView.addObject("useCasesList", useCaseDomList);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("featureId", featureDom.getIdFeature());
            modelAndView.addObject("featureName", featureDom.getName());
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a list of use cases from project "
                +projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get an use cases list related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param modelAndView autogenerated model and view
     * @return model and view with user stories list
     */
    @RequestMapping(value ="/project/{projectId}/features/usecases" , method = RequestMethod.GET)
    public ModelAndView getUseCasesListView(@PathVariable int projectId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            List<UseCaseDom> useCaseDomList = useCaseService.getByProjectID(projectId);
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("useCasesDashboardProject");
            modelAndView.addObject("project", project);
            modelAndView.addObject("useCasesList", useCaseDomList);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to get a list of use cases from project "
                +projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get an useCase related with a project and a feature
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param featureId id of feature which is related with use case
     * @param useCaseId {@link UseCaseDom} UseCase id of useCase requested
     * @param modelAndView autogenerated model and view
     * @return model and view with user story
     */
    @RequestMapping(value = PATH_BASE+"usecase/{useCaseId}", method = RequestMethod.GET)
    public ModelAndView getUseCaseView(@PathVariable int projectId, @PathVariable int featureId,
                                         @PathVariable int useCaseId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            FeatureDom featureDom = featureService.getFeature(featureId);
            boolean isPm = commonMethods.isPM(user, principal.getName());
            boolean isStakeholder = commonMethods.isStakeholder(user, principal.getName(), project);
            List<Group_user> group = project.getGroup().getUsers();
            TraceDom traceability = traceabilityService.getTraceability(useCaseId);
            List<CommentDom> comments = commentService.getComments(useCaseId);
            CommentDom comment = new CommentDom();
            modelAndView.setViewName("useCase");
            modelAndView.addObject("useCase", useCaseService.getUseCase(useCaseId));
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("featureId",featureDom.getIdFeature());
            modelAndView.addObject("featureName", featureDom.getName());
            modelAndView.addObject("fileList", documentService.getFileList(projectId,useCaseId));
            modelAndView.addObject("user", principal.getName());
            modelAndView.addObject("group", group);
            modelAndView.addObject("isPM", isPm);
            modelAndView.addObject("traceability", traceability);
            modelAndView.addObject("traceObject", new TraceDom());
            modelAndView.addObject("reqTypes", project.getRequirementTypes());
            modelAndView.addObject("reqsNotTraced", traceabilityService.getNotTracedReqs(projectId,useCaseId));
            modelAndView.addObject("featureList", traceabilityService.getNotTracedFeatures(projectId, useCaseId));
            modelAndView.addObject("useCaseList", traceabilityService.getNotTracedUseCases(projectId,useCaseId));
            modelAndView.addObject("comments", comments);
            modelAndView.addObject("comment", comment);
            modelAndView.addObject("isStakeholder", isStakeholder);
            return modelAndView;
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to obtain a use case from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to create an use case related with a project and a feature
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the feature
     * @param featureId {@link EpicDom} feature id which will own the use case
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"usecases/create", method = RequestMethod.GET)
    public ModelAndView getCreateUseCaseView(@PathVariable int projectId, @PathVariable int featureId,
                                               ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            FeatureDom featureDom = featureService.getFeature(featureId);
            UseCaseDom useCaseDom = new UseCaseDom();
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("createUseCase");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("useCase", useCaseDom);
            modelAndView.addObject("featureId", featureDom.getIdFeature());
            modelAndView.addObject("featureName", featureDom.getName());
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
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to create a use case in project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of an use case creation.
     * Then calls {@link UseCaseService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link UseCaseDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param featureId {@link FeatureDom} id object
     * @param useCaseDom {@link UseCaseDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link UseCaseDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"usecase/create", method = RequestMethod.POST)
    public ModelAndView createUseCase(@PathVariable int projectId, @PathVariable int featureId, Principal principal,
                                   @ModelAttribute @Valid UseCaseDom useCaseDom, BindingResult result){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        useCaseDom.setAuthorId(user.getUserId());
        if (commonMethods.isAllowed(projectDomList, project)) {
            if (result.hasErrors()) {
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                FeatureDom featureDom = featureService.getFeature(featureId);
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("useCase");
                modelAndView.addObject("useCase", useCaseDom);
                modelAndView.addObject("project", project);
                modelAndView.addObject("featureId", featureDom.getIdFeature());
                modelAndView.addObject("featureName", featureDom.getName());
                modelAndView.addObject("projectList", projectDomList);
                modelAndView.addObject("user", principal.getName());
                modelAndView.addObject("group", group);
                modelAndView.addObject("isPM", isPm);
                return modelAndView;
            }
            UseCaseDom persistedUseCase = useCaseService.create(useCaseDom, featureId, projectId);
            String path = "/project/"+projectId+"/feature/"+featureId+"/usecase/"+persistedUseCase.getIdUseCase();
            return new ModelAndView("redirect:"+path);
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to create a use case in project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This rest controller receives a request to get a page to update a use case related with a project and a feature
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the feature
     * @param featureId {@link FeatureDom} feature Id which owns the use case
     * @param useCaseId {@link UseCaseDom} id of useCase to be updated
     * @param modelAndView autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"usecase/update/{useCaseId}", method = RequestMethod.GET)
    public ModelAndView getUpdateUseCaseView(@PathVariable int projectId,@PathVariable int featureId,
                                          @PathVariable int useCaseId, ModelAndView modelAndView, Principal principal){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            FeatureDom featureDom = featureService.getFeature(featureId);
            UseCaseDom useCaseDom = useCaseService.getUseCase(useCaseId);
            boolean isPm = commonMethods.isPM(user, principal.getName());
            List<Group_user> group = project.getGroup().getUsers();
            modelAndView.setViewName("updateUseCase");
            modelAndView.addObject("project", project);
            modelAndView.addObject("projectList", projectDomList);
            modelAndView.addObject("useCase", useCaseDom);
            modelAndView.addObject("featureId", featureDom.getIdFeature());
            modelAndView.addObject("featureName", featureDom.getName());
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
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update a use case from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * This method gets the request of an useCase update.
     * Then calls {@link UseCaseService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link UseCaseDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param featureId {@link FeatureDom} feature Id
     * @param useCaseId {@link UseCaseDom} use case Id to be updated
     * @param useCaseDom {@link UseCaseDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link UseCaseDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"usecase/{useCaseId}", method = RequestMethod.POST)
    public ModelAndView updateUseCase(@PathVariable int projectId, @PathVariable int featureId, Principal principal,
                                        @PathVariable int useCaseId, @ModelAttribute @Valid UseCaseDom useCaseDom,
                                      BindingResult result){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            if (result.hasErrors()) {
                FeatureDom feature = featureService.getFeature(featureId);
                boolean isPm = commonMethods.isPM(user, principal.getName());
                List<Group_user> group = project.getGroup().getUsers();
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("useCase");
                modelAndView.addObject("useCase", useCaseDom);
                modelAndView.addObject("project", project);
                modelAndView.addObject("projectList", projectDomList);
                modelAndView.addObject("featureId", feature.getIdFeature());
                modelAndView.addObject("featureName", feature.getName());
                modelAndView.addObject("user", principal.getName());
                modelAndView.addObject("group", group);
                modelAndView.addObject("isPM", isPm);
                return modelAndView;
            }
            UseCaseDom persistedUseCase = useCaseService.update(projectId,featureId,useCaseId,useCaseDom);
            String path = "/project/"+projectId+"/feature/"+featureId+"/usecase/"+persistedUseCase.getIdUseCase();
            return new ModelAndView("redirect:"+path);
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to update a use case from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }

    /**
     * Delete epic receives an use case id as path variable and uses it to call {@link UseCaseService} delete method.
     * this method is called via JavaScript so returns a HttpStatus ok if deletion has been done and a HttpStatus
     * Internal Server Error if not.
     * @param projectId project id
     * @param featureId feature id
     * @param useCaseId useCase id to be deleted
     * @return HttpStatus.ok if correct. HttpStatus.INTERNAL_SERVER_ERROR if not correct.
     */
    @RequestMapping(value = PATH_BASE+"usecase/{useCaseId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteUseCase(@PathVariable int projectId, @PathVariable int featureId,Principal principal,
                                     @PathVariable int useCaseId){
        ProjectDom project = projectService.getProject(projectId);
        UserDom user = userService.findUser(principal.getName());
        List<ProjectDom> projectDomList = commonMethods.getProjectsFromGroup(user);
        if (commonMethods.isAllowed(projectDomList, project)) {
            boolean deleted = useCaseService.deleteUseCase(useCaseId);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.OK).body("");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
            }
        }
        LOGGER.log(Level.INFO, loggerMessage+"User "+principal.getName()+" has tried to delete a use case from project "+projectId);
        throw new AccessDeniedException("Not allowed");
    }
}
