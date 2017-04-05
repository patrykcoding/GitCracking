package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewIssue extends AppCompatActivity implements CreateMilestoneDialog.milestoneCreationListener, CreateLabelDialog.labelCreationListener {

    private GitHubClient gitHubClient;
    private Issue newIssue;
    private List<Repository> repositories;
    private List<Label> repositoryLabels;
    private List<Milestone> repositoryMilestones;
    private List<User>  repositoryCollaborators;
    private boolean isRepositoryDataReady;
    private boolean isRepositoryListReady;
    private Repository selectedRepository;
    private TextView textViewRepository;
    private TextView textViewLabels;
    private TextView textViewMilestone;
    private TextView textViewAssignee;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_issue);

        context = this;

        newIssue = new Issue();
        isRepositoryListReady = false;
        isRepositoryDataReady = false;


        textViewRepository = (TextView) findViewById(R.id.btn_select_repo);
        textViewLabels = (TextView) findViewById(R.id.btn_select_labels);
        textViewMilestone = (TextView) findViewById(R.id.btn_select_milestone);
        textViewAssignee = (TextView) findViewById(R.id.btn_select_assignee);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("New Issue");
        setSupportActionBar(toolbar);
        NavBarUtils navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        navBarUtils.setNavigationDrawerButtonAsUp();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        navBarUtils.killAllActivitiesOnNewActivityStart(true);
        AccountUtils accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        new GetRepositories().execute(gitHubClient);

        setUpOnClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                new CreateNewIssue().execute();
               return true;
            default:
                return false;
        }
    }

    private void setUpOnClickListeners() {
        textViewRepository.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepository();
            }
        });
        textViewLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLabels();
            }
        });
        textViewMilestone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMilestone();
            }
        });
        textViewAssignee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAssignee();
            }
        });
    }

    private void setRepository() {
        if (!isRepositoryListReady) {
            ShowLoadingDialog loadingDialog = new ShowLoadingDialog("Loading Repositories", "Please wait", ShowLoadingDialog.REPOSITORY_LOADING, ShowLoadingDialog.REPOSITORY_CALLBACK);
            loadingDialog.execute();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select repository");
        String[] options = new String[repositories.size()];
        final int[] selectedOption = new int[1];
        selectedOption[0] = -1;
        if (repositories != null) {
            int i = 0;
            for (Repository repository : repositories) {
                options[i++] = repository.getOwner().getLogin() + "/" + repository.getName();
            }
        }

        builder.setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = which;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedOption[0] == -1) {
                    // TODO show message -- has to be selected
                    return;
                }
                int index = selectedOption[0];
                selectedRepository = repositories.get(index);
                textViewRepository.setText(selectedRepository.getName());
                textViewLabels.setText(getResources().getString(R.string.select_labels));
                new GetRepositoryData().execute(selectedRepository);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void setLabels() {
        if (!isRepositoryDataReady) {
            ShowLoadingDialog loadingDialog = new ShowLoadingDialog("Getting labels", "Please wait", ShowLoadingDialog.DATA_LOADING, ShowLoadingDialog.LABELS_CALLBACK);
            loadingDialog.execute();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select labels");
        String[] options = new String[repositoryLabels.size()]; // TODO crash if there are no labels
        final List<Label> issueLabels;
        if (newIssue.getLabels() != null) {
            issueLabels = newIssue.getLabels();
        } else {
            issueLabels = new ArrayList<>();
        }

        final boolean[] selection = new boolean[repositoryLabels.size()];
        if (repositoryLabels != null) {
            for (int i = 0; i < repositoryLabels.size(); i++) {
                Label label = repositoryLabels.get(i);
                options[i] = label.getName();
                selection[i] = issueLabels.contains(label);
            }
        }
        builder.setMultiChoiceItems(options, selection, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selection[which] = isChecked;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Label> newLabels = new ArrayList<>();
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i]) {
                        newLabels.add(repositoryLabels.get(i));
                    }
                }
                newIssue.setLabels(newLabels);

                if (newLabels.isEmpty()) {
                    textViewLabels.setText(getResources().getString(R.string.select_labels));
                } else {
                    int selectedCount = newLabels.size();
                    String message = selectedCount + " labels selected";
                    textViewLabels.setText(message);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        final CreateLabelDialog createLabelDialog = new CreateLabelDialog();
        builder.setNeutralButton("New Label", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createLabelDialog.show(getFragmentManager(), "New Label");
            }
        });
        builder.show();
    }

    @Override
    public void onSaveLabel(CreateLabelDialog dialog) {
        String labelName = dialog.getLabelName();
        String labelColor = dialog.getLabelHexColor();

        Label label = new Label();
        label.setName(labelName);
        label.setColor(labelColor);

        dialog.dismiss();
        Snackbar.make(findViewById(android.R.id.content), "Label created", Snackbar.LENGTH_LONG)
                .show();
        new NewLabel().execute(label);
    }

    private void setMilestone() {
        if (!isRepositoryDataReady) {
            ShowLoadingDialog loadingDialog = new ShowLoadingDialog("Getting milestones", "Please wait", ShowLoadingDialog.DATA_LOADING, ShowLoadingDialog.MILESTONE_CALLBACK);
            loadingDialog.execute();
            return;
        }

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
        options[0] = "---NO MILESTONE---";
        int i = 1;
        if (repositoryMilestones != null) {
            for (Milestone milestone : repositoryMilestones) {
                if (newIssue.getMilestone() != null && newIssue.getMilestone().getTitle().equals(milestone.getTitle())) {
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
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedOption[0] == 0) {
                    newIssue.setMilestone(null);
                    textViewMilestone.setText(getResources().getString(R.string.select_milestone));
                } else {
                    Milestone newMilestone = repositoryMilestones.get(selectedOption[0] - 1);
                    newIssue.setMilestone(newMilestone);
                    textViewMilestone.setText(newMilestone.getTitle());
                }
            }
        });
        final CreateMilestoneDialog milestoneDialog = new CreateMilestoneDialog();
        builder.setNeutralButton("New Milestone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                milestoneDialog.show(getFragmentManager(), "New Milestone");
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

        textViewMilestone.setText(milestone.getTitle());

        new NewMilestone().execute(milestone);
    }

    private void setAssignee() {
        if (!isRepositoryDataReady) {
            ShowLoadingDialog loadingDialog = new ShowLoadingDialog("Getting collaborators", "Please wait", ShowLoadingDialog.DATA_LOADING, ShowLoadingDialog.ASSIGNEE_CALLBACK);
            loadingDialog.execute();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Assignee");
        int currentAssignee = 0;
        final int[] selectedOption = new int[1];
        String[] options;
        if (repositoryCollaborators == null) {
            options = new String[1];
        } else {
            options = new String[repositoryCollaborators.size() + 1];
        }
        options[0] = "--NO ASSIGNEE--";
        int i = 1;
        if (repositoryCollaborators != null) {
            for (User collaborator : repositoryCollaborators) {
                if (newIssue.getAssignee() != null && newIssue.getAssignee().getLogin().equals(collaborator.getLogin())) {
                    currentAssignee = i;
                }
                options[i++] = collaborator.getLogin();
            }
        }

        builder.setSingleChoiceItems(options, currentAssignee, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = which;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedOption[0] == 0) {
                    newIssue.setAssignee(null);
                } else {
                    User assignee = repositoryCollaborators.get(selectedOption[0] - 1);
                    newIssue.setAssignee(assignee);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**********************************************************************************************
     * Background tasks
     **********************************************************************************************/

    private class CreateNewIssue extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO show loading
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            IssueService issueService = new IssueService(gitHubClient);

            try {
                issueService.createIssue(selectedRepository, newIssue);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("issue", newIssue);
                bundle.putSerializable("repository", selectedRepository);
                intent.putExtras(bundle);
                intent.setClass(context, IssueDetail.class);
                startActivity(intent);
            }
        }
    }

    private class ShowLoadingDialog extends AsyncTask<Void, Void, Void> {

        public static final int REPOSITORY_LOADING = 0;
        public static final int DATA_LOADING = 1;
        public static final int REPOSITORY_CALLBACK = 3;
        public static final int LABELS_CALLBACK = 4;
        public static final int MILESTONE_CALLBACK = 5;
        public static final int ASSIGNEE_CALLBACK = 6;
        private int whichFlag;
        private int callBack;
        private String title;
        private String message;
        private ProgressDialog progressDialog;

        public ShowLoadingDialog(String title, String message, int whichBooleanFlag, int callbackMethod) {
            this.title = title;
            this.message = message;
            this.whichFlag = whichBooleanFlag;
            this.callBack = callbackMethod;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, title, message, true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean ready = false;
            while (!ready) {
                switch (whichFlag) {
                    case REPOSITORY_LOADING:
                        ready = isRepositoryListReady;
                        break;
                    case DATA_LOADING:
                        ready = isRepositoryDataReady;
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            switch (callBack) {
                case REPOSITORY_CALLBACK:
                    setRepository();
                    break;
                case LABELS_CALLBACK:
                    setLabels();
                    break;
                case MILESTONE_CALLBACK:
                    setMilestone();
                    break;
                case ASSIGNEE_CALLBACK:
                    setAssignee();
                    break;
            }
        }
    }

    private class GetRepositories extends AsyncTask<GitHubClient, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRepositoryListReady = false;
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                repositories = repositoryService.getRepositories();
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            isRepositoryListReady = success;
        }
    }

    private class GetRepositoryData extends AsyncTask<Repository, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRepositoryDataReady = false;
        }

        @Override
        protected Boolean doInBackground(Repository... params) {
            MilestoneService milestoneService = new MilestoneService(gitHubClient);
            LabelService labelService = new LabelService(gitHubClient);
            CollaboratorService collaboratorService = new CollaboratorService(gitHubClient);

            try {
                repositoryLabels = labelService.getLabels(selectedRepository);
                repositoryMilestones = milestoneService.getMilestones(selectedRepository, "open");
                repositoryCollaborators = collaboratorService.getCollaborators(selectedRepository);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            isRepositoryDataReady = success;
        }
    }

    private class NewLabel extends AsyncTask<Label, Void, Boolean> {

        private Label newLabel;

        @Override
        protected Boolean doInBackground(Label... params) {
            Label label = params[0];
            LabelService labelService = new LabelService(gitHubClient);

            try {
                newLabel = labelService.createLabel(selectedRepository, label);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                List<Label> labels = newIssue.getLabels();
                if (labels == null) {
                    labels = new ArrayList<>();
                }
                labels.add(newLabel);
                newIssue.setLabels(labels);

                int labelCount = labels.size();
                String message = labelCount + " labels selected";
                textViewLabels.setText(message);

                new GetRepositoryData().execute(selectedRepository);
            }
        }
    }

    private class NewMilestone extends AsyncTask<Milestone, Void, Boolean> {

        private Milestone newMilestone;

        @Override
        protected Boolean doInBackground(Milestone... params) {
            Milestone milestone = params[0];
            MilestoneService milestoneService = new MilestoneService(gitHubClient);

            try {
                newMilestone = milestoneService.createMilestone(selectedRepository, milestone);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                newIssue.setMilestone(newMilestone);
                new GetRepositoryData().execute(selectedRepository);
            }
        }
    }
}
