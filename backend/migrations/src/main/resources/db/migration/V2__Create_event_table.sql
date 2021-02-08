create table event (
    ID uuid not null,
    eventType character varying,
    created_at timestamp without time zone default now()
);
