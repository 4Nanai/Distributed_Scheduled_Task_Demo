package insight.shinanai.distributed_scheduled_task_demo.job;

import insight.shinanai.distributed_scheduled_task_demo.constant.LogLevelConstant;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "spring.profiles.active=test")
class RunScriptJobTest {

    @Mock
    private ScriptFilesService scriptFilesService;

    @Mock
    private JobInfoService jobInfoService;

    @Mock
    private JobLogService jobLogService;

    @Mock
    private ShardingContext shardingContext;

    private RunShellScriptJob runScriptJob;

    private static final Long JOB_ID = 1L;
    private static final String JOB_NAME = "test-job";
    private static final String CRON = "0/30 * * * * ?";
    private static final Long SCRIPT_FILE_ID = 1L;
    private static final String COMMAND_ARGS = "arg1 arg2";

    @BeforeEach
    void setUp() {
        runScriptJob = new RunShellScriptJob(
                JOB_ID,
                JOB_NAME,
                CRON,
                SCRIPT_FILE_ID,
                COMMAND_ARGS,
                jobInfoService,
                scriptFilesService,
                jobLogService
        );
    }

    @Test
    void testExecuteSuccess() {
        // test a simple successful execution
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'Hello World'");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(2);

        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        verify(jobInfoService).updateJobExecutionTime(JOB_ID, CRON);
        verify(scriptFilesService).getById(SCRIPT_FILE_ID);
        verify(shardingContext).getShardingItem();
        verify(shardingContext).getShardingTotalCount();
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), anyString(), anyString());
        verify(jobLogService, atLeastOnce()).saveCompleteLog(eq(JOB_ID),
                                                             eq(JOB_NAME),
                                                             eq(0),
                                                             anyString(),
                                                             anyString(),
                                                             anyString()
        );
    }

    @Test
    void testExecuteWithCommandArgs() {
        // test execution with command line arguments
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho $1 $2");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(1);
        when(shardingContext.getShardingTotalCount()).thenReturn(3);

        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        verify(jobInfoService).updateJobExecutionTime(JOB_ID, CRON);
        verify(scriptFilesService).getById(SCRIPT_FILE_ID);
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), anyString(), anyString());
    }

    @Test
    void testExecuteWithShardingEnvironment() {
        // test execution with sharding environment variables
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho \"Shard item: $SHARD_ITEM\"");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(2);
        when(shardingContext.getShardingTotalCount()).thenReturn(5);

        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // verify logs contain sharding info
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.INFO), contains("Shard: 2/4"));
    }

    @Test
    void testExecuteScriptFailure() {
        // test a script that fails
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\nexit 1");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> runScriptJob.execute(shardingContext)
        );

        assertTrue(exception.getMessage()
                           .contains("Script execution failed"));

        // verify error log
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.ERROR), anyString());
        verify(jobLogService).saveCompleteLog(eq(JOB_ID),
                                              eq(JOB_NAME),
                                              eq(0),
                                              eq(LogLevelConstant.ERROR),
                                              anyString(),
                                              anyString()
        );
    }

    @Test
    void testExecuteScriptFileNotFound() {
        // test script file not found
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(null);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // expect an exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> runScriptJob.execute(shardingContext)
        );

        // verify error log
        verify(jobInfoService).updateJobExecutionTime(JOB_ID, CRON);
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.ERROR), anyString());
    }

    @Test
    void testExecuteServiceException() {
        // mock server exception
        when(scriptFilesService.getById(SCRIPT_FILE_ID))
                .thenThrow(new RuntimeException("Database connection error"));
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> runScriptJob.execute(shardingContext)
        );

        assertEquals("Database connection error",
                     exception.getCause()
                             .getMessage()
        );

        // verify execution time updated
        verify(jobInfoService).updateJobExecutionTime(JOB_ID, CRON);
    }

    @Test
    void testExecuteWithComplexScript() {
        // test a more complex script
        String complexScript = """
                #!/bin/bash
                echo "Starting script execution"
                echo "Shard item: $SHARD_ITEM"
                for i in {1..3}; do
                    echo "Processing item $i"
                done
                echo "Script completed successfully"
                """;

        ScriptFiles scriptFiles = createTestScriptFiles(complexScript);

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(1);
        when(shardingContext.getShardingTotalCount()).thenReturn(4);

        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // verify info logs
        verify(jobLogService, atLeast(5)).sendLog(eq(JOB_ID), eq(LogLevelConstant.INFO), anyString());
    }

    @Test
    void testExecuteWithMoreComplexScript() {
        // test a more complex script
        String complexScript = this.complexScript;

        ScriptFiles scriptFiles = createTestScriptFiles(complexScript);

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        assertThrows(RuntimeException.class, () -> runScriptJob.execute(shardingContext));

        // verify info logs
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.ERROR), anyString());
    }

    @Test
    void testExecuteWithErrorOutput() {
        // test a script that produces stderr output
        ScriptFiles scriptFiles = createTestScriptFiles(
                "#!/bin/bash\necho 'This is stdout'\necho 'This is stderr' >&2\nexit 0");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // verify info & error logs
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.INFO), anyString());
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.ERROR), anyString());
        verify(jobLogService).saveCompleteLog(eq(JOB_ID),
                                              eq(JOB_NAME),
                                              eq(0),
                                              eq(LogLevelConstant.INFO),
                                              anyString(),
                                              anyString()
        );
        verify(jobLogService).saveCompleteLog(eq(JOB_ID),
                                              eq(JOB_NAME),
                                              eq(0),
                                              eq(LogLevelConstant.ERROR),
                                              anyString(),
                                              anyString()
        );
    }

    @Test
    void testExecuteWithNullCommandArgs() {
        // test null command args
        RunShellScriptJob jobWithNullArgs = new RunShellScriptJob(
                JOB_ID,
                JOB_NAME,
                CRON,
                SCRIPT_FILE_ID,
                null,
                jobInfoService,
                scriptFilesService,
                jobLogService
        );
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'No args'");
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);
        assertDoesNotThrow(() -> jobWithNullArgs.execute(shardingContext));
    }

    @Test
    void testExecuteWithEmptyCommandArgs() {
        // test empty command args
        RunShellScriptJob jobWithEmptyArgs = new RunShellScriptJob(
                JOB_ID,
                JOB_NAME,
                CRON,
                SCRIPT_FILE_ID,
                "", // empty command args
                jobInfoService,
                scriptFilesService,
                jobLogService
        );
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'Empty args'");
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);
        assertDoesNotThrow(() -> jobWithEmptyArgs.execute(shardingContext));
    }

    @Test
    void testLogCollection() {
        // test log collection
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'Test log collection'");
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // verify logs sent
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID), eq(LogLevelConstant.INFO), contains("Starting job"));
        verify(jobLogService, atLeastOnce()).sendLog(eq(JOB_ID),
                                                     eq(LogLevelConstant.INFO),
                                                     contains("executed successfully")
        );
        verify(jobLogService).saveCompleteLog(eq(JOB_ID),
                                              eq(JOB_NAME),
                                              eq(0),
                                              eq(LogLevelConstant.INFO),
                                              anyString(),
                                              anyString()
        );
    }

    @Test
    void testUniqueExecutionId() {
        // test UUID
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'Test execution ID'");
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // create two job instances to simulate two executions
        RunShellScriptJob job1 = new RunShellScriptJob(
                JOB_ID, JOB_NAME, CRON, SCRIPT_FILE_ID, COMMAND_ARGS,
                jobInfoService, scriptFilesService, jobLogService
        );
        RunShellScriptJob job2 = new RunShellScriptJob(
                JOB_ID, JOB_NAME, CRON, SCRIPT_FILE_ID, COMMAND_ARGS,
                jobInfoService, scriptFilesService, jobLogService
        );

        assertDoesNotThrow(() -> {
            job1.execute(shardingContext);
            job2.execute(shardingContext);
        });

        // verify logs saved with unique execution IDs
        ArgumentCaptor<String> executionIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(jobLogService, atLeast(2)).saveCompleteLog(
                eq(JOB_ID), eq(JOB_NAME), eq(0), eq(LogLevelConstant.INFO),
                anyString(), executionIdCaptor.capture()
        );
        List<String> executionIds = executionIdCaptor.getAllValues();
        assertTrue(executionIds.size() >= 2);
        executionIds.forEach(id -> {
            assertNotNull(id);
            assertTrue(id.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        });
    }

    /**
     * Create a test ScriptFiles object
     */
    private ScriptFiles createTestScriptFiles(String content) {
        ScriptFiles scriptFiles = new ScriptFiles();
        scriptFiles.setId(SCRIPT_FILE_ID);
        scriptFiles.setFileName("test-script");
        scriptFiles.setFileType(".sh");
        scriptFiles.setFileContent(content);
        return scriptFiles;
    }

    private final String complexScript = """
            #!/bin/bash
            # 初始化参数标志
            M_FLAG=false
            N_FLAG=false
            M_VALUE=""
            N_VALUE=""
            
            # 显示使用方法
            show_usage() {
                echo "用法: $SCRIPT_NAME [-m value] [-n value] [-h]"
                echo ""
                echo "选项:"
                echo "  -m VALUE    指定m参数的值"
                echo "  -n VALUE    指定n参数的值"
                echo "  -h          显示帮助信息"
                echo ""
                echo "示例:"
                echo "  $SCRIPT_NAME -m test1 -n test2"
                echo "  $SCRIPT_NAME -m \\"hello world\\" -n 123"
            }
            
            # 解析命令行参数
            while getopts "m:n:h" opt; do
                case "$opt" in
                    m)
                        M_FLAG=true
                        M_VALUE="$OPTARG"
                        echo "检测到 -m 参数，值为: $M_VALUE"
                        ;;
                    n)
                        N_FLAG=true
                        N_VALUE="$OPTARG"
                        echo "检测到 -n 参数，值为: $N_VALUE"
                        ;;
                    h)
                        show_usage
                        exit 0
                        ;;
                    \\?)
                        echo "错误: 无效的选项 -$OPTARG" >&2
                        show_usage
                        exit 1
                        ;;
                    :)
                        echo "错误: 选项 -$OPTARG 需要参数" >&2
                        show_usage
                        exit 1
                        ;;
                esac
            done
            
            # 移除已处理的参数
            shift $((OPTIND-1))
            
            # 检查是否提供了必需的参数
            if [ "$M_FLAG" = false ] && [ "$N_FLAG" = false ]; then
                echo "警告: 没有提供 -m 或 -n 参数"
                show_usage
                exit 1
            fi
            
            # 主要业务逻辑
            echo "========== 脚本执行开始 =========="
            echo "执行时间: $(date)"
            echo ""
            
            if [ "$M_FLAG" = true ]; then
                echo "-m 参数值为: $M_VALUE"
            fi
            
            if [ "$N_FLAG" = true ]; then
                echo "-n 参数值为: $N_VALUE"
            fi
            
            echo ""
            echo "========== 脚本执行完成 =========="
            
            exit 0
            """;
}
