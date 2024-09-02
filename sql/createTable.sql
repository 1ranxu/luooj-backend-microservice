-- 数据库初始化
-- 创建库
create
    database if not exists luooj;

-- 切换库
use
    luooj;

-- 用户表
create table user
(
    id           bigint auto_increment comment 'id' primary key comment '用户id',
    userAccount  varchar(256)                              not null comment '账号',
    userPassword varchar(512)                              null comment '密码',
    email        varchar(256)                              null comment '邮箱',
    userName     varchar(256)                              null comment '用户昵称',
    userAvatar   varchar(1024)                             null comment '用户头像',
    userProfile  varchar(512)                              null comment '用户简介',
    userRole     varchar(256)    default 'user'            not null comment '用户角色：user/admin/ban',
    gender       tinyint                                   null comment '性别 0-男 1-女',
    score        bigint          default 30                not null comment '积分',
    fans         bigint unsigned default 0                 not null comment '粉丝数',
    followers    bigint unsigned default 0                 not null comment '关注数',
    createTime   datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint         default 0                 not null comment '是否删除'
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 通过表
create table accepted_question
(
    id         bigint auto_increment comment 'id' primary key comment '通过记录id',
    questionId bigint                             not null comment '题目id',
    userId     bigint                             not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_questionId (questionId)
) comment '题目通过表' collate = utf8mb4_unicode_ci;

-- 关注表
create table follow
(
    id         bigint auto_increment comment '主键' primary key comment '关注记录id',
    userId     bigint unsigned                    not null comment '用户id',
    fansId     bigint unsigned                    not null comment '粉丝id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_userId (userId) comment '用户id索引',
    index idx_fansId (fansId) comment '粉丝id索引'
) comment '关注表' collate = utf8mb4_unicode_ci;

-- 题目表
create table question
(
    id          bigint auto_increment comment 'id' primary key comment '题目id',
    title       varchar(512)                         null comment '题目标题',
    content     text                                 null comment '题目内容',
    tags        varchar(1024)                        null comment '标签列表（json 数组）',
    answer      text                                 null comment '题目答案',
    submitNum   int        default 0                 not null comment '题目提交数',
    acceptedNum int        default 0                 not null comment '题目通过数',
    judgeConfig varchar(128)                         null comment '判题配置（json对象）',
    judgeCase   text                                 null comment '判题用例（json数组）',
    comments    bigint(20) default 0                 not null comment '评论数',
    thumbNum    bigint(20) default 0                 not null comment '点赞数',
    favourNum   bigint(20) default 0                 not null comment '收藏数',
    userId      bigint                               not null comment '创建用户 id',
    createTime  datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint    default 0                 not null comment '是否删除',
    index idx_userId (userId) comment '创建者id索引'
) comment '题目表' collate = utf8mb4_unicode_ci;

-- 题目评论表
create table question_comment
(
    id         bigint auto_increment comment '评论id'
        primary key,
    userId     bigint unsigned                            not null comment '用户id',
    questionId bigint unsigned                            not null comment '题目id',
    parentId   bigint unsigned                            not null comment '本评论关联的1级评论id，如果本评论是一级评论，则值为0',
    respondId  bigint unsigned                            not null comment '本评论回复的评论的id，如果本评论是一级评论，则值为0',
    content    varchar(1024)                              not null comment '回复内容',
    likes      bigint unsigned  default 0                 not null comment '点赞数',
    status     tinyint unsigned default 0                 not null comment '状态，0：正常，1：被举报，2：禁止查看',
    createTime datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime         default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint          default 0                 not null comment '逻辑删除 0-删除 1-正常',
    index idx_userId (userId) comment '用户id索引',
    index idx_questionId (questionId) comment '题目id索引',
    index idx_questionId_userId (questionId, userId) comment '题目id,用户id索引'
) comment '题目评论表' collate = utf8mb4_unicode_ci;

-- 题单表
create table question_list
(
    id         bigint auto_increment comment '题单id' primary key,
    title      varchar(64)                          not null comment '题单标题',
    userId     bigint                               not null comment '创建人id',
    favourNum  bigint(20) default 0                 not null comment '收藏数',
    createTime datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint    default 0                 not null comment '是否删除',
    index idx_userId_title (userId, title) comment '创建人id,题单标题索引'
) comment '题单表' collate = utf8mb4_unicode_ci;

-- 题单收藏题目表
create table question_collect
(
    id             bigint auto_increment comment '题目收藏记录id' primary key,
    questionListId varchar(64)                        not null comment '题单id',
    questionId     bigint                             not null comment '收藏的题目的id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    index idx_questionListId (questionListId) comment '题单id索引',
    index idx_questionId (questionId) comment '收藏的题目的id索引'
) comment '题单收藏题目表';

-- 题单收藏表
create table question_list_collect
(
    id             bigint auto_increment comment 'id' primary key,
    questionListId bigint                             not null comment '题单id',
    userId         bigint                             not null comment '收藏人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId) comment '收藏人id索引',
    index idx_questionListId (questionListId) comment '题单id索引'
) comment '题单收藏表' collate = utf8mb4_unicode_ci;

-- 题目提交记录表
create table question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    questionId bigint                             not null comment '题目id',
    userId     bigint                             not null comment '提交用户id',
    code       text                               not null comment '用户提交的代码',
    judgeInfo  varchar(128)                       null comment '判题信息',
    status     int      default 0                 not null comment '判题状态（0-待判题、1-判题中、2-通过、3-失败）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_questionId (questionId)
) comment '题目提交记录表' collate = utf8mb4_unicode_ci;

