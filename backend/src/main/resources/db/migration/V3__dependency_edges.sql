create table dependency_edges (
    id bigserial primary key,
    scan_task_id bigint not null references scan_tasks(id) on delete cascade,
    source_ref varchar(1000),
    source_name varchar(500),
    source_version varchar(255),
    target_ref varchar(1000),
    target_name varchar(500),
    target_version varchar(255),
    scope varchar(255)
);

create index idx_dependency_edges_scan on dependency_edges(scan_task_id);
