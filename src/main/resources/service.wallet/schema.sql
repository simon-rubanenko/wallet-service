create schema wallet;

create table wallet.wallet (
    wallet_id varchar(100) primary key,
    wallet_create_dt timestamp with time zone not null default current_timestamp,
    wallet_delete_dt timestamp with time zone default null
);

create table wallet.wallet_account (
    wallet_account_wallet_id varchar(100) not null,
    wallet_account_account_id varchar(100) not null,
    wallet_account_currency_id varchar(10) not null,
    wallet_account_create_dt timestamp with time zone not null default current_timestamp,
    wallet_account_delete_dt timestamp with time zone default null
);

