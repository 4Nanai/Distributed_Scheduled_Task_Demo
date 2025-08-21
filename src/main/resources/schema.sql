CREATE TABLE IF NOT EXISTS job_info
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id           BIGINT       NOT NULL COMMENT '用户ID',
    job_name          VARCHAR(255) NOT NULL COMMENT '作业名称',
    cron_expression   VARCHAR(255) NOT NULL COMMENT 'Cron表达式',
    sharding_count    INT          NOT NULL DEFAULT 1 COMMENT '分片数量',
    script_file_id    BIGINT       NOT NULL COMMENT '脚本文件ID',
    job_status        VARCHAR(50)  NOT NULL DEFAULT 'STOPPED' COMMENT '作业状态 (STARTED, STOPPED, PAUSED)',
    description       TEXT COMMENT '作业描述',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_execute_time DATETIME              DEFAULT NULL COMMENT '上次执行时间',
    next_execute_time DATETIME              DEFAULT NULL COMMENT '下次执行时间',
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_job (user_id, job_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS script_files
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    job_name     VARCHAR(255) NOT NULL COMMENT '作业名称',
    file_name    VARCHAR(255) NOT NULL COMMENT '脚本文件名',
    file_content LONGTEXT     NOT NULL COMMENT '脚本内容',
    command_args TEXT                  DEFAULT NULL COMMENT '启动命令行参数，多个参数用空格分隔',
    file_type    VARCHAR(50)  NOT NULL COMMENT '脚本类型 (SHELL, PYTHON)',
    file_size    BIGINT       NOT NULL COMMENT '脚本文件大小',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_log
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    job_id        BIGINT       NOT NULL COMMENT '作业ID',
    job_name      VARCHAR(255) NOT NULL COMMENT '作业名称',
    sharding_item INT          NOT NULL COMMENT '分片项',
    log_level     VARCHAR(20)  NOT NULL COMMENT '日志级别 (INFO, WARN, ERROR, DEBUG)',
    message       TEXT COMMENT '日志信息',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    execution_id  VARCHAR(255) NOT NULL COMMENT '执行ID',
    INDEX idx_job_id_create_time (job_id, create_time),
    INDEX idx_created_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username   VARCHAR(255) NOT NULL UNIQUE COMMENT '用户名',
    password   VARCHAR(255) NOT NULL COMMENT '密码',
    email      VARCHAR(255) COMMENT '邮箱地址',
    role       VARCHAR(50)  NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息表';