-- 题解表
create table question_solution
(
    id         bigint auto_increment comment '题解id' primary key,
    questionId bigint unsigned                           not null comment '题目id',
    userId     bigint unsigned                           not null comment '用户id',
    title      varchar(256)                              not null comment '题解标题',
    content    varchar(2048)                             not null comment '题解内容',
    likes      bigint unsigned default 0                 not null comment '点赞数',
    comments   bigint unsigned default 0                 not null comment '评论数',
    favourNum  bigint          default 0                 not null comment '收藏数',
    createTime datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint         default 0                 not null comment '逻辑删除 0-删除 1-正常',
    index idx_questionId (questionId) comment '题目id索引',
    index idx_userId (userId) comment '用户id索引'
) comment '题解表' collate = utf8mb4_unicode_ci;


-- 题解评论表
create table question_solution_comment
(
    id         bigint auto_increment comment '题解评论id' primary key,
    userId     bigint unsigned                            not null comment '用户id',
    solutionId bigint unsigned                            not null comment '题解id',
    parentId   bigint unsigned                            not null comment '本评论关联的1级评论id，如果本评论是一级评论，则值为0',
    respondId  bigint unsigned                            not null comment '本评论回复的评论的id，如果本评论是一级评论，则值为0',
    content    varchar(1024)                              not null comment '回复内容',
    likes      bigint unsigned  default 0                 not null comment '点赞数',
    status     tinyint unsigned default 0                 not null comment '状态，0：正常，1：被举报，2：禁止查看',
    createTime datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime         default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint          default 0                 not null comment '逻辑删除 0-删除 1-正常',
    index idx_userId (userId) comment '用户id索引',
    index idx_solutionId (solutionId) comment '题目id索引'
) comment '题解评论表' collate = utf8mb4_unicode_ci;

-- 题解收藏表
create table question_solution_collect
(
    id             bigint auto_increment comment '题解收藏记录id'
        primary key,
    questionListId bigint                             not null comment '题解id',
    userId         bigint                             not null comment '收藏人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId) comment '用户id索引',
    index idx_questionListId (questionListId) comment '题解id索引'
) comment '题解收藏表' collate = utf8mb4_unicode_ci;

-- 竞赛表
create table contest
(
    id         bigint auto_increment comment '竞赛id' primary key,
    title      varchar(512)                       null comment '竞赛标题',
    startTime  datetime default CURRENT_TIMESTAMP not null comment '开始时间',
    endTime    datetime default CURRENT_TIMESTAMP not null comment '结束时间',
    award      text                               null comment '竞赛奖励',
    tips       text                               null comment '重要提示',
    questions  varchar(1024)                      null comment '题目列表（json 数组）',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '竞赛表' collate = utf8mb4_unicode_ci;

-- 竞赛报名表
create table contest_apply
(
    id          bigint auto_increment comment '竞赛报名记录id'
        primary key,
    contestId   bigint                             not null comment '竞赛id',
    applicantId bigint                             not null comment '参赛者id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_applicantId (applicantId) comment '参赛者id索引',
    index idx_contestId (contestId) comment '竞赛id索引'
) comment '竞赛报名表' collate = utf8mb4_unicode_ci;
-- 竞赛成绩表
create table contest_result
(
    id            bigint auto_increment comment '竞赛成绩记录id'
        primary key,
    contestId     bigint                             not null comment '竞赛id',
    applicantId   bigint                             not null comment '参赛者id',
    userName      varchar(256)                       not null comment '参赛者名称',
    totalScore    bigint                             not null comment '总得分',
    totalTime     bigint                             not null comment '所有题目的总耗时(s)',
    totalMemory   bigint                             not null comment '所有题目的总内存(KB)',
    contestDetail text                               null comment '竞赛详情（json）',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    index idx_userId (applicantId) comment '参赛者id索引',
    index idx_contestId (contestId) comment '竞赛id索引'
) comment '竞赛成绩表' collate = utf8mb4_unicode_ci;




