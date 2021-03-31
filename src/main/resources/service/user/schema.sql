create schema wallet_user;

create table wallet_user.user (
    user_id varchar(100) primary key,
    user_wallet_id varchar(100) not null,
    user_create_dt timestamp with time zone not null default current_timestamp,
    user_delete_dt timestamp with time zone default null
);
