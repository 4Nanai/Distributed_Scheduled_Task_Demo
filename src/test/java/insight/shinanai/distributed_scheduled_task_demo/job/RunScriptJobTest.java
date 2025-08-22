package insight.shinanai.distributed_scheduled_task_demo.job;

import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "spring.profiles.active=test")
class RunScriptJobTest {

    @Mock
    private ScriptFilesService scriptFilesService;

    @Mock
    private ShardingContext shardingContext;

    private RunScriptJob runScriptJob;

    private static final Long JOB_ID = 1L;
    private static final String JOB_NAME = "test-job";
    private static final Long SCRIPT_FILE_ID = 1L;
    @Mock
    private JobLogService jobLogService;

    @BeforeEach
    void setUp() {
        runScriptJob = new RunScriptJob(JOB_ID,
                                        JOB_NAME,
                                        SCRIPT_FILE_ID,
                                        "arg1 arg2",
                                        scriptFilesService,
                                        jobLogService
        );
    }

    @Test
    void testExecuteSuccess() {
        // 准备测试数据
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho 'Hello World'");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(2);

        // 执行测试
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // 验证调用
        verify(scriptFilesService).getById(SCRIPT_FILE_ID);
        verify(shardingContext).getShardingItem();
        verify(shardingContext).getShardingTotalCount();
    }

    @Test
    void testExecuteWithCommandArgs() {
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho $1 $2");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(1);
        when(shardingContext.getShardingTotalCount()).thenReturn(3);

        // 执行测试
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));

        // 验证调用
        verify(scriptFilesService).getById(SCRIPT_FILE_ID);
    }

    @Test
    void testExecuteWithShardingEnvironment() {
        // 测试分片环境变量传递
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\necho \"Shard item: $SHARD_ITEM\"");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(2);
        when(shardingContext.getShardingTotalCount()).thenReturn(5);

        // 执行测试
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));
    }

    @Test
    void testExecuteScriptFailure() {
        // 创建一个会失败的脚本
        ScriptFiles scriptFiles = createTestScriptFiles("#!/bin/bash\nexit 1");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // 验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> runScriptJob.execute(shardingContext)
        );

        assertTrue(exception.getMessage()
                           .contains("Script execution failed"));
    }

    @Test
    void testExecuteScriptFileNotFound() {
        // 模拟脚本文件不存在的情况
        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(null);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> runScriptJob.execute(shardingContext));
    }

    @Test
    void testExecuteServiceException() {
        // 模拟服务层异常
        when(scriptFilesService.getById(SCRIPT_FILE_ID))
                .thenThrow(new RuntimeException("Database connection error"));
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // 验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> runScriptJob.execute(shardingContext)
        );

        assertEquals("Database connection error",
                     exception.getCause()
                             .getMessage()
        );
    }

    @Test
    void testExecuteWithComplexScript() {
        // 测试复杂脚本
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

        // 执行测试
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));
    }

    @Test
    void testExecuteWithErrorOutput() {
        // 创建一个输出到stderr但不失败的脚本
        ScriptFiles scriptFiles = createTestScriptFiles(
                "#!/bin/bash\necho 'This is stdout'\necho 'This is stderr' >&2\nexit 0");

        when(scriptFilesService.getById(SCRIPT_FILE_ID)).thenReturn(scriptFiles);
        when(shardingContext.getShardingItem()).thenReturn(0);
        when(shardingContext.getShardingTotalCount()).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> runScriptJob.execute(shardingContext));
    }

    /**
     * 创建测试用的ScriptFiles对象
     */
    private ScriptFiles createTestScriptFiles(String content) {
        ScriptFiles scriptFiles = new ScriptFiles();
        scriptFiles.setId(SCRIPT_FILE_ID);
        scriptFiles.setFileName("test-script");
        scriptFiles.setFileType(".sh");
        scriptFiles.setFileContent(content);
        return scriptFiles;
    }
}
