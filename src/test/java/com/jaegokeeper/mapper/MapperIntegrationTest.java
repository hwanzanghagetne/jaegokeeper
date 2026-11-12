package com.jaegokeeper.mapper;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.model.Alba;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.image.mapper.ImageMapper;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.enums.ItemFilter;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestListResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private BoardMapper boardMapper;

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
        boardMapper = ctx.getBean(BoardMapper.class);
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

    private void requestWithStatus(int id, int itemId, int albaId, String status) {
        db.update("INSERT INTO request (request_id, item_id, alba_id, request_amount, request_type, request_status, created_at) VALUES (?, ?, ?, 1, 'ORDER', ?, '2026-01-01 10:00:00')",
                id, itemId, albaId, status);
    }

    private void image(int id) {
        db.update("INSERT INTO image (image_id, image_path, origin_name) VALUES (?, ?, ?)", id, "path" + id + ".png", "origin" + id);
    }

    private void board(int id, int storeId, String type, String title, String content, String writer,
                       boolean pinned, String createdAt) {
        db.update("INSERT INTO board (board_id, store_id, board_type, title, content, writer, is_pinned, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, storeId, type, title, content, writer, pinned, createdAt);
    }

    private void log(int itemId, String type, String createdAt) {
        db.update("INSERT INTO log (item_id, log_type, log_amount, created_at) VALUES (?, ?, 1, ?)", itemId, type, createdAt);
    }

    private List<Integer> itemIds(ItemFilter filter, String keyword, boolean excludeZero) {
        List<ItemListResponse> list = itemMapper.findItemList(1, filter, keyword, excludeZero, 0, 50);
        assertEquals(list.size(), itemMapper.countItemList(1, filter, keyword, excludeZero));
        return list.stream().map(ItemListResponse::getItemId).sorted().collect(java.util.stream.Collectors.toList());
    }

    private List<Integer> boardIds(BoardType type, String keyword, BoardSearchType searchType) {
        List<BoardListResponse> list = boardMapper.findBoardList(1, type, keyword, searchType, 50, 0);
        assertEquals(list.size(), boardMapper.countBoardList(1, type, keyword, searchType));
        return list.stream().map(BoardListResponse::getBoardId).collect(java.util.stream.Collectors.toList());
    }

    private List<Integer> requestIds(List<RequestListResponse> list) {
        return list.stream().map(RequestListResponse::getRequestId).collect(java.util.stream.Collectors.toList());
    }

    private void runConcurrently(int threads, Runnable task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier startTogether = new CyclicBarrier(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    startTogether.await();
                    task.run();
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
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
    public void 동시에_출고해도_재고만큼만_성공하고_음수가_되지_않는다() throws Exception {
        storeWithOwner(1);
        item(1, 1, null);
        AtomicInteger succeeded = new AtomicInteger();

        runConcurrently(30, () -> {
            if (stockMapper.decreaseQuantity(1, 1, 1) == 1) {
                succeeded.incrementAndGet();
            }
        });

        assertEquals(10, succeeded.get());
        assertEquals(Integer.valueOf(0), stockMapper.findStockAmountByItemId(1, 1));
    }

    @Test
    public void 동시에_입고해도_갱신이_유실되지_않는다() throws Exception {
        storeWithOwner(1);
        item(1, 1, null);

        runConcurrently(20, () -> stockMapper.increaseQuantity(1, 1, 1));

        assertEquals(Integer.valueOf(30), stockMapper.findStockAmountByItemId(1, 1));
    }

    @Test
    public void 요청_목록은_같은_초에_쌓인_요청도_페이지_경계에서_중복_누락_없이_나뉜다() {
        storeWithOwner(1);
        item(1, 1, null);
        alba(1, 1, "010-1", null);
        for (int id = 1; id <= 3; id++) {
            requestWithStatus(id, 1, 1, "WAIT");
        }

        List<Integer> firstPage = requestIds(requestMapper.findRequestList(1, null, null, 0, 2));
        List<Integer> secondPage = requestIds(requestMapper.findRequestList(1, null, null, 2, 2));

        assertEquals(List.of(3, 2), firstPage);
        assertEquals(List.of(1), secondPage);
    }

    @Test
    public void 삭제된_요청은_수정되지_않는다() {
        storeWithOwner(1);
        item(1, 1, null);
        alba(1, 1, "010-1", null);
        request(1, 1, 1);
        requestMapper.softDeleteRequest(1, 1);
        RequestUpdateRequest update = new RequestUpdateRequest();
        update.setRequestAmount(5);

        assertEquals(0, requestMapper.updateRequest(1, 1, update));
    }

    @Test
    public void 요청_있음_필터는_취소_완료_승인_상태를_제외하고_포함한다() {
        storeWithOwner(1);
        alba(1, 1, "010-1", null);
        String[] statuses = {"WAIT", "WORKING", "CONFIRM", "REVERT", "DONE", "CANCEL"};
        for (int i = 0; i < statuses.length; i++) {
            item(i + 1, 1, null);
            requestWithStatus(i + 1, i + 1, 1, statuses[i]);
        }

        List<ItemListResponse> list = itemMapper.findItemList(1, ItemFilter.HAS_REQUEST, null, false, 0, 50);

        assertEquals(List.of("item4", "item2", "item1"),
                list.stream().map(ItemListResponse::getItemName).collect(java.util.stream.Collectors.toList()));
        assertEquals(3, itemMapper.countItemList(1, ItemFilter.HAS_REQUEST, null, false));
    }

    @Test
    public void 상품_목록_필터는_즐겨찾기_재고부족_키워드_재고0제외를_걸러내고_개수와_일치한다() {
        storeWithOwner(1);
        storeWithOwner(2);
        item(1, 1, null);
        item(2, 1, null);
        item(3, 1, null);
        item(4, 2, null);
        db.update("UPDATE item SET is_pinned = 1 WHERE item_id = 2");
        db.update("UPDATE stock SET stock_amount = 2 WHERE item_id = 3");
        db.update("UPDATE stock SET stock_amount = 0 WHERE item_id = 1");

        assertEquals(List.of(1, 2, 3), itemIds(null, null, false));
        assertEquals(List.of(2, 3), itemIds(null, null, true));
        assertEquals(List.of(2), itemIds(ItemFilter.FAVORITE, null, false));
        assertEquals(List.of(1, 3), itemIds(ItemFilter.LOW_STOCK, null, false));
        assertEquals(List.of(3), itemIds(ItemFilter.LOW_STOCK, null, true));
        assertEquals(List.of(2), itemIds(null, "item2", false));
        assertEquals(List.of(), itemIds(null, "없는이름", false));
    }

    @Test
    public void 오래_움직이지_않은_상품_필터는_입고_출고_기록의_마지막_시각이_3개월_전인_상품만_걸린다() {
        storeWithOwner(1);
        for (int id = 1; id <= 4; id++) {
            item(id, 1, null);
        }
        String yesterday = LocalDateTime.now().minusDays(1).toString().replace('T', ' ').substring(0, 19);
        log(1, "OUT", "2020-01-02 00:00:00");
        log(2, "IN", "2020-01-02 00:00:00");
        log(2, "IN", yesterday);
        log(3, "ADJUST", yesterday);

        assertEquals(List.of(1), itemIds(ItemFilter.BAD_STOCK, null, false));
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
    public void 게시글_목록은_유형_검색_조건으로_걸러내고_고정글_최신순_번호순으로_정렬한다() {
        storeWithOwner(1);
        storeWithOwner(2);
        board(1, 1, "NOTICE", "공지 하나", "본문 가", "홍길동", false, "2026-01-01 10:00:00");
        board(2, 1, "NOTICE", "공지 둘", "본문 나", "김철수", true, "2026-01-01 09:00:00");
        board(3, 1, "BOARD", "자유글", "공지 이야기", "홍길동", false, "2026-01-01 10:00:00");
        board(4, 1, "BOARD", "삭제될 글", "본문", "홍길동", false, "2026-01-02 10:00:00");
        board(5, 2, "NOTICE", "다른 매장", "본문", "홍길동", false, "2026-01-03 10:00:00");
        db.update("UPDATE board SET is_active = b'0' WHERE board_id = 4");

        assertEquals(List.of(2, 3, 1), boardIds(null, null, BoardSearchType.ALL));
        assertEquals(List.of(2, 1), boardIds(BoardType.NOTICE, null, BoardSearchType.ALL));
        assertEquals(List.of(1), boardIds(null, "하나", BoardSearchType.TITLE));
        assertEquals(List.of(3), boardIds(null, "이야기", BoardSearchType.CONTENT));
        assertEquals(List.of(3, 1), boardIds(null, "홍길동", BoardSearchType.WRITER));
        assertEquals(List.of(3, 1), boardIds(null, "홍길동", BoardSearchType.ALL));
        assertEquals(List.of(2, 3, 1), boardIds(null, "공지", BoardSearchType.ALL));
        assertEquals(List.of(2, 3, 1), boardIds(null, "", null));
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
