create table app_users (
    id bigserial primary key,
    username varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null
);

create table projects (
    id bigserial primary key,
    name varchar(255) not null,
    git_url varchar(1000) not null,
    default_branch varchar(255) not null,
    owner_id bigint not null references app_users(id),
    token_hash varchar(255) not null unique
);

create table scan_tasks (
    id bigserial primary key,
    project_id bigint not null references projects(id),
    branch varchar(255) not null,
    trigger_type varchar(32) not null,
    status varchar(32) not null,
    started_at timestamp,
    finished_at timestamp,
    vulnerability_count integer not null default 0,
    critical_count integer not null default 0,
    high_count integer not null default 0,
    medium_count integer not null default 0,
    low_count integer not null default 0,
    component_count integer not null default 0,
    license_count integer not null default 0,
    failure_reason varchar(2000),
    raw_json text
);

create table vulnerabilities (
    id bigserial primary key,
    scan_task_id bigint not null references scan_tasks(id) on delete cascade,
    target varchar(1000),
    vulnerability_id varchar(255),
    package_name varchar(500),
    installed_version varchar(255),
    fixed_version varchar(255),
    severity varchar(32),
    title varchar(1000),
    reference_url varchar(2000)
);

create table components (
    id bigserial primary key,
    scan_task_id bigint not null references scan_tasks(id) on delete cascade,
    target varchar(1000),
    package_name varchar(500),
    version varchar(255),
    type varchar(255)
);

create table licenses (
    id bigserial primary key,
    scan_task_id bigint not null references scan_tasks(id) on delete cascade,
    target varchar(1000),
    package_name varchar(500),
    version varchar(255),
    license_name varchar(255)
);

create index idx_scan_tasks_project on scan_tasks(project_id);
create index idx_vulnerabilities_scan on vulnerabilities(scan_task_id);
create index idx_components_scan on components(scan_task_id);
create index idx_licenses_scan on licenses(scan_task_id);
