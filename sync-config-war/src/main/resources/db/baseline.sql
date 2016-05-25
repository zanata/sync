CREATE TABLE IF NOT EXISTS Sync_Work_Config_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    createdDate TIMESTAMP,
    yaml CLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS Job_Status_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workId BIGINT NOT NULL,
    jobType VARCHAR(20) NOT NULL,
    jobStatusType VARCHAR(20) NOT NULL,
    startTime TIMESTAMP,
    endTime TIMESTAMP,
    nextStartTime TIMESTAMP,
    FOREIGN KEY (workId) REFERENCES Sync_Work_Config_table (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS System_Settings_table (
   key VARCHAR(50) NOT NULL,
   value VARCHAR(50) NOT NULL
);
