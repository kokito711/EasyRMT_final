/*
 * Copyright (c) $today.year.Sergio López Jiménez and Universidad de Valladolid
 *                             All rights reserved
 */

package com.Sergio.EasyRMT.Controller;

import com.Sergio.EasyRMT.Domain.FeatureDom;
import com.Sergio.EasyRMT.Domain.ProjectDom;
import com.Sergio.EasyRMT.Model.types.*;
import com.Sergio.EasyRMT.Service.DocumentService;
import com.Sergio.EasyRMT.Service.FeatureService;
import com.Sergio.EasyRMT.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@RestController
public class FeatureController {
    final String PATH_BASE = "/project/{projectId}/";
    ProjectService projectService;
    FeatureService featureService;
    DocumentService documentService;

    @Autowired
    public FeatureController(ProjectService projectService, FeatureService featureService,
                            DocumentService documentService) {
        this.projectService = projectService;
        this.featureService = featureService;
        this.documentService = documentService;
    }

    /**
     * This rest controller receives a request to get a feature list related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} id
     * @param mav autogenerated model and view
     * @return model and view with feature list
     */
    @RequestMapping(value = PATH_BASE+"features", method = RequestMethod.GET)
    public ModelAndView getFeatureListView(@PathVariable int projectId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        List<FeatureDom> featureDomList = featureService.getFeatures(projectId);
        ProjectDom project = projectService.getProject(projectId);
        mav.setViewName("featuresDashboard");
        mav.addObject("project", project);
        mav.addObject("featureList", featureDomList);
        mav.addObject("projectList", projectDomList);
        return mav;
    }

    /**
     * This rest controller receives a request to get a feature related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the epic
     * @param featureId {@link FeatureDom} FeatureId of feature requested
     * @param mav autogenerated model and view
     * @return model and view with feature
     */
    @RequestMapping(value = PATH_BASE+"feature/{featureId}", method = RequestMethod.GET)
    public ModelAndView getFeatureView(@PathVariable int projectId, @PathVariable int featureId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        mav.setViewName("feature");
        mav.addObject("feature", featureService.getFeature(featureId));
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("fileList", documentService.getFileList(projectId,featureId));
        return mav;
    }

    /**
     * This rest controller receives a request to get a page to create a feature related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the feature
     * @param mav autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"features/create", method = RequestMethod.GET)
    public ModelAndView getCreateFeatureView(@PathVariable int projectId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        FeatureDom featureDom = new FeatureDom();
        mav.setViewName("createFeature");
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("feature", featureDom);
        mav.addObject("priority", Priority.values());
        mav.addObject("state", State.values());
        mav.addObject("risk", Risk.values());
        mav.addObject("complexity", Complexity.values());
        mav.addObject("scope", Scope.values());
        return mav;
    }

    /**
     * This method gets the request of a feature creation.
     * Then calls {@link FeatureService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link FeatureDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param feature {@link FeatureDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link FeatureDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"features", method = RequestMethod.POST)
    public ModelAndView createFeature(@PathVariable int projectId, @ModelAttribute @Valid FeatureDom feature){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        FeatureDom persistedFeature = featureService.create(feature, projectId);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("feature");
        mav.addObject("feature", persistedFeature);
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);

        return mav;
    }

    /**
     * This rest controller receives a request to get a page to update a feature related with a project
     * Then creates the model and view and returns it.
     * @param projectId {@link ProjectDom} project Id which owns the feature
     * @param featureId {@link FeatureDom} feature Id to be updated
     * @param mav autogenerated model and view
     * @return model and view with page
     */
    @RequestMapping(value = PATH_BASE+"feature/update/{featureId}", method = RequestMethod.GET)
    public ModelAndView getUpdateFeatureView(@PathVariable int projectId,@PathVariable int featureId, ModelAndView mav){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        FeatureDom featureDom = featureService.getFeature(featureId);
        mav.setViewName("updateFeature");
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        mav.addObject("feature", featureDom);
        mav.addObject("priority", Priority.values());
        mav.addObject("state", State.values());
        mav.addObject("risk", Risk.values());
        mav.addObject("complexity", Complexity.values());
        mav.addObject("scope", Scope.values());
        return mav;
    }

    /**
     * This method gets the request of a feature update.
     * Then calls {@link FeatureService}to manage it.
     * When the information is returned this method generate a new ModelAndView with a project view and the persisted
     * {@link FeatureDom} as object.
     * @param projectId {@link ProjectDom} id object
     * @param featureId {@link FeatureDom} feature Id to be updated
     * @param featureDom {@link FeatureDom} information to be persisted
     * @return ModelAndView with a project view and the persisted
     *       {@link FeatureDom} as object.
     */
    @RequestMapping(value = PATH_BASE+"feature/{featureId}/update", method = RequestMethod.POST)
    public ModelAndView updateFeature(@PathVariable int projectId, @PathVariable int featureId,
                                   @ModelAttribute @Valid FeatureDom featureDom){
        List<ProjectDom> projectDomList = projectService.getProjects();
        ProjectDom project = projectService.getProject(projectId);
        FeatureDom persistedFeature = featureService.update(featureDom, featureId,projectId);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("feature");
        mav.addObject("feature", persistedFeature);
        mav.addObject("project", project);
        mav.addObject("projectList", projectDomList);
        return mav;
    }

    /**
     * Delete feature receives a feature id as path variable and uses it to call {@link FeatureService} delete method.
     * this method is called via JavaScript so returns a HttpStatus ok if deletion has been done and a HttpStatus
     * Internal Server Error if not.
     * @param projectId project id
     * @param featureId feature id to be deleted.
     * @return HttpStatus.ok if correct. HttpStatus.INTERNAL_SERVER_ERROR if not correct.
     */
    @RequestMapping(value = PATH_BASE+"/feature/{featureId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteFeature(@PathVariable int projectId, @PathVariable int featureId){
        boolean deleted = featureService.deleteFeature(featureId);
        if(deleted){
            return ResponseEntity.status(HttpStatus.OK).body("");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }
}
