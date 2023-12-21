create table accepted_question_1722221123978498050
(
    id         bigint auto_increment comment 'id'
        primary key,
    questionId bigint                             not null comment '题目id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index      idx_questionId (questionId)
) comment '某用户通过的题目' collate = utf8mb4_unicode_ci;