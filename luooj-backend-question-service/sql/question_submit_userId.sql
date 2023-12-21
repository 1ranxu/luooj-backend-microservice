create table question_submit_1722221123978498050
(
    id         bigint auto_increment comment 'id'
        primary key,
    language   varchar(128)                       not null comment '编程语言',
    questionId bigint                             not null comment '题目id',
    userId     bigint                             not null comment '提交用户id',
    code       text                               not null comment '用户提交的代码',
    judgeInfo  varchar(128) null comment '判题信息',
    status     int      default 0                 not null comment '判题状态（0-待判题、1-判题中、2-通过、3-失败）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index      idx_userId (userId),
    index      idx_questionId (questionId)
) comment '某用户的提交记录' collate = utf8mb4_unicode_ci;