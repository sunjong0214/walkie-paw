package com.WalkiePaw.presentation.domain.chat.response;

import com.WalkiePaw.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMsgListResponse {

    private final Long writerId;
    private final String nickname;
    private final String content;
    private final LocalDateTime createDate;

    public static ChatMsgListResponse from(ChatMessage chatMessage) {
        return new ChatMsgListResponse(chatMessage.getWriter().getId(), chatMessage.getWriter().getNickname(), chatMessage.getContent(), chatMessage.getCreatedDate());
    }
}
