create schema account_transaction;

create table account_transaction.transaction(
    transaction_id varchar(100) primary key,
    transaction_account_from varchar(100) not null,
    transaction_account_to varchar(100) not null,
    transaction_amount numeric not null,
    transaction_currency_id varchar(10) not null,
    transaction_completion_dt timestamp with time zone not null default current_timestamp
);

