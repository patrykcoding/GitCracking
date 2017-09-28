package com.kaczmarkiewiczp.gitcracking;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.User;

import java.util.Comparator;

public class Comparators {

    public static class RepositoryComparator implements Comparator<Repository> {

        @Override
        public int compare(Repository o1, Repository o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class MilestonesComparator implements Comparator<Milestone> {
        @Override
        public int compare(Milestone o1, Milestone o2) {
            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }
    }

    public static class CollaboratorComparator implements Comparator<User> {
        @Override
        public int compare(User o1, User o2) {
            return o1.getLogin().compareToIgnoreCase(o2.getLogin());
        }
    }

    public static class PullRequestsComparator implements Comparator<PullRequest> {

        @Override
        public int compare(PullRequest o1, PullRequest o2) {
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        }
    }

    public static class IssuesComparator implements Comparator<Issue> {

        @Override
        public int compare(Issue o1, Issue o2) {
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        }
    }

    public static class LabelsComparator implements Comparator<Label> {

        @Override
        public int compare(Label o1, Label o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class FilesComparator implements Comparator<RepositoryContents> {

        @Override
        public int compare(RepositoryContents o1, RepositoryContents o2) {
            if (o1.getType().equals("dir") && !o2.getType().equals("dir")) {
                return -1;
            }

            if (!o1.getType().equals("dir") && o2.getType().equals("dir")) {
                return 1;
            }

            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
