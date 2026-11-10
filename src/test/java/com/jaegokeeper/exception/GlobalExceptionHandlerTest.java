package com.jaegokeeper.exception;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    @RestController
    static class TestController {

        @GetMapping("/param")
        public String param(@RequestParam int n) {
            return "ok";
        }

        @PostMapping("/upload")
        public String upload() {
            throw new MaxUploadSizeExceededException(1);
        }

        @GetMapping("/boom")
        public String boom() {
            throw new IllegalStateException("boom");
        }
    }

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void 파라미터_타입이_틀리면_400() throws Exception {
        mockMvc.perform(get("/param").param("n", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));
    }

    @Test
    public void 필수_파라미터가_없으면_400() throws Exception {
        mockMvc.perform(get("/param"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void 허용되지_않는_메서드는_405() throws Exception {
        mockMvc.perform(post("/param").param("n", "1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string(containsString("\"code\":\"METHOD_NOT_ALLOWED\"")));
    }

    @Test
    public void 없는_경로는_404() throws Exception {
        mockMvc.perform(get("/none"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("\"code\":\"NOT_FOUND\"")));
    }

    @Test
    public void 업로드_크기_초과는_413() throws Exception {
        mockMvc.perform(post("/upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().string(containsString("\"code\":\"FILE_TOO_LARGE\"")));
    }

    @Test
    public void 예상하지_못한_예외는_여전히_500() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("\"code\":\"INTERNAL_ERROR\"")));
    }
}
