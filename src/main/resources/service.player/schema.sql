create schema player;

create table player.player (
    player_id varchar(100) primary key,
    player_wallet_id varchar(100) not null,
    player_create_dt timestamp with time zone not null default current_timestamp,
    player_delete_dt timestamp with time zone default null
);

