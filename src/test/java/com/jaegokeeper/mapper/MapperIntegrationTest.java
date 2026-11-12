package com.jaegokeeper.mapper;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.model.Alba;
import com.jaegokeeper.image.mapper.ImageMapper;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.request.mapper.RequestMapper;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import com.jaegokeeper.stock.dto.StockDetailResponse;
import com.jaegokeeper.stock.enums.LogType;
import com.jaegokeeper.stock.mapper.StockMapper;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.mysql.MySQLContainer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MapperIntegrationTest {

    private static final String[] TABLES = {
            "request", "work", "schedule", "log", "buffer", "stock", "board", "item",
            "alba", "uid", "store", "image", "email_auth", "user"
    };
    private static final String[] APP_PROPERTIES = {
            "DB_URL", "DB_USERNAME", "DB_PASSWORD", "S3_BUCKET_NAME",
            "MAIL_USERNAME", "MAIL_PASSWORD", "CORS_ALLOWED_ORIGINS"
    };

    private static MySQLContainer mysql;
    private static GenericWebApplicationContext ctx;
    private static JdbcTemplate db;

    private StockMapper stockMapper;
    private ItemMapper itemMapper;
    private RequestMapper requestMapper;
    private AlbaMapper albaMapper;
    private ScheduleMapper scheduleMapper;
    private UserMapper userMapper;
    private ImageMapper imageMapper;

    @BeforeClass
    public static void startDatabaseAndContext() throws Exception {
        Assume.assumeTrue("Docker가 없어 매퍼 통합 테스트를 건너뜁니다", DockerClientFactory.instance().isDockerAvailable());

        mysql = new MySQLContainer("mysql:8.0");
        mysql.start();
        db = new JdbcTemplate(new DriverManagerDataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword()));

        String ddl = new String(Files.readAllBytes(Paths.get("src/test/resources/schema.sql")), StandardCharsets.UTF_8);
        for (String statement : ddl.split(";")) {
            if (!statement.isBlank()) {
                db.execute(statement.trim());
            }
        }

        System.setProperty("DB_URL", mysql.getJdbcUrl());
        System.setProperty("DB_USERNAME", mysql.getUsername());
        System.setProperty("DB_PASSWORD", mysql.getPassword());
        System.setProperty("S3_BUCKET_NAME", "test-bucket");
        System.setProperty("MAIL_USERNAME", "test");
        System.setProperty("MAIL_PASSWORD", "test");
        System.setProperty("CORS_ALLOWED_ORIGINS", "http://localhost");

        ctx = new GenericWebApplicationContext(new MockServletContext());
        new XmlBeanDefinitionReader(ctx)
                .loadBeanDefinitions(new FileSystemResource("src/main/webapp/WEB-INF/spring/root-context.xml"));
        ctx.refresh();
    }

    @AfterClass
    public static void stopDatabaseAndContext() {
        if (ctx != null) {
            ctx.close();
        }
        for (String property : APP_PROPERTIES) {
            System.clearProperty(property);
        }
        if (mysql != null) {
            mysql.stop();
        }
    }

    @Before
    public void cleanDatabaseAndLoadMappers() throws SQLException {
        try (Connection connection = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : TABLES) {
                statement.execute("TRUNCATE TABLE `" + table + "`");
            }
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        stockMapper = ctx.getBean(StockMapper.class);
        itemMapper = ctx.getBean(ItemMapper.class);
        requestMapper = ctx.getBean(RequestMapper.class);
        albaMapper = ctx.getBean(AlbaMapper.class);
        scheduleMapper = ctx.getBean(ScheduleMapper.class);
        userMapper = ctx.getBean(UserMapper.class);
        imageMapper = ctx.getBean(ImageMapper.class);
    }


    private void user(int id) {
        db.update("INSERT INTO user (user_id, user_name, user_mail) VALUES (?, ?, ?)", id, "user" + id, "user" + id + "@test.com");
    }

    private void store(int id, int userId) {
        db.update("INSERT INTO store (store_id, user_id, store_name) VALUES (?, ?, ?)", id, userId, "store" + id);
    }

    private void storeWithOwner(int id) {
        user(id);
        store(id, id);
    }

    private void item(int id, int storeId, Integer imageId) {
        db.update("INSERT INTO item (item_id, store_id, item_name, image_id) VALUES (?, ?, ?, ?)", id, storeId, "item" + id, imageId);
        db.update("INSERT INTO stock (item_id, stock_amount) VALUES (?, 10)", id);
        db.update("INSERT INTO buffer (item_id, buffer_amount) VALUES (?, 3)", id);
    }

    private void alba(int id, int storeId, String phone, Integer imageId) {
        db.update("INSERT INTO alba (alba_id, store_id, alba_name, alba_phone, image_id) VALUES (?, ?, ?, ?, ?)",
                id, storeId, "alba" + id, phone, imageId);
    }

    private void request(int id, int itemId, int albaId) {
        db.update("INSERT INTO request (request_id, item_id, alba_id, request_amount, request_type, request_status) VALUES (?, ?, ?, 1, 'ORDER', 'WAIT')",
                id, itemId, albaId);
    }

    private void image(int id) {
        db.update("INSERT INTO image (image_id, image_path, origin_name) VALUES (?, ?, ?)", id, "path" + id + ".png", "origin" + id);
    }

    @Test
    public void 재고_상세는_snake_case_컬럼이_모두_매핑된다() {
        storeWithOwner(1);
        item(1, 1, null);

        StockDetailResponse detail = stockMapper.findStockDetail(1, 1);

        assertNotNull(detail);
        assertEquals(Integer.valueOf(10), detail.getStockAmount());
        assertEquals("item1", detail.getItemName());
        assertNotNull(detail.getStockId());
        assertNotNull(detail.getCreatedAt());
    }

    @Test
    public void 다른_매장_상품의_재고_상세는_조회되지_않는다() {
        storeWithOwner(1);
        storeWithOwner(2);
        item(1, 1, null);

        assertNull(stockMapper.findStockDetail(2, 1));
    }

    @Test
    public void 출고는_재고가_충분할_때만_차감되고_부족하면_그대로_둔다() {
        storeWithOwner(1);
        item(1, 1, null);

        assertEquals(0, stockMapper.decreaseQuantity(1, 1, 11));
        assertEquals(Integer.valueOf(10), stockMapper.findStockAmountByItemId(1, 1));

        assertEquals(1, stockMapper.decreaseQuantity(1, 1, 4));
        assertEquals(Integer.valueOf(6), stockMapper.findStockAmountByItemId(1, 1));
    }

    @Test
    public void 상품_목록의_최신_요청은_삭제된_요청을_제외한다() {
        storeWithOwner(1);
        item(1, 1, null);
        alba(1, 1, "010-1", null);
        request(1, 1, 1);
        request(2, 1, 1);

        requestMapper.softDeleteRequest(1, 2);
        List<ItemListResponse> list = itemMapper.findItemList(1, null, null, false, 0, 10);

        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(1), list.get(0).getLatestRequestId());
    }

    @Test
    public void 상품_상세는_같은_초에_쌓인_로그가_둘이어도_가장_마지막_로그를_돌려준다() {
        storeWithOwner(1);
        item(1, 1, null);
        db.update("INSERT INTO log (item_id, log_type, log_amount, created_at) VALUES (1, 'IN', 1, '2026-01-01 10:00:00')");
        db.update("INSERT INTO log (item_id, log_type, log_amount, created_at) VALUES (1, 'OUT', 1, '2026-01-01 10:00:00')");

        ItemDetailResponse detail = itemMapper.findItemDetail(1, 1);

        assertNotNull(detail);
        assertEquals(LogType.OUT, detail.getLastLogType());
    }

    @Test
    public void 알바_등록은_생성된_ID를_채우고_이미지는_등록한_매장에서만_조회된다() {
        storeWithOwner(1);
        storeWithOwner(2);
        image(7);

        Alba alba = Alba.create(1, "홍길동", "010-1", "a@test.com", "STAFF", 7);
        albaMapper.insertAlba(alba);

        assertNotNull(alba.getAlbaId());
        AlbaDetailResponse saved = albaMapper.getAlbaById(1, alba.getAlbaId());
        assertEquals("홍길동", saved.getAlbaName());
        assertEquals(Integer.valueOf(7), saved.getImageId());
        assertNotNull(imageMapper.findImgById(1, 7));
        assertNull(imageMapper.findImgById(2, 7));
    }

    @Test
    public void 알바_이름만_보내면_이름만_바뀌고_다른_필드는_유지된다() {
        storeWithOwner(1);
        alba(1, 1, "010-1", null);
        AlbaUpdateRequest request = new AlbaUpdateRequest();
        request.setAlbaId(1);
        request.setAlbaName("새이름");

        assertEquals(1, albaMapper.updateAlba(1, request));

        assertEquals("새이름", albaMapper.getAlbaById(1, 1).getAlbaName());
        assertEquals("010-1", albaMapper.getAlbaById(1, 1).getAlbaPhone());
    }

    @Test
    public void 다른_매장에서는_알바를_조회_수정_삭제할_수_없다() {
        storeWithOwner(1);
        storeWithOwner(2);
        alba(1, 1, "010-1", null);
        AlbaUpdateRequest request = new AlbaUpdateRequest();
        request.setAlbaId(1);
        request.setAlbaName("탈취");

        assertNull(albaMapper.getAlbaById(2, 1));
        assertEquals(0, albaMapper.updateAlba(2, request));
        assertEquals(0, albaMapper.deleteAlba(2, 1));
        assertEquals("alba1", albaMapper.getAlbaById(1, 1).getAlbaName());
    }

    @Test
    public void 삭제된_알바는_조회와_중복검사에서_제외된다() {
        storeWithOwner(1);
        alba(1, 1, "010-1", null);

        assertEquals(1, albaMapper.deleteAlba(1, 1));

        assertNull(albaMapper.getAlbaById(1, 1));
        assertEquals(0, albaMapper.existsByAlbaPhone(1, "010-1", null));
        assertEquals(0, albaMapper.deleteAlba(1, 1));
    }

    @Test
    public void 전화번호_중복검사는_수정_대상_자신을_제외한다() {
        storeWithOwner(1);
        alba(1, 1, "010-1", null);
        alba(2, 1, "010-2", null);

        assertEquals(1, albaMapper.existsByAlbaPhone(1, "010-1", null));
        assertEquals(0, albaMapper.existsByAlbaPhone(1, "010-1", 1));
        assertEquals(1, albaMapper.existsByAlbaPhone(1, "010-2", 1));
    }

    @Test
    public void 알바_목록은_매장_조건이_없으면_아무것도_돌려주지_않는다() {
        storeWithOwner(1);
        storeWithOwner(2);
        alba(1, 1, "010-1", null);
        alba(2, 2, "010-2", null);

        assertEquals(1, albaMapper.selectAllAlba(1).size());
        assertTrue(albaMapper.selectAllAlba(null).isEmpty());
    }

    @Test
    public void 스케줄은_같은_매장_알바만_수정할_수_있고_같은_요일은_중복_저장되지_않는다() {
        storeWithOwner(1);
        storeWithOwner(2);
        alba(1, 1, "010-1", null);
        db.update("INSERT INTO schedule (schedule_id, alba_id, schedule_time) VALUES (1, 1, 'MONDAY')");
        db.update("INSERT INTO schedule (schedule_id, alba_id, schedule_time) VALUES (2, 1, 'TUESDAY')");
        ScheduleUpdateRequest toFriday = new ScheduleUpdateRequest();
        toFriday.setScheduleId(1);
        toFriday.setScheduleTime("FRIDAY");
        ScheduleUpdateRequest toTuesday = new ScheduleUpdateRequest();
        toTuesday.setScheduleId(1);
        toTuesday.setScheduleTime("TUESDAY");

        assertEquals(0, scheduleMapper.updateSchedule(2, toFriday));
        assertEquals(1, scheduleMapper.updateSchedule(1, toFriday));
        assertThrows(DuplicateKeyException.class, () -> scheduleMapper.updateSchedule(1, toTuesday));
    }

    @Test
    public void 퇴근하지_않은_출근_기록이_있으면_출근이_중복_저장되지_않는다() {
        storeWithOwner(1);
        alba(1, 1, "010-1", null);
        LocalDate today = LocalDate.of(2026, 11, 9);
        WorkInOutRequest in = new WorkInOutRequest();
        in.setAlbaId(1);
        in.setWorkIn(LocalDateTime.of(2026, 11, 9, 9, 0));
        in.setWorkDate(today);
        in.setWorkStatus("ON");

        assertEquals(1, scheduleMapper.insertWorkIn(in));
        assertEquals(0, scheduleMapper.insertWorkIn(in));

        WorkInOutRequest out = new WorkInOutRequest();
        out.setAlbaId(1);
        out.setWorkOut(LocalDateTime.of(2026, 11, 9, 18, 0));
        out.setWorkDate(today);
        assertEquals(1, scheduleMapper.updateWorkOut(out));
        assertEquals(1, scheduleMapper.insertWorkIn(in));
    }

    @Test
    public void 유저_수정은_이메일을_보내지_않으면_기존_이메일을_유지한다() {
        user(1);
        UserUpdateRequest phoneOnly = new UserUpdateRequest();
        phoneOnly.setUserId(1);
        phoneOnly.setUserPhone("010-9999-9999");

        assertEquals(1, userMapper.updateUser(phoneOnly));
        assertEquals("user1@test.com", userMapper.findUserDetail(1).getUserMail());
        assertEquals("010-9999-9999", userMapper.findUserDetail(1).getUserPhone());

        UserUpdateRequest withMail = new UserUpdateRequest();
        withMail.setUserId(1);
        withMail.setUserMail("new@test.com");
        withMail.setUserPhone("010-9999-9999");
        userMapper.updateUser(withMail);
        assertEquals("new@test.com", userMapper.findUserDetail(1).getUserMail());
    }

    @Test
    public void 이미지는_내_매장의_상품_알바_게시글이_쓰는_것만_조회된다() {
        storeWithOwner(1);
        storeWithOwner(2);
        for (int id = 1; id <= 5; id++) {
            image(id);
        }
        item(1, 1, 1);
        alba(1, 1, "010-1", 2);
        db.update("INSERT INTO board (store_id, board_type, title, image_id) VALUES (2, 'NOTICE', 't', 3)");

        assertNotNull(imageMapper.findImgById(1, 1));
        assertNotNull(imageMapper.findImgById(1, 2));
        assertNull(imageMapper.findImgById(1, 3));
        assertNotNull(imageMapper.findImgById(2, 3));
        assertNull(imageMapper.findImgById(1, 4));
        assertNull(imageMapper.findImgById(1, 999));
    }
}
