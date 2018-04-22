/*
 * Copyright (c) $today.year.Sergio López Jiménez and Universidad de Valladolid
 *                             All rights reserved
 */

package com.Sergio.EasyRMT.Controller;

import com.Sergio.EasyRMT.Domain.EpicDom;
import com.Sergio.EasyRMT.Domain.ProjectDom;
import com.Sergio.EasyRMT.Domain.UserStoryDom;
import com.Sergio.EasyRMT.Model.types.*;
import com.Sergio.EasyRMT.Service.EpicService;
import com.Sergio.EasyRMT.Service.ProjectService;
import com.Sergio.EasyRMT.Service.UserStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserStoryController {
    final String PATH_BASE = "/project/{projectId}/epic/{epicId}/";
    ProjectService projectService;
    EpicService epicService;
    UserStoryService userStoryService;

    @Autowired
    public UserStoryController(ProjectService projectService, EpicService epicService, UserStoryService userStoryService) {
        this.projectService = projectService;
        this.epicService = epicService;
        this.userStoryService = userStoryService;
    }

    /**
     * This rest controller receives a request to get an user stories list related with an epic
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param epicId {@link UserStoryDom} id
     * @param mav autogenerated model and view
     * @return model and view with user stories list
     */
    @RequestMapping(value = PATH_BASE+"userstories", method = RequestMethod.GET)
    public ModelAndView getUserStoriesListView(@PathVariable int projectId, @PathVariable int epicId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        List<UserStoryDom> userStoryDomList = userStoryService.getUserStories(epicId);
        ProjectDom project = projectService.getProject(projectId);
        EpicDom epicDom = epicService.getEpic(epicId);
        mav.setViewName("userStoriesDashboard");
        mav.addObject("project", project);
        mav.addObject("userStoriesList", userStoryDomList);
        mav.addObject("projectList", projectDomList);
        mav.addObject("epicId",epicDom.getIdEpic());
        mav.addObject("epicName", epicDom.getName());
        return mav;
    }

    /**
     * This rest controller receives a request to get an userStory related with a project and an epic
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId id of epid which is related with user story
     * @param userStoryId {@link UserStoryDom} UserStory id of epic requested
     * @param mav autogenerated model and view
     * @return model and view with user story
     */
    @RequestMapping(value = PATH_BASE+"userstory/{userStoryId}", method = RequestMethod.GET)
    public ModelAndView getUserStoryView(@PathVariable int projectId, @PathVariable int epicId,
                                         @PathVariable int userStoryId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        EpicDom epicDom = epicService.getEpic(epicId);
        mav.setViewName("userStory");
        mav.addObject("userStory", userStoryService.getUserStory(userStoryId));
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("epicId",epicDom.getIdEpic());
        mav.addObject("epicName", epicDom.getName());
        return mav;
    }

    /**
     * This rest controller receives a request to get a page to create an userstory related with a project and an epic
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId {@link EpicDom} epic id which will own the user story
     * @param mav autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"userstories/create", method = RequestMethod.GET)
    public ModelAndView getCreateUserStoryView(@PathVariable int projectId, @PathVariable int epicId,
                                               ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        EpicDom epicDom = epicService.getEpic(epicId);
        UserStoryDom userStoryDom = new UserStoryDom();
        mav.setViewName("createUserStory");
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("userStory", userStoryDom);
        mav.addObject("epicId",epicDom.getIdEpic());
        mav.addObject("epicName", epicDom.getName());
        mav.addObject("priority", Priority.values());
        mav.addObject("state", State.values());
        mav.addObject("risk", Risk.values());
        mav.addObject("complexity", Complexity.values());
        mav.addObject("scope", Scope.values());
        return mav;
    }

    /**
     * This method gets the request of an user story creation.
     * Then calls {@link UserStoryService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link UserStoryDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param epicId {@link EpicDom} id object
     * @param userStoryDom {@link UserStoryDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link UserStoryDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"userstory/create", method = RequestMethod.POST)
    public ModelAndView createUserStory(@PathVariable int projectId, @PathVariable int epicId,
                                   @ModelAttribute @Valid UserStoryDom userStoryDom){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        UserStoryDom persistedUserStory = userStoryService.create(userStoryDom, epicId, projectId);
        EpicDom epicDom = epicService.getEpic(epicId);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("userStory");
        mav.addObject("userStory", persistedUserStory);
        mav.addObject("project", project);
        mav.addObject("epicId",epicDom.getIdEpic());
        mav.addObject("epicName", epicDom.getName());
        mav.addObject("projectList", projectDomList);
        return mav;
    }

    /**
     * This rest controller receives a request to get a page to update an user story related with a project and an epic
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId {@link EpicDom} epic Id which owns the user story
     * @param userStoryId {@link UserStoryDom} id of userStory to be updated
     * @param mav autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"userstory/update/{userStoryId}", method = RequestMethod.GET)
    public ModelAndView getUpdateUserStoryView(@PathVariable int projectId,@PathVariable int epicId,
                                          @PathVariable int userStoryId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        EpicDom epicDom = epicService.getEpic(epicId);
        UserStoryDom userStoryDom = userStoryService.getUserStory(userStoryId);
        mav.setViewName("createUserStory");
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("userStory", userStoryDom);
        mav.addObject("epicId",epicDom.getIdEpic());
        mav.addObject("epicName", epicDom.getName());
        mav.addObject("priority", Priority.values());
        mav.addObject("state", State.values());
        mav.addObject("risk", Risk.values());
        mav.addObject("complexity", Complexity.values());
        mav.addObject("scope", Scope.values());
        return mav;
    }

    /**
     * This method gets the request of an userStory update.
     * Then calls {@link UserStoryService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link UserStoryDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param epicId {@link EpicDom} epic Id
     * @param userStoryId {@link UserStoryDom} userstory Id to be updated
     * @param userStoryDom {@link UserStoryDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link UserStoryDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"userstory/{userStoryId}", method = RequestMethod.POST)
    public ModelAndView updateUserStory(@PathVariable int projectId, @PathVariable int epicId, @PathVariable int userStoryId,
                                        @ModelAttribute @Valid UserStoryDom userStoryDom){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        UserStoryDom persistedUserStory = userStoryService.update(projectId,epicId,userStoryId,userStoryDom);
        EpicDom epic = epicService.getEpic(epicId);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("userStory");
        mav.addObject("userStory", persistedUserStory);
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("epicId",epic.getIdEpic());
        mav.addObject("epicName", epic.getName());
        return mav;
    }

    /**
     * Delete epic receives an userStory id as path variable and uses it to call {@link UserStoryService} delete method.
     * this method is called via JavaScript so returns a HttpStatus ok if deletion has been done and a HttpStatus
     * Internal Server Error if not.
     * @param projectId project id
     * @param epicId epic id
     * @param userStoryId userStory id to be deleted
     * @return HttpStatus.ok if correct. HttpStatus.INTERNAL_SERVER_ERROR if not correct.
     */
    @RequestMapping(value = PATH_BASE+"userStory/{epicId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEpic(@PathVariable int projectId, @PathVariable int epicId, @PathVariable int userStoryId){
        boolean deleted = userStoryService.deleteUserStory(userStoryId);
        if(deleted){
            return ResponseEntity.status(HttpStatus.OK).body("");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }
}
