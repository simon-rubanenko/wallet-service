create schema account;
create table account.account (
    account_id varchar(100) primary key,
    account_balance numeric,
    account_create_dt timestamp with time zone not null default current_timestamp,
    account_delete_dt timestamp with time zone default null
);