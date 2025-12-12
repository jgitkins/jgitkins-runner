CREATE TABLE runner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE runner_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    runner_id BIGINT NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_runner_config_runner FOREIGN KEY (runner_id) REFERENCES runner (id) ON DELETE CASCADE,
    UNIQUE (runner_id, config_key)
);

CREATE TABLE job_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    runner_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP NULL,
    log_path TEXT,
    exit_code INT,
    CONSTRAINT fk_job_history_runner FOREIGN KEY (runner_id) REFERENCES runner (id) ON DELETE CASCADE,
    INDEX idx_job_history_runner (runner_id),
    INDEX idx_job_history_job (job_id)
);
