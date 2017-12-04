CREATE TABLE log
(
    Client_No INT,
    Name VARCHAR(50) NOT NULL,
    startMonth INT NOT NULL,
    startWeek INT NOT NULL,
    startDay INT NOT NULL,
    startTime VARCHAR(50) NOT NULL,
    endMonth INT NOT　NULL,
    endWeek INT NOT NULL,
    endDay INT NOT NULL,
    endTime VARCHAR(50) NOT NULL,
    netDuration INT NOT NULL,
    energy FLOAT NOT NULL,
    fee FLOAT NOT NULL,
    level INT NOT NULL,
    startTemp INT NOT NULL,
    endTemp INT NOT NULL,
    checkOut BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT log_Client_No_startMonth_startWeek_startDay_startTime_pk PRIMARY KEY (Client_No, startMonth, startWeek, startDay, startTime)
)

CREATE TABLE roomInfo
(
    Client_No INT PRIMARY KEY NOT NULL,
    Name VARCHAR(50) NOT NULL,
    Password VARCHAR(50) NOT NULL
)
