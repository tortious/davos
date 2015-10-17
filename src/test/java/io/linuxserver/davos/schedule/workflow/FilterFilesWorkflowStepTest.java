package io.linuxserver.davos.schedule.workflow;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.linuxserver.davos.schedule.ScheduleConfiguration;
import io.linuxserver.davos.transfer.ftp.FTPFile;
import io.linuxserver.davos.transfer.ftp.connection.Connection;
import io.linuxserver.davos.transfer.ftp.exception.FileListingException;

public class FilterFilesWorkflowStepTest {

    @InjectMocks
    private FilterFilesWorkflowStep workflowStep = new FilterFilesWorkflowStep();

    @Mock
    private DownloadFilesWorkflowStep mockNextStep;

    @Mock
    private Connection mockConnection;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void workflowStepShouldListFilesInTheRemoteDirectory() {

        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        verify(mockConnection).listFiles("remote/");
    }

    @Test
    public void workflowStepShouldFilterOutAnyFilesThatAreNotInTheGivenConfigList() {

        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        config.setFilters(Arrays.asList("file1", "file2", "file4"));
        config.setLastRun(DateTime.now().minusDays(1));

        ArrayList<FTPFile> files = new ArrayList<FTPFile>();

        FTPFile file1 = new FTPFile("file1", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file2 = new FTPFile("file2", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file3 = new FTPFile("file3", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file4 = new FTPFile("file4", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file5 = new FTPFile("file5", 0, "remote/", DateTime.now().getMillis(), false);

        files.add(file1);
        files.add(file2);
        files.add(file3);
        files.add(file4);
        files.add(file5);

        when(mockConnection.listFiles("remote/")).thenReturn(files);

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        verify(mockNextStep).setFilesToDownload(Arrays.asList(file1, file2, file4));
    }

    @Test
    public void workflowStepShouldFilterOutAnyFilesThatAreNotInTheGivenConfigListAndWereModifiedBeforeLastRun() {

        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        config.setFilters(Arrays.asList("file1", "file2", "file4"));
        config.setLastRun(DateTime.now().minusDays(1));

        ArrayList<FTPFile> files = new ArrayList<FTPFile>();

        FTPFile file1 = new FTPFile("file1", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file2 = new FTPFile("file2", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file3 = new FTPFile("file3", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file4 = new FTPFile("file4", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file5 = new FTPFile("file5", 0, "remote/", DateTime.now().getMillis(), false);

        files.add(file1);
        files.add(file2);
        files.add(file3);
        files.add(file4);
        files.add(file5);

        when(mockConnection.listFiles("remote/")).thenReturn(files);

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        verify(mockNextStep).setFilesToDownload(Arrays.asList(file2));
    }

    @Test
    public void workflowStepShouldFilterOutAnyFilesThatDoNotMatchTheWildcards() {

        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        config.setFilters(Arrays.asList("file1?and?Stuff", "file2*something", "file4*", "file5"));
        config.setLastRun(DateTime.now().minusDays(1));

        ArrayList<FTPFile> files = new ArrayList<FTPFile>();

        FTPFile file1 = new FTPFile("file1.and-stuff", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file2 = new FTPFile("file2.andMoreTextsomething", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file3 = new FTPFile("file3", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file4 = new FTPFile("file4.txt", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file5 = new FTPFile("file5.txt", 0, "remote/", DateTime.now().getMillis(), false);

        files.add(file1);
        files.add(file2);
        files.add(file3);
        files.add(file4);
        files.add(file5);

        when(mockConnection.listFiles("remote/")).thenReturn(files);

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        verify(mockNextStep).setFilesToDownload(Arrays.asList(file2, file4));
    }
    
    @Test
    public void workflowStepShouldCallNextStepRunMethodOnceSettingFilters() {
        
        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        config.setFilters(Arrays.asList("file1", "file2", "file4"));
        config.setLastRun(DateTime.now().minusDays(1));

        ArrayList<FTPFile> files = new ArrayList<FTPFile>();

        FTPFile file1 = new FTPFile("file1", 0, "remote/", DateTime.now().getMillis(), false);

        files.add(file1);

        when(mockConnection.listFiles("remote/")).thenReturn(files);

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        InOrder inOrder = Mockito.inOrder(mockNextStep);
        
        inOrder.verify(mockNextStep).setFilesToDownload(Arrays.asList(file1));
        inOrder.verify(mockNextStep).runStep(schedule);
    }
    
    @Test
    public void ifFilterListIsInitiallyEmptyThenAssumeThatAllFilesAfterLastRunShouldBeDownloaded() {
        
        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        config.setLastRun(DateTime.now().minusDays(1));

        ArrayList<FTPFile> files = new ArrayList<FTPFile>();

        FTPFile file1 = new FTPFile("file1", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file2 = new FTPFile("file2", 0, "remote/", DateTime.now().getMillis(), false);
        FTPFile file3 = new FTPFile("file3", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file4 = new FTPFile("file4", 0, "remote/", DateTime.now().minusDays(2).getMillis(), false);
        FTPFile file5 = new FTPFile("file5", 0, "remote/", DateTime.now().getMillis(), false);

        files.add(file1);
        files.add(file2);
        files.add(file3);
        files.add(file4);
        files.add(file5);

        when(mockConnection.listFiles("remote/")).thenReturn(files);

        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);

        verify(mockNextStep).setFilesToDownload(Arrays.asList(file2, file5));
    }
    
    @Test
    public void ifListingFilesIsUnsuccessfulThenDoNotCallNextStep() {
        
        
        
        ScheduleConfiguration config = new ScheduleConfiguration(null, null, null, 0, null, "remote/", "local/");
        
        when(mockConnection.listFiles("remote/")).thenThrow(new FileListingException());
        
        ScheduleWorkflow schedule = new ScheduleWorkflow(config);
        schedule.setConnection(mockConnection);

        workflowStep.runStep(schedule);
        
        verify(mockNextStep, never()).runStep(schedule);
    }
}