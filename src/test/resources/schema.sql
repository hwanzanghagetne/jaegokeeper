CREATE TABLE `user` (
  `user_id` INT NOT NULL AUTO_INCREMENT COMMENT '사용자_순번',
  `user_name` VARCHAR(20) NOT NULL COMMENT '사용자_이름',
  `pass_hash` VARCHAR(255) NULL COMMENT '비밀번호_해시',
  `user_mail` VARCHAR(255) NOT NULL COMMENT '사용자_이메일',
  `user_phone` VARCHAR(20) NULL COMMENT '사용자_연락처',
  `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성여부',
  `email_verified` BIT(1) NOT NULL DEFAULT b'0' COMMENT '이메일_인증여부',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_mail` (`user_mail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

CREATE TABLE `email_auth` (
  `email` VARCHAR(255) NOT NULL COMMENT '인증_이메일',
  `code` VARCHAR(20) NOT NULL COMMENT '인증코드',
  `expires_at` DATETIME NOT NULL COMMENT '만료일시',
  `verified_at` DATETIME NULL COMMENT '인증완료일시',
  PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이메일 인증';

CREATE TABLE `image` (
  `image_id` INT NOT NULL AUTO_INCREMENT COMMENT '이미지_순번',
  `image_path` VARCHAR(500) NOT NULL COMMENT '이미지_경로',
  `origin_name` VARCHAR(255) NULL COMMENT '원본_파일명',
  PRIMARY KEY (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이미지';

CREATE TABLE `store` (
  `store_id` INT NOT NULL AUTO_INCREMENT COMMENT '매장_순번',
  `user_id` INT NOT NULL COMMENT '사용자_순번',
  `store_name` VARCHAR(20) NOT NULL COMMENT '매장_이름',
  `store_add1` VARCHAR(255) NULL COMMENT '매장_주소1',
  `store_add2` VARCHAR(255) NULL COMMENT '매장_주소2',
  `store_tel` VARCHAR(20) NULL COMMENT '매장_연락처',
  PRIMARY KEY (`store_id`),
  KEY `idx_store_user_id` (`user_id`),
  CONSTRAINT `fk_store_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='매장';

CREATE TABLE `uid` (
  `auth_id` INT NOT NULL AUTO_INCREMENT COMMENT '인증수단_순번',
  `user_id` INT NOT NULL COMMENT '사용자_순번',
  `provider` VARCHAR(20) NOT NULL COMMENT '인증_제공자',
  `provider_uid` VARCHAR(255) NOT NULL COMMENT '제공자_사용자식별자',
  `pass_hash` VARCHAR(255) NULL COMMENT '로컬_비밀번호_해시',
  PRIMARY KEY (`auth_id`),
  UNIQUE KEY `uk_uid_provider_uid` (`provider`, `provider_uid`),
  UNIQUE KEY `uk_uid_user_provider` (`user_id`, `provider`),
  KEY `idx_uid_user_id` (`user_id`),
  CONSTRAINT `fk_uid_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 인증수단';

CREATE TABLE `item` (
  `item_id` INT NOT NULL AUTO_INCREMENT COMMENT '상품_순번',
  `store_id` INT NOT NULL COMMENT '매장_순번',
  `item_name` VARCHAR(50) NOT NULL COMMENT '상품_이름',
  `image_id` INT NULL COMMENT '이미지_순번',
  `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성여부',
  `is_pinned` BIT(1) NOT NULL DEFAULT b'0' COMMENT '고정여부',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`item_id`),
  KEY `idx_item_store_id` (`store_id`),
  KEY `idx_item_image_id` (`image_id`),
  CONSTRAINT `fk_item_store`
    FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_item_image`
    FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품';

CREATE TABLE `stock` (
  `stock_id` INT NOT NULL AUTO_INCREMENT COMMENT '재고_순번',
  `item_id` INT NOT NULL COMMENT '상품_순번',
  `stock_amount` INT NOT NULL DEFAULT 0 COMMENT '재고_수량',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `uk_stock_item_id` (`item_id`),
  CONSTRAINT `fk_stock_item`
    FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='재고';

CREATE TABLE `buffer` (
  `item_id` INT NOT NULL COMMENT '상품_순번',
  `buffer_amount` INT NOT NULL DEFAULT 0 COMMENT '적정_재고수량',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_buffer_item`
    FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='적정 재고';

CREATE TABLE `log` (
  `log_id` INT NOT NULL AUTO_INCREMENT COMMENT '재고로그_순번',
  `item_id` INT NOT NULL COMMENT '상품_순번',
  `log_type` VARCHAR(20) NOT NULL COMMENT '로그_유형',
  `log_amount` INT NOT NULL COMMENT '로그_수량',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  PRIMARY KEY (`log_id`),
  KEY `idx_log_item_id` (`item_id`),
  CONSTRAINT `fk_log_item`
    FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='재고 로그';

CREATE TABLE `alba` (
  `alba_id` INT NOT NULL AUTO_INCREMENT COMMENT '알바_순번',
  `store_id` INT NOT NULL COMMENT '매장_순번',
  `image_id` INT NULL COMMENT '이미지_순번',
  `alba_name` VARCHAR(50) NOT NULL COMMENT '알바_이름',
  `alba_phone` VARCHAR(50) NOT NULL COMMENT '알바_연락처',
  `alba_email` VARCHAR(255) NULL COMMENT '알바_이메일',
  `alba_status` VARCHAR(20) NULL COMMENT '알바_재직상태',
  `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성여부',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`alba_id`),
  KEY `idx_alba_store_id` (`store_id`),
  KEY `idx_alba_image_id` (`image_id`),
  UNIQUE KEY `uk_alba_store_phone_active` (`store_id`, `alba_phone`, `is_active`),
  UNIQUE KEY `uk_alba_store_email_active` (`store_id`, `alba_email`, `is_active`),
  CONSTRAINT `fk_alba_store`
    FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_alba_image`
    FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알바';

CREATE TABLE `schedule` (
  `schedule_id` INT NOT NULL AUTO_INCREMENT COMMENT '스케줄_순번',
  `alba_id` INT NOT NULL COMMENT '알바_순번',
  `schedule_time` VARCHAR(20) NOT NULL COMMENT '스케줄_요일',
  PRIMARY KEY (`schedule_id`),
  KEY `idx_schedule_alba_id` (`alba_id`),
  UNIQUE KEY `uk_schedule_alba_time` (`alba_id`, `schedule_time`),
  CONSTRAINT `fk_schedule_alba`
    FOREIGN KEY (`alba_id`) REFERENCES `alba` (`alba_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알바 스케줄';

CREATE TABLE `work` (
  `work_id` INT NOT NULL AUTO_INCREMENT COMMENT '근무기록_순번',
  `alba_id` INT NOT NULL COMMENT '알바_순번',
  `work_in` DATETIME NULL COMMENT '출근일시',
  `work_out` DATETIME NULL COMMENT '퇴근일시',
  `work_date` DATE NOT NULL COMMENT '근무일자',
  `work_status` VARCHAR(20) NULL COMMENT '근무상태',
  PRIMARY KEY (`work_id`),
  KEY `idx_work_alba_date` (`alba_id`, `work_date`),
  CONSTRAINT `fk_work_alba`
    FOREIGN KEY (`alba_id`) REFERENCES `alba` (`alba_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='근무 기록';

CREATE TABLE `board` (
  `board_id` INT NOT NULL AUTO_INCREMENT COMMENT '게시글_순번',
  `store_id` INT NOT NULL COMMENT '매장_순번',
  `image_id` INT NULL COMMENT '이미지_순번',
  `board_type` VARCHAR(20) NOT NULL COMMENT '게시글_유형',
  `title` VARCHAR(100) NOT NULL COMMENT '제목',
  `content` TEXT NULL COMMENT '내용',
  `writer` VARCHAR(50) NULL COMMENT '작성자',
  `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성여부',
  `is_pinned` BIT(1) NOT NULL DEFAULT b'0' COMMENT '고정여부',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`board_id`),
  KEY `idx_board_store_id` (`store_id`),
  KEY `idx_board_image_id` (`image_id`),
  CONSTRAINT `fk_board_store`
    FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`),
  CONSTRAINT `fk_board_image`
    FOREIGN KEY (`image_id`) REFERENCES `image` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판';

CREATE TABLE `request` (
  `request_id` INT NOT NULL AUTO_INCREMENT COMMENT '요청_순번',
  `item_id` INT NOT NULL COMMENT '상품_순번',
  `alba_id` INT NOT NULL COMMENT '알바_순번',
  `request_amount` INT NOT NULL COMMENT '요청_수량',
  `request_date` DATETIME NULL COMMENT '요청_일시',
  `request_type` VARCHAR(20) NOT NULL COMMENT '요청_유형',
  `request_status` VARCHAR(20) NOT NULL COMMENT '요청_상태',
  `is_active` BIT(1) NOT NULL DEFAULT b'1' COMMENT '활성여부',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (`request_id`),
  KEY `idx_request_item_id` (`item_id`),
  KEY `idx_request_alba_id` (`alba_id`),
  CONSTRAINT `fk_request_item`
    FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`),
  CONSTRAINT `fk_request_alba`
    FOREIGN KEY (`alba_id`) REFERENCES `alba` (`alba_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='재고 요청';

