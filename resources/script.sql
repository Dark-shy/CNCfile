create table amadafile
(
    fileId        int identity
        primary key,
    categoryId    varchar(20) collate SQL_Latin1_General_CP1_CI_AS,
    categoryName  nvarchar(50),
    specification nvarchar(50),
    addUser       nvarchar(20),
    engineer      nvarchar(20),
    addTime       datetime2,
    versions      varchar(20) collate SQL_Latin1_General_CP1_CI_AS,
    procedureName varchar(50),
    type          nvarchar(10),
    state         int
)
go

create table cnc_detail
(
    user_name      nvarchar(50),
    scattered_type nvarchar(50),
    id             int identity
        constraint cnc_detail_pk
            primary key
)
go

create table cnc_upload_file
(
    applicant       nvarchar(50) not null,
    department      nvarchar(50) not null,
    apply_date      datetime2    not null,
    engineer        nvarchar(50),
    product_code    nvarchar(50) not null,
    product_name    nvarchar(100),
    specification   nvarchar(100),
    model_name      nvarchar(100),
    plate_thickness decimal(10, 2),
    plate_material  nvarchar(50),
    version_type    int,
    cad_file_name   nvarchar(255),
    id              int identity
        primary key
)
go

create table cnc_file_detail
(
    id              int identity
        primary key,
    cnc_upload_id   int not null
        constraint cnc_file_detail_cnc_upload_file_null_fk
            references cnc_upload_file,
    scattered_type  nvarchar(50),
    version         nvarchar(50),
    cnc_ard_program nvarchar(255),
    suspend_time    datetime2,
    suspender       nvarchar(50),
    suspend_reason  nvarchar(255),
    state           nvarchar(50),
    remark          nvarchar(255)
)
go

create table cnc_download_file
(
    id            int identity
        primary key,
    cnc_detail_id int          not null
        references cnc_file_detail,
    download_time datetime2    not null,
    downloader    nvarchar(50) not null
)
go

create table users
(
    id       int identity
        primary key,
    username nvarchar(50)  not null
        unique,
    password nvarchar(255) not null,
    role     nvarchar(50)
)
go


