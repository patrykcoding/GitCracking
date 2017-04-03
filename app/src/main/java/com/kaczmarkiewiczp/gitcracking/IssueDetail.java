package com.kaczmarkiewiczp.gitcracking;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.flexbox.FlexboxLayout;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.ocpsoft.prettytime.PrettyTime;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class IssueDetail extends AppCompatActivity implements CreateMilestoneDialog.SaveMilestoneListener {

    private Issue issue;
    private Repository repository;
    private List<Comment> comments;
    private List<Label> repositoryLabels;
    private List<Contributor> repositoryContributors;
    private List<Milestone> repositoryMilestones;
    private FloatingActionMenu floatingActionMenu;
    private GitHubClient gitHubClient;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        Bundle bundle = getIntent().getExtras();
        issue = (Issue) bundle.getSerializable("issue");
        repository = (Repository) bundle.getSerializable("repository");
        if (issue == null || repository == null) {
            // something really bad happened - return
            finish();
            return;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Issue #" + issue.getNumber());
        setSupportActionBar(toolbar);
        NavBarUtils navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        navBarUtils.setNavigationDrawerButtonAsUp();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navBarUtils.killAllActivitiesOnNewActivityStart(true);
        AccountUtils accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        floatingActionMenu.setClosedOnTouchOutside(true);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_issue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetIssue().execute(issue);
            }
        });

        setUpOnClickListeners();
        setContent();
        new GetIssue().execute(issue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_refresh:
                 Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                 findViewById(R.id.action_refresh).startAnimation(rotate);
                 loadingIndicator.setVisibility(View.VISIBLE);
                 swipeRefreshLayout.setEnabled(false);
                 new GetIssue().execute(issue);
                 return true;
             default:
                return super.onOptionsItemSelected(item);
         }
    }

    public void changeIssueState(View view) {
        floatingActionMenu.close(true);
        String currentState = issue.getState();

        if (currentState.equals(IssueService.STATE_OPEN)) {
            issue = issue.setState(IssueService.STATE_CLOSED);
        } else {
            issue = issue.setState(IssueService.STATE_OPEN);
        }
        new UpdateIssue().execute(issue);
    }

    public void setMilestone() {
        floatingActionMenu.close(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Milestone");
        int currentMilestone = 0;
        final int[] selectedOption = new int[1];
        String[] options;
        if (repositoryMilestones == null) {
            options = new String[1];
        } else {
            options = new String[repositoryMilestones.size() + 1];
        }
        options[0] = "NO MILESTONE";
        int i = 1;
        if (repositoryMilestones != null) {
            for (Milestone milestone : repositoryMilestones) {
                if (issue.getMilestone() != null && issue.getMilestone().getTitle().equals(milestone.getTitle())) {
                    currentMilestone = i;
                }
                options[i++] = milestone.getTitle();
            }
        }
        
        builder.setSingleChoiceItems(options, currentMilestone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = which;
            }
        });
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedOption[0] == 0) {
                    Milestone noMilestone = new Milestone();
                    noMilestone.setTitle("");
                    issue.setMilestone(noMilestone);
                } else {
                    Milestone newMilestone = repositoryMilestones.get(selectedOption[0] - 1);
                    issue.setMilestone(newMilestone);
                }
                new UpdateIssue().execute(issue);
            }
        });
        final CreateMilestoneDialog milestoneDialog = new CreateMilestoneDialog();
        builder.setNeutralButton("New Milestone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                milestoneDialog.show(getFragmentManager(), "Milestone");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onSaveMilestone(CreateMilestoneDialog dialog) {
        String title = dialog.getMilestoneTitle();
        String description = dialog.getMilestoneDescription();

        Milestone milestone = new Milestone();
        milestone.setTitle(title);
        milestone.setDescription(description);
        milestone.setState("open");
        if (dialog.isMilestoneDueDateSet()) {
            milestone.setDueOn(dialog.getMilestoneDueDate());
        }
        dialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "Milestone created", Snackbar.LENGTH_LONG)
                .show();

        new NewMilestone().execute(milestone);
    }

    private void setAssignee() {
        floatingActionMenu.close(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Assignee");
        int currentAssignee = 0;
        final int[] selectedOption = new int[1];
        String[] options;
        if (repositoryContributors == null) {
            options = new String[1];
        } else {
            options = new String[repositoryContributors.size() + 1];
        }
        options[0] = "NO ASSIGNEE";
        int i = 1;
        if (repositoryContributors != null) {
            for (Contributor contributor : repositoryContributors) {
                if (issue.getAssignee() != null && issue.getAssignee().getLogin().equals(contributor.getLogin())) {
                    currentAssignee = i;
                }
                options[i++] = contributor.getLogin();
            }
        }

        builder.setSingleChoiceItems(options, currentAssignee, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = which;
            }
        });
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedOption[0] == 0) {
                    User user = new User();
                    user.setLogin("");
                    issue.setAssignee(user);
                } else {
                    Contributor contributor = repositoryContributors.get(selectedOption[0] - 1);
                    User user = new User();
                    user.setLogin(contributor.getLogin());
                    issue.setAssignee(user);
                }
                new UpdateIssue().execute(issue);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void setContent() {
        setIssueStateContent();
        setTitleContent();
        setRepositoryContent();
        setUserContent();
        setMilestoneContent();
        setLabelsContent();
        setAssigneeContent();
        setDescriptionContent();
    }

    private void setUpOnClickListeners() {
        findViewById(R.id.fab_milestone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMilestone();
            }
        });
        findViewById(R.id.fab_assignee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAssignee();
            }
        });
    }

    @SuppressWarnings("deprecation") // for getColor -- check in code for android version
    private void setIssueStateContent() {
        String status = issue.getState();
        status = status.substring(0, 1).toUpperCase() + status.substring(1);

        LinearLayout linearLayoutIssueStatus = (LinearLayout) findViewById(R.id.ll_issue_status);
        ImageView imageViewIssueStatus = (ImageView) findViewById(R.id.iv_issue_status);
        TextView textViewIssueStatus = (TextView) findViewById(R.id.tv_issue_status);

        int color;
        if (status.equals("Open")) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color = getColor(R.color.issue_open);
            } else {
                color = getResources().getColor(R.color.issue_open);
            }
            imageViewIssueStatus.setImageResource(R.drawable.ic_issue_opened_white);
        } else { //if (status.equals("Closed"))
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color = getColor(R.color.issue_closed);
            } else {
                color = getResources().getColor(R.color.issue_closed);
            }
            imageViewIssueStatus.setImageResource(R.drawable.ic_issue_closed_white);
        }
        linearLayoutIssueStatus.setBackgroundColor(color);
        textViewIssueStatus.setText(status);
    }

    private void setTitleContent() {
        String title = issue.getTitle();
        String issueNumber = "#" + String.valueOf(issue.getNumber());

        TextView textViewTitle = (TextView) findViewById(R.id.tv_issue_title);
        TextView textViewIssueNumber = (TextView) findViewById(R.id.tv_issue_number);

        textViewTitle.setText(title);
        textViewIssueNumber.setText(issueNumber);
    }

    private void setRepositoryContent() {
        String repositoryName = repository.getName();
        String repositoryOwner = repository.getOwner().getLogin();
        repositoryName = repositoryOwner + "/" + repositoryName;

        TextView textViewRepositoryName = (TextView) findViewById(R.id.tv_issue_repo);
        textViewRepositoryName.setText(repositoryName);
    }

    private void setUserContent() {
        String userLogin = issue.getUser().getLogin();
        String userIconUrl = issue.getUser().getAvatarUrl();
        Date date = issue.getCreatedAt();
        PrettyTime prettyTime = new PrettyTime();
        String action = "opened this issue";

        ImageView imageViewUserIcon = (ImageView) findViewById(R.id.iv_user_icon);
        TextView textViewUser = (TextView) findViewById(R.id.tv_user_login);
        TextView textViewAction = (TextView) findViewById(R.id.tv_open_close_action);
        TextView textViewDate = (TextView) findViewById(R.id.tv_date);

        Glide
                .with(this)
                .load(userIconUrl)
                .error(getDrawable(android.R.drawable.sym_def_app_icon))
                .placeholder(R.drawable.progress_animation)
                .crossFade()
                .into(imageViewUserIcon);
        textViewUser.setText(userLogin);
        textViewAction.setText(action);
        textViewDate.setText(prettyTime.format(date));
    }

    @SuppressWarnings("deprecation") // for getColor -- check in code for android version
    private void setMilestoneContent() {
        Milestone milestone = issue.getMilestone();
        LinearLayout linearLayoutMilestone = (LinearLayout) findViewById(R.id.ll_milestone);
        if (milestone == null) {
            // milestone is the first element for the section, if (after refresh) there is no milestone
            // for this issue, make the divider invisible (it could have been visible before refresh)
            // other elements (assignee, labels) don't have to do this, and if they're visible, they
            // will set the divider to visible
            setDescriptionDivider(false);
            linearLayoutMilestone.setVisibility(View.GONE);
            return;
        }

        TextView textViewMilestone = (TextView) findViewById(R.id.tv_milestone);
        ProgressBar progressBarMilestone = (ProgressBar) findViewById(R.id.pb_milestone);

        String milestoneTitle = milestone.getTitle();
        int openIssues = milestone.getOpenIssues();
        int closedIssues = milestone.getClosedIssues();

        linearLayoutMilestone.setVisibility(View.VISIBLE);
        textViewMilestone.setText(milestoneTitle);
        progressBarMilestone.setMax(openIssues + closedIssues);
        progressBarMilestone.setProgress(closedIssues);

        Date dueDate = milestone.getDueOn();
        Date today = new Date();
        int progressColor;
        if (dueDate != null) {
            if (today.after(dueDate)) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    progressColor = getColor(R.color.milestone_progress_overdue);
                } else {
                    progressColor = getResources().getColor(R.color.milestone_progress_overdue);
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    progressColor = getColor(R.color.milestone_progress);
                } else {
                    progressColor = getResources().getColor(R.color.milestone_progress);
                }
            }
            progressBarMilestone.getProgressDrawable().setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);
        }
        setDescriptionDivider(true);
    }

    private void setLabelsContent() {
        List<Label> labels = issue.getLabels();
        LinearLayout linearLayoutLabels = (LinearLayout) findViewById(R.id.ll_tags);

        if (labels == null || labels.size() < 1) {
            linearLayoutLabels.setVisibility(View.GONE);
            return;
        }
        linearLayoutLabels.setVisibility(View.VISIBLE);
        FlexboxLayout flexboxLayoutLabels = (FlexboxLayout) findViewById(R.id.fl_tags);
        if (flexboxLayoutLabels.getChildCount() > 0) {
            flexboxLayoutLabels.removeAllViews();
        }
        for (Label label : labels) {
            String colorString = label.getColor();
            int rgb = Color.parseColor("#" + colorString);
            int r = Color.red(rgb);
            int g = Color.green(rgb);
            int b = Color.blue(rgb);
            double a = 1 - (0.299 * r + 0.587 * g + 0.114 * b) / 255;

            TextView textViewLabel = new TextView(this);
            textViewLabel.setText(label.getName());
            textViewLabel.setBackgroundColor(Color.parseColor("#" + colorString));
            if (a < 0.5) {
                textViewLabel.setTextColor(Color.BLACK);
            } else {
                textViewLabel.setTextColor(Color.WHITE);
            }
            textViewLabel.setElevation(4);
            textViewLabel.setPadding(8, 4, 8, 4);
            FlexboxLayout.LayoutParams labelLayoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLayoutParams.setMargins(0, 0, 12, 12);
            textViewLabel.setLayoutParams(labelLayoutParams);
            flexboxLayoutLabels.addView(textViewLabel);
        }
        setDescriptionDivider(true);
    }

    private void setAssigneeContent() {
        User assignee = issue.getAssignee();
        LinearLayout linearLayoutAssignee = (LinearLayout) findViewById(R.id.ll_assignee);
        if (assignee == null) {
            linearLayoutAssignee.setVisibility(View.GONE);
            return;
        }

        String assigneeName = assignee.getLogin();
        String assigneeIconUrl = assignee.getAvatarUrl();

        TextView textViewAssigneeName = (TextView) findViewById(R.id.tv_assignee_name);
        ImageView imageViewAssigneeIcon = (ImageView) findViewById(R.id.iv_assignee_icon);

        linearLayoutAssignee.setVisibility(View.VISIBLE);
        Glide
                .with(this)
                .load(assigneeIconUrl)
                .error(getDrawable(android.R.drawable.sym_def_app_icon))
                .placeholder(R.drawable.progress_animation)
                .crossFade()
                .into(imageViewAssigneeIcon);
        textViewAssigneeName.setText(assigneeName);

        setDescriptionDivider(true);
    }

    @SuppressWarnings("deprecation") // for Html.fromHtml -- check in code for android version
    private void setDescriptionContent() {
        String description = issue.getBodyHtml();
        TextView textViewDescription = (TextView) findViewById(R.id.tv_issue_description);

        if (description == null || description.isEmpty()) {
            textViewDescription.setVisibility(View.GONE);
            return;
        }

        textViewDescription.setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textViewDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textViewDescription.setText(Html.fromHtml(description));
        }

        setDescriptionDivider(true);
    }

    private void setDescriptionDivider(boolean visible) {
        View separator = findViewById(R.id.description_divider);
        if (visible) {
            if (separator.getVisibility() == View.GONE) {
                separator.setVisibility(View.VISIBLE);
            }
        } else {
            if (separator.getVisibility() != View.GONE) {
                separator.setVisibility(View.GONE);
            }
        }
    }

    /************************************************************************************************
     * Background tasks
     ************************************************************************************************/

    private class GetIssue extends AsyncTask<Issue, Void, Boolean> {

        Issue newIssue;

        @Override
        protected Boolean doInBackground(Issue... params) {
            Issue issue = params[0];
            IssueService issueService = new IssueService(gitHubClient);
            RepositoryService repositoryService = new RepositoryService(gitHubClient);
            MilestoneService milestoneService = new MilestoneService(gitHubClient);
            LabelService labelService = new LabelService(gitHubClient);

            try {
                newIssue = issueService.getIssue(repository, issue.getNumber());
                repositoryContributors = repositoryService.getContributors(repository, false);
                repositoryMilestones= milestoneService.getMilestones(repository, "open");
                repositoryLabels = labelService.getLabels(repository);

                Collections.sort(repositoryMilestones, new MilestonesComparator());
                Collections.sort(repositoryContributors, new ContributorComparator());
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                return;
            }
            issue = newIssue;
            setContent();
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (!swipeRefreshLayout.isEnabled()) {
                swipeRefreshLayout.setEnabled(true);
            }
            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
        }

        public class MilestonesComparator implements Comparator<Milestone> {
            @Override
            public int compare(Milestone o1, Milestone o2) {
                return o2.getTitle().compareToIgnoreCase(o1.getTitle());
            }
        }

        public class ContributorComparator implements Comparator<Contributor> {
            @Override
            public int compare(Contributor o1, Contributor o2) {
                return o2.getLogin().compareToIgnoreCase(o1.getLogin());
            }
        }
    }

    private class GetComments extends AsyncTask<Issue, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Issue... params) {
            Issue issue = params[0];
            IssueService issueService = new IssueService(gitHubClient);
            try {
                comments = issueService.getComments(repository, issue.getNumber());
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                return;
            }
            // TODO show comments
        }
    }

    private class UpdateIssue extends AsyncTask<Issue, Void, Boolean> {

        Issue updatedIssue;

        @Override
        protected Boolean doInBackground(Issue... params) {
            Issue issue = params[0];
            IssueService issueService = new IssueService(gitHubClient);

            try {
                updatedIssue = issueService.editIssue(repository, issue);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            issue = updatedIssue;
            if (!success) {
                return;
            }
            setContent();

            /*
            FloatingActionButton fab_close_issue = (FloatingActionButton) findViewById(R.id.fab_close_issue);
            String message;
            if (issue.getState().equals("open")) {
                message = "Issue reopened";
                fab_close_issue.setLabelText("Open issue");
            } else {
                message = "Issue closed";
                fab_close_issue.setLabelText("Close issue");
            }
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeIssueState(findViewById(android.R.id.content));
                        }
                    }).show();
                    */
        }
    }

    private class NewMilestone extends AsyncTask<Milestone, Void, Boolean> {

        private Milestone newMilestone;

        @Override
        protected Boolean doInBackground(Milestone... params) {
            Milestone milestone = params[0];
            MilestoneService milestoneService = new MilestoneService(gitHubClient);

            try {
                newMilestone = milestoneService.createMilestone(repository, milestone);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                issue.setMilestone(newMilestone);
                new UpdateIssue().execute(issue);
            }
        }
    }
}
