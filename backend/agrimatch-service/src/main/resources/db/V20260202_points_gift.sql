-- 积分赠送记录表
CREATE TABLE IF NOT EXISTS bus_points_gift (
  id           bigint AUTO_INCREMENT PRIMARY KEY,
  sender_id    bigint NOT NULL    COMMENT '赠送者 user_id',
  receiver_id  bigint NOT NULL    COMMENT '接收者 user_id',
  points       int    NOT NULL    COMMENT '赠送积分数',
  message      varchar(200)       COMMENT '留言',
  is_deleted   tinyint DEFAULT 0,
  create_time  datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  update_time  datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  INDEX idx_gift_sender(sender_id),
  INDEX idx_gift_receiver(receiver_id),
  INDEX idx_gift_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分赠送记录';
