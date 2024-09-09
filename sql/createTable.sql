-- 数据库初始化
-- 创建库
create database if not exists luooj;

-- 切换库
use luooj;

-- 用户表
create table user
(
    id           bigint unsigned auto_increment comment '用户id' primary key,
    userAccount  varchar(32)                                   not null comment '账号',
    userPassword varchar(64)                                   null comment '密码',
    email        varchar(64)                                   null comment '邮箱',
    userName     varchar(64)                                   null comment '用户昵称',
    userAvatar   varchar(1024)                                 null comment '用户头像',
    userProfile  varchar(512)                                  null comment '用户简介',
    userRole     varchar(32)         default 'user'            not null comment '用户角色：user/admin/ban',
    gender       tinyint(4) unsigned                           null comment '性别 0-男 1-女',
    score        bigint unsigned     default 30                not null comment '积分',
    fans         bigint unsigned     default 0                 not null comment '粉丝数',
    followers    bigint unsigned     default 0                 not null comment '关注数',
    createTime   datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint(4) unsigned default 0                 not null comment '是否删除'
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 通过表
create table accepted_question
(
    id         bigint unsigned auto_increment comment '通过记录id' primary key,
    questionId bigint unsigned                    not null comment '题目id',
    userId     bigint unsigned                    not null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index      idx_userId (userId),
    index      idx_questionId (questionId)
) comment '题目通过表' collate = utf8mb4_unicode_ci;

-- 关注表
create table follow
(
    id         bigint unsigned auto_increment comment '关注记录id' primary key,
    userId     bigint unsigned                    not null comment '用户id',
    fansId     bigint unsigned                    not null comment '粉丝id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index      idx_userId (userId) comment '用户id索引',
    index      idx_fansId (fansId) comment '粉丝id索引'
) comment '关注表' collate = utf8mb4_unicode_ci;

-- 题目表
create table question
(
    id          bigint unsigned auto_increment comment '题目id' primary key,
    title       varchar(128)                                  null comment '题目标题',
    content     text                                          null comment '题目内容',
    difficulty  tinyint(4) unsigned                           null comment '题目难度（0-简单，1-中等，2-困难）',
    tags        varchar(1024)                                 null comment '标签列表（json 数组）',
    answer      text                                          null comment '题目答案',
    submitNum   bigint unsigned     default 0                 not null comment '题目提交数',
    acceptedNum bigint unsigned     default 0                 not null comment '题目通过数',
    judgeConfig varchar(128)                                  null comment '判题配置（json对象）',
    judgeCase   text                                          null comment '判题用例（json数组）',
    comments    bigint unsigned     default 0                 not null comment '评论数',
    thumbNum    bigint unsigned     default 0                 not null comment '点赞数',
    favourNum   bigint unsigned     default 0                 not null comment '收藏数',
    userId      bigint unsigned                               not null comment '创建用户 id',
    createTime  datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint(4) unsigned default 0                 not null comment '是否删除',
    index       idx_userId (userId) comment '创建者id索引'
) comment '题目表' collate = utf8mb4_unicode_ci;

-- 题目评论表
create table question_comment
(
    id            bigint unsigned auto_increment comment '评论id' primary key,
    userId        bigint unsigned                               not null comment '用户id',
    questionId    bigint unsigned                               not null comment '题目id',
    parentId      bigint unsigned     default 0                 not null comment '本评论关联的1级评论id，如果本评论是一级评论，则值为0',
    respondUserId bigint unsigned     default 0                 not null comment '本评论回复的评论的发布人，如果本评论是一级评论，则值为0',
    content       varchar(1024)                                 not null comment '回复内容',
    likes         bigint unsigned     default 0                 not null comment '点赞数',
    createTime    datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete      tinyint(4) unsigned default 0                 not null comment '是否删除',
    index         idx_userId (userId) comment '用户id索引',
    index         idx_questionId (questionId) comment '题目id索引',
    index         idx_questionId_userId (questionId, userId) comment '题目id,用户id索引'
) comment '题目评论表' collate = utf8mb4_unicode_ci;

-- 题单表
create table question_list
(
    id         bigint unsigned auto_increment comment '题单id' primary key,
    title      varchar(128)                       not null comment '题单标题',
    userId     bigint unsigned                    not null comment '创建人id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index      idx_userId_title (userId, title) comment '创建人id,题单标题索引'
) comment '题单表' collate = utf8mb4_unicode_ci;

-- 题单收藏题目表
create table question_collect
(
    id             bigint unsigned auto_increment comment '题目收藏记录id' primary key,
    questionListId bigint unsigned                    not null comment '题单id',
    questionId     bigint unsigned                    not null comment '收藏的题目的id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index          idx_questionListId (questionListId) comment '题单id索引',
    index          idx_questionId (questionId) comment '收藏的题目的id索引'
) comment '题单收藏题目表';

-- 题单收藏表
create table question_list_collect
(
    id             bigint unsigned auto_increment comment '题单收藏记录id' primary key,
    questionListId bigint unsigned                    not null comment '题单id',
    userId         bigint unsigned                    not null comment '收藏人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index          idx_userId (userId) comment '收藏人id索引',
    index          idx_questionListId (questionListId) comment '题单id索引'
) comment '题单收藏表' collate = utf8mb4_unicode_ci;

-- 题目提交记录表
create table question_submit
(
    id         bigint unsigned auto_increment comment '提交记录id' primary key,
    language   varchar(64)                                   not null comment '编程语言',
    questionId bigint unsigned                               not null comment '题目id',
    userId     bigint unsigned                               not null comment '提交用户id',
    code       text                                          not null comment '用户提交的代码',
    judgeInfo  varchar(128)                                  null comment '判题信息',
    status     tinyint(4) unsigned default 0                 not null comment '判题状态（0-待判题、1-判题中、2-通过、3-失败）',
    createTime datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete   tinyint(4) unsigned default 0                 not null comment '是否删除',
    index      idx_userId (userId) comment '用户id索引',
    index      idx_questionId (questionId) comment '题目id索引'
) comment '题目提交记录表' collate = utf8mb4_unicode_ci;

-- 题解表
create table question_solution
(
    id         bigint unsigned auto_increment comment '题解id' primary key,
    questionId bigint unsigned                               not null comment '题目id',
    userId     bigint unsigned                               not null comment '用户id',
    title      varchar(128)                                  not null comment '题解标题',
    content    varchar(2048)                                 not null comment '题解内容',
    tags       varchar(1024)                                 null comment '标签列表（json 数组）',
    likes      bigint unsigned     default 0                 not null comment '点赞数',
    comments   bigint unsigned     default 0                 not null comment '评论数',
    createTime datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint(4) unsigned default 0                 not null comment '逻辑删除 0-删除 1-正常',
    index      idx_questionId (questionId) comment '题目id索引',
    index      idx_userId (userId) comment '用户id索引'
) comment '题解表' collate = utf8mb4_unicode_ci;


-- 题解评论表
create table question_solution_comment
(
    id            bigint unsigned auto_increment comment '题解评论id' primary key,
    userId        bigint unsigned                               not null comment '用户id',
    solutionId    bigint unsigned                               not null comment '题解id',
    parentId      bigint unsigned     default 0                 not null comment '本评论关联的1级评论id，如果本评论是一级评论，则值为0',
    respondUserId bigint unsigned     default 0                 not null comment '本评论回复的评论的发布人，如果本评论是一级评论，则值为0',
    content       varchar(1024)                                 not null comment '回复内容',
    likes         bigint unsigned     default 0                 not null comment '点赞数',
    createTime    datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete      tinyint(4) unsigned default 0                 not null comment '是否删除',
    index         idx_userId (userId) comment '用户id索引',
    index         idx_solutionId (solutionId) comment '题目id索引'
) comment '题解评论表' collate = utf8mb4_unicode_ci;

-- 题解收藏表
create table question_solution_collect
(
    id         bigint unsigned auto_increment comment '题解收藏记录id' primary key,
    solutionId bigint unsigned                    not null comment '题解id',
    userId     bigint unsigned                    not null comment '收藏人id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index          idx_userId (userId) comment '用户id索引',
    index          idx_solutionId (solutionId) comment '题解id索引'
) comment '题解收藏表' collate = utf8mb4_unicode_ci;

-- 竞赛表
create table contest
(
    id         bigint unsigned auto_increment comment '竞赛id' primary key,
    title      varchar(128)                                  null comment '竞赛标题',
    startTime  datetime            default CURRENT_TIMESTAMP not null comment '开始时间',
    endTime    datetime            default CURRENT_TIMESTAMP not null comment '结束时间',
    duration   int(11) unsigned                              null comment '比赛时长（秒）',
    award      text                                          null comment '竞赛奖励',
    tips       text                                          null comment '重要提示',
    questions  varchar(128)                                  null comment '题目列表（json 数组）',
    userId     bigint unsigned                               not null comment '创建用户 id',
    status     tinyint(4) unsigned default 0                 not null comment '状态（0-待开始、1-进行中、2-已结束）',
    createTime datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint(4) unsigned default 0                 not null comment '是否删除',
    index       idx_userId (userId) comment '创建用户id索引',
    index       idx_status (status) comment '状态索引索引'
) comment '竞赛表' collate = utf8mb4_unicode_ci;

-- 竞赛报名表
create table contest_apply
(
    id          bigint unsigned auto_increment comment '竞赛报名记录id' primary key,
    contestId   bigint unsigned                    not null comment '竞赛id',
    applicantId bigint unsigned                    not null comment '参赛者id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index       idx_applicantId (applicantId) comment '参赛者id索引',
    index       idx_contestId (contestId) comment '竞赛id索引'
) comment '竞赛报名表' collate = utf8mb4_unicode_ci;

-- 竞赛成绩表
create table contest_result
(
    id            bigint unsigned auto_increment comment '竞赛成绩记录id' primary key,
    contestId     bigint unsigned                               not null comment '竞赛id',
    applicantId   bigint unsigned                               not null comment '参赛者id',
    userName      varchar(64)                                   not null comment '参赛者名称',
    totalScore    int(11) unsigned                              not null comment '总得分',
    totalTime     int(11) unsigned                              not null comment '所有题目的总耗时(s)',
    contestDetail text                                          null comment '竞赛详情（json）',
    createTime    datetime            default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime            default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint(4) unsigned default 0                 not null comment '是否删除',
    index         idx_userId (applicantId) comment '参赛者id索引',
    index         idx_contestId (contestId) comment '竞赛id索引'
) comment '竞赛成绩表' collate = utf8mb4_unicode_ci;

-- 评论举报表
create table comment_report
(
    id             bigint unsigned auto_increment comment '评论举报记录id' primary key,
    userId         bigint unsigned                    not null comment '检举人id',
    commentId      bigint unsigned                    not null comment '被检举评论的id',
    reportedUserId bigint unsigned                    not null comment '被检举人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_userId (userId) comment '检举人id索引',
    index idx_commentId (commentId) comment '被检举评论的id索引',
    index idx_reportedUserId (reportedUserId) comment '被检举人id索引'
) comment '评论举报表' collate = utf8mb4_unicode_ci;

-- 题解举报表
create table question_solution_report
(
    id             bigint unsigned auto_increment comment '题解举报记录id' primary key,
    userId         bigint unsigned                    not null comment '检举人id',
    solutionId     bigint unsigned                    not null comment '被检举题解的id',
    reportedUserId bigint unsigned                    not null comment '被检举人id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_userId (userId) comment '检举人id索引',
    index idx_solutionId (solutionId) comment '被检举题解的id索引',
    index idx_reportedUserId (reportedUserId) comment '被检举人id索引'
) comment '题解举报表' collate = utf8mb4_unicode_ci;




