create table trade.grid
(
    name         varchar(255) not null
        primary key,
    centralPrice double       not null,
    gridAmount   double       not null,
    gridNumber   int          not null,
    updateTime   datetime     not null
);

create table trade.virtualOrder
(
    id               varchar(255)     not null
        primary key,
    symbol           varchar(255)     null,
    gridIndex        int              not null,
    price            double           not null,
    quantity         double           not null,
    executedQuantity double default 0 null,
    avgPrice         double default 0 null,
    side             int              not null comment ' 0 买入做多 1买入做空 2 卖多单 3卖空单

',
    status           int              not null
);

