/*
 * Copyright (c) $today.year.Sergio López Jiménez and Universidad de Valladolid
 *                             All rights reserved
 */

package com.Sergio.EasyRMT.Controller;

import com.Sergio.EasyRMT.Domain.EpicDom;
import com.Sergio.EasyRMT.Domain.ProjectDom;
import com.Sergio.EasyRMT.Model.types.*;
import com.Sergio.EasyRMT.Service.EpicService;
import com.Sergio.EasyRMT.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class EpicController {
    final String PATH_BASE = "/project/{projectId}/";
    ProjectService projectService;
    EpicService epicService;

    @Autowired
    public EpicController(ProjectService projectService, EpicService epicService) {
        this.projectService = projectService;
        this.epicService = epicService;
    }

    /**
     * This rest controller receives a request to get an epic list related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param mav autogenerated model and view
     * @return model and view with epic list
     */
    @RequestMapping(value = PATH_BASE+"epics", method = RequestMethod.GET)
    public ModelAndView getEpicListView(@PathVariable int projectId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        List<EpicDom> epicDomList = epicService.getEpics(projectId);
        ProjectDom project = projectService.getProject(projectId);
        mav.setViewName("epicsDashboard");
        mav.addObject("project", project);
        mav.addObject("epicList", epicDomList);
        mav.addObject("projectList", projectDomList);
        return mav;
    }

    /**
     * This rest controller receives a request to get an epic related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param epicId {@link EpicDom} EpicId of epic requested
     * @param mav autogenerated model and view
     * @return model and view with epic
     */
    @RequestMapping(value = PATH_BASE+"epic/{epicId}", method = RequestMethod.GET)
    public ModelAndView getEpicView(@PathVariable int projectId, @PathVariable int epicId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        mav.setViewName("epic");
        mav.addObject("epic", epicService.getEpic(epicId));
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        return mav;
    }

    /**
     * This rest controller receives a request to get a page to create an epic related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param mav autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"epics/create", method = RequestMethod.GET)
    public ModelAndView getCreateEpicView(@PathVariable int projectId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        EpicDom epicDom = new EpicDom();
        mav.setViewName("createEpic");
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("epic", epicDom);
        mav.addObject("priority", Priority.values());
        mav.addObject("state", State.values());
        mav.addObject("risk", Risk.values());
        mav.addObject("complexity", Complexity.values());
        mav.addObject("scope", Scope.values());
        return mav;
    }
}
