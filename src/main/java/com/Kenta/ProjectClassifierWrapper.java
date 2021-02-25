package com.Kenta;

import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.projects.Project;

import java.io.IOException;

/**
 * Classifier Wrapper. Taken from Pete Bankhead: https://github.com/qupath
 * @param <T>
 */
public class ProjectClassifierWrapper<T> implements ClassifierWrapper<T> {

    private Project<T> project;
    private String name;

    public ProjectClassifierWrapper(Project<T> project, String name) {
        this.project = project;
        this.name = name;
    }

    @Override
    public ObjectClassifier<T> getClassifier() throws IOException {
        return project.getObjectClassifiers().get(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectClassifierWrapper other = (ProjectClassifierWrapper) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        return true;
    }

}
