create table trade.binance_orders
(
    order_id                   bigint unsigned                   not null comment '订单的唯一标识符，由 Binance 分配'
        primary key,
    symbol                     varchar(10)                       not null comment '交易对的符号，如 LTCBTC',
    order_list_id              bigint         default -1         null comment '如果订单属于一个 OCO 订单组，这个字段会提供订单组的 ID。对于单个订单，值为 -1',
    client_order_id            varchar(50)                       not null comment '用户自定义的订单标识符',
    price                      decimal(18, 8)                    not null comment '订单的价格',
    orig_qty                   decimal(18, 8)                    not null comment '订单的原始数量，即用户下单时请求的数量',
    executed_qty               decimal(18, 8)                    not null comment '订单已执行的数量，即已成交的部分',
    cummulative_quote_qty      decimal(18, 8) default 0.00000000 not null comment '已累计执行的报价资产数量',
    status                     varchar(20)                       not null comment '订单的当前状态，例如 NEW, PARTIALLY_FILLED, FILLED 等',
    time_in_force              varchar(10)                       not null comment '订单的有效方式，例如 GTC, IOC, FOK 等',
    type                       varchar(20)                       not null comment '订单类型，例如 LIMIT, MARKET, STOP_LOSS 等',
    side                       varchar(10)                       not null comment '订单方向，表示是买入 (BUY) 还是卖出 (SELL)',
    stop_price                 decimal(18, 8) default 0.00000000 not null comment '止损或止盈订单触发的价格',
    iceberg_qty                decimal(18, 8) default 0.00000000 not null comment '冰山订单的可见数量',
    time                       bigint unsigned                   not null comment '订单创建的时间戳，以毫秒为单位',
    update_time                bigint unsigned                   not null comment '订单最近一次更新的时间戳，以毫秒为单位',
    is_working                 tinyint(1)     default 1          not null comment '订单是否还在市场上有效（正在工作中）',
    orig_quote_order_qty       decimal(18, 8) default 0.00000000 not null comment '订单的原始报价资产数量',
    working_time               bigint unsigned                   not null comment '订单进入市场并开始工作的时间戳，以毫秒为单位',
    self_trade_prevention_mode varchar(255)   default 'NONE'     not null comment '自交易预防模式，用于避免同一用户的不同订单相互交易'
)
    comment 'Binance 交易订单记录';

create index idx_status
    on trade.binance_orders (status);

create index idx_symbol
    on trade.binance_orders (symbol);

create index idx_time
    on trade.binance_orders (time);

create table trade.binance_trades
(
    symbol           varchar(10)       not null comment '交易对符号，如 "BNBBTC"',
    id               bigint unsigned   not null comment '交易的唯一标识符'
        primary key,
    order_id         bigint unsigned   not null comment '相关联订单的唯一标识符',
    order_list_id    bigint default -1 null comment '订单组的 ID。对于单个订单，值为 -1',
    price            decimal(18, 8)    not null comment '交易价格',
    qty              decimal(18, 8)    not null comment '交易数量',
    quote_qty        decimal(18, 8)    not null comment '交易的报价资产数量',
    commission       decimal(18, 8)    not null comment '交易的手续费',
    commission_asset varchar(10)       not null comment '手续费的资产类型，如 "BNB"',
    time             bigint unsigned   not null comment '交易的时间戳，以毫秒为单位',
    is_buyer         tinyint(1)        not null comment '是否为买方',
    is_maker         tinyint(1)        not null comment '是否为挂单方',
    is_best_match    tinyint(1)        not null comment '是否为最佳匹配'
)
    comment 'Binance 交易记录';

create index idx_order_id
    on trade.binance_trades (order_id);

create index idx_symbol
    on trade.binance_trades (symbol);

