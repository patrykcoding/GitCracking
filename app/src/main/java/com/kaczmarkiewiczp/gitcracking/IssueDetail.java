package com.kaczmarkiewiczp.gitcracking;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class IssueDetail extends AppCompatActivity {

    private Issue issue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        Bundle bundle = getIntent().getExtras();
        issue = (Issue) bundle.getSerializable("issue");
        if (issue == null) {
            // something really bad happened - return
            // TODO show error screen
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
        
        setContent();
    }

    private void setContent() {
        setTitleContent();
        setRepositoryContent();
        setUserContent();
        setMilestoneContent();
        setLabelsContent();
        setAssigneeContent();
        setDescriptionContent();
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
        String repositoryUrl = issue.getUrl();
        int repositoryNameStartIndex = 29;
        int repositoryNameEndIndex = repositoryUrl.indexOf("/issues/");
        String repositoryName = repositoryUrl.substring(repositoryNameStartIndex, repositoryNameEndIndex);

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
        if (dueDate.after(today)) {
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

        setDescriptionDivider();
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
        setDescriptionDivider();
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

        setDescriptionDivider();
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

        setDescriptionDivider();
    }

    private void setDescriptionDivider() {
        View separator = findViewById(R.id.description_divider);
        if (separator.getVisibility() == View.GONE) {
            separator.setVisibility(View.VISIBLE);
        }
    }
}
