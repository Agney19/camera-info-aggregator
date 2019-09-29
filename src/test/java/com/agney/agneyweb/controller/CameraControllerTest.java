package com.agney.agneyweb.controller;

import com.agney.agneyweb.AbstractTest;
import com.agney.agneyweb.constant.UrlType;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CameraControllerTest extends AbstractTest {
    @Test
    public void getPackageInfo_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/camera/info/aggregate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].urlType", is(UrlType.LIVE.getDesc())))
                .andExpect(jsonPath("$[0].videoUrl", is("videoOkSourceUrl")))
                .andExpect(jsonPath("$[0].value", is("tokenOkValue")))
                .andExpect(jsonPath("$[0].ttl", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].urlType", nullValue()))
                .andExpect(jsonPath("$[1].videoUrl", nullValue()))
                .andExpect(jsonPath("$[1].value", nullValue()))
                .andExpect(jsonPath("$[1].ttl", nullValue()));
    }
}