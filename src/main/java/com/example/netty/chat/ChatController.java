package com.example.netty.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @date : 2019/11/21
 */
@Controller
@RequestMapping("")
@Slf4j
public class ChatController {

    @GetMapping("/1")
    public String index(){
        return "index";
    }

    @GetMapping("/2")
    public String chat(){
        return "chat";
    }

}
